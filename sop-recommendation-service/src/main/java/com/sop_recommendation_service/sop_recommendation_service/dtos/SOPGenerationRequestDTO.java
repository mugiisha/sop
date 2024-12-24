package com.sop_recommendation_service.sop_recommendation_service.dtos;
import lombok.Data;
import java.util.List;

@Data
public class SOPGenerationRequestDTO {
    private String title;
    private String department;
    private String category;
    private List<String> keywords;
    private String purpose;
    private String scope;
}