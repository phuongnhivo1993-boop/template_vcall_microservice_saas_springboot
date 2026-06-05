package com.vcall.xr.scene.repository;

import com.vcall.xr.scene.domain.SceneNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SceneNodeRepository extends JpaRepository<SceneNode, UUID> {

    List<SceneNode> findBySceneIdOrderBySortOrderAsc(UUID sceneId);

    List<SceneNode> findBySceneIdAndParentIdOrderBySortOrderAsc(UUID sceneId, UUID parentId);

    long countBySceneId(UUID sceneId);

    void deleteBySceneId(UUID sceneId);
}
