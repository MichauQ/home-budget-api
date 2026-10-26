package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.dto.CategoryExpenseSummaryResponse;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.transactionDate BETWEEN :from AND :to")
    BigDecimal sumAmountByTypeAndDateRange(TransactionType type, LocalDate from, LocalDate to);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.transactionDate >= :from")
    BigDecimal sumAmountByTypeAndFromDate(TransactionType type, LocalDate from);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.transactionDate <= :to")
    BigDecimal sumAmountByTypeAndToDate(TransactionType type, LocalDate to);

    @Query("""
            SELECT new io.github.michauq.homebudget.transaction.dto.CategoryExpenseSummaryResponse(
                t.category,
                SUM(t.amount)
            )
             FROM Transaction t\s
             WHERE t.type = :type
             GROUP BY t.category
             ORDER BY SUM(t.amount) DESC
            \s""")
    List<CategoryExpenseSummaryResponse> sumExpenseByCategory(@Param("type") TransactionType type);

    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    Page<Transaction> findByTransactionDate(LocalDate transactionDate, Pageable pageable);
    Page<Transaction> findByTypeAndTransactionDate(TransactionType type, LocalDate transactionDate, Pageable pageable);

    Page<Transaction> findByTransactionDateBetween(LocalDate from, LocalDate to, Pageable pageable);
    Page<Transaction> findByTypeAndTransactionDateBetween(TransactionType type, LocalDate from, LocalDate to, Pageable pageable);

    Page<Transaction> findByTransactionDateGreaterThanEqual(LocalDate from, Pageable pageable);
    Page<Transaction> findByTypeAndTransactionDateGreaterThanEqual(TransactionType type, LocalDate from, Pageable pageable);

    Page<Transaction> findByTransactionDateLessThanEqual(LocalDate to, Pageable pageable);
    Page<Transaction> findByTypeAndTransactionDateLessThanEqual(TransactionType type, LocalDate to, Pageable pageable);
}
