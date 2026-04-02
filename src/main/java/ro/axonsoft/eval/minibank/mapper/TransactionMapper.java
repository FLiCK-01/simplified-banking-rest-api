package ro.axonsoft.eval.minibank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ro.axonsoft.eval.minibank.dto.TransactionResponse;
import ro.axonsoft.eval.minibank.model.Transaction;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    TransactionResponse toResponse(Transaction transaction);
}
