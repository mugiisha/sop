package com.sop_recommendation_service.sop_recommendation_service.repository;

import com.sop_recommendation_service.sop_recommendation_service.models.SOP;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SOPRepository extends ReactiveMongoRepository<SOP, String> {
    Flux<SOP> findByDepartmentAndCategory(String department, String category);
    Flux<SOP> findByDepartment(String department);
    Flux<SOP> findByTitleContainingIgnoreCase(String title);
    Flux<SOP> findByStatusAndDepartment(String status, String department);
}