package khuong.com.authservice.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import khuong.com.authservice.entity.ERole;
import khuong.com.authservice.entity.Role;
import khuong.com.authservice.repository.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findAll().isEmpty()) {
            for (ERole eRole : ERole.values()) {
                Role role = new Role();
                role.setName(eRole);
                roleRepository.save(role);
            }
        }
    }
}