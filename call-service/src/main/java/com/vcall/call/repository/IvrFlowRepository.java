package com.vcall.call.repository;

import com.vcall.call.entity.IvrFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IvrFlowRepository extends JpaRepository<IvrFlow, Long> {

    Optional<IvrFlow> findByName(String name);
}
