package nl.fortytwo.rest.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import nl.fortytwo.rest.user.PrincipalService;
import nl.fortytwo.rest.user.Role;
import nl.fortytwo.rest.user.User;

public class SpringUserDetailsServiceTest {

    @Tested
    private SpringUserDetailsService srv;

    @Mocked
    @Injectable
    private PrincipalService principalService;

    @Test
    public void shoudLoadUser() {
        new Expectations() {
            {
                principalService.findByEmail("email");
                result = Optional.of(new User("email", "123", Role.ROLE_USER));
            }
        };
        
        UserDetails result = srv.loadUserByUsername("email");

        assertNotNull(result);
        assertEquals("email", result.getUsername());
        assertEquals("123", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

    }

    @Test(expected = UsernameNotFoundException.class)
    public void shoudNotLoadUser() {
        new Expectations() {
            {
                principalService.findByEmail("email");
                result = Optional.empty();
            }
        };

        srv.loadUserByUsername("email");

    }

}
