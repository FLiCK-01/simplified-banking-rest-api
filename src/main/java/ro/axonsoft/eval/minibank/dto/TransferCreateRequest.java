package ro.axonsoft.eval.minibank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferCreateRequest {
    @NotBlank(message = "sourceIban is required")
    private String sourceIban;

    @NotBlank(message = "targetIban is required")
    private String targetIban;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    private String idempotencyKey;
}
