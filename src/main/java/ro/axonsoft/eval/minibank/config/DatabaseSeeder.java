package ro.axonsoft.eval.minibank.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Currency;
import ro.axonsoft.eval.minibank.repository.AccountRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        if (!accountRepository.existsByIban("RO49AAAA1B31007593840000")) {
            Account systemBank = Account.builder()
                    .ownerName("System Bank")
                    .iban("RO49AAAA1B31007593840000")
                    .currency(Currency.RON)
                    .accountType(AccountType.CHECKING)
                    .balance(BigDecimal.ZERO)
                    .build();

            accountRepository.save(systemBank);
            System.out.println("System Bank Account (ID 1) successfully seeded!");
        }
    }
}