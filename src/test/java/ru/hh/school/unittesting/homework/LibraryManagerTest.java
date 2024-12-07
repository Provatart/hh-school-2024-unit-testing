package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    libraryManager.borrowBook("053", "001");
    boolean result = libraryManager.borrowBook("053", "001");
    assertFalse(result);
  }

  @Test
  void borrowBookWithSufficientBooks() {
    when(userService.isUserActive("001")).thenReturn(true);
    boolean result = libraryManager.borrowBook("054", "001");
    assertTrue(result);
  }

  @Test
  void returnBorrowedBook() {
    when(userService.isUserActive("001")).thenReturn(true);
    libraryManager.borrowBook("054", "001");
    boolean result = libraryManager.returnBook("054", "001");
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

  @Test
  void calculateDynamicLateFeeBestseller() {
    double fee = libraryManager.calculateDynamicLateFee(2, true, false);
    assertEquals(1.5, fee);
  }

  @Test
  void calculateDynamicLateFeePremiumMember() {
    double fee = libraryManager.calculateDynamicLateFee(2, false, true);
    assertEquals(0.8, fee);
  }

  @ParameterizedTest
  @CsvSource({
      "4, true, false, 3",
      "20, true, true, 12",
      "10, true, true, 6",
      "3, false, true, 1.2"
  })
  void calculateDynamicLateFee(int overdueDays, boolean isBestseller, boolean isPremiumMember,double expectedFee) {
    double fee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller,isPremiumMember);
    assertEquals(expectedFee, fee);
  }

}