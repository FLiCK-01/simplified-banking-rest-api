package ro.axonsoft.eval.minibank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ro.axonsoft.eval.minibank.dto.TransferResponse;
import ro.axonsoft.eval.minibank.model.Transfer;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransferMapper {
    TransferResponse toResponse(Transfer transfer);
}
