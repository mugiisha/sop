package com.sop_workflow_service.sop_workflow_service.service;
import com.sop_workflow_service.sop_workflow_service.model.Author;
import com.sop_workflow_service.sop_workflow_service.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    // Create a new Author
    public Author createAuthor(Author author) {
        return authorRepository.save(author);
    }

    // Get all Authors
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    // Get an Author by ID
    public Author getAuthorById(String id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with ID: " + id));
    }

    // Update an Author
    public Author updateAuthor(String id, Author updatedAuthor) {
        Author existingAuthor = getAuthorById(id);
        existingAuthor.setName(updatedAuthor.getName());
        existingAuthor.setEmail(updatedAuthor.getEmail());
        return authorRepository.save(existingAuthor);
    }

    // Delete an Author
    public void deleteAuthor(String id) {
        if (!authorRepository.existsById(id)) {
            throw new RuntimeException("Author not found with ID: " + id);
        }
        authorRepository.deleteById(id);
    }
}
