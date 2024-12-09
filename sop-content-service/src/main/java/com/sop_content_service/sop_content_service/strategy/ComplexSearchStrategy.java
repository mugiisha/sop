//package com.sop_content_service.sop_content_service.strategy;
//
//import com.sop_content_service.sop_content_service.dto.SopSearchRequest;
//import com.sop_content_service.sop_content_service.model.SopModel;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//public class ComplexSearchStrategy implements SearchStrategy {
//    private final MongoTemplate mongoTemplate;
//
//    public ComplexSearchStrategy(MongoTemplate mongoTemplate) {
//        this.mongoTemplate = mongoTemplate;
//    }
//
//    @Override
//    public Page<SopModel> search(SopSearchRequest request, Pageable pageable) {
//        Query query = buildQuery(request);
//        long total = mongoTemplate.count(query, SopModel.class);
//
//        query.with(pageable);
//        List<SopModel> results = mongoTemplate.find(query, SopModel.class);
//
//        return new PageImpl<>(results, pageable, total);
//    }
//
//    private Query buildQuery(SopSearchRequest request) {
//        Query query = new Query();
//        List<Criteria> criteriaList = new ArrayList<>();
//
//        Optional.ofNullable(request.getKeyword())
//                .filter(StringUtils::hasText)
//                .ifPresent(keyword -> criteriaList.add(new Criteria().orOperator(
//                        Criteria.where("title").regex(keyword, "i"),
//                        Criteria.where("description").regex(keyword, "i"),
//                        Criteria.where("content").regex(keyword, "i")
//                )));
//
//        Optional.ofNullable(request.getStatus())
//                .filter(StringUtils::hasText)
//                .ifPresent(status -> criteriaList.add(Criteria.where("status").is(status)));
//
//        Optional.ofNullable(request.getDepartment())
//                .filter(StringUtils::hasText)
//                .ifPresent(dept -> criteriaList.add(Criteria.where("department").is(dept)));
//
//        Optional.ofNullable(request.getCategory())
//                .filter(StringUtils::hasText)
//                .ifPresent(category -> criteriaList.add(Criteria.where("category").is(category)));
//
//        if (!criteriaList.isEmpty()) {
//            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
//        }
//
//        return query;
//    }
//}
