package io.github.michauq.homebudget.transaction;

import io.github.michauq.homebudget.transaction.dto.CategoryExpenseSummaryResponse;
import io.github.michauq.homebudget.transaction.dto.CreateTransactionRequest;
import io.github.michauq.homebudget.transaction.dto.TransactionResponse;
import io.github.michauq.homebudget.transaction.dto.TransactionSummaryResponse;
import io.github.michauq.homebudget.transaction.enums.TransactionType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/summary/expenses-by-category")
    public ResponseEntity<List<CategoryExpenseSummaryResponse>> getExpensesByCategory(){
        return ResponseEntity.ok(transactionService.getExpensesByCategory());
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getBalanceSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        if (from == null && to == null) {
            return ResponseEntity.ok(transactionService.getBalanceSummary());
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date range"
            );
        }

        return ResponseEntity.ok(transactionService.getBalanceSummary(from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id){
        return transactionService.getTransaction(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping
    public List<TransactionResponse> getTransactions(
            @RequestParam(required = false)
            TransactionType type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to){
        if (date != null && (from != null || to != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid query parameters"
            );
        } else if (date != null) {
            return transactionService.getTransactions(type, date);
        }
        return transactionService.getTransactions(type, from, to);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request){
        TransactionResponse response =  transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @Valid @RequestBody CreateTransactionRequest request){
        return transactionService.updateTransaction(id,request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        boolean deleted = transactionService.deleteTransaction(id);
        if (deleted){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArgumentException() {
    }
}
