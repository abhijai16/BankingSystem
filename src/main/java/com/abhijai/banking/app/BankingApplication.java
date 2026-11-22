package com.abhijai.banking.app;

import com.abhijai.banking.config.BankingConfig;
import com.abhijai.banking.config.BankingConfigLoader;
import com.abhijai.banking.console.ConsoleUI;
import com.abhijai.banking.console.InputHandler;
import com.abhijai.banking.persistence.json.JsonBankingRepository;
import com.abhijai.banking.security.PasswordHasher;
import com.abhijai.banking.service.AccountService;
import com.abhijai.banking.service.AuthenticationService;
import com.abhijai.banking.service.StatementService;
import com.abhijai.banking.service.TransactionService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Clock;

public final class BankingApplication {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IOException | IllegalArgumentException | IllegalStateException error) {
            System.err.println("Banking System: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        Path directory = Path.of("data");
        boolean recover = false;

        for(int i = 0; i < args.length; i++){
            switch(args[i]){
                case "--data-dir" ->{
                    if(++i == args.length || args[i].isBlank() || args[i].startsWith("--")){
                        throw new IllegalArgumentException("--data-dir needs a directory path.");
                    }
                    directory = Path.of(args[i]);
                }
                case "--restore-backup" -> recover = true;
                case "--help", "-h" -> {
                    System.out.println("Usage: java -jar BankingSystem.jar [--data-dir PATH] [--restore-backup]");
                    return;
                }
                default -> throw new IllegalArgumentException("Unknown option " + args[i] + ". Use --help for usage.");
            }
        }

        JsonBankingRepository repository = new JsonBankingRepository(directory);
        if(recover){
            repository.restoreBackup();
            System.out.println("Restored the previous snapshot in " + directory.toAbsolutePath().normalize() + ".");
            return;
        }

        repository.load();
        BankingConfig config = new BankingConfigLoader().load();

        PrintWriter out = new PrintWriter(System.out, true);

        out.println("Banking System | Local INR simulator");
        out.println("Opening deposit from " + config.getMinimumOpeningDeposit() + ". Transactions "
                + config.getMinimumTransactionAmount() + " to " + config.getMaximumTransactionAmount() + ".");

        out.println("Data directory: " + directory.toAbsolutePath().normalize());

        InputHandler input = new InputHandler(System.console(), new InputStreamReader(System.in), out);

        new ConsoleUI(input, out,
                new AuthenticationService(repository, config, new PasswordHasher(), Clock.systemUTC()),
                new AccountService(repository, config), new TransactionService(repository, config),
                new StatementService(repository)).run();
    }
}
