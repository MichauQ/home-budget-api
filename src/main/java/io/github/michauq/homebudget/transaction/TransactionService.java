package io.github.michauq.homebudget.transaction;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransactionService {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final List<TransactionResponse> transactionResponseList = new ArrayList<>();
    public List<TransactionResponse> getTransactions(){
        return List.copyOf(transactionResponseList);
    }

    public TransactionResponse createTransaction(CreateTransactionRequest  request){
        Long id = generateId();
        TransactionResponse transactionResponse = new TransactionResponse(
                id,
                request.name(),
                request.amount(),
                request.type(),
                request.category(),
                request.transactionDate(),
                request.transactionTime());
        transactionResponseList.add(transactionResponse);
    return transactionResponse;
    }

    // helper method to temporarily generate incremental ID
    private Long generateId(){
        return idGenerator.incrementAndGet();
    }
}
