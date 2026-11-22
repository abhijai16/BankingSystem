package com.abhijai.banking.console;

import com.abhijai.banking.domain.AccountStatus;
import com.abhijai.banking.domain.BankAccount;
import com.abhijai.banking.domain.Money;
import com.abhijai.banking.domain.TransactionRecord;
import com.abhijai.banking.domain.Transfer;
import com.abhijai.banking.exception.BankingException;
import com.abhijai.banking.service.AccountService;
import com.abhijai.banking.service.AuthenticationService;
import com.abhijai.banking.service.StatementService;
import com.abhijai.banking.service.TransactionService;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ConsoleUI {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);
    private final InputHandler input;
    private final PrintWriter out;
    private final AuthenticationService auth;
    private final AccountService accounts;
    private final TransactionService transactions;
    private final StatementService statements;

    public ConsoleUI(InputHandler input, PrintWriter out, AuthenticationService auth, AccountService accounts,
                     TransactionService transactions, StatementService statements) {
        this.input = input;
        this.out = out;
        this.auth = auth;
        this.accounts = accounts;
        this.transactions = transactions;
        this.statements = statements;
    }

    public void run() throws IOException {
        String customer = null;
        try {
            while (true) {
                try {
                    if (customer == null) {
                        out.println("\n1. Register   2. Log in   0. Exit");
                        String option = input.line("Choice:");
                        if (option.equals("0")) {
                            break;
                        }
                        switch (option) {
                            case "1" -> customer = signIn(true);
                            case "2" -> customer = signIn(false);
                            default -> out.println("Choose a number from the menu.");
                        }
                    } else {
                        out.println("\n1. Open account   2. My accounts   3. Deposit   4. Withdraw   5. Transfer");
                        out.println("6. Statement      7. Close account 8. Change login ID  9. Change password  0. Log out");
                        switch (input.line("Choice:")) {
                            case "1" -> open(customer);
                            case "2" -> list(customer);
                            case "3" -> cash(customer, true);
                            case "4" -> cash(customer, false);
                            case "5" -> transfer(customer);
                            case "6" -> statement(customer);
                            case "7" -> close(customer);
                            case "8" -> changeCredentials(customer, false);
                            case "9" -> changeCredentials(customer, true);
                            case "0" -> {
                                customer = null;
                                out.println("Logged out.");
                            }
                            default -> out.println("Choose a number from the menu.");
                        }
                    }
                } catch (IllegalArgumentException | IllegalStateException rejected) {
                    out.println("Action failed. " + rejected.getMessage());
                }
            }
        } catch (EOFException endOfInput) {
            out.println();
        }
        out.println("Goodbye.");
    }

    private String signIn(boolean register) throws IOException {
        String login = input.line("Login ID:");
        char[] password = input.password(register ? "New password (8 to 128 characters):" : "Password:");
        try {
            String customer = register ? auth.register(login, password) : auth.login(login, password);
            out.println(register ? "Registered and signed in." : "Signed in.");
            return customer;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void open(String customer) throws IOException {
        BankAccount account = accounts.open(customer, input.money("Opening deposit (INR):"));
        out.println("Opened account " + account.getAccountNumber() + ". Balance " + account.getBalance() + ".");
    }

    private void list(String customer) {
        List<BankAccount> available = accounts.accounts(customer);
        if (available.isEmpty()) {
            out.println("You have no accounts yet. Choose Open account to create one.");
        } else {
            printAccounts(available);
        }
    }

    private void printAccounts(List<BankAccount> available) {
        out.println("     Account       Status   Balance");
        for (int i = 0; i < available.size(); i++) {
            BankAccount account = available.get(i);
            out.printf(Locale.ROOT, "%3d  %-12s  %-7s  %s%n", i + 1, account.getAccountNumber(),
                    account.getStatus(), account.getBalance());
        }
    }

    private BankAccount select(String customer, boolean includeClosed) throws IOException {
        List<BankAccount> available = accounts.accounts(customer).stream()
                .filter(account -> includeClosed || account.getStatus() == AccountStatus.ACTIVE).toList();
        if (available.isEmpty()) {
            throw new BankingException("No eligible accounts. Choose Open account to create one.");
        }
        if (available.size() == 1) {
            BankAccount account = available.get(0);
            out.println("Using account " + account.getAccountNumber() + ".");
            return account;
        }
        printAccounts(available);
        String choice = input.line("Select a row or enter its account number:");
        for (int i = 0; i < available.size(); i++) {
            BankAccount account = available.get(i);
            if (choice.equals(Integer.toString(i + 1)) || choice.equals(account.getAccountNumber())) {
                return account;
            }
        }
        throw new BankingException("Select one of the displayed accounts.");
    }

    private void cash(String customer, boolean deposit) throws IOException {
        String number = select(customer, false).getAccountNumber();
        Money amount = input.money(deposit ? "Deposit amount (INR):" : "Withdrawal amount (INR):");
        TransactionRecord receipt = deposit ? transactions.deposit(customer, number, amount)
                : transactions.withdraw(customer, number, amount);
        out.println((deposit ? "Deposited " : "Withdrew ") + amount + ". Balance " + receipt.getResultingBalance() + ".");
    }

    private void transfer(String customer) throws IOException {
        String from = select(customer, false).getAccountNumber();
        String to = input.line("Destination account number:");
        Money amount = input.money("Transfer amount (INR):");
        String reference = input.line("Transfer reference (Enter to generate one):");
        if (reference.isEmpty()) {
            reference = UUID.randomUUID().toString();
        }
        out.println("Reference " + reference);
        if (input.confirm("Send " + amount + " from " + from + " to " + to + "?")) {
            Transfer receipt = transactions.transfer(customer, from, to, amount, reference);
            out.println("Transfer completed. Reference " + receipt.getTransferReference() + ".");
        } else {
            out.println("Transfer cancelled.");
        }
    }

    private void statement(String customer) throws IOException {
        String number = select(customer, true).getAccountNumber();
        StatementService.Statement statement = statements.statement(customer, number);
        out.println("Statement for " + number + " (" + statement.account().getStatus() + ")");
        out.printf("%-19s  %-13s  %12s  %14s  %s%n", "Date/time (UTC)", "Type", "Amount (INR)", "Balance (INR)", "Transfer reference");
        for (TransactionRecord entry : statement.entries()) {
            String signed = (entry.getAmount().isPositive() ? "+" : "") + entry.getAmount().getAmount().toPlainString();
            out.printf(Locale.ROOT, "%-19s  %-13s  %12s  %14s  %s%n", TIME.format(entry.getTimestamp()),
                    entry.getType(), signed, entry.getResultingBalance().getAmount().toPlainString(),
                    entry.getTransferReference() == null ? "-" : entry.getTransferReference());
        }
        out.println("Current balance " + statement.account().getBalance() + ".");
    }

    private void close(String customer) throws IOException {
        String number = select(customer, false).getAccountNumber();
        if (input.confirm("Close account " + number + "?")) {
            accounts.close(customer, number);
            out.println("Account closed. Its statement is still available.");
        } else {
            out.println("Closure cancelled.");
        }
    }

    private void changeCredentials(String customer, boolean passwordChange) throws IOException {
        char[] current = input.password("Current password:");
        try {
            if (passwordChange) {
                char[] replacement = input.password("New password (8 to 128 characters):");
                try {
                    auth.changePassword(customer, current, replacement);
                } finally {
                    Arrays.fill(replacement, '\0');
                }
                out.println("Password updated.");
            } else {
                auth.changeLoginId(customer, current, input.line("New login ID:"));
                out.println("Login ID updated.");
            }
        } finally {
            Arrays.fill(current, '\0');
        }
    }
}
