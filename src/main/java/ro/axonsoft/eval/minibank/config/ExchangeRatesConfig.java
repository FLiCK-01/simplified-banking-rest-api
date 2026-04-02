package ro.axonsoft.eval.minibank.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "exchange")
@Getter
@Setter
public class ExchangeRatesConfig {
    private Map<String, BigDecimal> rates;
}
