package nl.fortytwo.rest.security;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import nl.fortytwo.rest.user.PrincipalService;

public class RestAuthenticationFilterTest {

    @Mocked
    @Injectable
    private AntPathRequestMatcher matcher;
    @Mocked
    @Injectable
    private AuthenticationManager authenticationManager;
    @Mocked
    @Injectable
    private PrincipalService principalService;

    @Tested
    private RestAuthenticationFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Mocked
    private FilterChain chain;

    @Before
    public void init() {
        request = new MockHttpServletRequest();
        request.setContent("{\"username\":\"name\",\"password\":\"pwd\"}".getBytes());
        response = new MockHttpServletResponse();
    }

    @Test
    public void shouldProceedWhenNoMatch() throws IOException, ServletException {
        new Expectations() {
            {
                matcher.matches(request);
                result = false;
            }
        };
        filter.doFilter(request, response, chain);

        new Verifications() {
            {
                chain.doFilter(request, response);
            }
        };
    }

    @Test
    public void shouldProceedWhenAuthSuccess() throws IOException, ServletException {
        new Expectations() {
            {
                matcher.matches(request);
                result = true;
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("name", "pwd"));
            }
        };
        filter.doFilter(request, response, chain);

        new Verifications() {
            {
                principalService.markLoginSuccess("name");
                chain.doFilter(request, response);
            }
        };
    }

    @Test
    public void shouldNotProceedWhenAuthFails() throws IOException, ServletException {
        new Expectations() {
            {
                matcher.matches(request);
                result = true;
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("name", "pwd"));
                result = new BadCredentialsException("");

            }
        };
        filter.doFilter(request, response, chain);

        assertEquals(org.apache.http.HttpStatus.SC_FORBIDDEN, response.getStatus());

        new Verifications() {
            {
                principalService.markLoginFailed("name");
            }
        };
    }

    @Test
    public void shouldNotProceedWhenUserLocked() throws IOException, ServletException {
        new Expectations() {
            {
                matcher.matches(request);
                result = true;
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("name", "pwd"));
                result = new LockedException("");

            }
        };
        filter.doFilter(request, response, chain);

        assertEquals(org.apache.http.HttpStatus.SC_FORBIDDEN, response.getStatus());

        new Verifications() {
            {
                principalService.markLoginFailed("name");
            }
        };
    }

    @Test
    public void shouldNotProceedWhenBadJson() throws IOException, ServletException {
        new Expectations() {
            {
                matcher.matches(request);
                result = true;
            }
        };
        request.setContent("{ some lsfdpoksdfpo k".getBytes());
        filter.doFilter(request, response, chain);

        assertEquals(org.apache.http.HttpStatus.SC_BAD_REQUEST, response.getStatus());

        new Verifications() {
            {
            }
        };
    }

}
