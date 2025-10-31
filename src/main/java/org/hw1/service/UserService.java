package org.hw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.hw1.data.UserRepository;
import java.security.MessageDigest;
import java.util.Optional;

import org.hw1.data.User;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

	public Boolean authenticate (String name, String password){
        Optional<User> user = userRepository.findByName(name);
        if (user.isEmpty()) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        return user.get().getPassword().equals(hashedPassword);


	}

    public String hashPassword(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(password.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

}
