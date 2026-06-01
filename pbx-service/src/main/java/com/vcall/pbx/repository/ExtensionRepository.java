package com.vcall.pbx.repository;

import com.vcall.pbx.entity.Extension;
import com.vcall.pbx.entity.Extension.ExtensionStatus;
import com.vcall.pbx.entity.Extension.ExtensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtensionRepository extends JpaRepository<Extension, Long> {

    Optional<Extension> findByExtensionNumber(String extensionNumber);

    List<Extension> findByStatus(ExtensionStatus status);

    List<Extension> findBySipAccountId(Long sipAccountId);

    List<Extension> findByType(ExtensionType type);
}
