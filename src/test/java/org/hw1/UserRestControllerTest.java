package org.hw1;

import org.hw1.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.hw1.boundary.UserRestController;

@WebMvcTest(UserRestController.class)
public class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void authenticate_Success() throws Exception {
        when(userService.authenticate("validUser", "validPassword")).thenReturn(true);

        mockMvc.perform(post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"validUser\",\"password\":\"validPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void authenticate_Failure() throws Exception {
        when(userService.authenticate("invalidUser", "wrongPassword")).thenReturn(false);

        mockMvc.perform(post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"invalidUser\",\"password\":\"wrongPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
