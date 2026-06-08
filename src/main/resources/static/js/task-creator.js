document.addEventListener('DOMContentLoaded', function() {
    console.log('task-creator.js loaded');

    const container = document.getElementById('questions-container');
    const addBtn = document.getElementById('add-question-btn');
    if (!container || !addBtn) return;

    let questionIdx = 0;

    addBtn.addEventListener('click', function() {
        const questionHtml = `
            <div class="question-block task-card" style="margin-top: 15px;" id="q-block-${questionIdx}">
                <h4>Pytanie #${questionIdx + 1}</h4>
                <div class="form-group">
                    <label>Treść pytania:</label>
                    <textarea name="questions[${questionIdx}].content" required class="form-control"></textarea>
                </div>
                <div class="form-group">
                    <label>Typ pytania:</label>
                    <select name="questions[${questionIdx}].type" class="form-control type-select" data-idx="${questionIdx}">
                        <option value="MULTI_CHOICE">Wielokrotny wybór</option>
                        <option value="OPEN">Otwarte</option>
                        <option value="TRUE_FALSE">Prawda / Fałsz</option>
                    </select>
                </div>
                <div class="options-wrapper" id="options-wrapper-${questionIdx}">
                    <label>Opcje wyboru (JSON Array, np. ["A", "B", "C"]):</label>
                    <input type="text" name="questions[${questionIdx}].options" class="form-control" placeholder='["Opcja A", "Opcja B"]'>
                </div>
                <div class="form-group" style="margin-top: 10px;">
                    <label>Poprawna odpowiedź:</label>
                    <input type="text" name="questions[${questionIdx}].correctAnswer" required class="form-control">
                </div>
                <div class="form-group">
                    <label>Punkty:</label>
                    <input type="number" name="questions[${questionIdx}].points" min="1" value="1" required class="form-control">
                </div>
                <button type="button" class="btn btn-danger remove-q-btn" data-target="q-block-${questionIdx}" style="background-color: #d32f2f;">Usuń pytanie</button>
            </div>
        `;

        container.insertAdjacentHTML('beforeend', questionHtml);

        const currentSelect = container.querySelector(`[data-idx="${questionIdx}"]`);
        const currentWrapper = container.querySelector(`#options-wrapper-${questionIdx}`);

        currentSelect.addEventListener('change', function() {
            if (this.value === 'MULTI_CHOICE') {
                currentWrapper.style.display = 'block';
            } else {
                currentWrapper.style.display = 'none';
            }
        });

        questionIdx++;
    });

    container.addEventListener('click', function(e) {
        if (e.target.classList.contains('remove-q-btn')) {
            const targetId = e.target.getAttribute('data-target');
            const targetElement = document.getElementById(targetId);
            if (targetElement) {
                targetElement.remove();
            }
        }
    });
});