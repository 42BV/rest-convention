package nl.fortytwo.rest.user;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import nl.fortytwo.rest.user.dto.CreateUserDTO;
import nl.fortytwo.rest.user.dto.UserDTO;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordBlacklist blacklist;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordBlacklist blacklist, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.blacklist = blacklist;
        this.passwordEncoder = passwordEncoder;
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
    public User create(CreateUserDTO form) {
        LOG.info("User.create(" + form + ")");
        if (form.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password is too short");
        }
        if (blacklist.isBlacklisted(form.getPassword())) {
            throw new IllegalArgumentException("Password is blacklisted");
        }
        User user = new User(form.getEmail(), passwordEncoder.encode(form.getPassword()), form.getRole());
        userRepository.create(user);
        return user;
    }

    public User update(String email, UserDTO form) {
        LOG.info("User.update(" + form + ")");
        // TODO
        return null;
    }

}
