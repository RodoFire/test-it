package com.example.testit.service;

import com.example.testit.adapter.mail.MailService;
import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BookServiceIT {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MailService mailService;



    @Autowired
    private BookService bookService;


    @Test
    void borrowBook() {
        Book book1 = new Book("Test Book1", "Test Author1");
        book1.setStatus(Status.DISPONIBLE);

        Member member1 = new Member("Test Member1");

        book1 = bookRepository.save(book1);
        member1 = memberRepository.save(member1);

        book1 = bookService.borrowBook(book1.getId(), member1.getId());

        assertEquals(Status.EMPRUNTE, book1.getStatus());

    }

    @Test
    void returnBook() {
        Member member1 = new Member("Test Member1");

        Book book1 = new Book("Test Book1", "Test Author1");
        book1.setStatus(Status.EMPRUNTE);
        book1.setBorrower(member1);


        member1 = memberRepository.save(member1);
        book1 = bookRepository.save(book1);

        book1 = bookService.returnBook(book1.getId(), member1.getId());


        assertEquals(Status.RENDU, book1.getStatus());
    }

}
