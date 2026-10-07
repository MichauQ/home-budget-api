package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@PersistenceContext
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateTransactionAndSaveItInDatabase() {
        // given
        CreateTransactionRequest testTransactionRequest = new CreateTransactionRequest(
                "Test Transaction",
                BigDecimal.valueOf(25),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 8, 17),
                null
        );
        // when
        TransactionResponse result = transactionService.createTransaction(testTransactionRequest);
        entityManager.flush();
        entityManager.clear();
        Optional<Transaction> foundTransaction = transactionRepository.findById(result.id());
        // then
        assertNotNull(result.id());
        assertEquals("Test Transaction", result.name());
        assertEquals(BigDecimal.valueOf(25),result.amount());
        assertTrue(foundTransaction.isPresent());
        assertEquals("Test Transaction", foundTransaction.get().getName());
        assertEquals(0,new BigDecimal("25").compareTo(foundTransaction.get().getAmount()));
    }
}
