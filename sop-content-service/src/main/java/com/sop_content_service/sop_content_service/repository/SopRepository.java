package com.sop_content_service.sop_content_service.repository;

import com.sop_content_service.sop_content_service.model.SopModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SopRepository extends MongoRepository<SopModel, String> {
    List<SopModel> findAllByOrderByCreatedAtDesc();
    List<SopModel> findByStatusOrderByCreatedAtDesc(String status);
    boolean existsById(String id);
    SopModel findVersionsById(String id);


}
