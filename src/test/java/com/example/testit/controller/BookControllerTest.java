package com.example.testit.controller;

import com.example.testit.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private BookController bookController;

    @Test
    void createBook() {
        BookController.BookRequest request = new BookController.BookRequest();
        request.title = "Test Title";
        request.author = "Test Author";

        Book book = bookController.createBook(request);
        assertNotNull(book);
        assertEquals("Test Title", book.getTitle());
        assertEquals("Test Author", book.getAuthor());
    }
}