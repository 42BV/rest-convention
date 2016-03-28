package nl.fortytwo.rest.security;

import static org.junit.Assert.assertNotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import mockit.Tested;

public class CorsInterceptorTest {

    @Tested
    private CorsInterceptor interceptor;

    @Test
    public void shouldReturnNoContent() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, null);

        assertNotNull(response.getHeader(CorsInterceptor.CREDENTIALS_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.HEADERS_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.MAX_AGE_NAME));
        assertNotNull(response.getHeader(CorsInterceptor.ORIGIN_NAME));
    }

}
