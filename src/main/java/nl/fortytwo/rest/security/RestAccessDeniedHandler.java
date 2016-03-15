package nl.fortytwo.rest.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ErrorDto(ex.getMessage()));
        response.getWriter().flush();
    }
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), new ErrorDto("Please Login First"));
        response.getWriter().flush();
    }

}
