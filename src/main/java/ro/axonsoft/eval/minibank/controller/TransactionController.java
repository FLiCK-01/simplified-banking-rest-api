package ro.axonsoft.eval.minibank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dto.PaginatedResponse;
import ro.axonsoft.eval.minibank.dto.TransactionResponse;
import ro.axonsoft.eval.minibank.mapper.TransactionMapper;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @GetMapping("/{accountId}/transactions")
    public PaginatedResponse<TransactionResponse> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("Account not found");
        }

        Page<TransactionResponse> transactions = transactionRepository
                .findByAccountIdOrderByTimestampAsc(accountId, PageRequest.of(page, size))
                .map(transactionMapper::toResponse);

        return new PaginatedResponse<>(transactions);
    }
}