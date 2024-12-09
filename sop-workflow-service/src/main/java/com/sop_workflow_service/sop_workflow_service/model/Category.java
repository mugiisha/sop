package com.sop_workflow_service.sop_workflow_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "category")
public class Category {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
}
