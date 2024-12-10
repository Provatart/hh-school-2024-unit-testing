package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibraryManagerTest {
  private LibraryManager libraryManager;
  private NotificationService notificationService;
  private UserService userService;

  @BeforeEach
  void setUp() {
    notificationService = mock(NotificationService.class);
    userService = mock(UserService.class);
    libraryManager = new LibraryManager(notificationService, userService);
    libraryManager.addBook("054", 10);

  }

  @Test
  void borrowBookByNotActiveUser() {
    when(userService.isUserActive("001")).thenReturn(false);
    boolean result = libraryManager.borrowBook("054", "001");
    verify(notificationService).notifyUser("001", "Your account is not active.");
    assertFalse(result);
  }

  @Test
  void borrowBookNoSufficientBooks() {
    when(userService.isUserActive("001")).thenReturn(true);
    libraryManager.addBook("053", 0);
    boolean result = libraryManager.borrowBook("053", "001");
    assertFalse(result);
  }

  @Test
  void borrowBookSufficientSingleBooks() {
    when(userService.isUserActive("001")).thenReturn(true);
    libraryManager.addBook("053", 1);
    assertEquals(libraryManager.getAvailableCopies("053"), 1);
    libraryManager.borrowBook("053", "001");
    assertEquals(libraryManager.getAvailableCopies("053"), 0);
    verify(notificationService).notifyUser("001", "You have borrowed the book: 053");
    boolean result = libraryManager.borrowBook("053", "001");
    assertFalse(result);
  }

  @Test
  void borrowBookWithSufficientBooks() {
    when(userService.isUserActive("001")).thenReturn(true);
    boolean result = libraryManager.borrowBook("054", "001");
    assertEquals(libraryManager.getAvailableCopies("054"), 9);
    verify(notificationService).notifyUser("001", "You have borrowed the book: 054");
    assertTrue(result);
  }

  @Test
  void returnBorrowedBook() {
    when(userService.isUserActive("001")).thenReturn(true);
    libraryManager.borrowBook("054", "001");
    assertEquals(libraryManager.getAvailableCopies("054"), 9);
    boolean result = libraryManager.returnBook("054", "001");
    verify(notificationService).notifyUser("001", "You have returned the book: 054");
    assertEquals(libraryManager.getAvailableCopies("054"), 10);
    assertTrue(result);
  }

  @Test
  void returnBorrowedBookDifferentUser() {
    when(userService.isUserActive("002")).thenReturn(true);
    libraryManager.borrowBook("054", "002");
    boolean result = libraryManager.returnBook("054", "001");
    assertFalse(result);
  }

  @Test
  void returnNotBorrowedBook() {
    when(userService.isUserActive("001")).thenReturn(true);
    boolean result = libraryManager.returnBook("054", "001");
    assertFalse(result);
  }

  @Test
  void getAvailableCopiesTest() {
    int availableCopies = libraryManager.getAvailableCopies("054");
    assertEquals(10, availableCopies);
  }

  @Test
  void calculateDynamicLateFeeWithException() {
    assertThrows(IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-10, true, true));
  }

  @ParameterizedTest
  @CsvSource({
      "4, true, false, 3",
      "20, true, true, 12",
      "10, false, false, 5",
      "3, false, true, 1.2",
      "0, false, false, 0"
  })
  void calculateDynamicLateFee(int overdueDays, boolean isBestseller, boolean isPremiumMember, double expectedFee) {
    double fee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);
    assertEquals(expectedFee, fee);
  }

}