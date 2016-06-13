package nl.fortytwo.rest.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.fortytwo.rest.security.dto.ErrorDto;

public class RestAccessDeniedHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    private ObjectMapper objectMapper;

    @Autowired
    public RestAccessDeniedHandler() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Handles an AccessDeniedException thrown by the application when the user is already authenticated <i>and</i> the exception is
     * not handled by controller or any other exception advice.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException, ServletException {
        LoggerFactory.getLogger(getClass()).info("Forbidden: " + ex.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ErrorDto(ex.getMessage()));
        response.getWriter().flush();
    }

    /**
     * Called when the user is not authenticated but requires authentication or if authentication has failed. 
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        LoggerFactory.getLogger(getClass()).info("Unauthorized: " + authException.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        ErrorDto body = new ErrorDto(isInitialRequest(authException) ? "Please Login." : "Authentication failed.");
        objectMapper.writeValue(response.getWriter(), body);
        response.getWriter().flush();
    }

    private boolean isInitialRequest(AuthenticationException authException) {
        return authException instanceof InsufficientAuthenticationException;
    }

}
