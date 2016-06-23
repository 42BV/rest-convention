package nl.fortytwo.rest.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import mockit.Tested;

public class CorsControllerTest {

    @Tested
    private CorsController controller;

    @Test
    public void shouldReturnNoContent() {
        assertEquals(HttpStatus.NO_CONTENT, controller.handle().getStatusCode());
    }

}
