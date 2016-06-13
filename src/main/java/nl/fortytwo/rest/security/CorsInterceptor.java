package nl.fortytwo.rest.security;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * See http://dontpanic.42.nl/2015/04/cors-with-spring-mvc.html 
 */
public class CorsInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsInterceptor.class);

    public static final String REQUEST_ORIGIN_NAME = "Origin";

    public static final String CREDENTIALS_NAME = "Access-Control-Allow-Credentials";
    public static final String ORIGIN_NAME = "Access-Control-Allow-Origin";
    public static final String METHODS_NAME = "Access-Control-Allow-Methods";
    public static final String HEADERS_NAME = "Access-Control-Allow-Headers";
    public static final String MAX_AGE_NAME = "Access-Control-Max-Age";

    private final List<String> origins;

    public CorsInterceptor(String origins) {
        this.origins = Arrays.asList(origins.trim().split("( )*,( )*"));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader(CREDENTIALS_NAME, "true");
        response.setHeader(METHODS_NAME, "GET, OPTIONS, POST, PUT, PATCH, DELETE");
        response.setHeader(HEADERS_NAME, "Origin, X-Requested-With, Content-Type, Accept, X-XSRF-TOKEN");
        response.setHeader(MAX_AGE_NAME, "3600");

        String origin = request.getHeader(REQUEST_ORIGIN_NAME);
        if (origins.contains(origin)) {
            response.setHeader(ORIGIN_NAME, origin);
            return true; // Proceed
        } else {
            LOGGER.warn("Attempted access from non-allowed origin: {}", origin);
            // Include an origin to provide a clear browser error
            response.setHeader(ORIGIN_NAME, origins.iterator().next());
            return false; // No need to find handler
        }
    }

}