package pl.studyshare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidOptionsJsonValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOptionsJson {

    String message() default "Opcje pytania muszą być poprawnym JSON-em z polami label i correct.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
