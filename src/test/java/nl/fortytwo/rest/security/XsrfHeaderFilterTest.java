package nl.fortytwo.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

public class XsrfHeaderFilterTest {

    @Tested
    private XsrfHeaderFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Mocked
    private FilterChain chain;

    @Before
    public void init() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void shouldProceedWithoutToken() throws ServletException, IOException {
        filter.doFilter(request, response, chain);

        new Verifications() {
            {
                chain.doFilter(request, response);
            }
        };
    }

    @Test
    public void shouldAddToken() throws ServletException, IOException {
        request.setAttribute(CsrfToken.class.getName(), new DefaultCsrfToken("hdr", "prm", "token"));
        request.setSecure(true);

        filter.doFilter(request, response, chain);

        assertEquals("token", response.getCookie("XSRF-TOKEN").getValue());
        assertFalse(response.getCookie("XSRF-TOKEN").isHttpOnly());
        assertTrue(response.getCookie("XSRF-TOKEN").getSecure());

        new Verifications() {
            {
                chain.doFilter(request, response);
            }
        };
    }

    @Test
    public void shouldNotAddTokenIfPresent() throws ServletException, IOException {
        request.setAttribute(CsrfToken.class.getName(), new DefaultCsrfToken("hdr", "prm", "token"));
        request.setSecure(true);
        request.setCookies(new Cookie("XSRF-TOKEN", "token"));

        filter.doFilter(request, response, chain);

        assertNull(response.getCookie("XSRF-TOKEN"));

        new Verifications() {
            {
                chain.doFilter(request, response);
            }
        };
    }

    @Test
    public void shouldUpdateTokenIfDifferent() throws ServletException, IOException {
        request.setAttribute(CsrfToken.class.getName(), new DefaultCsrfToken("hdr", "prm", "token"));
        request.setSecure(true);
        request.setCookies(new Cookie("XSRF-TOKEN", "differen"));

        filter.doFilter(request, response, chain);

        assertEquals("token", response.getCookie("XSRF-TOKEN").getValue());
        assertFalse(response.getCookie("XSRF-TOKEN").isHttpOnly());
        assertTrue(response.getCookie("XSRF-TOKEN").getSecure());

        new Verifications() {
            {
                chain.doFilter(request, response);
            }
        };
    }

}
