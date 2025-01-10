package com.sop_workflow_service.sop_workflow_service.controller;

import com.sop_workflow_service.sop_workflow_service.dto.CategoryDto;
import com.sop_workflow_service.sop_workflow_service.model.Category;
import com.sop_workflow_service.sop_workflow_service.service.CategoryService;
import com.sop_workflow_service.sop_workflow_service.utils.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public Response<Category> createCategory(@Valid @RequestBody CategoryDto categoryDto){
        Category category = categoryService.addCategory(categoryDto.getName());
        return new Response<>(true, "Category added successfully", category);
    }

    @GetMapping
    public Response<List<Category>> getCategories(){
        List<Category> categories = categoryService.getCategories();
        return new Response<>(true, "Categories retrieved successfully", categories);
    }

    @GetMapping("/{id}")
    public Response<Category> getCategory(@PathVariable String id){
        Category category = categoryService.getCategoryById(id);
        return new Response<>(true, "Category retrieved successfully", category);
    }

    @PutMapping("/{id}")
    public Response<Category> updateCategory(@PathVariable String id, @Valid @RequestBody CategoryDto categoryDto){
        Category category = categoryService.updateCategory(id,categoryDto.getName());
        return new Response<>(true, "Category updated successfully", category);
    }

    @DeleteMapping("/{id}")
    public Response<Object> deleteCategory(@PathVariable String id){
        categoryService.deleteCategory(id);
        return new Response<>(true, "Category deleted successfully", null);
    }

}
