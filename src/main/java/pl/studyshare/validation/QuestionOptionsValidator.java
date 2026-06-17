package pl.studyshare.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.studyshare.domain.Question;
import pl.studyshare.enums.QuestionType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class QuestionOptionsValidator implements ConstraintValidator<QuestionOptionsValid, Question> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isValid(Question question, ConstraintValidatorContext context) {
        if (question.getType() != QuestionType.MULTI_CHOICE) {
            return true;
        }
        if (question.getOptions() == null || question.getOptions().isBlank()) {
            context.buildConstraintViolationWithTemplate("Opcje nie mogą być puste dla MULTI_CHOICE")
                    .addPropertyNode("options").addConstraintViolation();
            return false;
        }
        try {
            List<String> options = objectMapper.readValue(
                    question.getOptions(), new TypeReference<List<String>>() {});
            if (options == null || options.size() < 2) {
                context.buildConstraintViolationWithTemplate(
                                "MULTI_CHOICE wymaga co najmniej 2 opcji")
                        .addPropertyNode("options").addConstraintViolation();
                return false;
            }
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate(
                            "Nieprawidłowy format JSON dla opcji")
                    .addPropertyNode("options").addConstraintViolation();
            return false;
        }
        return true;
    }
}
