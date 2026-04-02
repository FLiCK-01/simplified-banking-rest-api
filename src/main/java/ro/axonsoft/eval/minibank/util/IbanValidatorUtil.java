package ro.axonsoft.eval.minibank.util;

import org.apache.commons.validator.routines.IBANValidator;
import java.util.Set;

public class IbanValidatorUtil {
    private static final Set<String> SEPA_COUNTRIES = Set.of(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU",
            "IS", "IE", "IT", "LV", "LI", "LT", "LU", "MT", "MC", "NL", "NO", "PL", "PT",
            "RO", "SM", "SK", "SI", "ES", "SE", "CH", "GB"
    );

    public static boolean isValidIban(String iban) {
        if (iban == null) {
            return false;
        }
        return IBANValidator.getInstance().isValid(iban);
    }

    public static boolean isSepaCountry(String iban) {
        if (iban == null || iban.length() < 2) {
            return false;
        }
        String countryCode = iban.substring(0, 2).toUpperCase();
        return SEPA_COUNTRIES.contains(countryCode);
    }
}