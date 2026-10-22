package io.github.michauq.homebudget.transaction.dto;

import java.math.BigDecimal;

public record TransactionSummaryResponse(
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
) {
}
