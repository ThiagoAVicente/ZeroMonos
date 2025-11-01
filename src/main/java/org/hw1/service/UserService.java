package org.hw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.hw1.data.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import org.hw1.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Boolean authenticate (String name, String password){
        logger.info("Authenticating user with name: {}", name);
        Optional<User> user = userRepository.findByName(name);
        if (user.isEmpty()) {
            logger.warn("Authentication failed: user '{}' not found", name);
            return false;
        }

        String hashedPassword = hashPassword(password);
        boolean authenticated = user.get().getPassword().equals(hashedPassword);
        logger.info ("Expected password {} - {}",user.get().getPassword(),hashedPassword);
        if (authenticated) {
            logger.info("Authentication successful for user '{}'", name);
        } else {
            logger.warn("Authentication failed: invalid password for user '{}'", name);
        }
        return authenticated;
    }

    public Optional<User> getUserByName(String name) {
        logger.info("Retrieving user with name: {}", name);
        return userRepository.findByName(name);

    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

}
