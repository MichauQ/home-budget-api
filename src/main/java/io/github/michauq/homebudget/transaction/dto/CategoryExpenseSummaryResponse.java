package io.github.michauq.homebudget.transaction.dto;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;

import java.math.BigDecimal;

public record CategoryExpenseSummaryResponse(TransactionCategory category, BigDecimal total) {
}
