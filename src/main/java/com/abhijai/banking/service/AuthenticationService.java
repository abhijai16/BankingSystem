package com.abhijai.banking.service;

import com.abhijai.banking.config.BankingConfig;
import com.abhijai.banking.domain.BankingState;
import com.abhijai.banking.domain.Customer;
import com.abhijai.banking.exception.BankingException;
import com.abhijai.banking.repository.BankingRepository;
import com.abhijai.banking.security.PasswordHasher;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public final class AuthenticationService {
    private static final Pattern LOGIN = Pattern.compile("[A-Za-z0-9._-]{3,40}");
    private final BankingRepository repository;
    private final BankingConfig config;
    private final PasswordHasher hasher;
    private final Clock clock;

    public AuthenticationService(BankingRepository repository, BankingConfig config, PasswordHasher hasher, Clock clock) {
        this.repository = repository;
        this.config = config;
        this.hasher = hasher;
        this.clock = clock;
    }

    public String register(String loginId, char[] password) {
        String login = validLogin(loginId);
        validateNewPassword(password);
        return repository.update(state -> {
            if (state.findByLogin(login).isPresent()) {
                throw new BankingException("That login ID is already in use.");
            }
            Customer customer = new Customer(UUID.randomUUID().toString(), login, hasher.hash(password));
            state.addCustomer(customer);
            return customer.getCustomerId();
        });
    }

    public String login(String loginId, char[] password) {
        return repository.update(state -> verify(state.findByLogin(loginId.strip()).orElse(null), password))
                .requireSuccess();
    }

    public void changeLoginId(String customerId, char[] currentPassword, String newLoginId) {
        String login = validLogin(newLoginId);
        changeCredentials(customerId, currentPassword, (state, customer) -> {
            if (state.findByLogin(login).filter(other -> other != customer).isPresent()) {
                throw new BankingException("That login ID is already in use.");
            }
            customer.changeLoginId(login);
        });
    }

    public void changePassword(String customerId, char[] currentPassword, char[] newPassword) {
        validateNewPassword(newPassword);
        changeCredentials(customerId, currentPassword, (state, customer) ->
                customer.changePasswordHash(hasher.hash(newPassword)));
    }

    private void changeCredentials(String customerId, char[] password, BiConsumer<BankingState, Customer> change) {
        repository.update(state -> {
            Customer customer = state.requireCustomer(customerId);
            Attempt attempt = verify(customer, password);
            if (attempt.error() == null) {
                change.accept(state, customer);
            }
            return attempt;
        }).requireSuccess();
    }

    private Attempt verify(Customer customer, char[] password) {
        Instant now = clock.instant();
        if (customer != null && customer.isLoginLocked(now)) {
            return locked(customer);
        }
        if (customer == null || !hasher.matches(password, customer.getPasswordHash())) {
            if (customer != null) {
                customer.recordFailedLogin(now, config.getMaximumLoginAttempts(), config.getLoginCooldownMinutes());
                if (customer.isLoginLocked(now)) {
                    return locked(customer);
                }
            }
            return new Attempt(null, "Invalid login ID or password.");
        }
        customer.clearLoginFailures();
        return new Attempt(customer.getCustomerId(), null);
    }

    private static Attempt locked(Customer customer) {
        return new Attempt(null, "Too many failed attempts. Try again at " + customer.getLockedUntil() + " (UTC).");
    }

    private static String validLogin(String login) {
        String trimmed = login.strip();
        if (!LOGIN.matcher(trimmed).matches()) {
            throw new BankingException("Use 3 to 40 letters, digits, dots, hyphens or underscores for the login ID.");
        }
        return trimmed;
    }

    private static void validateNewPassword(char[] password) {
        if (password.length < 8 || password.length > 128) {
            throw new BankingException("Use a password of 8 to 128 characters.");
        }
        for (char character : password) {
            if (!Character.isWhitespace(character)) {
                return;
            }
        }
        throw new BankingException("The password cannot consist only of spaces.");
    }

    // Failed attempts must commit before an error is shown to the caller.
    private record Attempt(String customerId, String error) {
        String requireSuccess() {
            if (error != null) {
                throw new BankingException(error);
            }
            return customerId;
        }
    }
}
