package com.sop_content_service.sop_content_service.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SopRequest {

    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Description is required")
    private String description;

    @NotEmpty(message = "Body is required")
    private String body;

    @NotEmpty(message = "Category is required")
    private String category;

    @NotEmpty(message = "Visibility is required")
    private String visibility;

    @NotEmpty(message = "Authors list is required")
    private List<String> authors;

    public @NotEmpty(message = "Approvers list is required") List<String> getApprovers() {
        return approvers;
    }

    public void setApprovers(@NotEmpty(message = "Approvers list is required") List<String> approvers) {
        this.approvers = approvers;
    }

    public @NotEmpty(message = "Reviewers list is required") List<String> getReviewers() {
        return reviewers;
    }

    public void setReviewers(@NotEmpty(message = "Reviewers list is required") List<String> reviewers) {
        this.reviewers = reviewers;
    }

    public @NotEmpty(message = "Authors list is required") List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(@NotEmpty(message = "Authors list is required") List<String> authors) {
        this.authors = authors;
    }

    public @NotEmpty(message = "Visibility is required") String getVisibility() {
        return visibility;
    }

    public void setVisibility(@NotEmpty(message = "Visibility is required") String visibility) {
        this.visibility = visibility;
    }

    public @NotEmpty(message = "Category is required") String getCategory() {
        return category;
    }

    public void setCategory(@NotEmpty(message = "Category is required") String category) {
        this.category = category;
    }

    public @NotEmpty(message = "Body is required") String getBody() {
        return body;
    }

    public void setBody(@NotEmpty(message = "Body is required") String body) {
        this.body = body;
    }

    public @NotEmpty(message = "Description is required") String getDescription() {
        return description;
    }

    public void setDescription(@NotEmpty(message = "Description is required") String description) {
        this.description = description;
    }

    public @NotEmpty(message = "Title is required") String getTitle() {
        return title;
    }

    public void setTitle(@NotEmpty(message = "Title is required") String title) {
        this.title = title;
    }

    @NotEmpty(message = "Reviewers list is required")
    private List<String> reviewers;

    @NotEmpty(message = "Approvers list is required")
    private List<String> approvers;
}
