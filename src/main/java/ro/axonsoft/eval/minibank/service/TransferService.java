package ro.axonsoft.eval.minibank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axonsoft.eval.minibank.config.ExchangeRatesConfig;
import ro.axonsoft.eval.minibank.dto.TransferCreateRequest;
import ro.axonsoft.eval.minibank.dto.TransferResponse;
import ro.axonsoft.eval.minibank.exception.BusinessValidationException;
import ro.axonsoft.eval.minibank.mapper.TransferMapper;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.model.TransactionType;
import ro.axonsoft.eval.minibank.model.Transfer;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.repository.TransferRepository;
import ro.axonsoft.eval.minibank.util.IbanValidatorUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferMapper transferMapper;
    private final ExchangeRatesConfig exchangeRatesConfig;

    @Transactional
    public TransferResponse createTransfer(TransferCreateRequest request) {
        if(request.getIdempotencyKey() != null) {
            Optional<Transfer> existing = transferRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if(existing.isPresent()) {
                return transferMapper.toResponse(existing.get());
            }
        }

        if(!IbanValidatorUtil.isSepaCountry(request.getSourceIban()) ||
        !IbanValidatorUtil.isSepaCountry(request.getTargetIban())) {
            throw new BusinessValidationException("Both source and target must be SEPA countries");
        }

        Account source = accountRepository.findByIbanForUpdate(request.getSourceIban())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account target = accountRepository.findByIbanForUpdate(request.getTargetIban())
                .orElseThrow(() -> new IllegalArgumentException("Target account not found"));

        if(source.getId() != 1L && source.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessValidationException("Insufficient funds");
        }

        if(source.getAccountType() == AccountType.SAVINGS) {
            checkSavingsDailyLimit(source, request.getAmount());
        }

        BigDecimal amount = request.getAmount();
        BigDecimal convertedAmount = amount;
        BigDecimal exchangeRate = null;

        if(source.getCurrency() != target.getCurrency()) {
            BigDecimal sourceToRon = exchangeRatesConfig.getRates().get(source.getCurrency().name());
            BigDecimal targetToRon = exchangeRatesConfig.getRates().get(target.getCurrency().name());

            exchangeRate = sourceToRon.divide(targetToRon, 6, RoundingMode.HALF_EVEN);
            convertedAmount = amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_EVEN);
        }

        if(source.getId() != 1L) {
            source.setBalance(source.getBalance().subtract(amount));
            accountRepository.save(source);
        }

        if(target.getId() != 1L) {
            target.setBalance(target.getBalance().add(convertedAmount));
            accountRepository.save(target);
        }

        Transfer transfer = Transfer.builder()
                .sourceIban(source.getIban())
                .targetIban(target.getIban())
                .amount(amount)
                .currency(source.getCurrency())
                .targetCurrency(target.getCurrency())
                .exchangeRate(exchangeRate)
                .convertedAmount(source.getCurrency() == target.getCurrency() ? null : convertedAmount)
                .idempotencyKey(request.getIdempotencyKey())
                .build();
        transfer = transferRepository.save(transfer);

        saveLedgerEntries(source, target, transfer, amount, convertedAmount);

        return transferMapper.toResponse(transfer);
    }

    private void checkSavingsDailyLimit(Account source, BigDecimal newAmount) {
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfDay = startOfDay.plusSeconds(86399);

        List<Transaction> todaysTransfers = transactionRepository.findOutgoingTransactionsForDay(
                source.getId(), startOfDay, endOfDay);

        BigDecimal totalEurToday = BigDecimal.ZERO;
        for(Transaction t : todaysTransfers) {
            totalEurToday = totalEurToday.add(convertToEur(t.getAmount(), t.getCurrency().name()));
        }

        BigDecimal newAmountEur = convertToEur(newAmount, source.getCurrency().name());
        BigDecimal combinedTotal = totalEurToday.add(newAmountEur);

        if(combinedTotal.compareTo(new BigDecimal("5000.00")) > 0) {
            throw new BusinessValidationException("Daily outgoing limit of 5,000 EUR exceeded for SAVINGS account");
        }
    }

    private BigDecimal convertToEur(BigDecimal amount, String fromCurrency) {
        if("EUR".equals(fromCurrency)) return amount;

        BigDecimal fromToRon = exchangeRatesConfig.getRates().get(fromCurrency);
        BigDecimal eurToRon  = exchangeRatesConfig.getRates().get("EUR");
        BigDecimal rate = fromToRon.divide(fromToRon, 6, RoundingMode.HALF_EVEN);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_EVEN);
    }

    private void saveLedgerEntries(Account source, Account target, Transfer transfer, BigDecimal amount, BigDecimal convertedAmount) {
        if(source.getId() != 1L) {
            Transaction sourceTx = Transaction.builder()
                    .accountId(source.getId())
                    .type(target.getId() == 1L ? TransactionType.WITHDRAWAL : TransactionType.TRANSFER_OUT)
                    .amount(amount)
                    .currency(source.getCurrency())
                    .balanceAfter(source.getBalance())
                    .counterpartyIban(target.getId() == 1L ? null : target.getIban())
                    .transferId(transfer.getId())
                    .build();
            transactionRepository.save(sourceTx);
        }

        if(target.getId() != 1L) {
            Transaction targetTx = Transaction.builder()
                    .accountId(target.getId())
                    .type(source.getId() == 1L ? TransactionType.DEPOSIT : TransactionType.TRANSFER_IN)
                    .amount(convertedAmount)
                    .currency(target.getCurrency())
                    .balanceAfter(target.getBalance())
                    .counterpartyIban(source.getId() == 1L ? null : source.getIban())
                    .transferId(transfer.getId())
                    .build();
            transactionRepository.save(targetTx);
        }
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransfer(Long id) {
        return transferRepository.findById(id)
                .map(transferMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found"));
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfers(String iban, Instant fromDate, Instant toDate, Pageable pageable) {
        return transferRepository.findByFilters(iban, fromDate, toDate, pageable)
                .map(transferMapper::toResponse);
    }
}
