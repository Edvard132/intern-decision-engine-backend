package ee.taltech.inbankbackend.utils;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidPersonAgeException;

import java.time.Period;

/**
 * Validates that user is an adult and sum of person age and requested loan period
 * is not bigger than the expected lifetime of a particular country
 */
public class LoanAgeValidator {

    public LoanAgeValidator() {}

    private final EstonianPersonalCodeParser parser = new EstonianPersonalCodeParser();

    public boolean isNotUnderAge(String personalCode) throws InvalidPersonAgeException {
        Period age;
        try {
            age = parser.getAge(personalCode);
        } catch (PersonalCodeException e) {
            throw new InvalidPersonAgeException("Invalid personal code");
        }
        return age.getYears() >= 18;
    }

    public boolean isNotExceedingAverageLifetime(String personalCode, int loanPeriod, String country) throws InvalidPersonAgeException {
        Period age;
        try {
            age = parser.getAge(personalCode);
        } catch (PersonalCodeException e) {
            throw new InvalidPersonAgeException("Invalid personal code", e);
        }

        int expectedLifetime = switch (country.toUpperCase()) {
            case "LATVIA" -> DecisionEngineConstants.EXPECTED_LIFETIME_LATVIA;
            case "LITHUANIA" -> DecisionEngineConstants.EXPECTED_LIFETIME_LITHUANIA;
            default -> DecisionEngineConstants.EXPECTED_LIFETIME_ESTONIA;
        };

        return age.getYears() + (loanPeriod / 12) < expectedLifetime;

    }

}
