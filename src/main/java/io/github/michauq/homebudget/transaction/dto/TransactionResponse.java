package io.github.michauq.homebudget.transaction.dto;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TransactionResponse(
        Long id,
        String name,
        BigDecimal amount,
        TransactionType type,
        TransactionCategory category,
        LocalDate transactionDate,
        LocalTime transactionTime
) {
}
