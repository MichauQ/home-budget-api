package io.github.michauq.homebudget.transaction;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @GetMapping
    public List<TransactionResponse> getTransactions(){
        TransactionResponse transactionResponse = new TransactionResponse(1L,"Jedzenie na miescie", new BigDecimal("25.50"));
        return List.of(transactionResponse);
    }
}
