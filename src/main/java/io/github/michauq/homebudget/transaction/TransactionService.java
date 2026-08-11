package io.github.michauq.homebudget.transaction;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransactionService {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final List<TransactionResponse> transactionResponseList = new ArrayList<>();

    public List<TransactionResponse> getTransactions(){
        return List.copyOf(transactionResponseList);
    }

    public Optional<TransactionResponse> getTransaction(Long id){
        return transactionResponseList.stream()
                .filter(transactionResponse -> Objects.equals(transactionResponse.id(), id))
                .findFirst();
    }

    public Optional<TransactionResponse> updateTransaction(Long id, CreateTransactionRequest request){
        for (int i = 0; i < transactionResponseList.size(); i++) {
            TransactionResponse transaction = transactionResponseList.get(i);
            if(Objects.equals(transaction.id(), id)){
                TransactionResponse updatedTransaction = new TransactionResponse(
                        id,
                        request.name(),
                        request.amount(),
                        request.type(),
                        request.category(),
                        request.transactionDate(),
                        request.transactionTime()
                );
                transactionResponseList.set(i, updatedTransaction);
                return Optional.of(updatedTransaction);
            }
        }
        return Optional.empty();
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

    public boolean deleteTransaction(Long id){
        return transactionResponseList.removeIf(transactionResponse -> Objects.equals(transactionResponse.id(), id));
    }
}
