package org.hw1.boundary;
import lombok.Getter;
import lombok.Setter;
import org.hw1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hw1.boundary.dto.AuthRequest;
@RestController
@RequestMapping("/users")
public class UserRestController {
    @Autowired
    private UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<Boolean> authenticate(@RequestBody AuthRequest request) {
        boolean isAuthenticated = userService.authenticate(request.getName(), request.getPassword());
        return ResponseEntity.ok(isAuthenticated);
    }

}
