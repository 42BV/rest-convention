package nl.fortytwo.rest.security;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import nl.fortytwo.rest.security.RestAuthenticationFilter.LoginForm;
import nl.fortytwo.rest.user.Role;
import nl.fortytwo.rest.user.User;
import nl.fortytwo.rest.user.dto.UserDTO;

public class SecurityIntegrationTest extends AbstractHttpIntegrationTest {

    @Before
    public void init() throws GeneralSecurityException {
        super.init("https://localhost:8443/api");
    }

    @Test
    public void shouldHaveSecurityHeaders() throws ClientProtocolException, IOException {
        Response resp = perform(new Request("/authentication", HttpMethod.GET));
        assertTrue(resp.isOk());

        assertTrue(resp.hasHeaderValue("X-Content-Type-Options", "nosniff"));
        assertTrue(resp.hasHeaderValue("X-XSS-Protection", "1; mode=block"));
        assertTrue(resp.hasHeaderValue("X-Frame-Options", "DENY"));

        assertTrue(resp.hasHeaderValue("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"));
        assertTrue(resp.hasHeaderValue("Pragma", "no-cache"));
        assertTrue(resp.hasHeaderValue("Expires", "0"));

        assertTrue(resp.hasHeaderValue("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains"));

        assertTrue(resp.hasHeaderValue("Access-Control-Allow-Credentials", "true"));
        assertTrue(resp.hasHeaderValue("Access-Control-Allow-Origin", "http://localhost:9000"));
        assertTrue(resp.hasHeaderValue("Access-Control-Allow-Methods", "GET, OPTIONS, POST, PUT, PATCH, DELETE"));
        assertTrue(resp.hasHeaderValue("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-XSRF-TOKEN"));
        assertTrue(resp.hasHeaderValue("Access-Control-Max-Age", "3600"));

    }

    @Test
    public void shouldHaveSecureSessionAndXsrfToken() throws ClientProtocolException, IOException {
        Response resp = perform(new Request("/authentication", HttpMethod.GET));
        assertTrue(resp.isOk());

        String session = resp.getCookie("JSESSIONID");
        String xsrf = resp.getCookie("XSRF-TOKEN");

        assertTrue(session.contains("Secure"));
        assertTrue(session.contains("HttpOnly"));

        assertTrue(xsrf.contains("Secure"));
        assertFalse(xsrf.contains("HttpOnly"));
    }

    @Test
    public void shouldBeUnauthenticated() throws ClientProtocolException, IOException {
        Response resp = perform(new Request("/authentication", HttpMethod.GET));
        assertTrue(resp.isOk());

        assertEquals(Role.ROLE_ANONYMOUS, resp.getBodyObject(UserDTO.class).getRole());
    }

    @Test
    public void shouldBeUnauthorized() throws ClientProtocolException, IOException {
        Response resp = perform(new Request("/users", HttpMethod.GET));
        assertTrue(resp.isUnauthorized());
    }

    @Test
    public void shouldAuthenticateAndChangeSession() throws ClientProtocolException, IOException {
        //
        // First a GET to obtain JSESSIONID and XSRF-TOKEN. 
        //
        Response resp = perform(new Request("/authentication", HttpMethod.GET));
        assertTrue(resp.isOk());

        //
        // With the session and token we log in.
        //
        Response auth = perform(new Request(resp, "/authentication", HttpMethod.POST)
                .addHeader("X-XSRF-TOKEN", resp.getXsrfToken())
                .setBodyObject(new LoginForm("user@42.nl", "123456")));
        assertTrue(auth.isOk());

        //
        // The Session Id should change.
        //
        assertFalse(resp.getCookie("JSESSIONID").equals(auth.getCookie("JSESSIONID")));

        //
        // Do a GET (on a secured resource) to get the new XSRF-TOKEN.
        //
        Response get = perform(new Request(auth, "/users", HttpMethod.GET));
        assertTrue(get.isOk());

        //
        // Check that the XSRF token has changed.
        //
        assertNotNull(get.getXsrfToken());
        assertFalse(get.getXsrfToken().equals(resp.getXsrfToken()));

        //
        // Logout
        //
        Response unauth = perform(new Request(auth, "/authentication", HttpMethod.DELETE)
                .addHeader("X-XSRF-TOKEN", get.getXsrfToken()));
        assertTrue(unauth.isOk());

        //
        // Get the new Session and XSRF-TOKEN.
        //
        resp = perform(new Request(unauth, "/authentication", HttpMethod.GET));
        assertTrue(resp.isOk());

        assertFalse(resp.getCookie("JSESSIONID").equals(auth.getCookie("JSESSIONID")));
        assertFalse(resp.getCookie("XSRF-TOKEN").equals(auth.getCookie("XSRF-TOKEN")));

    }
    
    
    @Test
    public void shouldNotAccessAdminSecuredFunctionsAsUser() throws ClientProtocolException, IOException {
        Response resp = perform(new Request("/authentication", HttpMethod.GET));
        assertTrue(resp.isOk());

        Response auth = perform(new Request(resp, "/authentication", HttpMethod.POST)
                .addHeader("X-XSRF-TOKEN", resp.getXsrfToken())
                .setBodyObject(new LoginForm("user@42.nl", "123456")));
        assertTrue(auth.isOk());

        Response get = perform(new Request(auth, "/users", HttpMethod.GET));
        assertTrue(get.isOk());
        
        Response newUser = perform(new Request(auth,"/users", HttpMethod.POST)
                .addHeader("X-XSRF-TOKEN", get.getXsrfToken())
                .addHeader("Content-Type", "application/json")
                .setBodyObject(new UserDTO(new User("test@test.nl",Role.ROLE_USER))));
        assertTrue(newUser.isForbidden());
    }

    @Test
    public void shouldAccessAdminSecuredFunctionsAsAdmin() throws ClientProtocolException, IOException {
        Response resp = perform(new Request("/authentication", HttpMethod.GET));
        assertTrue(resp.isOk());
        
        Response auth = perform(new Request(resp, "/authentication", HttpMethod.POST)
                .addHeader("X-XSRF-TOKEN", resp.getXsrfToken())
                .setBodyObject(new LoginForm("admin@42.nl", "123456")));
        assertTrue(auth.isOk());

        Response get = perform(new Request(auth, "/users", HttpMethod.GET));
        assertTrue(get.isOk());
        
        Response newUser = perform(new Request(auth,"/users", HttpMethod.POST)
                .addHeader("X-XSRF-TOKEN", get.getXsrfToken())
                .addHeader("Content-Type", "application/json")
                .setBodyObject(new UserDTO(new User("test@test.nl",Role.ROLE_USER))));
        assertTrue(newUser.isOk());
    }

}
