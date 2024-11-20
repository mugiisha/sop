package com.sop_workflow_service.sop_workflow_service.controller;
import com.sop_workflow_service.sop_workflow_service.model.Author;
import com.sop_workflow_service.sop_workflow_service.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    // Create a new Author
    @PostMapping
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        Author createdAuthor = authorService.createAuthor(author);
        return ResponseEntity.status(201).body(createdAuthor);
    }

    // Get all Authors
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    // Get an Author by ID
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable String id) {
        Author author = authorService.getAuthorById(id);
        return ResponseEntity.ok(author);
    }

    // Update an Author
    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable String id, @RequestBody Author updatedAuthor) {
        Author author = authorService.updateAuthor(id, updatedAuthor);
        return ResponseEntity.ok(author);
    }

    // Delete an Author
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable String id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
