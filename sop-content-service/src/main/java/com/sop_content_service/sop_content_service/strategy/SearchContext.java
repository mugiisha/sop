//package com.sop_content_service.sop_content_service.strategy;
//
//import com.sop_content_service.sop_content_service.dto.SopSearchRequest;
//import com.sop_content_service.sop_content_service.model.SopModel;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//@Service
//public class SearchContext {
//    private final SimpleSearchStrategy simpleSearchStrategy;
//    private final ComplexSearchStrategy complexSearchStrategy;
//
//    public SearchContext(SimpleSearchStrategy simpleSearchStrategy,
//                         ComplexSearchStrategy complexSearchStrategy) {
//        this.simpleSearchStrategy = simpleSearchStrategy;
//        this.complexSearchStrategy = complexSearchStrategy;
//    }
//
//    public Page<SopModel> executeSearch(SopSearchRequest request, Pageable pageable) {
//        SearchStrategy strategy = isSimpleQuery(request) ?
//                simpleSearchStrategy : complexSearchStrategy;
//        return strategy.search(request, pageable);
//    }
//
//    private boolean isSimpleQuery(SopSearchRequest request) {
//        boolean hasKeyword = StringUtils.hasText(request.getKeyword());
//        boolean hasDepartment = StringUtils.hasText(request.getDepartment());
//        boolean hasStatus = StringUtils.hasText(request.getStatus());
//        boolean hasCategory = StringUtils.hasText(request.getCategory());
//
//        return !hasKeyword && ((hasDepartment && !hasStatus && !hasCategory) ||
//                (hasStatus && !hasDepartment && !hasCategory) ||
//                (hasCategory && !hasDepartment && !hasStatus));
//    }
//}
