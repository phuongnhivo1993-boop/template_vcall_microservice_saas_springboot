package com.vcall.xr.scene.repository;

import com.vcall.xr.scene.domain.Hotspot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HotspotRepository extends JpaRepository<Hotspot, UUID> {

    List<Hotspot> findBySceneId(UUID sceneId);

    List<Hotspot> findByNodeId(UUID nodeId);

    List<Hotspot> findBySceneIdAndNodeId(UUID sceneId, UUID nodeId);

    long countBySceneId(UUID sceneId);

    void deleteBySceneId(UUID sceneId);
}
