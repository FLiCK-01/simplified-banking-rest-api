package ro.axonsoft.eval.minibank.dto;

import lombok.Data;
import ro.axonsoft.eval.minibank.model.Currency;
import ro.axonsoft.eval.minibank.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionResponse {
    private Long id;
    private Instant timestamp;
    private TransactionType type;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal balanceAfter;
    private String counterpartyIban;
    private Long transferId;
}
