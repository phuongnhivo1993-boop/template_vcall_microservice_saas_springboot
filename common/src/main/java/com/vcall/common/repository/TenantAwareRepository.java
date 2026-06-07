package com.vcall.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

@NoRepositoryBean
public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.isDeleted = true")
    Page<T> findDeleted(Pageable pageable);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = false, e.deletedAt = null WHERE e.id = :id")
    int restoreById(@Param("id") ID id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.isDeleted = false, e.deletedAt = null WHERE e.id IN :ids")
    int restoreAllById(@Param("ids") Collection<ID> ids);
}
