package com.vcall.pbx.repository;

import com.vcall.pbx.entity.Extension;
import com.vcall.pbx.entity.Extension.ExtensionStatus;
import com.vcall.pbx.entity.Extension.ExtensionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtensionRepository extends JpaRepository<Extension, Long>, JpaSpecificationExecutor<Extension> {

    Optional<Extension> findByExtensionNumber(String extensionNumber);

    List<Extension> findByStatus(ExtensionStatus status);
    Page<Extension> findByStatus(ExtensionStatus status, Pageable pageable);

    List<Extension> findBySipAccountId(Long sipAccountId);
    Page<Extension> findBySipAccountId(Long sipAccountId, Pageable pageable);

    List<Extension> findByType(ExtensionType type);
}
