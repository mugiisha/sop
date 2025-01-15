package com.sop_workflow_service.sop_workflow_service.services;

import com.sop_workflow_service.sop_workflow_service.model.Category;
import com.sop_workflow_service.sop_workflow_service.repository.CategoryRepository;
import com.sop_workflow_service.sop_workflow_service.service.CategoryService;
import com.sop_workflow_service.sop_workflow_service.utils.exception.AlreadyExistsException;
import com.sop_workflow_service.sop_workflow_service.utils.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private static final String TEST_ID = "test-id";
    private static final String TEST_NAME = "Test Category";

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(TEST_ID);
        testCategory.setName(TEST_NAME);
    }

    @Test
    void addCategory_Success() {
        // Arrange
        when(categoryRepository.findFirstByName(TEST_NAME)).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        Category result = categoryService.addCategory(TEST_NAME);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_NAME, result.getName());
        verify(categoryRepository).findFirstByName(TEST_NAME);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void addCategory_ThrowsAlreadyExistsException() {
        // Arrange
        when(categoryRepository.findFirstByName(TEST_NAME)).thenReturn(testCategory);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () ->
                categoryService.addCategory(TEST_NAME)
        );
        verify(categoryRepository).findFirstByName(TEST_NAME);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategories_ReturnsListOfCategories() {
        // Arrange
        List<Category> expectedCategories = Arrays.asList(testCategory);
        when(categoryRepository.findAll()).thenReturn(expectedCategories);

        // Act
        List<Category> result = categoryService.getCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_NAME, result.get(0).getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryRepository.findById(TEST_ID)).thenReturn(Optional.of(testCategory));

        // Act
        Category result = categoryService.getCategoryById(TEST_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(categoryRepository).findById(TEST_ID);
    }

    @Test
    void getCategoryById_ThrowsNotFoundException() {
        // Arrange
        when(categoryRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                categoryService.getCategoryById(TEST_ID)
        );
        verify(categoryRepository).findById(TEST_ID);
    }

    @Test
    void getCategoryByName_Success() {
        // Arrange
        when(categoryRepository.findFirstByName(TEST_NAME)).thenReturn(testCategory);

        // Act
        Category result = categoryService.getCategoryByName(TEST_NAME);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_NAME, result.getName());
        verify(categoryRepository).findFirstByName(TEST_NAME);
    }

    @Test
    void getCategoryByName_ReturnsNull() {
        // Arrange
        when(categoryRepository.findFirstByName(TEST_NAME)).thenReturn(null);

        // Act
        Category result = categoryService.getCategoryByName(TEST_NAME);

        // Assert
        assertNull(result);
        verify(categoryRepository).findFirstByName(TEST_NAME);
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        String newName = "Updated Category";
        Category updatedCategory = new Category();
        updatedCategory.setId(TEST_ID);
        updatedCategory.setName(newName);

        when(categoryRepository.findById(TEST_ID)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // Act
        Category result = categoryService.updateCategory(TEST_ID, newName);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        verify(categoryRepository).findById(TEST_ID);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ThrowsNotFoundException() {
        // Arrange
        when(categoryRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                categoryService.updateCategory(TEST_ID, "New Name")
        );
        verify(categoryRepository).findById(TEST_ID);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        when(categoryRepository.findById(TEST_ID)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).deleteById(TEST_ID);

        // Act
        categoryService.deleteCategory(TEST_ID);

        // Assert
        verify(categoryRepository).findById(TEST_ID);
        verify(categoryRepository).deleteById(TEST_ID);
    }

    @Test
    void deleteCategory_ThrowsNotFoundException() {
        // Arrange
        when(categoryRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                categoryService.deleteCategory(TEST_ID)
        );
        verify(categoryRepository).findById(TEST_ID);
        verify(categoryRepository, never()).deleteById(any());
    }
}