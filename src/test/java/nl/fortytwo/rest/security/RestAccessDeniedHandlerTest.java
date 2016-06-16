package nl.fortytwo.rest.security;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import mockit.Tested;

public class RestAccessDeniedHandlerTest {

    @Tested
    private RestAccessDeniedHandler handler;

    @Test
    public void shouldCommenceInitial() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new InsufficientAuthenticationException(""));

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("{\"error\":\"Please Login.\"}", response.getContentAsString());
        assertEquals("application/json", response.getContentType());
    }

    @Test
    public void shouldCommenceAfterFailure() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(request, response, new BadCredentialsException(""));

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("{\"error\":\"Authentication failed.\"}", response.getContentAsString());
        assertEquals("application/json", response.getContentType());
    }

    @Test
    public void shouldHandle() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Access Denied"));

        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
        assertEquals("{\"error\":\"Access Denied\"}", response.getContentAsString());
        assertEquals("application/json", response.getContentType());
    }

}
