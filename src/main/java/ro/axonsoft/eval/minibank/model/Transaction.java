package ro.axonsoft.eval.minibank.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The account this ledger entry belongs to (so we can query it later)
    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    // Nullable, because if the counterparty is the System Bank (ID 1), it must be null
    @Column(name = "counterparty_iban")
    private String counterpartyIban;

    // Links back to the Transfer that caused this ledger entry
    @Column(nullable = false)
    private Long transferId;
}
