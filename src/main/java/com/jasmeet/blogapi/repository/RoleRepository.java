package com.jasmeet.blogapi.repository;

import com.jasmeet.blogapi.model.role.Role;
import com.jasmeet.blogapi.model.role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(RoleName name);
}
