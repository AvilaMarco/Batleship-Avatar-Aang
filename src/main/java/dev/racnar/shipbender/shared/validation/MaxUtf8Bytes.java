package dev.racnar.shipbender.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** El texto no puede superar {@code value} bytes en UTF-8; null es válido. */
@Documented
@Constraint(validatedBy = MaxUtf8Bytes.Validator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface MaxUtf8Bytes {
    int value();

    String message() default "no puede superar {value} bytes en UTF-8";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<MaxUtf8Bytes, CharSequence> {
        private int max;

        @Override
        public void initialize ( MaxUtf8Bytes annotation ) {
            max = annotation.value();
        }

        @Override
        public boolean isValid ( CharSequence text, ConstraintValidatorContext context ) {
            return text == null || text.toString().getBytes( StandardCharsets.UTF_8 ).length <= max;
        }
    }
}
