package nl.fortytwo.rest.user;

import java.util.Date;

public class User {

    private static final int MAX_ATTEMPTS = 5;

    private Date lastAttempt = new Date(0);

    private int failedAttempts = 0;

    private Role role;

    private String email;

    private boolean active = true;

    private String password;

    public User() {
        this("", Role.ANONYMOUS);
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
        Date unlock = new Date(System.currentTimeMillis() - 60L * 1000L);
        return unlock.before(lastAttempt);
    }

    public void markLoginSuccess() {
        failedAttempts = 0;
        lastAttempt = new Date(0);
    }

    public void markLoginFailed() {
        failedAttempts++;
        if (failedAttempts >= MAX_ATTEMPTS) {
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
