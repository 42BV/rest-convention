package nl.fortytwo.rest.user;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    /**
     * Determines the number of login attempts the user is allowed to make before temporarily locking the account.
     */
    public static final int MAX_FAILED_LOGIN_ATTEMPTS_BEFORE_LOCK = 10;

    /**
     * Determines how long the user should be locked out after the maximum number of failed attempts has been exceeded.
     * Normally this should average out to one attempt per minute, so for 10 attempts a 10 minute lockout. 
     */
    public static final long LOCK_TIMEOUT_IN_MINUTES = 10;

    private Date lastAttempt = new Date(0);

    private int failedAttempts = 0;

    private Role role;

    private String email;

    private boolean active = true;

    private String password;

    public User() {
        this("", Role.ROLE_ANONYMOUS);
    }

    public User(String email, Role role) {
        this.email = email;
        this.role = role;
    }

    public User(String email, String pwd, Role role) {
        this(email, role);
        this.password = pwd;
    }

    public boolean isLocked() {
        Date unlock = new Date(System.currentTimeMillis() - LOCK_TIMEOUT_IN_MINUTES * 60L * 1000L);
        return unlock.before(lastAttempt);
    }

    public void markLoginSuccess() {
        failedAttempts = 0;
        lastAttempt = new Date(0);
    }

    public void markLoginFailed() {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS_BEFORE_LOCK) {
            lastAttempt = new Date();
            failedAttempts = 0;
        }
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}
