package io.github.michauq.homebudget.transaction;

import java.math.BigDecimal;

public record TransactionSummaryResponse(
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal balance
) {
}
