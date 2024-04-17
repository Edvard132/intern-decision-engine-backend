package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
@Slf4j
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private int creditModifier = 0;

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount   Requested loan amount
     * @param loanPeriod   Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException   If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException   If the requested loan period is invalid
     * @throws NoValidLoanException         If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException {
        try {
            verifyInputs(personalCode, loanAmount, loanPeriod);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }

        creditModifier = getCreditModifier(personalCode);

        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        loanAmount = highestValidLoanAmount(loanPeriod, loanAmount);

        double creditScore = getCreditScore(loanAmount, loanPeriod);

        if (creditScore < 1) {

            if (getCreditScore(loanAmount, loanPeriod) < 1) {
                while (getCreditScore(loanAmount, loanPeriod) < 1 && loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
                    loanPeriod++;
                }
            }
        }

        if (loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            loanAmount = Math.min(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, loanAmount);
        } else {
            throw new NoValidLoanException("No valid loan found!");
        }
        return new Decision(loanAmount.intValue(), loanPeriod, null);
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     *
     * @return Largest valid loan amount
     */
    private Long highestValidLoanAmount(int loanPeriod, Long loanAmount) {
        double creditScore = getCreditScore(loanAmount, loanPeriod);

        if (creditScore < 1) {
            while (getCreditScore(loanAmount, loanPeriod) < 1 && loanAmount > DecisionEngineConstants.MINIMUM_LOAN_AMOUNT) {
                loanAmount -= 100;
            }

        } else {
            while (getCreditScore(loanAmount, loanPeriod) > 1 && loanAmount < DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT) {
                loanAmount += 100;
            }
        }

        return loanAmount;
    }

    /**
     * Calculates the person's credit score according to user input.
     *
     * @return credit score
     */
    private double getCreditScore(Long loanAmount, int loanPeriod) {
        return (double) (creditModifier) / loanAmount * loanPeriod;
    }

    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount   Requested loan amount
     * @param loanPeriod   Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException   If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException   If the requested loan period is invalid
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }
}
