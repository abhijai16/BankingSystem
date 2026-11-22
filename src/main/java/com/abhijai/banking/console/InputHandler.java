package com.abhijai.banking.console;

import com.abhijai.banking.domain.Money;
import com.abhijai.banking.exception.BankingException;

import java.io.BufferedReader;
import java.io.Console;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class InputHandler {
    private static final Pattern AMOUNT = Pattern.compile("(?:[0-9]+(?:\\.[0-9]{1,2})?|\\.[0-9]{1,2})");
    private final Console console;
    private final BufferedReader reader;
    private final PrintWriter out;

    public InputHandler(Console console, Reader reader, PrintWriter out) {
        this.console = console;
        this.reader = new BufferedReader(reader);
        this.out = out;
    }

    public String line(String prompt) throws IOException {
        prompt(prompt);
        String value = console == null ? reader.readLine() : console.readLine();
        if (value == null) {
            throw new EOFException();
        }
        return value.strip();
    }

    public char[] password(String label) throws IOException {
        prompt(label);
        if (console != null) {
            char[] value = console.readPassword();
            if (value == null) {
                throw new EOFException();
            }
            return value;
        }
        String value = reader.readLine();
        if (value == null) {
            throw new EOFException();
        }
        return value.toCharArray();
    }

    public Money money(String label) throws IOException {
        String value = line(label);
        if (!AMOUNT.matcher(value).matches()) {
            throw new BankingException("Enter a rupee amount with up to two decimal places, for example 250.50.");
        }
        return new Money(new BigDecimal(value), "INR");
    }

    public boolean confirm(String label) throws IOException {
        String choice = line(label + " [y/N]");
        return choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes");
    }

    private void prompt(String text) {
        out.print(text + " ");
        out.flush();
    }
}
