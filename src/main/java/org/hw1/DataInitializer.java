package org.hw1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.hw1.service.MunicipalityService;
import org.hw1.service.UserService;
import org.hw1.data.UserRepository;
import org.hw1.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final MunicipalityService municipalityService;
    private final UserRepository userRepository;

    @Autowired
    public DataInitializer(MunicipalityService municipalityService, UserRepository userRepository) {
        this.municipalityService = municipalityService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        municipalityService.fetchFromAPI();
        logger.info("Municipalities loaded successfully");

        createTestUsers();
    }

    private void createTestUsers() {

        if (userRepository.findByName("Staff Member").isEmpty()) {
            User staff = new User();
            staff.setName("Staff Member");
            staff.setPassword(UserService.hashPassword("staff123"));
            staff.setStaff(true);
            userRepository.save(staff);
            logger.info("Created test staff user: Staff Member (password: staff123)");
        }

        // Create additional test users for variety
        if (userRepository.findByName("Maria Silva").isEmpty()) {
            User user = new User();
            user.setName("Maria Silva");
            user.setPassword(UserService.hashPassword("maria123"));
            user.setStaff(false);
            userRepository.save(user);
            logger.info("Created test citizen user: Maria Silva (password: maria123)");
        }

        if (userRepository.findByName("Carlos Santos").isEmpty()) {
            User user = new User();
            user.setName("Carlos Santos");
            user.setPassword(UserService.hashPassword("carlos123"));
            user.setStaff(false);
            userRepository.save(user);
            logger.info("Created test citizen user: Carlos Santos (password: carlos123)");
        }
    }
}
