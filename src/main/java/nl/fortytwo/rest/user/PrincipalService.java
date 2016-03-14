package nl.fortytwo.rest.user;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrincipalService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieve the user with a specific email.
     * @param email the requested email
     * @return the user with that email, if any.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * marks a login attempt as failed.
     * @param email the username.
     */
    public void markLoginFailed(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            user.get().markLoginFailed();
        }
    }

    /**
     * marks a login attempt as successful.
     * @param email the username.
     */
    public void markLoginSuccess(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            user.get().markLoginFailed();
        }
    }

}
