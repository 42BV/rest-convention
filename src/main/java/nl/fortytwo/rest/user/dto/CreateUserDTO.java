package nl.fortytwo.rest.user.dto;

import nl.fortytwo.rest.user.Role;

public class CreateUserDTO {

    private String email;
    private String password;
    private Role role;

    protected CreateUserDTO() {
    }

    public CreateUserDTO(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

}
