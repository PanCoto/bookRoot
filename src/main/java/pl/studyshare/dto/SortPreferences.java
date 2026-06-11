package pl.studyshare.dto;

import org.springframework.data.domain.Sort;
import pl.studyshare.enums.SortCriteria;

public record SortPreferences(SortCriteria sortBy, Sort.Direction sortDir) {
    
    public static SortPreferences defaultPreferences() {
        return new SortPreferences(SortCriteria.CREATED_AT, Sort.Direction.DESC);
    }
}
