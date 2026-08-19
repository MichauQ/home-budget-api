package io.github.michauq.homebudget.transaction;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> getTransactions(){
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.id,
                        transaction.name,
                        transaction.amount,
                        transaction.type,
                        transaction.category,
                        transaction.transactionDate,
                        transaction.transactionTime))
                .toList();
    }

    public Optional<TransactionResponse> getTransaction(Long id){
        Optional<Transaction> foundTransaction = transactionRepository.findById(id);
        return foundTransaction.map(transaction -> new TransactionResponse(
                transaction.id,
                transaction.name,
                transaction.amount,
                transaction.type,
                transaction.category,
                transaction.transactionDate,
                transaction.transactionTime
        ));
    }

    public Optional<TransactionResponse> updateTransaction(Long id, CreateTransactionRequest request){
        return transactionRepository.findById(id)
                .map(transaction ->{
                        transaction.setName(request.name());
                        transaction.setAmount(request.amount());
                        transaction.setType(request.type());
                        transaction.setCategory(request.category());
                        transaction.setTransactionDate(request.transactionDate());
                        transaction.setTransactionTime(request.transactionTime());
                        Transaction savedTransaction = transactionRepository.save(transaction);
                        return new TransactionResponse(
                                savedTransaction.getId(),
                                savedTransaction.getName(),
                                savedTransaction.getAmount(),
                                savedTransaction.getType(),
                                savedTransaction.getCategory(),
                                savedTransaction.getTransactionDate(),
                                savedTransaction.getTransactionTime());
                });
    }


    public TransactionResponse createTransaction(CreateTransactionRequest  request){;
        Transaction transaction = new Transaction(
                request.name(),
                request.amount(),
                request.type(),
                request.category(),
                request.transactionDate(),
                request.transactionTime());
        Transaction savedTransaction = transactionRepository.save(transaction);
    return new TransactionResponse(
            savedTransaction.id,
            savedTransaction.name,
            savedTransaction.amount,
            savedTransaction.type,
            savedTransaction.category,
            savedTransaction.transactionDate,
            savedTransaction.transactionTime
    );
    }

    public boolean deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            return false;
        }

        transactionRepository.deleteById(id);
        return true;
    }
}
