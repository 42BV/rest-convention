package nl.fortytwo.rest.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BasicStringValidatorTest {

    @Test
    public void shouldPass() {
        assertTrue(new BasicStringValidator().isValid("abcABC123!@#", null));
    }

    @Test
    public void shouldFail() {
        assertFalse(new BasicStringValidator().isValid("\n", null));
        assertFalse(new BasicStringValidator().isValid("\t", null));
        assertFalse(new BasicStringValidator().isValid("\r", null));
    }

    @Test
    public void shouldPassNull() {
        assertTrue(new BasicStringValidator().isValid(null, null));
    }

}
