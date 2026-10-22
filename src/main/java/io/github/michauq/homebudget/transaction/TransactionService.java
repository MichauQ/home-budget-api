package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.dto.CategoryExpenseSummaryResponse;
import io.github.michauq.homebudget.transaction.dto.CreateTransactionRequest;
import io.github.michauq.homebudget.transaction.dto.TransactionResponse;
import io.github.michauq.homebudget.transaction.dto.TransactionSummaryResponse;
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
    public TransactionSummaryResponse getBalanceSummary(LocalDate from, LocalDate to){
        BigDecimal income;
        BigDecimal expenses;
        if(to == null){
            income = Optional.ofNullable(transactionRepository.sumAmountByTypeAndFromDate(TransactionType.INCOME, from)).orElse(BigDecimal.ZERO);
            expenses = Optional.ofNullable(transactionRepository.sumAmountByTypeAndFromDate(TransactionType.EXPENSE, from)).orElse(BigDecimal.ZERO);
        }
        else if(from == null){
            income = Optional.ofNullable(transactionRepository.sumAmountByTypeAndToDate(TransactionType.INCOME,to)).orElse(BigDecimal.ZERO);
            expenses = Optional.ofNullable(transactionRepository.sumAmountByTypeAndToDate(TransactionType.EXPENSE,to)).orElse(BigDecimal.ZERO);
        }
        else{
            income = Optional.ofNullable(transactionRepository.sumAmountByTypeAndDateRange(TransactionType.INCOME, from, to)).orElse(BigDecimal.ZERO);
            expenses = Optional.ofNullable(transactionRepository.sumAmountByTypeAndDateRange(TransactionType.EXPENSE, from, to)).orElse(BigDecimal.ZERO);
        }
        return new TransactionSummaryResponse(income,expenses, income.subtract(expenses));
    }
    public List<CategoryExpenseSummaryResponse> getExpensesByCategory() {
        return transactionRepository.sumExpenseByCategory(TransactionType.EXPENSE);
    }

    //GET ALL + FILTER BY DATE RANGE
    public List<TransactionResponse> getTransactions(TransactionType type, LocalDate from, LocalDate to){
        List<Transaction> transactions;
        if(from != null && to != null && from.isAfter(to)){
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        if(type == null) {
            if (from == null && to == null) {
                transactions = transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else if (to == null) {
                transactions = transactionRepository.findByTransactionDateGreaterThanEqual(from, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else if (from == null) {
                transactions = transactionRepository.findByTransactionDateLessThanEqual(to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else {
                transactions = transactionRepository.findByTransactionDateBetween(from, to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            }
        }
        else {
            if (from == null && to == null) {
                transactions = transactionRepository.findByType(type, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else if (to == null) {
                transactions = transactionRepository.findByTypeAndTransactionDateGreaterThanEqual(type, from, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else if (from == null) {
                transactions = transactionRepository.findByTypeAndTransactionDateLessThanEqual(type, to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else {
                transactions = transactionRepository.findByTypeAndTransactionDateBetween(type, from, to, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            }
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
    public List<TransactionResponse> getTransactions(TransactionType type, LocalDate date){
        List<Transaction> transactions;
        if (type == null) {
            if (date == null) {
                transactions = transactionRepository.findAll(Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else {
                transactions = transactionRepository.findByTransactionDate(date, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            }
        }
        else {
            if (date == null) {
                transactions = transactionRepository.findByType(type, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            } else {
                transactions = transactionRepository.findByTypeAndTransactionDate(type, date, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
            }
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
