package ro.axonsoft.eval.minibank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axonsoft.eval.minibank.dto.AccountCreateRequest;
import ro.axonsoft.eval.minibank.dto.AccountResponse;
import ro.axonsoft.eval.minibank.exception.BusinessValidationException;
import ro.axonsoft.eval.minibank.mapper.AccountMapper;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.util.IbanValidatorUtil;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        if(!IbanValidatorUtil.isValidIban(request.getIban())) {
            throw new BusinessValidationException("Invalid IBAN format");
        }

        if(accountRepository.existsByIban(request.getIban())) {
            throw new BusinessValidationException("IBAN is already in use");
        }

        Account account = accountMapper.toEntity(request);
        Account savedAccount = accountRepository.save(account);

        return accountMapper.toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return accountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(accountMapper::toResponse);
    }
}
