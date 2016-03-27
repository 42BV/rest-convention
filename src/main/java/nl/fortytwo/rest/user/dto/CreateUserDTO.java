package nl.fortytwo.rest.user.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import nl.fortytwo.rest.user.Role;
import nl.fortytwo.rest.validator.BasicString;

public class CreateUserDTO {

    @Email
    @NotEmpty
    private String email;
    @NotEmpty
    @BasicString
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
