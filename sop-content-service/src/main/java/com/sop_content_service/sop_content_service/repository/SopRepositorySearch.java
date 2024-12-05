package com.sop_content_service.sop_content_service.repository;
import com.sop_content_service.sop_content_service.model.SopModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SopRepositorySearch extends MongoRepository<SopModel, String> {
//    Page<SopModel> findByDepartment(String department, Pageable pageable);
    Page<SopModel> findByCategory(String category, Pageable pageable);
    Page<SopModel> findByStatus(String status, Pageable pageable);

    @Query("{ $or: [ " +
            "{ title: { $regex: ?0, $options: 'i' } }, " +
            "{ description: { $regex: ?0, $options: 'i' } }, " +
            "{ content: { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<SopModel> searchByKeyword(String keyword, Pageable pageable);
}
