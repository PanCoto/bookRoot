
function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    if (typeof dateStr === 'string') {
        const match = dateStr.match(/^(\d{4})-(\d{2})-(\d{2})/);
        if (match) {
            return `${match[3]}.${match[2]}.${match[1]}`;
        }
    }
    try {
        const d = new Date(dateStr);
        if (!isNaN(d.getTime())) {
            const day = String(d.getDate()).padStart(2, '0');
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const year = d.getFullYear();
            return `${day}.${month}.${year}`;
        }
    } catch (e) {
        console.error(e);
    }
    return dateStr;
}

function abbreviate(str, maxLen = 140) {
    if (!str) return '';
    if (str.length <= maxLen) return str;
    return str.substring(0, maxLen - 3) + '...';
}

function buildParams(overrides = {}) {
    const params = new URLSearchParams(window.location.search);

    const isFilterOrSortChanging = Object.keys(overrides).some(key =>
        ['categoryId', 'taskType', 'since', 'sortField', 'sortDir'].includes(key)
    );

    if (isFilterOrSortChanging) {
        params.delete('page');
    }

    for (const [key, value] of Object.entries(overrides)) {
        if (value === null || value === undefined || value === '') {
            params.delete(key);
        } else {
            params.set(key, value);
        }
    }
    return params;
}

function renderTasks(page) {
    const container = document.getElementById('tasksContainer');
    const noTasksMessage = document.getElementById('noTasksMessage');
    if (!container) return;

    const countText = document.querySelector('main .col p.text-muted.small');
    if (countText) {
        countText.textContent = `Znaleziono: ${page.totalElements} zadań`;
    }

    if (!page.content || page.content.length === 0) {
        container.innerHTML = '';
        if (noTasksMessage) {
            noTasksMessage.classList.remove('d-none');
        }
        return;
    }

    if (noTasksMessage) {
        noTasksMessage.classList.add('d-none');
    }

    container.innerHTML = page.content.map(task => {
        const categoryName = escapeHtml(task.categoryName || '');
        const taskType = escapeHtml(task.taskType || 'OPEN');
        const title = escapeHtml(task.title || '');
        const contentAbbrev = escapeHtml(abbreviate(task.content, 140));
        const authorHtml = task.anonymous ? 'Anonim' : escapeHtml(task.authorName || 'Autor');
        const formattedDate = escapeHtml(formatDate(task.createdDate));
        const viewCount = task.viewCount !== null && task.viewCount !== undefined ? task.viewCount : 0;
        const taskId = task.id;

        return `
            <div class="col">
                <div class="card h-100 shadow-sm border-0 task-card">
                    <div class="card-body">
                        <div class="d-flex justify-content-between mb-2">
                            <span class="badge bg-secondary small">${categoryName}</span>
                            <span class="badge rounded-pill bg-primary small">${taskType}</span>
                        </div>
                        <h2 class="card-title h5 fw-semibold">
                            <a href="/tasks/${taskId}" class="text-decoration-none text-dark stretched-link">${title}</a>
                        </h2>
                        <p class="card-text text-muted small">${contentAbbrev}</p>
                    </div>
                    <div class="card-footer bg-transparent pt-0 d-flex justify-content-between align-items-center">
                        <small class="text-muted">
                            <span>${authorHtml}</span>
                            &bull;
                            <span>${formattedDate}</span>
                        </small>
                        <small class="text-muted">
                            <span aria-hidden="true">👁</span> <span>${viewCount}</span>
                        </small>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderPagination(page) {
    const container = document.getElementById('paginationContainer');
    const nav = document.getElementById('paginationNav');
    if (!container) return;

    if (page.totalPages <= 1) {
        container.innerHTML = '';
        if (nav) {
            nav.classList.add('d-none');
        }
        return;
    }

    if (nav) {
        nav.classList.remove('d-none');
    }

    let html = '';

    const prevDisabled = page.first ? 'disabled' : '';
    html += `
        <li class="page-item ${prevDisabled}">
            <a class="page-link" ${!page.first ? `data-page="${page.number - 1}"` : ''} aria-label="Poprzednia strona" style="cursor: pointer;">‹</a>
        </li>
    `;

    for (let i = 0; i < page.totalPages; i++) {
        const activeClass = page.number === i ? 'active' : '';
        html += `
            <li class="page-item ${activeClass}">
                <a class="page-link" data-page="${i}" aria-label="Strona ${i + 1}" style="cursor: pointer;">${i + 1}</a>
            </li>
        `;
    }

    const nextDisabled = page.last ? 'disabled' : '';
    html += `
        <li class="page-item ${nextDisabled}">
            <a class="page-link" ${!page.last ? `data-page="${page.number + 1}"` : ''} aria-label="Następna strona" style="cursor: pointer;">›</a>
        </li>
    `;

    container.innerHTML = html;
}

function updateSortButtons(params) {
    const sortField = params.get('sortField') || 'createdDate';
    const sortDir = params.get('sortDir') || 'desc';

    document.querySelectorAll('[data-sort-field]').forEach(btn => {
        const field = btn.getAttribute('data-sort-field');
        const dir = btn.getAttribute('data-sort-dir');

        if (field === sortField && dir === sortDir) {
            btn.classList.remove('btn-outline-secondary');
            btn.classList.add('btn-secondary');
            btn.setAttribute('aria-current', 'true');
        } else {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-outline-secondary');
            btn.removeAttribute('aria-current');
        }
    });
}

function updateFilterForm(params) {
    const form = document.getElementById('filterForm');
    if (!form) return;

    const categoryId = params.get('categoryId') || '';
    const taskType = params.get('taskType') || '';
    const since = params.get('since') || '';

    const catSelect = form.querySelector('[name="categoryId"]');
    if (catSelect) catSelect.value = categoryId;

    const typeSelect = form.querySelector('[name="taskType"]');
    if (typeSelect) typeSelect.value = taskType;

    const sinceInput = form.querySelector('[name="since"]');
    if (sinceInput) sinceInput.value = since;
}

async function loadTasks(params, { pushState = true } = {}) {
    try {
        const response = await fetch('/api/tasks?' + params.toString());
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const page = await response.json();

        renderTasks(page);
        renderPagination(page);
        updateSortButtons(params);
        updateFilterForm(params);

        if (pushState) {
            const newUrl = params.toString() ? ('/tasks?' + params.toString()) : '/tasks';
            history.pushState(null, '', newUrl);
        }
    } catch (error) {
        console.error('Error loading tasks:', error);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const filterForm = document.getElementById('filterForm');
    if (filterForm) {
        filterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const formData = new FormData(filterForm);
            const overrides = Object.fromEntries(formData);
            loadTasks(buildParams(overrides));
        });
    }

    document.querySelectorAll('[data-sort-field]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const sortField = btn.getAttribute('data-sort-field');
            const sortDir = btn.getAttribute('data-sort-dir');
            loadTasks(buildParams({ sortField, sortDir }));
        });
    });

    const paginationContainer = document.getElementById('paginationContainer');
    if (paginationContainer) {
        paginationContainer.addEventListener('click', (e) => {
            const pageLink = e.target.closest('[data-page]');
            if (pageLink) {
                e.preventDefault();
                const page = pageLink.getAttribute('data-page');
                loadTasks(buildParams({ page }));
            }
        });
    }
});

window.addEventListener('popstate', () => {
    loadTasks(new URLSearchParams(window.location.search), { pushState: false });
});
