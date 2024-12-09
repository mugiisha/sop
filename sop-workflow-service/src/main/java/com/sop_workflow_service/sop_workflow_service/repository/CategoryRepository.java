package com.sop_workflow_service.sop_workflow_service.repository;

import com.sop_workflow_service.sop_workflow_service.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Category findFirstByName(String name);
}
