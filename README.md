# Banking System

A local Core Java console project for practising accounts, authentication and transactions with simulated Indian rupees. Each customer can open several accounts. The app saves them between runs.

## Run the downloaded project

1. Extract the entire ZIP. Open a terminal in the `BankingSystem` folder containing `BankingSystem.jar` and `lib`.
2. With Java 17 or newer installed, run the following command. The three Jackson libraries used at runtime are included.

```sh
java -jar BankingSystem.jar
```

3. Choose **1. Register**. Pick a login ID of 3–40 letters, digits, dots, hyphens or underscores, and a password of 8–128 characters. Registration signs you in. On future runs choose **2. Log in**. A normal terminal hides the password as you type. An IDE's input console may echo it.

## Try a transfer and check the math

You can do this whole exercise with one login and two accounts.

1. Choose **1. Open account** and enter `1000`. Call this account A in your notes.
2. Choose **1. Open account** again and enter `500`. Copy this second account's ten-digit number. Choose **2. My accounts** to see both numbers and balances.
3. Choose **5. Transfer**. Select A as the source, paste B's account number, enter `200` and use the reference `practice-001`. Check the details and enter `y`.
4. Choose **6. Statement** for A, then for B. Find the same reference in both statements.

| Account | Before | Entry with reference `practice-001` | After |
| --- | ---: | --- | ---: |
| A | ₹1,000.00 | `TRANSFER_OUT` −₹200.00 | ₹800.00 |
| B | ₹500.00 | `TRANSFER_IN` +₹200.00 | ₹700.00 |

5. Repeat the transfer with the **same source, destination, amount and reference**. The service returns the first receipt. A's balance is still ₹800.00. A different reference identifies a new payment.
6. Log out with **0**, exit with **0**, then start the app from the same folder and log in again. Both statements and balances are still there. You can also send money to an account belonging to another registered customer if you know its number.

## Follow that payment through the Java code

The production source is under `src/main/java/com/abhijai/banking/`.

| Where to look | What happens during the example |
| --- | --- |
| `app/BankingApplication.java` | `main` selects the data folder, loads the rules and connects the console, services and repository. All four services use the same repository. |
| `console/ConsoleUI.java` and `console/InputHandler.java` | Menu option 5 reads the account numbers and an INR amount. After confirmation it calls `TransactionService.transfer(customerId, from, to, amount, reference)`. The customer ID comes from the signed-in session. |
| `service/TransactionService.java` | `transfer` checks who owns A and whether the reference was used. For a new transfer it checks the amount and destination, withdraws from A, deposits into B and records both entries. |
| `domain/BankAccount.java` and `domain/Money.java` | The account enforces rules such as sufficient funds and active status. `Money` uses `BigDecimal` with two decimal places, so balances such as ₹499.95 retain their exact paise value. |
| `persistence/json/JsonBankingRepository.java` | `update` takes a file lock, loads a fresh snapshot and runs the service operation on that in-memory copy. When the operation succeeds, it saves one new snapshot containing both balances, the transfer and both entries. |
| `service/StatementService.java` | `statement` checks account ownership and returns the account's entries in posting order. The console displays the signed amount, balance after each entry, UTC time and transfer reference. |

**What happens if B is closed?** A's withdrawal can succeed on the in-memory copy before B rejects the deposit. The exception ends the operation before the repository writes the file. A keeps its original balance, B receives nothing and neither statement gains an entry. This all-or-nothing behavior is called **atomicity**. You can see the exact case in `BankingWorkflowTest.failingDestinationAfterDebitLeavesBothPrimaryAndBackupUntouched`.

## What the completed version adds

| Area | Changes from the uploaded banking project |
| --- | --- |
| Login | Registration, case-insensitive login, password and login ID changes, and a failed-attempt cooldown that survives a restart. `PasswordHasher` stores a random salt and a PBKDF2-HMAC-SHA256 hash. At login it derives a hash from the entered password and compares the two. |
| Accounts | Opening, listing balances and closing an empty account. The opening deposit is the first statement entry. Closed accounts keep their statements. |
| Payments | Deposits, withdrawals, transfers between accounts, running balances and a shared reference for each transfer. Transfers can be retried using the original reference. |
| Storage | JSON snapshots and a previous-version backup. The DTOs (data transfer objects) describe the file format. On load, they reconnect accounts to customers and check the statement totals against balances. |
| Existing code | The original money, account, transfer, transaction and configuration types provide the core rules. The account and customer classes can now reload their saved timestamps and status. `BankingState` also records changes and enforces account ownership through its lookup methods. |
| Build and tests | A console entry point, a Gradle `run` task and a `portable` build that produces the runnable JAR and its libraries. The 131 original tests are joined by 18 tests for the new workflows. |

## Rules and saved data

The default opening deposit is at least **₹500**. Subsequent deposits, withdrawals and transfers accept **₹1 to ₹1,00,000** per operation. After **five** failed login attempts the login is locked for **five minutes**. The settings live in `src/main/resources/banking.properties`. Rebuild after editing them. The original account model also enforces an opening floor of ₹500.

By default the app saves to `data/` under the directory where you run it. To practise with a separate set of accounts, run `java -jar BankingSystem.jar --data-dir practice-data`.

- `banking.json` holds customers, accounts, transaction entries and transfers. The balances and statements are readable local data.
- `banking.json.bak` holds the previous saved version after a subsequent change. A file lock serializes updates from separate running copies. Each write uses a temporary file and an atomic replacement on the same local filesystem.
- If the current snapshot is damaged or missing, use the command below in the same folder. Recovery validates the backup, keeps a copy of a damaged primary file, and restores the previous version. Changes made after that backup are outside the restored snapshot. With a custom data directory, pass the same `--data-dir` here.

```sh
java -jar BankingSystem.jar --restore-backup
```

## Build and test the source

Use a JDK 17 or newer. The first Gradle build downloads the pinned build tools and dependencies. These commands work from the extracted project folder.

| Task | macOS or Linux | Windows PowerShell |
| --- | --- | --- |
| Run the tests | `./gradlew test` | `.\gradlew.bat test` |
| Run the console from source | `./gradlew --console=plain run` | `.\gradlew.bat --console=plain run` |
| Rebuild the portable app | `./gradlew portable` | `.\gradlew.bat portable` |

Launch your rebuilt version with `java -jar build/portable/BankingSystem.jar`. In IntelliJ IDEA, open the folder containing `build.gradle`, import the Gradle project and run `BankingApplication.main`.

For a guided reading exercise, set a breakpoint in `TransactionService.transfer`, run `BankingWorkflowTest.transferWritesTwoMatchingEntriesAndSurvivesRestart`, and inspect the two `BankAccount` objects as you step through the debit, credit and ledger entries.
