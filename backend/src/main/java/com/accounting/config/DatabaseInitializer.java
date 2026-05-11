package com.accounting.config;

import com.accounting.entity.User;
import com.accounting.repository.UserRepository;
import com.accounting.service.AccountService;
import com.accounting.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking for admin user initialization...");
        
        var existingAdmin = userRepository.findByUsername("admin");
        
        if (existingAdmin.isEmpty()) {
            logger.info("Admin user not found. Creating admin user...");
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin123456"));
            admin.setEmail("admin@example.com");
            admin.setNickname("管理员");
            
            admin = userRepository.save(admin);
            
            // Initialize default data
            try {
                categoryService.initDefaultCategories(admin);
                accountService.initDefaultAccount(admin);
                logger.info("Admin user created successfully with default data.");
            } catch (Exception e) {
                logger.error("Error initializing admin data: {}", e.getMessage());
            }
        } else {
            // Reset admin password to ensure it's correct
            User admin = existingAdmin.get();
            admin.setPassword(passwordEncoder.encode("Admin123456"));
            userRepository.save(admin);
            logger.info("Admin user already exists. Password has been reset to Admin123456.");
        }
    }
}
