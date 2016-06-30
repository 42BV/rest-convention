package nl.fortytwo.rest.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class UserTest {

    private User user;

    @Before
    public void init() {
        user = new User("spam@42.nl", Role.ROLE_ADMIN);
    }

    @Test
    public void shouldResetAfterCorrectLogin() {
        user.markLoginFailed();
        user.markLoginSuccess();
        shouldLockAfterNAttempts();
    }

    @Test
    public void shouldLockAfterNAttempts() {
        for (int t = 0; t < User.MAX_FAILED_LOGIN_ATTEMPTS_BEFORE_LOCK; t++) {
            assertFalse(user.isLocked());
            user.markLoginFailed();
        }
        assertTrue(user.isLocked());
    }

    @Test
    public void shouldUnLockAfterSomeTime() {
        shouldLockAfterNAttempts();
        Date ref = (Date) ReflectionTestUtils.getField(user, "lastAttempt");
        for (int t = 1; t <= User.LOCK_TIMEOUT_IN_MINUTES; t++) {
            assertTrue(user.isLocked());
            ReflectionTestUtils.setField(user, "lastAttempt", new Date(ref.getTime() - t * 60 * 1000));
        }
        assertFalse(user.isLocked());
    }

}
