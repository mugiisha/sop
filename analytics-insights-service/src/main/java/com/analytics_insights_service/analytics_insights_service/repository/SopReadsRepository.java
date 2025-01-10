package com.analytics_insights_service.analytics_insights_service.repository;

import com.analytics_insights_service.analytics_insights_service.model.SopReads;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SopReadsRepository extends MongoRepository<SopReads, String> {
}
