package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.dto.CreateTransactionRequest;
import io.github.michauq.homebudget.transaction.dto.TransactionResponse;
import io.github.michauq.homebudget.transaction.dto.TransactionSummaryResponse;
import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldReturnTransactionSummary() throws Exception {
        //given
        TransactionSummaryResponse summaryResponse = new TransactionSummaryResponse(
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(120)
        );
        when(transactionService.getBalanceSummary()).thenReturn(summaryResponse);
        //when
        mockMvc.perform(get("/api/transactions/summary"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(BigDecimal.valueOf(150)))
                .andExpect(jsonPath("$.expenses").value(BigDecimal.valueOf(30)))
                .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(120)));
        verify(transactionService).getBalanceSummary();
    }

    @Test
    void shouldReturnTransactionWhenExists() throws Exception {
        //given
        TransactionResponse response = new TransactionResponse(
                1L,
                "Controller transaction test data",
                BigDecimal.valueOf(25),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,8,17),
                null
        );
        when(transactionService.getTransaction(1L)).thenReturn(Optional.of(response));
        //when
        mockMvc.perform(get("/api/transactions/1"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Controller transaction test data"));
        verify(transactionService).getTransaction(1L);
    }

    @Test
    void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception{
        //given
        when(transactionService.getTransaction(999L)).thenReturn(Optional.empty());
        //when
        mockMvc.perform(get("/api/transactions/999"))
                //then
                .andExpect(status().isNotFound());
        verify(transactionService).getTransaction(999L);
    }

    @Test
    void shouldReturnTransactions() throws Exception{
        //given
        List<TransactionResponse> response = List.of(
                new TransactionResponse(
                1L,
                "Controller transaction test data1",
                BigDecimal.valueOf(100),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,8,17),
                null
        ),
                new TransactionResponse(
                        2L,
                        "Controller transaction test data2",
                        BigDecimal.valueOf(30),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026,9,21),
                        null
                )
        );
        when(transactionService.getTransactions(null,null, null)).thenReturn(response);
        //when
        mockMvc.perform(get("/api/transactions"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Controller transaction test data1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Controller transaction test data2"));
        verify(transactionService).getTransactions(null,null, null);
    }
    @Test
    void shouldReturnTransactionsFilteredByDate() throws Exception {
        //given
        LocalDate dateParam = LocalDate.of(2026,10,5);
        List<TransactionResponse> response = List.of(new TransactionResponse(
                        1L,
                        "Controller transaction test data1",
                        BigDecimal.valueOf(100),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026,10,5),
                        null
                ));
        when(transactionService.getTransactions(null, dateParam)).thenReturn(response);
        //when
        mockMvc.perform(get("/api/transactions?date=2026-10-05"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Controller transaction test data1"))
                .andExpect(jsonPath("$[0].transactionDate").value(dateParam.toString()));
        verify(transactionService).getTransactions(null, dateParam);
    }
    @Test
    void shouldReturnBadRequestWhenDateParameterIsInvalid() throws Exception {
        //when
        mockMvc.perform(get("/api/transactions?date=abc"))
                //then
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldCreateTransaction() throws Exception{
        //given
        TransactionResponse response = new TransactionResponse(
                1L,
                "Controller transaction test data",
                BigDecimal.valueOf(25),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026,8,17),
                null
        );
        when(transactionService.createTransaction(any(CreateTransactionRequest.class))).thenReturn(response);
        //when
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "name": "Controller transaction test data",
                         "amount": 25,
                         "type": "EXPENSE",
                         "category": "OTHER",
                         "transactionDate": "2026-08-17",
                         "transactionTime": null
                        }
                        """))
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Controller transaction test data"));
        verify(transactionService).createTransaction(any(CreateTransactionRequest.class));
    }
    @Test
    void shouldReturnBadRequestWhenTransactionDateIsMissing() throws Exception{
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "name": "Controller transaction test data",
                         "amount": 25,
                         "type": "EXPENSE",
                         "category": "OTHER",
                         "transactionTime": null
                        }
                        """))
                .andExpect(status().isBadRequest());
        verify(transactionService,never()).createTransaction(any(CreateTransactionRequest.class));
    }

    @Test
    void shouldUpdateTransactionWhenExists() throws Exception{
        //given
        TransactionResponse updatedTransaction = new TransactionResponse(
                1L,
                "Updated controller transaction test data",
                BigDecimal.valueOf(25),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 8, 17),
                null
        );
        when(transactionService.updateTransaction(eq(1L),any(CreateTransactionRequest.class))).thenReturn(Optional.of(updatedTransaction));
        //when
        mockMvc.perform(put("/api/transactions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                         "name": "Updated controller transaction test data",
                         "amount": 25,
                         "type": "EXPENSE",
                         "category": "OTHER",
                         "transactionDate": "2026-08-17",
                         "transactionTime": null
                        }
                        """))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated controller transaction test data"));
        verify(transactionService).updateTransaction(eq(1L),any(CreateTransactionRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingTransaction() throws Exception{
        //given
        when(transactionService.updateTransaction(eq(999L),any(CreateTransactionRequest.class))).thenReturn(Optional.empty());
        //when
        mockMvc.perform(put("/api/transactions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                         "name": "Updated controller transaction test data",
                         "amount": 25,
                         "type": "EXPENSE",
                         "category": "OTHER",
                         "transactionDate": "2026-08-17",
                         "transactionTime": null
                        }
                        """))
                //then
                .andExpect(status().isNotFound());
        verify(transactionService).updateTransaction(eq(999L),any(CreateTransactionRequest.class));
    }

    @Test
    void shouldDeleteTransactionWhenExists() throws Exception{
        //given
        when(transactionService.deleteTransaction(1L)).thenReturn(true);
        //when
        mockMvc.perform(delete("/api/transactions/1"))
                //then
                .andExpect(status().isNoContent());
        verify(transactionService).deleteTransaction(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingTransaction() throws Exception{
        //given
        when(transactionService.deleteTransaction(999L)).thenReturn(false);
        //when
        mockMvc.perform(delete("/api/transactions/999"))
                //then
                .andExpect(status().isNotFound());
        verify(transactionService).deleteTransaction(999L);
    }
    @Test
    void shouldReturnBadRequestWhenDateAndFromAreProvided() throws Exception {
        //when
        mockMvc.perform(get("/api/transactions")
                .param("date", "2026-10-15")
                .param("from", "2026-10-01"))
                //then
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }
    @Test
    void shouldReturnBadRequestWhenDateAndToAreProvided() throws Exception {
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("date", "2026-10-15")
                        .param("to", "2026-10-18"))
                //then
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }
    @Test
    void shouldReturnBadRequestWhenFromIsAfterTo() throws Exception {
        //given
        LocalDate from = LocalDate.of(2026,10,31);
        LocalDate to = LocalDate.of(2026,10,15);
        when(transactionService.getTransactions(null, from, to)).thenThrow(IllegalArgumentException.class);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-31")
                        .param("to", "2026-10-15"))
                //then
                .andExpect(status().isBadRequest());
        verify(transactionService).getTransactions(null, from, to);
    }
    @Test
    void shouldReturnTransactionsWithinDateRange() throws Exception {
        //given
        LocalDate from = LocalDate.of(2026,10,1);
        LocalDate to = LocalDate.of(2026,10,31);
        List<TransactionResponse> testTransactions = List.of(
                new TransactionResponse(
                1L,
                "Transaction within range 1",
                BigDecimal.valueOf(25),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 2),
                null),
                new TransactionResponse(
                2L,
                "Transaction within range 2",
                BigDecimal.valueOf(25),
                TransactionType.EXPENSE,
                TransactionCategory.OTHER,
                LocalDate.of(2026, 10, 17),
                null));
        when(transactionService.getTransactions(null, from, to)).thenReturn(testTransactions);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$[1].transactionDate").value(LocalDate.of(2026, 10, 17).toString()));
        verify(transactionService).getTransactions(null, from, to);
    }
    @Test
    void shouldReturnTransactionsFromGivenDate() throws Exception {
        //given
        LocalDate from = LocalDate.of(2026,10,1);
        List<TransactionResponse> testTransactions = List.of(
                new TransactionResponse(
                        1L,
                        "Transaction within range 1",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 2),
                        null),
                new TransactionResponse(
                        2L,
                        "Transaction within range 2",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 17),
                        null));
        when(transactionService.getTransactions(null, from, null)).thenReturn(testTransactions);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-01"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$[1].transactionDate").value(LocalDate.of(2026, 10, 17).toString()));
        verify(transactionService).getTransactions(null, from,null);
    }
    @Test
    void shouldReturnTransactionsToGivenDate() throws Exception {
        //given
        LocalDate to = LocalDate.of(2026,10,31);
        List<TransactionResponse> testTransactions = List.of(
                new TransactionResponse(
                        1L,
                        "Transaction within range 1",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 2),
                        null),
                new TransactionResponse(
                        2L,
                        "Transaction within range 2",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 17),
                        null));
        when(transactionService.getTransactions(null,null, to)).thenReturn(testTransactions);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("to", "2026-10-31"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$[1].transactionDate").value(LocalDate.of(2026, 10, 17).toString()));
        verify(transactionService).getTransactions(null, null, to);
    }
    @Test
    void shouldReturnTransactionsWhenDateRangeIsSingleDay() throws Exception {
        //given
        LocalDate sameDate = LocalDate.of(2026,10,2);
        List<TransactionResponse> testTransactions = List.of(
                new TransactionResponse(
                        1L,
                        "Transaction within range 1",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 2),
                        null),
                new TransactionResponse(
                        2L,
                        "Transaction within range 2",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 2),
                        null));
        when(transactionService.getTransactions(null, sameDate, sameDate)).thenReturn(testTransactions);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-02")
                        .param("to", "2026-10-02"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$[1].transactionDate").value(LocalDate.of(2026, 10, 2).toString()));
        verify(transactionService).getTransactions(null, sameDate,sameDate);
    }
    @Test
    void shouldReturnTransactionsSortedByDateDescendingWhenNoFiltersProvided() throws Exception {
        // given
        List<TransactionResponse> testTransactions = List.of(
                new TransactionResponse(
                        1L,
                        "Transaction sorted 1",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 31),
                        null),
                new TransactionResponse(
                        2L,
                        "Transaction sorted 2",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 15),
                        null),
                new TransactionResponse(
                        3L,
                        "Transaction sorted 3",
                        BigDecimal.valueOf(25),
                        TransactionType.EXPENSE,
                        TransactionCategory.OTHER,
                        LocalDate.of(2026, 10, 1),
                        null));
        when(transactionService.getTransactions(null, null, null)).thenReturn(testTransactions);
        //when
        mockMvc.perform(get("/api/transactions"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].transactionDate").value(LocalDate.of(2026, 10, 31).toString()))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].transactionDate").value(LocalDate.of(2026, 10, 15).toString()))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].transactionDate").value(LocalDate.of(2026, 10, 1).toString()));
        verify(transactionService).getTransactions(null,null, null);
    }
    @Test
    void shouldReturnTransactionSummaryForDateRange() throws Exception {
        //given
        LocalDate from = LocalDate.of(2026,10,1);
        LocalDate to = LocalDate.of(2026,10,31);
        TransactionSummaryResponse summaryResponse = new TransactionSummaryResponse(
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(120)
        );
        when(transactionService.getBalanceSummary(from, to)).thenReturn(summaryResponse);
        //when
        mockMvc.perform(get("/api/transactions/summary?from=2026-10-01&to=2026-10-31"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(150))
                .andExpect(jsonPath("$.expenses").value(30))
                .andExpect(jsonPath("$.balance").value(120));
        verify(transactionService).getBalanceSummary(from,to);
    }

    @Test
    void shouldReturnBadRequestWhenSummaryDateRangeIsReversed() throws Exception{
        //given
        LocalDate from = LocalDate.of(2026,10,31);
        LocalDate to = LocalDate.of(2026,10,1);
        //when
        mockMvc.perform(get("/api/transactions/summary?from=2026-10-31&to=2026-10-01"))
                //then
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }
    @Test
    void shouldReturnTransactionSummaryWithOnlyFromDate() throws Exception {
        //given
        LocalDate from = LocalDate.of(2026,10,1);
        TransactionSummaryResponse summaryResponse = new TransactionSummaryResponse(
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(120)
        );
        when(transactionService.getBalanceSummary(from, null)).thenReturn(summaryResponse);
        //when
        mockMvc.perform(get("/api/transactions/summary?from=2026-10-01"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(150))
                .andExpect(jsonPath("$.expenses").value(30))
                .andExpect(jsonPath("$.balance").value(120));
        verify(transactionService).getBalanceSummary(from,null);
    }

    @Test
    void shouldReturnBadRequestForInvalidTransactionType() throws Exception{
        //when
        mockMvc.perform(get("/api/transactions?type=abcd"))
                //then
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);
    }
}
