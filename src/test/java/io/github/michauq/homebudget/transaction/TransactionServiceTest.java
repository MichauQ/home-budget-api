package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.dto.CreateTransactionRequest;
import io.github.michauq.homebudget.transaction.dto.TransactionResponse;
import io.github.michauq.homebudget.transaction.dto.TransactionSummaryResponse;
import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldReturnCorrectBalanceSummary(){
        //given
        when(transactionRepository.sumAmountByType(TransactionType.INCOME)).thenReturn(BigDecimal.valueOf(150));
        when(transactionRepository.sumAmountByType(TransactionType.EXPENSE)).thenReturn(BigDecimal.valueOf(30));
        //when
        TransactionSummaryResponse result = transactionService.getBalanceSummary();
        //then
        assertEquals(BigDecimal.valueOf(150),result.income());
        assertEquals(BigDecimal.valueOf(30),result.expenses());
        assertEquals(BigDecimal.valueOf(120),result.balance());
        verify(transactionRepository).sumAmountByType(TransactionType.INCOME);
        verify(transactionRepository).sumAmountByType(TransactionType.EXPENSE);
    }

    @Test
    void shouldReturnEmptyBalanceSummary(){
        //given
        when(transactionRepository.sumAmountByType(TransactionType.INCOME)).thenReturn(null);
        when(transactionRepository.sumAmountByType(TransactionType.EXPENSE)).thenReturn(null);
        //when
        TransactionSummaryResponse result = transactionService.getBalanceSummary();
        //then
        assertEquals(BigDecimal.ZERO,result.income());
        assertEquals(BigDecimal.ZERO,result.expenses());
        assertEquals(BigDecimal.ZERO,result.balance());
        verify(transactionRepository).sumAmountByType(TransactionType.INCOME);
        verify(transactionRepository).sumAmountByType(TransactionType.EXPENSE);
    }

    @Test
    void shouldCreateTransaction(){
        //given
        CreateTransactionRequest testCreateRequestData = new CreateTransactionRequest(
                "Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,8,17),
                null
        );
        Transaction savedTransaction = new Transaction(
                "Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 8, 17),
                null
        );
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        //when
        TransactionResponse result = transactionService.createTransaction(testCreateRequestData);
        //then
        assertEquals("Test Transaction", result.name());
        assertEquals(BigDecimal.valueOf(20),result.amount());
        assertEquals(TransactionType.EXPENSE,result.type());
        assertEquals(LocalDate.of(2026, 8, 17),result.transactionDate());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void  shouldReturnTransactionWhenExists(){
        //given
        Transaction transaction = new Transaction(
                "Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 8, 17),
                null
        );
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        //when
        Optional<TransactionResponse> result = transactionService.getTransaction(1L);
        //then
        assertTrue(result.isPresent());
        assertEquals("Test Transaction", result.get().name());
        assertEquals(BigDecimal.valueOf(20),result.get().amount());
        assertEquals(TransactionType.EXPENSE,result.get().type());
        assertEquals(LocalDate.of(2026, 8, 17),result.get().transactionDate());
        verify(transactionRepository).findById(1L);
    }
    @Test
    void shouldReturnEmptyWhenTransactionDoesNotExist() {
        // given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());
        // when
        Optional<TransactionResponse> result = transactionService.getTransaction(999L);
        // then
        assertTrue(result.isEmpty());
        verify(transactionRepository).findById(999L);
    }
    @Test
    void shouldReturnAllTransactions() {
        // given
        List<Transaction> testFindAllData = new ArrayList<>();
        testFindAllData.add(new Transaction("Test Transaction1",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 8, 17),
                null));
        testFindAllData.add(new Transaction("Test Transaction2",
                BigDecimal.valueOf(35),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 9, 18),
                null));
        when(transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id"))
        )).thenReturn(testFindAllData);
        // when
        List<TransactionResponse> result = transactionService.getTransactions(null);
        // then
        assertEquals(2, result.size());
        assertEquals("Test Transaction1",result.get(0).name());
        assertEquals(BigDecimal.valueOf(20),result.get(0).amount());
        assertEquals(TransactionType.EXPENSE,result.get(0).type());
        assertEquals(LocalDate.of(2026, 8, 17),result.get(0).transactionDate());
        assertEquals("Test Transaction2",result.get(1).name());
        assertEquals(BigDecimal.valueOf(35),result.get(1).amount());
        assertEquals(TransactionType.EXPENSE,result.get(1).type());
        assertEquals(LocalDate.of(2026, 9, 18),result.get(1).transactionDate());

        verify(transactionRepository).findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
    }
    @Test
    void shouldReturnTransactionsFilteredByDate(){
        //given
        LocalDate dateParam = LocalDate.of(2026,10,5);
        List<Transaction> transactionsForDate = new ArrayList<>();
        transactionsForDate.add(new Transaction("Test Transaction1",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 5),
                null));
        transactionsForDate.add(new Transaction("Test Transaction2",
                BigDecimal.valueOf(35),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 5),
                null));
        when(transactionRepository.findByTransactionDate(dateParam, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")))).thenReturn(transactionsForDate);
        //when
        List<TransactionResponse> result = transactionService.getTransactions(dateParam);
        //then
        assertEquals(2, result.size());
        assertEquals("Test Transaction1",result.get(0).name());
        assertEquals(LocalDate.of(2026, 10, 5),result.get(0).transactionDate());
        assertEquals("Test Transaction2",result.get(1).name());
        assertEquals(LocalDate.of(2026, 10, 5),result.get(1).transactionDate());
        verify(transactionRepository).findByTransactionDate(dateParam, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        verify(transactionRepository,never()).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist(){
        // given
        when(transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")))).thenReturn(new ArrayList<>());
        // when
        List<TransactionResponse> result = transactionService.getTransactions(null);
        // then
        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
    }
    @Test
    void shouldDeleteTransactionWhenExists() {
        // given
        when(transactionRepository.existsById(1L)).thenReturn(true);
        // when
        boolean result = transactionService.deleteTransaction(1L);
        // then
        assertTrue(result);
        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteTransactionWhenDoesNotExist() {
        // given
        when(transactionRepository.existsById(999L)).thenReturn(false);
        // when
        boolean result = transactionService.deleteTransaction(999L);
        // then
        assertFalse(result);
        verify(transactionRepository, never()).deleteById(999L);
    }
    @Test
    void shouldUpdateTransactionWhenExists() {
        // given
        Transaction oldTransactionTestData = new Transaction(
                "Old Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 8, 17),
                null
        );

        Transaction updatedTransactionTestData = new Transaction(
                "Updated Test Transaction",
                BigDecimal.valueOf(35),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,9,20),
                null
        );
        CreateTransactionRequest createUpdatedTestDataRequest = new CreateTransactionRequest(
                "Updated Test Transaction",
                BigDecimal.valueOf(35),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,9,20),
                null
        );
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(oldTransactionTestData));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransactionTestData);
        // when
        Optional<TransactionResponse> result = transactionService.updateTransaction(1L,createUpdatedTestDataRequest);
        // then
        ArgumentCaptor<Transaction> transactionCaptor  = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertTrue(result.isPresent());
        Transaction capturedTransaction = transactionCaptor.getValue();
        assertEquals("Updated Test Transaction", capturedTransaction.getName());
        assertEquals(BigDecimal.valueOf(35), capturedTransaction.getAmount());
        assertEquals(LocalDate.of(2026,9,20), capturedTransaction.getTransactionDate());
        verify(transactionRepository).findById(1L);
    }
    @Test
    void shouldNotUpdateTransactionWhenDoesNotExist(){
        // given
        CreateTransactionRequest createUpdatedTestDataRequest = new CreateTransactionRequest(
                "Updated Test Transaction",
                BigDecimal.valueOf(35),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,9,20),
                null
        );

        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());
        //when
        Optional<TransactionResponse> result = transactionService.updateTransaction(999L,createUpdatedTestDataRequest);
        //then
        assertTrue(result.isEmpty());
        verify(transactionRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    @Test
    void shouldReturnTransactionsFromGivenDate() {
        // given
        LocalDate from = LocalDate.of(2026, 10, 15);
        List<Transaction> transactionAfter = List.of(new Transaction(
                "After Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 16),
                null
        ));
        when(transactionRepository.findByTransactionDateGreaterThanEqual(from, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")))).thenReturn(transactionAfter);
        // when
        List<TransactionResponse> result = transactionService.getTransactions(from, null);
        // then
        assertEquals(1,result.size());
        assertEquals("After Test Transaction", result.getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 16),
                result.getFirst().transactionDate());
        verify(transactionRepository).findByTransactionDateGreaterThanEqual(from, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        verify(transactionRepository, never()).findAll();
    }
    @Test
    void shouldReturnTransactionsToGivenDate() {
        // given
        LocalDate to = LocalDate.of(2026, 10, 15);
        List<Transaction> transactionBefore = List.of(new Transaction(
                "Before Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 1),
                null
        ));
        when(transactionRepository.findByTransactionDateLessThanEqual(to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")))).thenReturn(transactionBefore);
        // when
        List<TransactionResponse> result = transactionService.getTransactions(null, to);
        // then
        assertEquals(1,result.size());
        assertEquals("Before Test Transaction", result.getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 1),
                result.getFirst().transactionDate());
        verify(transactionRepository).findByTransactionDateLessThanEqual(to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        verify(transactionRepository, never()).findAll();
    }
    @Test
    void shouldReturnTransactionsWithinDateRange() {
        // given
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        List<Transaction> transactionInRange = List.of(new Transaction(
                "In Range Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 5),
                null));
        when(transactionRepository.findByTransactionDateBetween(from,to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")))).thenReturn(transactionInRange);
        // when
        List<TransactionResponse> result = transactionService.getTransactions(from, to);
        // then
        assertEquals(1,result.size());
        assertEquals("In Range Test Transaction", result.getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 5),
                result.getFirst().transactionDate());
        verify(transactionRepository).findByTransactionDateBetween(from,to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        verify(transactionRepository, never()).findAll();
    }
    @Test
    void shouldReturnAllTransactionsWhenBothDatesAreNull(){
        //given
        List<Transaction> allTransactions = List.of(new Transaction(
                "Transaction 1",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 6),
                null),
                new Transaction(
                "Transaction 2",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 5),
                null));
        when(transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")))).thenReturn(allTransactions);
        //when
        List<TransactionResponse> result = transactionService.getTransactions(null, null);
        //then
        assertEquals(2,result.size());
        assertEquals("Transaction 1", result.getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 6),
                result.getFirst().transactionDate());
        assertEquals("Transaction 2", result.getLast().name());
        assertEquals(LocalDate.of(2026, 10, 5),
                result.getLast().transactionDate());
        verify(transactionRepository).findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        verify(transactionRepository, never())
                .findByTransactionDateBetween(any(), any(), any());
    }
    @Test
    void shouldThrowExceptionWhenFromDateIsAfterToDate() {
        // given
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        // when + then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactions(from, to));
        assertEquals(
                "From date cannot be after to date",
                exception.getMessage()
        );
        verifyNoInteractions(transactionRepository);
    }
    @Test
    void shouldReturnTransactionsSortedByDateDescendingWhenNoFiltersProvided() {
        // given
        List<Transaction> allTransactions = List.of(
                new Transaction(
                        "Transaction 1",
                        BigDecimal.valueOf(20),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 31),
                        null),
                new Transaction(
                        "Transaction 2",
                        BigDecimal.valueOf(20),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 15),
                        null),
                new Transaction(
                        "Transaction 3",
                        BigDecimal.valueOf(20),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 1),
                        null));
        when(transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")))).thenReturn(allTransactions);
        // when
        List<TransactionResponse> result = transactionService.getTransactions(null, null);
        // then
        assertEquals(3,result.size());
        assertEquals(LocalDate.of(2026,10,31),result.getFirst().transactionDate());
        assertEquals(LocalDate.of(2026,10,15),result.get(1).transactionDate());
        assertEquals(LocalDate.of(2026,10,1),result.getLast().transactionDate());
        verify(transactionRepository).findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
    }
    @Test
    void shouldReturnTransactionSummaryForDateRange(){
        //given
        LocalDate from = LocalDate.of(2026,10,1);
        LocalDate to = LocalDate.of(2026,10,31);

        when(transactionRepository.sumAmountByTypeAndDateRange(TransactionType.INCOME, from, to)).thenReturn(BigDecimal.valueOf(150));
        when(transactionRepository.sumAmountByTypeAndDateRange(TransactionType.EXPENSE, from, to)).thenReturn(BigDecimal.valueOf(30));
        //when
        TransactionSummaryResponse result = transactionService.getBalanceSummary(from, to);
        //then
        assertEquals(BigDecimal.valueOf(150),result.income());
        assertEquals(BigDecimal.valueOf(30),result.expenses());
        assertEquals(BigDecimal.valueOf(120),result.balance());
        verify(transactionRepository).sumAmountByTypeAndDateRange(TransactionType.INCOME, from, to);
        verify(transactionRepository).sumAmountByTypeAndDateRange(TransactionType.EXPENSE, from, to);
    }
    @Test
    void shouldReturnTransactionSummaryFromDateInclusive(){
        //given
        LocalDate from = LocalDate.of(2026,10,1);

        when(transactionRepository.sumAmountByTypeAndFromDate(TransactionType.INCOME, from)).thenReturn(BigDecimal.valueOf(150));
        when(transactionRepository.sumAmountByTypeAndFromDate(TransactionType.EXPENSE, from)).thenReturn(BigDecimal.valueOf(30));
        //when
        TransactionSummaryResponse result = transactionService.getBalanceSummary(from, null);
        //then
        assertEquals(BigDecimal.valueOf(150),result.income());
        assertEquals(BigDecimal.valueOf(30),result.expenses());
        assertEquals(BigDecimal.valueOf(120),result.balance());
        verify(transactionRepository).sumAmountByTypeAndFromDate(TransactionType.INCOME, from);
        verify(transactionRepository).sumAmountByTypeAndFromDate(TransactionType.EXPENSE, from);
    }
}
