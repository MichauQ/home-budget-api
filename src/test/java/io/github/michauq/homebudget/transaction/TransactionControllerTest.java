package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
        when(transactionService.getTransactions()).thenReturn(response);
        //when
        mockMvc.perform(get("/api/transactions"))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Controller transaction test data1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Controller transaction test data2"));
        verify(transactionService).getTransactions();
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
}
