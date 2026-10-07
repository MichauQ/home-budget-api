package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
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
}
