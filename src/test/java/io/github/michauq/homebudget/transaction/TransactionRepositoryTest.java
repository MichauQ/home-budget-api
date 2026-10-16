package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
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
        List<Transaction> result = transactionRepository.findByTransactionDate(LocalDate.of(2026, 10, 5), Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        //then
        assertEquals(2,result.size());
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
        List<Transaction> result = transactionRepository.findByTransactionDate(LocalDate.of(2026, 10, 5), Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlyTransactionsWithinDateRange() {
        //given
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
        List<Transaction> result = transactionRepository.findByTransactionDateBetween(LocalDate.of(2026, 10, 1),LocalDate.of(2026,10,31), Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        //then
        assertEquals(2,result.size());
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
        List<Transaction> result = transactionRepository.findByTransactionDateBetween(LocalDate.of(2026, 10, 1),LocalDate.of(2026,10,1), Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        //then
        assertEquals(1, result.size());
        assertEquals("Test single day Transaction", result.getFirst().getName());
    }
    @Test
    void shouldReturnEmptyListWhenNoTransactionsMatchDateRange() {
        //given
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
        List<Transaction> result = transactionRepository.findByTransactionDateBetween(LocalDate.of(2026, 10, 1),LocalDate.of(2026,10,31), Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        //then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTransactionsOnOrAfterGivenDate() {
        //given
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
        List<Transaction> result = transactionRepository.findByTransactionDateGreaterThanEqual(LocalDate.of(2026, 10, 15), Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        // then
        assertEquals(2,result.size());
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
        List<Transaction> result = transactionRepository.findByTransactionDateLessThanEqual(LocalDate.of(2026, 10, 15), Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        // then
        assertEquals(2,result.size());
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
        List<Transaction> result = transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        //then
        assertEquals(3,result.size());
        assertEquals(LocalDate.of(2026,10,31),result.getFirst().getTransactionDate());
        assertEquals(LocalDate.of(2026,10,15),result.get(1).getTransactionDate());
        assertEquals(LocalDate.of(2026,10,1),result.getLast().getTransactionDate());
    }
    @Test
    void shouldReturnTransactionsWithSameDateSortedByIdDescending() {
        //given
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
        List<Transaction> result = transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        //then
        assertEquals(3,result.size());
        assertEquals(testTransaction3.getId(), result.getFirst().getId());
        assertEquals(testTransaction2.getId(),result.get(1).getId());
        assertEquals(testTransaction1.getId(),result.getLast().getId());
        assertTrue(result.get(0).getId() > result.get(1).getId());
        assertTrue(result.get(1).getId() > result.get(2).getId());
    }
}
