package pl.studyshare.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

public class ValidOptionsJsonValidator implements ConstraintValidator<ValidOptionsJson, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isValid(String optionsJson, ConstraintValidatorContext context) {

        if (optionsJson == null || optionsJson.isBlank()) {
            return true;
        }

        try {
            List<Map<String, Object>> options = objectMapper.readValue(
                    optionsJson, new TypeReference<List<Map<String, Object>>>() {});

            if (options == null || options.isEmpty()) {
                buildViolation(context, "Opcje nie mogą być pustą tablicą.");
                return false;
            }

            for (Map<String, Object> option : options) {
                if (!option.containsKey("label") || !(option.get("label") instanceof String)) {
                    buildViolation(context, "Każda opcja musi zawierać pole 'label' (String).");
                    return false;
                }
                if (!option.containsKey("correct") || !(option.get("correct") instanceof Boolean)) {
                    buildViolation(context, "Każda opcja musi zawierać pole 'correct' (boolean).");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            buildViolation(context, "Nieprawidłowy format JSON. Oczekiwano tablicy obiektów [{\"label\": \"...\", \"correct\": true/false}].");
            return false;
        }
    }

    private void buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
