package nl.fortytwo.rest.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import nl.fortytwo.rest.user.dto.UserDTO;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        LOG.info("User.findById(" + email + ")");
        return userRepository.findByEmail(email);
    }

    public Iterable<User> findAll() {
        LOG.info("User.findAll()");
        return userRepository.findAll();
    }

    @Secured("ROLE_ADMIN")
    public User create(UserDTO form) {
        LOG.info("User.create(" + form + ")");
        return null;
    }

    public Optional<User> update(String email, UserDTO form) {
        LOG.info("User.update(" + form + ")");
        return null;
    }


}
