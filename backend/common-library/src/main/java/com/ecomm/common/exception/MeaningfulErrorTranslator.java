package com.ecomm.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for analyzing raw database, JPA, validation, and infrastructure exceptions
 * and translating them into user-friendly, human-readable domain exceptions.
 */
public final class MeaningfulErrorTranslator {

    private static final Logger log = LoggerFactory.getLogger(MeaningfulErrorTranslator.class);

    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile(
            "Duplicate entry '(?<value>[^']*)' for key '(?<key>[^']*)'",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NOT_NULL_PATTERN = Pattern.compile(
            "Column '(?<column>[^']+)' cannot be null",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATA_TOO_LONG_PATTERN = Pattern.compile(
            "Data too long for column '(?<column>[^']+)'",
            Pattern.CASE_INSENSITIVE
    );

    private MeaningfulErrorTranslator() {
        // Utility class
    }

    /**
     * Translates a raw Throwable into a meaningful domain exception if applicable.
     * If the exception is already a BaseDomainException, it is returned untouched.
     *
     * @param throwable the caught exception
     * @return translated or original Throwable
     */
    public static Throwable translate(Throwable throwable) {
        if (throwable instanceof BaseDomainException) {
            return throwable;
        }

        if (throwable instanceof DataIntegrityViolationException dive) {
            return translateDataIntegrityViolation(dive);
        }

        if (throwable instanceof OptimisticLockingFailureException) {
            return new ConflictException(
                    "Concurrency conflict: This record was modified by another user or operation while you were viewing it. Please reload the latest data and retry."
            );
        }

        if (throwable instanceof EmptyResultDataAccessException) {
            return new ResourceNotFoundException("The requested resource could not be found or has already been removed.");
        }

        if (throwable instanceof CannotAcquireLockException || throwable instanceof DeadlockLoserDataAccessException) {
            return new ConflictException(
                    "Database lock conflict: Unable to acquire a database lock due to concurrent activity. Please retry your request shortly."
            );
        }

        if (throwable instanceof QueryTimeoutException) {
            return new BadRequestException("The database query timed out. Please narrow your query criteria and try again.");
        }

        if (throwable instanceof IllegalArgumentException illegalArg) {
            return new BadRequestException("Invalid argument: " + illegalArg.getMessage());
        }

        // Return original throwable if not specifically translatable
        return throwable;
    }

    /**
     * Translates Spring's DataIntegrityViolationException by inspecting the root cause message
     * (e.g. MySQL duplicate key, foreign key constraint, not null constraint, or string length truncation).
     */
    public static BaseDomainException translateDataIntegrityViolation(DataIntegrityViolationException dive) {
        String rootMessage = getRootCauseMessage(dive);

        // 1. Check for Duplicate Entry (MySQL Error 1062)
        Matcher dupMatcher = DUPLICATE_ENTRY_PATTERN.matcher(rootMessage);
        if (dupMatcher.find()) {
            String value = dupMatcher.group("value");
            String key = dupMatcher.group("key");
            return createMeaningfulDuplicateException(key, value);
        }

        // 2. Check for Foreign Key Constraint Failure (MySQL Error 1451 / 1452)
        if (rootMessage.contains("foreign key constraint fails")) {
            if (rootMessage.contains("Cannot delete or update a parent row")) {
                return new ConflictException(
                        "Cannot delete or update this record because other records depend on it. Please remove dependent items first."
                );
            }
            return new BadRequestException(
                    "Cannot complete the operation: The referenced entity or foreign key relation does not exist."
            );
        }

        // 3. Check for Not-Null Constraint Failure (MySQL Error 1048)
        Matcher notNullMatcher = NOT_NULL_PATTERN.matcher(rootMessage);
        if (notNullMatcher.find()) {
            String column = notNullMatcher.group("column");
            return new BadRequestException(
                    String.format("Required field '%s' cannot be null or empty. Please provide a valid value.", column)
            );
        }

        // 4. Check for Data Too Long / String Truncation (MySQL Error 1406)
        Matcher lenMatcher = DATA_TOO_LONG_PATTERN.matcher(rootMessage);
        if (lenMatcher.find()) {
            String column = lenMatcher.group("column");
            return new BadRequestException(
                    String.format("The value provided for field '%s' exceeds the maximum allowed length.", column)
            );
        }

        // Fallback generic data integrity conflict
        log.warn("Unmatched DataIntegrityViolationException root message: {}", rootMessage);
        return new ConflictException(
                "Data integrity violation: The operation violates database consistency rules or unique constraints. Please verify your input."
        );
    }

    private static BaseDomainException createMeaningfulDuplicateException(String rawKey, String value) {
        String cleanKey = rawKey != null ? rawKey.toLowerCase() : "";

        if (cleanKey.contains("sku")) {
            return new ConflictException(
                    String.format("A product with SKU '%s' already exists in the catalog. Please specify a unique SKU.", value)
            );
        } else if (cleanKey.contains("email")) {
            return new ConflictException(
                    String.format("An account with email address '%s' is already registered. Please sign in or use a different email.", value)
            );
        } else if (cleanKey.contains("phone")) {
            return new ConflictException(
                    String.format("An account with phone number '%s' is already registered.", value)
            );
        } else if (cleanKey.contains("name")) {
            return new ConflictException(
                    String.format("An item with name '%s' already exists. Please choose a different name.", value)
            );
        } else if (cleanKey.contains("primary")) {
            return new ConflictException(
                    String.format("A record with identifier '%s' already exists.", value)
            );
        }

        return new ConflictException(
                String.format("Unique constraint violation: A record with value '%s' already exists for '%s'. Please provide a unique value.", value, rawKey)
        );
    }

    private static String getRootCauseMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : (throwable.getMessage() != null ? throwable.getMessage() : "");
    }
}
