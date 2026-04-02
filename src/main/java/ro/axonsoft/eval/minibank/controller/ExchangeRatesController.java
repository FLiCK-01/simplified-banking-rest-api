package ro.axonsoft.eval.minibank.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axonsoft.eval.minibank.config.ExchangeRatesConfig;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRatesController {
    private final ExchangeRatesConfig exchangeRatesConfig;

    @GetMapping
    public Map<String, BigDecimal> getExchangeRates() {
        return exchangeRatesConfig.getRates();
    }
}
