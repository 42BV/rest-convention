package nl.fortytwo.rest.security;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import nl.fortytwo.rest.user.PrincipalService;
import nl.fortytwo.rest.user.dto.UserDTO;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

    private final PrincipalService principalService;

    @Autowired
    public AuthenticationController(PrincipalService userService) {
        this.principalService = userService;
    }

    @ResponseBody
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.GET })
    public UserDTO authenticate(Principal principal) {
        return UserDTO.toResultDTO(principalService.findByEmail(principal == null ? null : principal.getName()));
    }

}