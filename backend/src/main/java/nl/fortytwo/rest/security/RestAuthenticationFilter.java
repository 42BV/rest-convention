package nl.fortytwo.rest.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.fortytwo.rest.security.dto.ErrorDto;
import nl.fortytwo.rest.user.PrincipalService;

public class RestAuthenticationFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestAuthenticationFilter.class);

    private final AntPathRequestMatcher matcher;

    private final AuthenticationManager authenticationManager;

    private final ObjectMapper objectMapper;

    private final PrincipalService principalService;

    public RestAuthenticationFilter(AntPathRequestMatcher matcher, AuthenticationManager authenticationManager, PrincipalService principalService) {
        this.matcher = matcher;
        this.authenticationManager = authenticationManager;
        this.principalService = principalService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && matcher.matches((HttpServletRequest) request)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            try {
                LoginForm form = objectMapper.readValue(request.getInputStream(), LoginForm.class);

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());

                try {
                    Authentication authentication = authenticationManager.authenticate(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    principalService.markLoginSuccess(form.getUsername());

                    chain.doFilter(request, response);

                } catch (BadCredentialsException ae) {
                    handleLoginFailure(httpResponse, form, ae);
                } catch (AuthenticationException ex) {
                    handleLoginFailure(httpResponse, form, ex);
                }
            } catch (IOException ex) {
                httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
                LOGGER.warn("Unexpected exception while authenticating", ex);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private void handleLoginFailure(HttpServletResponse httpResponse, LoginForm form, AuthenticationException ae) throws IOException {
        httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
        httpResponse.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(httpResponse.getOutputStream(), new ErrorDto("Login failed; Invalid userID or password"));
        principalService.markLoginFailed(form.getUsername());
        LOGGER.warn("Login failure", ae.getMessage());
    }

    public static class LoginForm {

        private String username;

        private String password;

        protected LoginForm() {
        }

        public LoginForm(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}
