package pl.studyshare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = QuestionOptionsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuestionOptionsValid {
    String message() default "Pytanie typu MULTI_CHOICE musi mieć co najmniej 2 opcje w formacie JSON";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
