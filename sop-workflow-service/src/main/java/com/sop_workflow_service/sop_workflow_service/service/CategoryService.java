package com.sop_workflow_service.sop_workflow_service.service;

import com.sop_workflow_service.sop_workflow_service.model.Category;
import com.sop_workflow_service.sop_workflow_service.repository.CategoryRepository;
import com.sop_workflow_service.sop_workflow_service.utils.exception.AlreadyExistsException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category addCategory(String name){
        log.info("creating category");
        Category existingCategory = categoryRepository.findFirstByName(name);

        if(existingCategory != null){
            throw new AlreadyExistsException("Category already exists");
        }

        Category category = new Category();
        category.setName(name);

        return categoryRepository.save(category);
    }

    public List<Category> getCategories(){
        return categoryRepository.findAll();
    }

    public Category getCategoryById(String id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error retrieving category with id: {}", id);
                    throw new NotFoundException("Category not found");
                });
    }

    public Category getCategoryByName(String name){
        return categoryRepository.findFirstByName(name);
    }

    public Category updateCategory(String id, String name){
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            log.error("Error retrieving category with id: {}", id);
            throw new NotFoundException("Category not found");
        });

        category.setName(name);
        categoryRepository.save(category);

        return category;
    }

    public void deleteCategory(String id){
        log.info("Deleting category");

        categoryRepository.findById(id).orElseThrow(
                () -> {
                    log.error("Error retrieving category with id: {}", id);
                    return new NotFoundException("Category not found");
                });

        categoryRepository.deleteById(id);
    }
}
