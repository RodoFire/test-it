package com.example.testit.repository;

import com.example.testit.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class BookRepositoryIT {

    @Autowired
    BookRepository bookRepository;

    @Test
    void findByMemberAndStatus() {
        Book book = new Book("Test Book", "Test Author");

        bookRepository.save(book);
    }

    @Test
    void findByBorrowerId() {
    }
}
