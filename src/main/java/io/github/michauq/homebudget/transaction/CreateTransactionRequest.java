package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateTransactionRequest(
        @NotBlank(message = "name must not be blank")
        String name,
        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,
        @NotNull(message = "type is required")
        TransactionType type,
        TransactionCategory category,
        @NotNull(message = "date is required")
        LocalDate transactionDate,
        LocalTime transactionTime
) {

}
