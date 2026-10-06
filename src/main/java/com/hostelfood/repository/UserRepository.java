package com.hostelfood.repository;

import com.hostelfood.entity.User;
import com.hostelfood.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    Optional<User> findByEmail(String email);

    Optional<User> findByStudentId(String studentId);

    boolean existsByRole(Role role);

    long countByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);
}
