package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionType;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionSummaryResponse getBalanceSummary(){
        BigDecimal income = Optional.ofNullable(transactionRepository.sumAmountByType(TransactionType.INCOME)).orElse(BigDecimal.ZERO);
        BigDecimal expenses = Optional.ofNullable(transactionRepository.sumAmountByType(TransactionType.EXPENSE)).orElse(BigDecimal.ZERO);
        return new TransactionSummaryResponse(income,expenses, income.subtract(expenses));
    }
    public TransactionSummaryResponse getBalanceSummary(LocalDate from, LocalDate to) throws IllegalArgumentException{
        BigDecimal income = Optional.ofNullable(transactionRepository.sumAmountByTypeAndDateRange(TransactionType.INCOME, from, to)).orElse(BigDecimal.ZERO);
        BigDecimal expenses = Optional.ofNullable(transactionRepository.sumAmountByTypeAndDateRange(TransactionType.EXPENSE, from, to)).orElse(BigDecimal.ZERO);
        return new TransactionSummaryResponse(income,expenses, income.subtract(expenses));
    }
    //GET ALL + FILTER BY DATE RANGE
    public List<TransactionResponse> getTransactions(LocalDate from, LocalDate to) throws IllegalArgumentException{
        List<Transaction> transactions;
        if(from != null && to != null && from.isAfter(to)){
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        if(from == null && to == null){
            transactions = transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        }
        else if(to == null){
            transactions = transactionRepository.findByTransactionDateGreaterThanEqual(from,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        }
        else if(from == null){
            transactions = transactionRepository.findByTransactionDateLessThanEqual(to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        }
        else{
            transactions = transactionRepository.findByTransactionDateBetween(from, to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        }
        return transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getName(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getCategory(),
                        transaction.getTransactionDate(),
                        transaction.getTransactionTime()))
                .toList();
    }
    //CRUD implementation
    //GET ALL + FILTER BY DATE
    public List<TransactionResponse> getTransactions(LocalDate date){
        List<Transaction> transactions;
        if(date == null){
             transactions = transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        }
        else{
            transactions = transactionRepository.findByTransactionDate(date,Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        }
        return transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getName(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getCategory(),
                        transaction.getTransactionDate(),
                        transaction.getTransactionTime()))
                .toList();
    }
    //GET BY ID
    public Optional<TransactionResponse> getTransaction(Long id){
        Optional<Transaction> foundTransaction = transactionRepository.findById(id);
        return foundTransaction.map(transaction -> new TransactionResponse(
                transaction.getId(),
                transaction.getName(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getTransactionDate(),
                transaction.getTransactionTime()
        ));
    }
    // (PUT) UPDATE
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

    //(POST) CREATE
    public TransactionResponse createTransaction(CreateTransactionRequest  request){
        Transaction transaction = new Transaction(
                request.name(),
                request.amount(),
                request.type(),
                request.category(),
                request.transactionDate(),
                request.transactionTime());
        Transaction savedTransaction = transactionRepository.save(transaction);
    return new TransactionResponse(
            savedTransaction.getId(),
            savedTransaction.getName(),
            savedTransaction.getAmount(),
            savedTransaction.getType(),
            savedTransaction.getCategory(),
            savedTransaction.getTransactionDate(),
            savedTransaction.getTransactionTime()
    );
    }
    //DELETE
    public boolean deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            return false;
        }

        transactionRepository.deleteById(id);
        return true;
    }
}
