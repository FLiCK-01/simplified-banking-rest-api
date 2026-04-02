package ro.axonsoft.eval.minibank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ro.axonsoft.eval.minibank.dto.AccountCreateRequest;
import ro.axonsoft.eval.minibank.dto.AccountResponse;
import ro.axonsoft.eval.minibank.model.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {
    Account toEntity(AccountCreateRequest request);
    AccountResponse toResponse(Account account);
}
