package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(transactionRepository.findAll()).thenReturn(testFindAllData);
        // when
        List<TransactionResponse> result = transactionService.getTransactions();
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

        verify(transactionRepository).findAll();
    }
    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist(){
        // given
        when(transactionRepository.findAll()).thenReturn(new ArrayList<>());
        // when
        List<TransactionResponse> result = transactionService.getTransactions();
        // then
        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll();
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
}
