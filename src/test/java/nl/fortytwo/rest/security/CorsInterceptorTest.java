package nl.fortytwo.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import mockit.Tested;

public class CorsInterceptorTest {

    @Tested
    private CorsInterceptor interceptor = new CorsInterceptor("http://localhost:9000");

    @Test
    public void shouldReturnNoContentWhenValidDomain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:9000");
        HttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, null));

        assertNotNull(response.getHeader(CorsInterceptor.CREDENTIALS_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.HEADERS_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.MAX_AGE_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.ORIGIN_NAME));
    }

    @Test
    public void shouldReturnNoContentWhenInvalidDomain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://evil.org");
        HttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, null));

        assertNotNull(response.getHeader(CorsInterceptor.CREDENTIALS_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.HEADERS_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.MAX_AGE_NAME));
        assertEquals("http://localhost:9000", response.getHeader(CorsInterceptor.ORIGIN_NAME));
    }

}
