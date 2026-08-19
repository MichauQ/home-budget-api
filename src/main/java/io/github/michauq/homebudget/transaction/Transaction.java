package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.enums.TransactionCategory;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    BigDecimal amount;
    @Enumerated(EnumType.STRING)
    TransactionType type;
    @Enumerated(EnumType.STRING)
    TransactionCategory category;
    LocalDate transactionDate;
    LocalTime transactionTime;

    public Transaction(){}

    public Transaction(String name, BigDecimal amount, TransactionType type, TransactionCategory category, LocalDate transactionDate, LocalTime transactionTime){
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public LocalTime getTransactionTime() {
        return transactionTime;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTransactionTime(LocalTime transactionTime) {
        this.transactionTime = transactionTime;
    }
}
