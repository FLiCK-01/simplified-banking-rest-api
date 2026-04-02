package ro.axonsoft.eval.minibank.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dto.PaginatedResponse;
import ro.axonsoft.eval.minibank.dto.TransferCreateRequest;
import ro.axonsoft.eval.minibank.dto.TransferResponse;
import ro.axonsoft.eval.minibank.service.TransferService;

import java.time.Instant;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public TransferResponse createTransfer(@Valid @RequestBody TransferCreateRequest request) {
        return transferService.createTransfer(request);
    }

    @GetMapping("/{transferId}")
    public TransferResponse getTransfer(@PathVariable Long transferId) {
        return transferService.getTransfer(transferId);
    }

    @GetMapping
    public PaginatedResponse<TransferResponse> getTransfers(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TransferResponse> transfersPage = transferService.getTransfers(iban, fromDate, toDate, PageRequest.of(page, size));
        return new PaginatedResponse<>(transfersPage);
    }
}