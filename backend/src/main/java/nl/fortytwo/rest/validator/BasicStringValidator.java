package nl.fortytwo.rest.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

/**
 * Checks if the string does not contain any control characters such as new lines.
 */
public class BasicStringValidator implements ConstraintValidator<BasicString, String> {

    @Override
    public void initialize(BasicString constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.isEmpty(value)) {
            for (int t = 0; t < value.length(); t++) {
                if (Character.isISOControl(value.charAt(t))) {
                    return false;
                }
            }
        }
        return true;
    }

}
