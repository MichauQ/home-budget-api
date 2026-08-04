package io.github.michauq.homebudget.transaction;

import java.math.BigDecimal;

public record TransactionResponse(
        Long id,
        String name,
        BigDecimal amount
) {
}
