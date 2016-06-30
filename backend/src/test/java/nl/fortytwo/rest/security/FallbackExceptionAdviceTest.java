package nl.fortytwo.rest.security;

import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;

public class FallbackExceptionAdviceTest {

    @Mocked
    private MethodParameter param;

    @Mocked
    private BindingResult bindingResult;

    @Tested
    private FallbackExceptionAdvice advice;

    @Test
    public void shouldAccessDenied() {
        advice.handleAccessDenied(new AccessDeniedException("msg"));
    }

    @Test
    public void shouldHandleIllegalArgument() {
        advice.handleIllegalArgument(new IllegalArgumentException("msg"));
    }

    @Test
    public void shouldHandleExceptions() {
        advice.handleException(new Exception("msg"));
        advice.handleRuntimeException(new RuntimeException("msg"));
    }

    @Test
    public void shouldHandleValidationError() {
        new Expectations() {
            {
                param.getMethod();
                result = getClass().getMethods()[0]; // Mocking Method gives funny results..
            }
        };

        advice.handleMethodArgument(new MethodArgumentNotValidException(param, bindingResult));
    }


}
