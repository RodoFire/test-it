package com.example.testit.service;

import com.example.testit.adapter.mail.MailService;
import com.example.testit.model.Book;
import com.example.testit.model.Member;
import com.example.testit.model.Status;
import com.example.testit.repository.BookRepository;
import com.example.testit.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MailService mailService;

    @InjectMocks
    private BookService bookService;

    @Test
    void updateBook_shouldThrowExceptionIfBookNotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        Book bookWNullId = new Book();
        Book bookWId = new Book("Test Book", "Test Author");
        bookWId.setId(1L);

        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(bookWNullId));
        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(bookWId));
    }

    @Test
    void borrowBook_shouldNotBeBorrowedIfNullId() {
        // then
        assertThrows(IllegalArgumentException.class, () -> bookService.borrowBook(null, null), "Book ID and Member ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> bookService.borrowBook(null, 1L), "Book ID cannot be null");
        assertThrows(IllegalArgumentException.class, () -> bookService.borrowBook(1L, null), "Member ID cannot be null");
    }

    @Test
    void borrowBook_shouldNotBeBorrowedIfBookNotFound() {
        // given
        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(IllegalArgumentException.class, () -> bookService.borrowBook(book.getId(), 1L), "Book not found");
    }

    @Test
    void borrowBook_shouldNotBeBorrowedIfMemberDontExists() {
        // given
        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(IllegalArgumentException.class, () -> bookService.borrowBook(book.getId(), 1L), "Member not found");
    }

    @Test
    void borrowBook_shouldNotBeBorrowedIfStatusNotAvailable() {
        // given
        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);

        Member member = new Member("Test Member");

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // then
        assertThrows(IllegalStateException.class, () -> bookService.borrowBook(book.getId(), 1L), "Book can only be borrowed if status is DISPONIBLE");
    }

    @Test
    void borrowBook_shouldNotBeBorrowedIfMemberHasOngoingLoan() {
        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);

        Member member = new Member("Test Member");

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByMemberAndStatus(1L, Status.RENDU)).thenReturn(List.of(new Book()));

        assertThrows(IllegalStateException.class, () -> bookService.borrowBook(book.getId(), 1L), "Member cannot borrow more than one book at a time");
    }

    @Test
    void returnBook_shouldNotBeReturnedIfNullId() {
        // then
        assertThrows(IllegalArgumentException.class, () -> bookService.returnBook(null, 1L), "Book ID cannot be null");
    }

    @Test
    void returnBook_shouldThrowExceptionIfBookNotFound() {
        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookService.returnBook(1L, 1L));
    }

    @Test
    void returnBook_shouldThrowExceptionIfBookIsNotBorrowed() {
        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        book.setStatus(Status.DISPONIBLE);
        when(bookRepository.findById(book.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookService.returnBook(1L, 1L), "Book can only be returned if status is EMPRUNTE");
    }

    @Test
    void returnBook_shouldSendMailToBorrower() {
        Member member = new Member("Test Member");
        member.setId(1L);

        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);
        book.setBorrower(member);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        bookService.returnBook(book.getId(), member.getId());

        verify(mailService).sendMail(member, "Livre rendu", "Merci d'avoir rendu '" + book.getTitle() + "'.");
    }

    @Test
    void returnBook_shouldSendMailToReferentIfPresent() {
        Member member = new Member("Test Member");
        member.setId(1L);
        member.setReferent(new Member("Referent"));

        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);
        book.setBorrower(member);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        bookService.returnBook(book.getId(), member.getId());

        verify(mailService).sendMail(member, "Livre rendu", "Merci d'avoir rendu '" + book.getTitle() + "'.");
        verify(mailService).sendMail(member.getReferent(), "Livre rendu par un de vos membres",
                "'" + book.getTitle() + "' a été rendu par " + member.getUsername() + ".");
    }

    @Test
    void returnBook_shouldNotSendMailToReferentIfPresent() {
        Member member = new Member("Test Member");
        member.setId(1L);

        Book book = new Book("Test Book", "Test Author");
        book.setId(1L);
        book.setStatus(Status.EMPRUNTE);
        book.setBorrower(member);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        bookService.returnBook(book.getId(), member.getId());

        verify(mailService).sendMail(member, "Livre rendu", "Merci d'avoir rendu '" + book.getTitle() + "'.");
        verify(mailService, never()).sendMail(member.getReferent(), "Livre rendu par un de vos membres",
                "'" + book.getTitle() + "' a été rendu par " + member.getUsername() + ".");
    }
}
