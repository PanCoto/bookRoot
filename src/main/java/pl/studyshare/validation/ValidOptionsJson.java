package pl.studyshare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that optionsJson is a valid JSON array of objects
 * with 'label' (String) and 'correct' (boolean) fields.
 * Required for MULTIPLE_CHOICE and TRUE_FALSE task types.
 * Implements YAML validation rule V6.
 */
@Documented
@Constraint(validatedBy = ValidOptionsJsonValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOptionsJson {

    String message() default "Opcje pytania muszą być poprawnym JSON-em z polami label i correct.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
