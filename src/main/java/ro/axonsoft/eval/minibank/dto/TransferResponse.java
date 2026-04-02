package ro.axonsoft.eval.minibank.dto;

import lombok.Data;
import ro.axonsoft.eval.minibank.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransferResponse {
    private Long id;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private Currency currency;
    private Currency targetCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String idempotencyKey;
    private Instant createdAt;
}
