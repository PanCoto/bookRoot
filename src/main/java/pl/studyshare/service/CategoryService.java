package pl.studyshare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.Category;
import pl.studyshare.dto.CategoryCreateRequest;
import pl.studyshare.dto.CategoryDTO;
import pl.studyshare.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> findAllOrderByPopularity() {
        return categoryRepository.findAllOrderByTaskCountDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO createCategory(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return toDto(categoryRepository.save(category));
    }

    private CategoryDTO toDto(Category c) {
        long count = categoryRepository.countByCategory(c);
        return new CategoryDTO(c.getId(), c.getName(), count);
    }
}