package com.vcall.iam.repository;

import com.vcall.iam.entity.Role;
import com.vcall.iam.entity.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    @EntityGraph(attributePaths = {"rolePermissions", "userRoles"})
    List<Role> findAll();

    @EntityGraph(attributePaths = {"rolePermissions", "userRoles"})
    Optional<Role> findByName(RoleName name);

    @EntityGraph(attributePaths = {"rolePermissions"})
    Optional<Role> findById(Long id);

    @NonNull
    @EntityGraph(attributePaths = {"rolePermissions"})
    Page<Role> findAll(Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"rolePermissions"})
    Page<Role> findAll(Specification<Role> spec, Pageable pageable);
}
