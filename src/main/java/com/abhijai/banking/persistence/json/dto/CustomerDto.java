package com.abhijai.banking.persistence.json.dto;

import com.abhijai.banking.domain.Customer;

import java.time.Instant;

public record CustomerDto(String customerId, String loginId, String passwordHash, String createdAt,
                          int failedLoginAttempts, String lockedUntil) {
    static CustomerDto from(Customer customer) {
        Instant until = customer.getLockedUntil();
        return new CustomerDto(customer.getCustomerId(), customer.getLoginId(), customer.getPasswordHash(),
                customer.getCreatedAt().toString(), customer.getFailedLoginAttempts(),
                until == null ? null : until.toString());
    }

    Customer toDomain() {
        return Customer.restore(customerId, loginId, passwordHash, Instant.parse(createdAt),
                failedLoginAttempts, lockedUntil == null ? null : Instant.parse(lockedUntil));
    }
}
