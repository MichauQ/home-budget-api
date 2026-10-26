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
import org.springframework.data.domain.*;

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
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<Transaction> transactionPage = new PageImpl<>(testFindAllData,pageable,5);
        when(transactionRepository.findAll(pageable)).thenReturn(transactionPage);
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(null,null, pageable);
        // then
        assertEquals(2, result.getContent().size());
        assertEquals("Test Transaction1",result.getContent().get(0).name());
        assertEquals(BigDecimal.valueOf(20),result.getContent().get(0).amount());
        assertEquals(TransactionType.EXPENSE,result.getContent().get(0).type());
        assertEquals(LocalDate.of(2026, 8, 17),result.getContent().get(0).transactionDate());
        assertEquals("Test Transaction2",result.getContent().get(1).name());
        assertEquals(BigDecimal.valueOf(35),result.getContent().get(1).amount());
        assertEquals(TransactionType.EXPENSE,result.getContent().get(1).type());
        assertEquals(LocalDate.of(2026, 9, 18),result.getContent().get(1).transactionDate());

        verify(transactionRepository).findAll(pageable);
    }
    @Test
    void shouldReturnTransactionsFilteredByDate(){
        //given
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<Transaction> transactionPage = new PageImpl<>(transactionsForDate,pageable,5);
        when(transactionRepository.findByTransactionDate(dateParam, pageable)).thenReturn(transactionPage);
        //when
        Page<TransactionResponse> result = transactionService.getTransactions(null, dateParam, pageable);
        //then
        assertEquals(2, result.getContent().size());
        assertEquals("Test Transaction1",result.getContent().get(0).name());
        assertEquals(LocalDate.of(2026, 10, 5),result.getContent().get(0).transactionDate());
        assertEquals("Test Transaction2",result.getContent().get(1).name());
        assertEquals(LocalDate.of(2026, 10, 5),result.getContent().get(1).transactionDate());
        verify(transactionRepository).findByTransactionDate(dateParam, pageable);
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist(){
        // given
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        when(transactionRepository.findAll(pageable)).thenReturn(Page.empty(pageable));
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(null,null,pageable);
        // then
        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll(pageable);
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
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        LocalDate from = LocalDate.of(2026, 10, 15);
        List<Transaction> transactionAfter = List.of(new Transaction(
                "After Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 16),
                null
        ));
        Page<Transaction> transactionPage = new PageImpl<>(transactionAfter,pageable,5);
        when(transactionRepository.findByTransactionDateGreaterThanEqual(from, pageable)).thenReturn(transactionPage);
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(null, from,null, pageable);
        // then
        assertEquals(1,result.getContent().size());
        assertEquals("After Test Transaction", result.getContent().getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 16),
                result.getContent().getFirst().transactionDate());
        verify(transactionRepository).findByTransactionDateGreaterThanEqual(from, pageable);
    }
    @Test
    void shouldReturnTransactionsToGivenDate() {
        // given
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        LocalDate to = LocalDate.of(2026, 10, 15);
        List<Transaction> transactionBefore = List.of(new Transaction(
                "Before Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 1),
                null
        ));
        Page<Transaction> transactionPage = new PageImpl<>(transactionBefore,pageable,5);
        when(transactionRepository.findByTransactionDateLessThanEqual(to, pageable)).thenReturn(transactionPage);
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(null,null, to, pageable);
        // then
        assertEquals(1,result.getContent().size());
        assertEquals("Before Test Transaction", result.getContent().getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 1),
                result.getContent().getFirst().transactionDate());
        verify(transactionRepository).findByTransactionDateLessThanEqual(to, pageable);
    }
    @Test
    void shouldReturnTransactionsWithinDateRange() {
        // given
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        List<Transaction> transactionInRange = List.of(new Transaction(
                "In Range Test Transaction",
                BigDecimal.valueOf(20),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 5),
                null));
        Page<Transaction> transactionPage = new PageImpl<>(transactionInRange,pageable,5);
        when(transactionRepository.findByTransactionDateBetween(from,to, pageable)).thenReturn(transactionPage);
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(null, from, to, pageable);
        // then
        assertEquals(1,result.getContent().size());
        assertEquals("In Range Test Transaction", result.getContent().getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 5),
                result.getContent().getFirst().transactionDate());
        verify(transactionRepository).findByTransactionDateBetween(from,to, pageable);
    }
    @Test
    void shouldReturnAllTransactionsWhenBothDatesAreNull(){
        //given
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<Transaction> transactionPage = new PageImpl<>(allTransactions,pageable,5);
        when(transactionRepository.findAll(pageable)).thenReturn(transactionPage);
        //when
        Page<TransactionResponse> result = transactionService.getTransactions(null,null, null, pageable);
        //then
        assertEquals(2,result.getContent().size());
        assertEquals("Transaction 1", result.getContent().getFirst().name());
        assertEquals(LocalDate.of(2026, 10, 6),
                result.getContent().getFirst().transactionDate());
        assertEquals("Transaction 2", result.getContent().getLast().name());
        assertEquals(LocalDate.of(2026, 10, 5),
                result.getContent().getLast().transactionDate());
        verify(transactionRepository).findAll(pageable);
        verify(transactionRepository, never())
                .findByTransactionDateBetween(any(), any(), any());
    }
    @Test
    void shouldThrowExceptionWhenFromDateIsAfterToDate() {
        // given
        Pageable pageable = PageRequest.of(0,2,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        // when + then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.getTransactions(null, from, to, pageable));
        assertEquals(
                "From date cannot be after to date",
                exception.getMessage()
        );
        verifyNoInteractions(transactionRepository);
    }
    @Test
    void shouldReturnTransactionsSortedByDateDescendingWhenNoFiltersProvided() {
        // given
        Pageable pageable = PageRequest.of(0,3,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<Transaction> transactionPage = new PageImpl<>(allTransactions,pageable,5);
        when(transactionRepository.findAll(pageable)).thenReturn(transactionPage);
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(null, null, pageable);
        // then
        assertEquals(3,result.getContent().size());
        assertEquals(LocalDate.of(2026,10,31),result.getContent().getFirst().transactionDate());
        assertEquals(LocalDate.of(2026,10,15),result.getContent().get(1).transactionDate());
        assertEquals(LocalDate.of(2026,10,1),result.getContent().getLast().transactionDate());
        verify(transactionRepository).findAll(pageable);
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
    @Test
    void shouldReturnPageOfTransactionResponsesFilteredByType() {
        // given
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
        PageImpl<Transaction> testTransactionPage = new PageImpl<>(List.of(testTransaction1,testTransaction2),pageable,5);

        when(transactionRepository.findByType(TransactionType.EXPENSE, pageable)).thenReturn(testTransactionPage);
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(TransactionType.EXPENSE, null, pageable);
        // then
        assertEquals(2,result.getContent().size());
        assertEquals(5,result.getTotalElements());
        assertEquals(3,result.getTotalPages());
        assertEquals(0,result.getNumber());
        assertEquals("TEST EXPENSE 1", result.getContent().getFirst().name());
        assertEquals("TEST EXPENSE 2", result.getContent().getLast().name());
        verify(transactionRepository).findByType(TransactionType.EXPENSE, pageable);
    }
    @Test
    void shouldReturnEmptyPageWhenNoTransactionsMatchType() {
        // given
        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id"))
        );
        when(transactionRepository.findByType(TransactionType.EXPENSE, pageable)).thenReturn(Page.empty(pageable));
        // when
        Page<TransactionResponse> result = transactionService.getTransactions(TransactionType.EXPENSE, null, pageable);
        // then
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        verify(transactionRepository).findByType(TransactionType.EXPENSE, pageable);
    }
}
