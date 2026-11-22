package com.abhijai.banking.repository;

import com.abhijai.banking.domain.Customer;
import com.abhijai.banking.exception.BankingException;

import java.util.Map;
import java.util.Optional;

public interface CustomerRepository {
    Map<String, Customer> getCustomers();

    default Optional<Customer> findByLogin(String loginId) {
        return getCustomers().values().stream()
                .filter(customer -> customer.getLoginId().equalsIgnoreCase(loginId))
                .findFirst();
    }

    default Customer requireCustomer(String customerId) {
        Customer customer = getCustomers().get(customerId);
        if (customer == null) {
            throw new BankingException("Customer unavailable. Please log in again.");
        }
        return customer;
    }
}
