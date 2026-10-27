package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.dto.CreateTransactionRequest;
import io.github.michauq.homebudget.transaction.dto.TransactionResponse;
import io.github.michauq.homebudget.transaction.dto.TransactionSummaryResponse;
import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Transaction with id 999 not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/transactions/999"))
                .andExpect(jsonPath("$.timestamp").exists());
        verify(transactionService).getTransaction(999L);
    }

    @Test
    void shouldReturnTransactions() throws Exception{
        //given
        Pageable pageable = PageRequest.of(0,2, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(response,pageable,5);
        when(transactionService.getTransactions(null,null,null, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("page","0")
                        .param("size", "2"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Controller transaction test data1"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value("Controller transaction test data2"))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(0));
        verify(transactionService).getTransactions(null,null, null, pageable);
    }
    @Test
    void shouldReturnTransactionsFilteredByDate() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,2, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(response,pageable,5);
        when(transactionService.getTransactions(null, dateParam, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions?date=2026-10-05")
                        .param("page","0")
                        .param("size", "2"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Controller transaction test data1"))
                .andExpect(jsonPath("$.content[0].transactionDate").value(dateParam.toString()));
        verify(transactionService).getTransactions(null, dateParam, pageable);
    }
    @Test
    void shouldReturnBadRequestWhenDateParameterIsInvalid() throws Exception {
        //when
        mockMvc.perform(get("/api/transactions?date=abc"))
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter: date"))
                .andExpect(jsonPath("$.path").value("/api/transactions"))
                .andExpect(jsonPath("$.timestamp").exists());
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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Transaction with id 999 not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/transactions/999"))
                .andExpect(jsonPath("$.timestamp").exists());
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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Transaction with id 999 not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/transactions/999"))
                .andExpect(jsonPath("$.timestamp").exists());
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
        Pageable pageable = PageRequest.of(0,2, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        LocalDate from = LocalDate.of(2026,10,31);
        LocalDate to = LocalDate.of(2026,10,15);
        when(transactionService.getTransactions(null, from, to, pageable)).thenThrow(new IllegalArgumentException("Invalid date range"));
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-31")
                        .param("to", "2026-10-15")
                        .param("page","0")
                        .param("size", "2"))
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid date range"))
                .andExpect(jsonPath("$.path").value("/api/transactions"))
                .andExpect(jsonPath("$.timestamp").exists());
        verify(transactionService).getTransactions(null, from, to, pageable);
    }
    @Test
    void shouldReturnTransactionsWithinDateRange() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,2, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(testTransactions,pageable,5);
        when(transactionService.getTransactions(null, from, to, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("page","0")
                        .param("size", "2"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$.content[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$.content[1].transactionDate").value(LocalDate.of(2026, 10, 17).toString()));
        verify(transactionService).getTransactions(null, from, to, pageable);
    }
    @Test
    void shouldReturnTransactionsFromGivenDate() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,2, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(testTransactions, pageable,5);
        when(transactionService.getTransactions(null, from, null, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-01")
                        .param("page","0")
                        .param("size", "2"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$.content[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$.content[1].transactionDate").value(LocalDate.of(2026, 10, 17).toString()));
        verify(transactionService).getTransactions(null, from,null, pageable);
    }
    @Test
    void shouldReturnTransactionsToGivenDate() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,2, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(testTransactions, pageable,5);
        when(transactionService.getTransactions(null,null, to, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("to", "2026-10-31")
                        .param("page","0")
                        .param("size", "2"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$.content[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$.content[1].transactionDate").value(LocalDate.of(2026, 10, 17).toString()));
        verify(transactionService).getTransactions(null, null, to, pageable);
    }
    @Test
    void shouldReturnTransactionsWhenDateRangeIsSingleDay() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,2, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(testTransactions, pageable,5);
        when(transactionService.getTransactions(null, sameDate, sameDate, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("from", "2026-10-02")
                        .param("to", "2026-10-02")
                        .param("page","0")
                        .param("size", "2"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Transaction within range 1"))
                .andExpect(jsonPath("$.content[0].transactionDate").value(LocalDate.of(2026, 10, 2).toString()))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].name").value("Transaction within range 2"))
                .andExpect(jsonPath("$.content[1].transactionDate").value(LocalDate.of(2026, 10, 2).toString()));
        verify(transactionService).getTransactions(null, sameDate, sameDate, pageable);
    }
    @Test
    void shouldReturnTransactionsSortedByDateDescendingWhenNoFiltersProvided() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0,3, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(testTransactions, pageable,5);
        when(transactionService.getTransactions(null, null,null, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("page","0")
                        .param("size", "3"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].transactionDate").value(LocalDate.of(2026, 10, 31).toString()))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].transactionDate").value(LocalDate.of(2026, 10, 15).toString()))
                .andExpect(jsonPath("$.content[2].id").value(3L))
                .andExpect(jsonPath("$.content[2].transactionDate").value(LocalDate.of(2026, 10, 1).toString()));
        verify(transactionService).getTransactions(null,null, null, pageable);
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
    @Test
    void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("page","-1")
                        .param("size", "3"))
                //then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid pagination parameters"))
                .andExpect(jsonPath("$.path")
                        .value("/api/transactions"))
                .andExpect(jsonPath("$.timestamp").exists());
        verifyNoInteractions(transactionService);

    }
    @Test
    void shouldReturnBadRequestWhenSizeIsZero() throws Exception {
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("page","0")
                        .param("size", "0"))
                //then
                .andExpect(status().isBadRequest());
        verifyNoInteractions(transactionService);

    }
    @Test
    void shouldReturnEmptyPageWhenPageIsOutOfRange() throws Exception {
        //given
        Pageable pageable = PageRequest.of(99999,3, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        when(transactionService.getTransactions(null,null,null, pageable)).thenReturn(Page.empty(pageable));
        //when
        mockMvc.perform(get("/api/transactions")
                        .param("page","99999")
                        .param("size", "3"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
        verify(transactionService).getTransactions(null,null, null, pageable);
    }
    @Test
    void shouldUseDefaultPaginationWhenParametersAreMissing() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,10, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
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
        Page<TransactionResponse> transactionPage = new PageImpl<>(testTransactions, pageable,3);
        when(transactionService.getTransactions(null, null, null, pageable)).thenReturn(transactionPage);
        //when
        mockMvc.perform(get("/api/transactions"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content.length()").value(3));
        verify(transactionService).getTransactions(null,null, null, pageable);
    }
}
