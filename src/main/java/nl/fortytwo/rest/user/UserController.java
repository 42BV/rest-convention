package nl.fortytwo.rest.user;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import nl.fortytwo.rest.user.dto.UserDTO;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/{email}", method = RequestMethod.GET)
    public UserDTO findById(@PathVariable String email) {
        return UserDTO.toResultDTO(userService.findByEmail(email));
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<UserDTO> get() {
        return stream(userService.findAll().spliterator(), false).map(user -> UserDTO.toResultDTO(user)).collect(toList());
    }

    @RequestMapping(value = "/{email}", method = RequestMethod.PUT)
    public UserDTO update(@PathVariable String email, @Valid @RequestBody UserDTO form) {
        return UserDTO.toResultDTO(userService.update(email, form));
    }

    @RequestMapping(method = RequestMethod.POST)
    public UserDTO create(@Valid @RequestBody UserDTO form) {
        return UserDTO.toResultDTO(userService.create(form));
    }
}
