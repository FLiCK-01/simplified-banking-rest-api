package ro.axonsoft.eval.minibank.dto;

import lombok.Data;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class AccountResponse {
    private Long id;
    private String ownerName;
    private String iban;
    private Currency currency;
    private AccountType accountType;
    private BigDecimal balance;
    private Instant createdAt;
}
