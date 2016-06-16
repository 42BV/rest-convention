package nl.fortytwo.rest.security;

import static org.junit.Assert.assertEquals;

import java.security.Principal;
import java.util.Optional;

import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import nl.fortytwo.rest.user.PrincipalService;
import nl.fortytwo.rest.user.Role;
import nl.fortytwo.rest.user.User;
import nl.fortytwo.rest.user.dto.UserDTO;

public class AuthenticationControllerTest {

    @Mocked
    private Principal principal;

    @Mocked
    @Injectable
    private PrincipalService userService;
    @Tested
    private AuthenticationController authenticationController;

    @Test
    public void shouldReturnAnonymousWhenNotAuthenticated() {
        UserDTO result = authenticationController.authenticate(null);
        assertEquals(Role.ROLE_ANONYMOUS, result.getRole());
    }

    @Test
    public void shouldReturnUserWhenAuthenticated() {
        new Expectations() {
            {
                principal.getName();
                result = "name";
                userService.findByEmail("name");
                result = Optional.of(new User("email", Role.ROLE_USER));
            }
        };
        UserDTO result = authenticationController.authenticate(principal);
        assertEquals(Role.ROLE_USER, result.getRole());
        assertEquals("email", result.getEmail());
    }

}
