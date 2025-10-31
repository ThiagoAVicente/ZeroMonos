package org.hw1;

import org.hw1.data.User;
import org.hw1.data.UserRepository;
import org.hw1.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void authenticate_success() {
        User user = new User();
        user.setName("joao");
        user.setPassword(UserService.hashPassword("senha123"));
        user.setStaff(false);
        userRepository.save(user);

        assertThat(userService.authenticate("joao", "senha123")).isTrue();
    }

    @Test
    void authenticate_wrongPassword() {
        User user = new User();
        user.setName("maria");
        user.setPassword(UserService.hashPassword("abc123"));
        user.setStaff(false);
        userRepository.save(user);

        assertThat(userService.authenticate("maria", "wrong")).isFalse();
    }

    @Test
    void authenticate_nonexistentUser() {
        assertThat(userService.authenticate("naoexiste", "qualquercoisa")).isFalse();
    }

    @Test
    void getUserByName_found() {
        User user = new User();
        user.setName("carlos");
        user.setPassword(UserService.hashPassword("pw"));
        user.setStaff(false);
        userRepository.save(user);

        assertThat(userService.getUserByName("carlos")).isPresent();
    }

    @Test
    void getUserByName_notFound() {
        assertThat(userService.getUserByName("fantasma")).isEmpty();
    }

    @Test
    void hashPassword_isDeterministic() {
        String hash1 = UserService.hashPassword("abc");
        String hash2 = UserService.hashPassword("abc");
        assertThat(hash1).isEqualTo(hash2);
    }
}
