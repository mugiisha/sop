package com.sop_content_service.sop_content_service.repository;

import com.sop_content_service.sop_content_service.model.Sop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SopRepositorySearch extends MongoRepository<Sop, String> {
    //    Page<SopModel> findByDepartment(String department, Pageable pageable);
    Page<Sop> findByCategory(String category, Pageable pageable);
    Page<Sop> findByVisibility(String visibility, Pageable pageable);  // Changed from findByStatus

    @Query("{ $or: [ " +
            "{ title: { $regex: ?0, $options: 'i' } }, " +
            "{ description: { $regex: ?0, $options: 'i' } }, " +
            "{ content: { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<Sop> searchByKeyword(String keyword, Pageable pageable);
}