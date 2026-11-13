package com.example.testit.repository;

import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DataJpaTest
class BookRepositoryIT {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void findByMemberAndStatus() {
        // given
        Book book1 = new Book("Test Book1", "Test Author1");
        Book book2 = new Book("Test Book2", "Test Author2");
        Book book3 = new Book("Test Book3", "Test Author3");
        Book book4 = new Book("Test Book4", "Test Author4");


        Member member1 = new Member("Test Member1");
        Member member2 = new Member("Test Member2");

        book1.setStatus(Status.EMPRUNTE);
        book2.setStatus(Status.EMPRUNTE);
        book3.setStatus(Status.EMPRUNTE);
        book4.setStatus(Status.DISPONIBLE);

        book1.setBorrower(member1);
        book2.setBorrower(member1);


        member1 = memberRepository.save(member1);
        member2 = memberRepository.save(member2);

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
        bookRepository.save(book4);

        // when
        List<Book> shouldBe2Books = bookRepository.findByMemberAndStatus(member1.getId(), Status.EMPRUNTE);
        List<Book> shouldBeNoBooks = bookRepository.findByMemberAndStatus(member2.getId(), Status.EMPRUNTE);

        //then
        assertEquals(2, shouldBe2Books.size());
        assertNotEquals(3, shouldBe2Books.size());

        assertEquals(0, shouldBeNoBooks.size());
        assertNotEquals(1, shouldBeNoBooks.size());

    }

    @Test
    void findByBorrowerId() {
        // given
        Book book1 = new Book("Test Book1", "Test Author1");
        Book book2 = new Book("Test Book2", "Test Author2");
        Book book3 = new Book("Test Book3", "Test Author3");
        Book book4 = new Book("Test Book4", "Test Author4");


        Member member1 = new Member("Test Member1");
        Member member2 = new Member("Test Member2");
        Member member3 = new Member("Test Member3");


        book1.setBorrower(member1);
        book2.setBorrower(member1);
        book3.setBorrower(member2);


        member1 = memberRepository.save(member1);
        member2 = memberRepository.save(member2);
        member3 = memberRepository.save(member3);

        bookRepository.save(book1);
        bookRepository.save(book2);
        bookRepository.save(book3);
        bookRepository.save(book4);

        // when
        List<Book> shouldBe2Books = bookRepository.findByBorrowerId(member1.getId());
        List<Book> shouldBe1Books = bookRepository.findByBorrowerId(member2.getId());
        List<Book> shouldBeNoBooks = bookRepository.findByBorrowerId(member3.getId());

        //then
        assertEquals(2, shouldBe2Books.size());
        assertNotEquals(3, shouldBe2Books.size());

        assertEquals(1, shouldBe1Books.size());
        assertNotEquals(0, shouldBe1Books.size());

        assertEquals(0, shouldBeNoBooks.size());
        assertNotEquals(1, shouldBeNoBooks.size());
    }
}
