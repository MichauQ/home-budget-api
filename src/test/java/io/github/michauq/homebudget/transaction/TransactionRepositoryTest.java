package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.dto.CategoryExpenseSummaryResponse;
import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void shouldReturnSumOfIncome(){
        //given
        Transaction testIncome1 = new Transaction(
                "Test Income 1",
                BigDecimal.valueOf(100),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,7),
                null
        );
        Transaction testIncome2 = new Transaction(
                "Test Income 2",
                BigDecimal.valueOf(50),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,7),
                null
        );
        Transaction testExpense = new Transaction(
                "Test Expense",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,7),
                null
        );
        transactionRepository.saveAndFlush(testIncome1);
        transactionRepository.saveAndFlush(testIncome2);
        transactionRepository.saveAndFlush(testExpense);
        //when
        BigDecimal result = transactionRepository.sumAmountByType(TransactionType.INCOME);
        //then
        assertEquals(0,new BigDecimal("150").compareTo(result));
    }

    @Test
    void shouldSaveTransactionAndReadIt(){
        //given
        Transaction testTransaction = new Transaction(
                "Test Transaction",
                BigDecimal.valueOf(25),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 8, 17),
                null
        );
        //when
        Transaction saveResult = transactionRepository.saveAndFlush(testTransaction);
        testEntityManager.clear();
        Optional<Transaction> findResult = transactionRepository.findById(saveResult.getId());
        //then
        assertNotNull(saveResult.getId());
        assertTrue(findResult.isPresent());
        assertEquals("Test Transaction", findResult.get().getName());
    }
    @Test
    void shouldFindOnlyTransactionsFromSpecifiedDate(){
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test Transaction 1",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,6),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction 2",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,5),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction 3",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,5),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByTransactionDate(LocalDate.of(2026, 10, 5), pageable);
        //then
        assertEquals(2, result.getContent().size());
        List<String> names = result.stream()
                .map(Transaction::getName)
                .toList();
        assertTrue(names.contains("Test Transaction 2"));
        assertTrue(names.contains("Test Transaction 3"));
        assertTrue(result.stream()
                .allMatch(transaction ->
                        transaction.getTransactionDate().equals(LocalDate.of(2026, 10, 5))));
        assertTrue(result.stream()
                .noneMatch(transaction ->
                        transaction.getName().equals("Test Transaction 1")));
    }
    @Test
    void shouldReturnEmptyListWhenNoTransactionsMatchDate(){
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Page<Transaction> result = transactionRepository.findByTransactionDate(LocalDate.of(2026, 10, 5), pageable);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlyTransactionsWithinDateRange() {
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test Transaction before range",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,9,6),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction 'from' border",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,1),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction 'to' border",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        Transaction testTransaction4 = new Transaction(
                "Test Transaction after range",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,11,5),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3,testTransaction4));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByTransactionDateBetween(LocalDate.of(2026, 10, 1),LocalDate.of(2026,10,31), pageable);
        //then
        assertEquals(2, result.getContent().size());
        List<String> names = result.stream()
                .map(Transaction::getName)
                .toList();
        assertTrue(names.contains("Test Transaction 'from' border"));
        assertTrue(names.contains("Test Transaction 'to' border"));
        assertTrue(result.stream()
                .allMatch(transaction ->
                        !transaction.getTransactionDate().isBefore(LocalDate.of(2026,10,1))
                                && !transaction.getTransactionDate().isAfter(LocalDate.of(2026,10,31))));
    }
    @Test
    void shouldReturnTransactionsWhenDateRangeIsSingleDay() {
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test single day Transaction",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,1),
                null
        );
        transactionRepository.saveAndFlush(testTransaction1);
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByTransactionDateBetween(LocalDate.of(2026, 10, 1),LocalDate.of(2026,10,1), pageable);
        //then
        assertEquals(1, result.getContent().size());
        assertEquals("Test single day Transaction", result.getContent().getFirst().getName());
    }
    @Test
    void shouldReturnEmptyListWhenNoTransactionsMatchDateRange() {
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test Transaction after range",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,11,5),
                null
        );
        transactionRepository.saveAndFlush(testTransaction1);
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByTransactionDateBetween(LocalDate.of(2026, 10, 1),LocalDate.of(2026,10,31), pageable);
        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTransactionsOnOrAfterGivenDate() {
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test Transaction before border",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,9),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction on border",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,15),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction after border",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByTransactionDateGreaterThanEqual(LocalDate.of(2026, 10, 15), pageable);
        // then
        assertEquals(2,result.getContent().size());
        List<String> names = result.stream()
                .map(Transaction::getName)
                .toList();
        assertTrue(names.contains("Test Transaction on border"));
        assertTrue(names.contains("Test Transaction after border"));
        assertTrue(result.stream()
                .noneMatch(transaction ->
                        transaction.getTransactionDate().isBefore(LocalDate.of(2026,10,15))));

    }
    @Test
    void shouldReturnTransactionsOnOrBeforeGivenDate() {
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test Transaction before border",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,9),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction on border",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,15),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction after border",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByTransactionDateLessThanEqual(LocalDate.of(2026, 10, 15), pageable);
        // then
        assertEquals(2,result.getContent().size());
        List<String> names = result.stream()
                .map(Transaction::getName)
                .toList();
        assertTrue(names.contains("Test Transaction on border"));
        assertTrue(names.contains("Test Transaction before border"));
        assertTrue(result.stream()
                .noneMatch(transaction ->
                        transaction.getTransactionDate().isAfter(LocalDate.of(2026,10,15))));
    }
    @Test
    void shouldReturnTransactionsSortedByDateDescending() {
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test Transaction sort 1",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction sort 2",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,1),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction sort 3",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,15),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findAll(pageable);
        //then
        assertEquals(3,result.getContent().size());
        assertEquals(LocalDate.of(2026,10,31),result.getContent().getFirst().getTransactionDate());
        assertEquals(LocalDate.of(2026,10,15),result.getContent().get(1).getTransactionDate());
        assertEquals(LocalDate.of(2026,10,1),result.getContent().getLast().getTransactionDate());
    }
    @Test
    void shouldReturnTransactionsWithSameDateSortedByIdDescending() {
        //given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "Test Transaction sort 1",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction sort 2",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction sort 3",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findAll(pageable);
        //then
        assertEquals(3,result.getContent().size());
        assertEquals(testTransaction3.getId(), result.getContent().getFirst().getId());
        assertEquals(testTransaction2.getId(),result.getContent().get(1).getId());
        assertEquals(testTransaction1.getId(),result.getContent().getLast().getId());
        assertTrue(result.getContent().get(0).getId() > result.getContent().get(1).getId());
        assertTrue(result.getContent().get(1).getId() > result.getContent().get(2).getId());
    }
    @Test
    void shouldSumTransactionsOfGivenTypeWithinDateRange(){
        //given
        LocalDate from = LocalDate.of(2026,10,1);
        LocalDate to = LocalDate.of(2026,10,31);
        Transaction testTransaction1 = new Transaction(
                "Test Transaction before range",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,9,6),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction 'from' border",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,1),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction 'to' border",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        Transaction testTransaction4 = new Transaction(
                "Test Transaction of different type",
                BigDecimal.valueOf(100),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,15),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3,testTransaction4));
        testEntityManager.clear();
        //when
        BigDecimal result = transactionRepository.sumAmountByTypeAndDateRange(TransactionType.EXPENSE,from, to);
        //then
        assertEquals(0,BigDecimal.valueOf(80).compareTo(result));
    }
    @Test
    void shouldSumTransactionsOfGivenTypeFromDateInclusive(){
        //given
        LocalDate from = LocalDate.of(2026,10,1);
        Transaction testTransaction1 = new Transaction(
                "Test Transaction before range",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,9,6),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "Test Transaction 'from' border",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,1),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "Test Transaction after 'from'",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,31),
                null
        );
        Transaction testTransaction4 = new Transaction(
                "Test Transaction of different type",
                BigDecimal.valueOf(100),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,15),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3,testTransaction4));
        testEntityManager.clear();
        //when
        BigDecimal result = transactionRepository.sumAmountByTypeAndFromDate(TransactionType.EXPENSE,from);
        //then
        assertEquals(0,BigDecimal.valueOf(80).compareTo(result));
    }

    @Test
    void shouldSumExpensesByCategoryAndSortDescending() {
        // Given
        Transaction testTransaction1 = new Transaction(
                "TEST FOOD EXPENSE 1",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                LocalDate.of(2026,9,6),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "TEST FOOD EXPENSE 2",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                LocalDate.of(2026,10,1),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "TEST TRANSPORT EXPENSE",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.TRANSPORT,
                LocalDate.of(2026,10,31),
                null
        );
        Transaction testTransaction4 = new Transaction(
                "TEST INCOME",
                BigDecimal.valueOf(500),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,15),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3,testTransaction4));
        testEntityManager.clear();
        //When
        List<CategoryExpenseSummaryResponse> result = transactionRepository.sumExpenseByCategory(TransactionType.EXPENSE);
        // Then
        assertEquals(2,result.size());
        assertEquals(TransactionCategory.FOOD,result.getFirst().category());
        assertEquals(0,BigDecimal.valueOf(150).compareTo(result.getFirst().total()));
        assertEquals(TransactionCategory.TRANSPORT,result.getLast().category());
        assertEquals(0,BigDecimal.valueOf(30).compareTo(result.getLast().total()));
    }
    @Test
    void shouldReturnEmptyListWhenNoExpensesExist(){
        //when
        List<CategoryExpenseSummaryResponse> result = transactionRepository.sumExpenseByCategory(TransactionType.EXPENSE);
        //then
        assertTrue(result.isEmpty());
    }
    @Test
    void shouldFindTransactionsByType(){
        // Given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Transaction testTransaction1 = new Transaction(
                "TEST EXPENSE",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                LocalDate.of(2026,9,6),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "TEST INCOME",
                BigDecimal.valueOf(50),
                TransactionType.INCOME,
                TransactionCategory.FOOD,
                LocalDate.of(2026,10,1),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByType(TransactionType.EXPENSE, pageable);
        // Then
        assertEquals(1,result.getContent().size());
        assertEquals("TEST EXPENSE", result.getContent().getFirst().getName());
        assertEquals(TransactionType.EXPENSE,result.getContent().getFirst().getType());
    }
    @Test
    void shouldFilterTransactionsByTypeAndDateRange(){
        // Given
        Pageable pageable = PageRequest.of(0,5,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        LocalDate from = LocalDate.of(2026,10,1);
        LocalDate to = LocalDate.of(2026,10,31);
        Transaction testTransaction1 = new Transaction(
                "TEST EXPENSE OUT OF RANGE",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                LocalDate.of(2026,9,6),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "TEST EXPENSE IN RANGE BORDER",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                LocalDate.of(2026,10,1),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "TEST EXPENSE IN RANGE",
                BigDecimal.valueOf(30),
                TransactionType.EXPENSE,
                TransactionCategory.TRANSPORT,
                LocalDate.of(2026,10,16),
                null
        );
        Transaction testTransaction4 = new Transaction(
                "TEST INCOME",
                BigDecimal.valueOf(500),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,15),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3,testTransaction4));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByTypeAndTransactionDateBetween(TransactionType.EXPENSE, from, to, pageable);
        // Then
        assertEquals(2,result.getContent().size());

        assertEquals("TEST EXPENSE IN RANGE", result.getContent().getFirst().getName());
        assertEquals(TransactionType.EXPENSE,result.getContent().getFirst().getType());
        assertEquals(LocalDate.of(2026,10,16),result.getContent().getFirst().getTransactionDate());

        assertEquals("TEST EXPENSE IN RANGE BORDER", result.getContent().getLast().getName());
        assertEquals(TransactionType.EXPENSE,result.getContent().getLast().getType());
        assertEquals(LocalDate.of(2026,10,1),result.getContent().getLast().getTransactionDate());
    }
    @Test
    void shouldReturnFirstPageOfFilteredTransactionsByType(){
        //given
        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id"))
        );
        Transaction testTransaction1 = new Transaction(
                "TEST EXPENSE 1",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.GROCERIES,
                LocalDate.of(2026,10,1),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "TEST EXPENSE 2",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.TRANSPORT,
                LocalDate.of(2026,10,5),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "TEST INCOME",
                BigDecimal.valueOf(500),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026,10,10),
                null
        );
        Transaction testTransaction4 = new Transaction(
                "TEST EXPENSE 3",
                BigDecimal.valueOf(60),
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                LocalDate.of(2026,10,15),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1,testTransaction2,testTransaction3,testTransaction4));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByType(TransactionType.EXPENSE, pageable);
        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(3,result.getTotalElements());
        assertEquals(2,result.getTotalPages());
        assertEquals(0,result.getNumber());
        assertEquals("TEST EXPENSE 3",result.getContent().getFirst().getName());
        assertEquals(TransactionCategory.FOOD,result.getContent().getFirst().getCategory());
        assertEquals("TEST EXPENSE 2",result.getContent().get(1).getName());
        assertEquals(TransactionCategory.TRANSPORT,result.getContent().get(1).getCategory());
    }
    @Test
    void shouldReturnSecondPageOfFilteredTransactionsByType() {
        //given
        Pageable pageable = PageRequest.of(
                1,
                2,
                Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id"))
        );
        Transaction testTransaction1 = new Transaction(
                "TEST EXPENSE 1",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.GROCERIES,
                LocalDate.of(2026, 10, 1),
                null
        );
        Transaction testTransaction2 = new Transaction(
                "TEST EXPENSE 2",
                BigDecimal.valueOf(50),
                TransactionType.EXPENSE,
                TransactionCategory.TRANSPORT,
                LocalDate.of(2026, 10, 5),
                null
        );
        Transaction testTransaction3 = new Transaction(
                "TEST INCOME",
                BigDecimal.valueOf(500),
                TransactionType.INCOME,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 10),
                null
        );
        Transaction testTransaction4 = new Transaction(
                "TEST EXPENSE 3",
                BigDecimal.valueOf(60),
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                LocalDate.of(2026, 10, 15),
                null
        );
        transactionRepository.saveAllAndFlush(List.of(testTransaction1, testTransaction2, testTransaction3, testTransaction4));
        testEntityManager.clear();
        //when
        Page<Transaction> result = transactionRepository.findByType(TransactionType.EXPENSE, pageable);
        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getNumber());
        assertEquals("TEST EXPENSE 1", result.getContent().getFirst().getName());
        assertEquals(TransactionCategory.GROCERIES, result.getContent().getFirst().getCategory());
    }
}
