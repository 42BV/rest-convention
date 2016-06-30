package nl.fortytwo.rest.user;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private Map<String, User> users = new ConcurrentHashMap<String, User>();

    @Autowired
    public UserRepository(PasswordEncoder encoder) {
        users.put("user@42.nl", new User("user@42.nl", encoder.encode("123456"), Role.ROLE_USER));
        users.put("admin@42.nl", new User("admin@42.nl", encoder.encode("123456"), Role.ROLE_ADMIN));
        users.put("random@42.nl", new User("random@42.nl", encoder.encode("123456"), Role.ROLE_USER));
    }

    public Collection<User> findAll() {
        return users.values();
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(users.getOrDefault(email == null ? "" : email, null));
    }

    public User create(User user) {
        users.put(user.getEmail(), user);
        return user;
    }

}
