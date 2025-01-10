package com.analytics_insights_service.analytics_insights_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sop_reads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SopReads {
    @Id
    private String sopId;
    private Integer reads;
}
