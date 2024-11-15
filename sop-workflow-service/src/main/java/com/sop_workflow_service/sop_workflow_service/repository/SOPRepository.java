package com.sop_workflow_service.sop_workflow_service.repository;


import com.sop_workflow_service.sop_workflow_service.model.SOP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface SOPRepository extends JpaRepository<SOP, Long> {
}
