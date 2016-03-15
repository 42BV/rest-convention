package nl.fortytwo.rest.user.dto;

import java.util.Optional;

import nl.fortytwo.rest.user.Role;
import nl.fortytwo.rest.user.User;

public class UserDTO {


    public static UserDTO toResultDTO(Optional<User> user) {
        return new UserDTO(user.orElse(new User("", Role.ROLE_ANONYMOUS)));
    }

    public static UserDTO toResultDTO(User user) {
        return new UserDTO(user);
    }

    private String email;
    private Role role;

    protected UserDTO() {
    }
    
    public UserDTO(User user) {
        email = user.getEmail();
        role = user.getRole();
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

}
