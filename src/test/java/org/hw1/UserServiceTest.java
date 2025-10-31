package org.hw1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.hw1.service.UserService;
import org.hw1.data.UserRepository;

import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.hw1.data.User;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {


    @Mock
    static private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;

    @BeforeEach
    public void setUp() {
        // create used and mock repository responses
        user1 = new User();
        user1.setName("user1");
        user1.setPassword(UserService.hashPassword("password"));
        when(userRepository.findByName("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findByName("nonexistent")).thenReturn(Optional.empty());
    }

    @Test
    public void hashIsDeterministic(){
        String s1 = UserService.hashPassword("mysecretpassword");
        String s2 = UserService.hashPassword("mysecretpassword");
        assertThat(s1, is(s2));
    }

    @Test
    public void userCorrectPassword() {
        Boolean auth = userService.authenticate("user1", "password");
        assertThat(auth, is(true));
    }
    @Test
    public void userWrongPassword() {
        Boolean auth = userService.authenticate("user1", "wrongpassword");
        assertThat(auth, is(false));
    }

    @Test
    public void getUserByNameReturnsUserWhenExists() {
        User found = userService.getUserByName("user1").orElse(null);
        assertThat(found, is(notNullValue()));
        assertThat(found.getName(), is("user1"));
        assertThat(found.getPassword(), is(UserService.hashPassword("password")));
    }

    @Test
    public void getUserByNameReturnsNullWhenNotExists() {
        User found = userService.getUserByName("nonexistent").orElse(null);
        assertThat(found, is(nullValue()));
    }

}
