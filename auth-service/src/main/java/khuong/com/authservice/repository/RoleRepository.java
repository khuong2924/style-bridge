package khuong.com.authservice.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import khuong.com.authservice.entity.ERole;
import khuong.com.authservice.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}