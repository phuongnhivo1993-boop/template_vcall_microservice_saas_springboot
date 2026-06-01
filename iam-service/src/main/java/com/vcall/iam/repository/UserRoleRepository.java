package com.vcall.iam.repository;

import com.vcall.iam.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(Long roleId);

    Optional<UserRole> findByUserIdAndRoleId(UUID userId, Long roleId);

    void deleteByUserIdAndRoleId(UUID userId, Long roleId);
}
