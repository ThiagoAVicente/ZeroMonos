package org.hw1.boundary;
import lombok.Getter;
import lombok.Setter;
import org.hw1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hw1.boundary.dto.AuthRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserRestController {
    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    private UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Boolean> authenticate(@RequestBody AuthRequest request) {
        logger.info("Received authentication request for user: {}", request.getName());
        boolean isAuthenticated = userService.authenticate(request.getName(), request.getPassword());
        logger.info("Authentication result for user {}: {}", request.getName(), isAuthenticated);
        return ResponseEntity.ok(isAuthenticated);
    }

}
