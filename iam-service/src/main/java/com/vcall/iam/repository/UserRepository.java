package com.vcall.iam.repository;

import com.vcall.iam.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.rolePermissions"})
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.rolePermissions"})
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.rolePermissions"})
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.rolePermissions"})
    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "userRoles.role.rolePermissions"})
    Optional<User> findByEmailVerificationToken(String token);

    @NonNull
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Page<User> findAll(Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);
}
