package com.vcall.call.repository;

import com.vcall.call.entity.IvrStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IvrStepRepository extends JpaRepository<IvrStep, Long> {

    List<IvrStep> findByIvrFlowIdOrderByStepOrder(Long ivrFlowId);
}
