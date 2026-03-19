package com.virtbank.config;

import com.virtbank.entity.*;
import com.virtbank.entity.enums.*;
import com.virtbank.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "P@ssword1";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMemberRepository businessMemberRepository;
    private final PayrollRepository payrollRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    private int txCount = 0, loanCount = 0, invoiceCount = 0, auditCount = 0, notifCount = 0;
    private int accountCount = 0;
    private String hashedPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("[VIRTBANK SEEDER] Data already exists — skipping seed.");
            return;
        }

        hashedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        // Ensure roles exist
        ensureRoles();

        // Seed users
        List<User> admins = seedAdmins();
        List<User> customers = seedCustomers();
        List<User> businessUsers = seedBusinessUsers(customers);

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(admins);
        allUsers.addAll(customers);
        allUsers.addAll(businessUsers);

        // Audit logs
        seedAuditLogs(admins, allUsers);

        // Notifications (5 per user)
        seedNotifications(allUsers);

        log.info("[VIRTBANK SEEDER] Seeding complete.");
        log.info("Users created   : {}", allUsers.size());
        log.info("Accounts created: {}", accountCount);
        log.info("Transactions    : {}", txCount);
        log.info("Loans           : {}", loanCount);
        log.info("Invoices        : {}", invoiceCount);
        log.info("Audit logs      : {}", auditCount);
        log.info("Notifications   : {}", notifCount);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Roles
    // ═══════════════════════════════════════════════════════════════════

    private void ensureRoles() {
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty())
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        if (roleRepository.findByName("ROLE_CUSTOMER").isEmpty())
            roleRepository.save(Role.builder().name("ROLE_CUSTOMER").build());
        if (roleRepository.findByName("ROLE_BUSINESS").isEmpty())
            roleRepository.save(Role.builder().name("ROLE_BUSINESS").build());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Admins — 3 users
    // ═══════════════════════════════════════════════════════════════════

    private List<User> seedAdmins() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        String[][] adminData = {
                {"Sarah", "Mitchell", "sarah.mitchell@virtbank.com", "03001234567"},
                {"James", "Okonkwo", "james.okonkwo@virtbank.com", "03009876543"},
                {"Priya", "Sharma", "priya.sharma@virtbank.com", "03005551234"}
        };

        List<User> admins = new ArrayList<>();
        for (String[] d : adminData) {
            User u = userRepository.save(User.builder()
                    .firstName(d[0]).lastName(d[1]).email(d[2]).phone(d[3])
                    .passwordHash(hashedPassword)
                    .userType(UserType.ADMIN).status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(adminRole))).build());
            admins.add(u);
        }
        return admins;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Customers — 10 users with accounts, transactions, loans, KYC, tickets
    // ═══════════════════════════════════════════════════════════════════

    private List<User> seedCustomers() {
        Role custRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow();
        String[][] custData = {
                {"Ahmed", "Khan",    "ahmed.khan@mail.com",    "03211234567"},
                {"Fatima", "Ali",    "fatima.ali@mail.com",    "03331234567"},
                {"Hassan", "Raza",   "hassan.raza@mail.com",   "03451234567"},
                {"Ayesha", "Malik",  "ayesha.malik@mail.com",  "03001112233"},
                {"Usman", "Sheikh",  "usman.sheikh@mail.com",  "03129998877"},
                {"Sana", "Bukhari",  "sana.bukhari@mail.com",  "03219876543"},
                {"Bilal", "Ahmed",   "bilal.ahmed@mail.com",   "03339876543"},
                {"Zainab", "Iqbal",  "zainab.iqbal@mail.com",  "03459876543"},
                {"Omar", "Farooq",   "omar.farooq@mail.com",   "03001239876"},
                {"Hira", "Noor",     "hira.noor@mail.com",     "03121239876"}
        };

        BigDecimal[] balances = {
                pkr(125000), pkr(47500), pkr(310000), pkr(89000), pkr(500000),
                pkr(5000), pkr(72000), pkr(215000), pkr(38000), pkr(165000)
        };

        List<User> customers = new ArrayList<>();
        for (int i = 0; i < custData.length; i++) {
            String[] d = custData[i];
            User u = userRepository.save(User.builder()
                    .firstName(d[0]).lastName(d[1]).email(d[2]).phone(d[3])
                    .passwordHash(hashedPassword)
                    .userType(UserType.CUSTOMER).status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(custRole))).build());
            customers.add(u);

            // Savings + Current account
            Account savings = createAccount(u, AccountType.SAVINGS, balances[i], "PKR",
                    "VB-SAV-" + String.format("%06d", u.getId()));
            Account current = createAccount(u, AccountType.CURRENT,
                    balances[i].multiply(new BigDecimal("0.3")).setScale(4, RoundingMode.HALF_UP), "PKR",
                    "VB-CUR-" + String.format("%06d", u.getId()));

            // 15 transactions per customer
            seedTransactions(savings, current, 15);
        }

        // 3 customers with active loans
        for (int i = 0; i < 3; i++) {
            User borrower = customers.get(i);
            Account acct = accountRepository.findAll().stream()
                    .filter(a -> a.getUser().getId().equals(borrower.getId())
                            && a.getAccountType() == AccountType.SAVINGS)
                    .findFirst().orElseThrow();
            seedLoan(borrower, acct, LoanStatus.ACTIVE,
                    pkr(200000 + i * 100000), new BigDecimal("12.5"), 12 + i * 6);
        }

        // 2 pending KYC documents
        seedKyc(customers.get(3));
        seedKyc(customers.get(7));

        // 4 open support tickets
        seedTicket(customers.get(0), "Unable to view transaction history", TicketCategory.TRANSACTION);
        seedTicket(customers.get(2), "Account balance discrepancy", TicketCategory.ACCOUNT);
        seedTicket(customers.get(5), "Loan EMI calculation query", TicketCategory.LOAN);
        seedTicket(customers.get(9), "Mobile app login not working", TicketCategory.TECHNICAL);

        return customers;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Business — 3 users with accounts, invoices, payroll, loans
    // ═══════════════════════════════════════════════════════════════════

    private List<User> seedBusinessUsers(List<User> customers) {
        Role bizRole = roleRepository.findByName("ROLE_BUSINESS").orElseThrow();
        String[][] bizOwnerData = {
                {"Tariq", "Mehmood", "tariq@peakretail.pk", "03001001001"},
                {"Nadia", "Hussain", "nadia@swiftlogistics.pk", "03002002002"},
                {"Imran", "Baig", "imran@alphaadvisors.pk", "03003003003"}
        };
        String[][] bizInfo = {
                {"Peak Retail", "NTN-10001", "Retail", "https://peakretail.pk"},
                {"Swift Logistics", "NTN-20002", "Logistics", "https://swiftlogistics.pk"},
                {"Alpha Advisors", "NTN-30003", "Consulting", "https://alphaadvisors.pk"}
        };
        BusinessType[] bizTypes = {BusinessType.SOLE_PROPRIETORSHIP, BusinessType.LLC, BusinessType.PARTNERSHIP};

        List<User> bizUsers = new ArrayList<>();
        for (int b = 0; b < 3; b++) {
            String[] d = bizOwnerData[b];
            User owner = userRepository.save(User.builder()
                    .firstName(d[0]).lastName(d[1]).email(d[2]).phone(d[3])
                    .passwordHash(hashedPassword)
                    .userType(UserType.BUSINESS).status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(bizRole))).build());
            bizUsers.add(owner);

            // 2 accounts per business
            Account ops = createAccount(owner, AccountType.CURRENT,
                    pkr(1500000 + b * 500000), "PKR",
                    "VB-BIZ-OPS-" + String.format("%04d", owner.getId()));
            Account reserve = createAccount(owner, AccountType.SAVINGS,
                    pkr(800000 + b * 200000), "PKR",
                    "VB-BIZ-RSV-" + String.format("%04d", owner.getId()));

            seedTransactions(ops, reserve, 10);

            // Business entity
            Business biz = businessRepository.save(Business.builder()
                    .owner(owner).businessName(bizInfo[b][0])
                    .registrationNumber(bizInfo[b][1]).businessType(bizTypes[b])
                    .industry(bizInfo[b][2]).website(bizInfo[b][3])
                    .status(UserStatus.ACTIVE).build());

            // 3 employees (use customers as employees)
            for (int e = 0; e < 3; e++) {
                User emp = customers.get(b * 3 + e);
                businessMemberRepository.save(BusinessMember.builder()
                        .business(biz).user(emp)
                        .role(e == 0 ? BusinessMemberRole.MANAGER : BusinessMemberRole.EMPLOYEE)
                        .status(UserStatus.ACTIVE).build());
            }

            // 5 invoices (mix of PAID, SENT, OVERDUE)
            seedInvoices(biz, 5);

            // 2 payroll runs
            seedPayroll(biz, owner, customers.subList(b * 3, b * 3 + 3), 2);

            // 1 active business loan
            seedLoan(owner, ops, LoanStatus.ACTIVE,
                    pkr(500000 + b * 250000), new BigDecimal("10.0"), 24);
        }

        return bizUsers;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper: Create Account
    // ═══════════════════════════════════════════════════════════════════

    private Account createAccount(User user, AccountType type, BigDecimal balance, String currency, String accNum) {
        accountCount++;
        return accountRepository.save(Account.builder()
                .user(user).accountNumber(accNum)
                .accountType(type).balance(balance)
                .currency(currency).status(AccountStatus.ACTIVE)
                .accountName(user.getFirstName() + " " + type.name())
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper: Transactions
    // ═══════════════════════════════════════════════════════════════════

    private void seedTransactions(Account primary, Account secondary, int count) {
        TransactionType[] types = {TransactionType.DEPOSIT, TransactionType.WITHDRAWAL, TransactionType.TRANSFER};
        BigDecimal balance = primary.getBalance();

        for (int i = 0; i < count; i++) {
            TransactionType type = types[i % 3];
            BigDecimal amount = pkr(1000 + ThreadLocalRandom.current().nextInt(49000));
            LocalDateTime date = LocalDateTime.now().minusDays(count - i);

            Transaction.TransactionBuilder txb = Transaction.builder()
                    .referenceNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .account(primary).transactionType(type).amount(amount)
                    .currency("PKR").status(TransactionStatus.COMPLETED)
                    .description(type.name() + " — seed data")
                    .createdAt(date);

            if (type == TransactionType.DEPOSIT) {
                balance = balance.add(amount);
            } else if (type == TransactionType.WITHDRAWAL) {
                if (balance.compareTo(amount) < 0) amount = balance.multiply(new BigDecimal("0.1"));
                balance = balance.subtract(amount);
                txb.amount(amount);
            } else {
                if (balance.compareTo(amount) < 0) amount = balance.multiply(new BigDecimal("0.1"));
                balance = balance.subtract(amount);
                txb.amount(amount);
                txb.sourceAccount(primary).destinationAccount(secondary);
            }
            txb.balanceAfter(balance);

            transactionRepository.save(txb.build());
            txCount++;
        }
        primary.setBalance(balance);
        accountRepository.save(primary);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper: Loans + Repayment Schedule
    // ═══════════════════════════════════════════════════════════════════

    private void seedLoan(User user, Account account, LoanStatus status,
                          BigDecimal amount, BigDecimal rate, int months) {
        BigDecimal monthlyRate = rate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = amount.multiply(monthlyRate)
                .divide(BigDecimal.ONE.subtract(
                        BigDecimal.ONE.add(monthlyRate).pow(-months, new java.math.MathContext(10))
                ), 4, RoundingMode.HALF_UP);

        Loan loan = loanRepository.save(Loan.builder()
                .user(user).account(account).loanType(LoanType.PERSONAL)
                .amount(amount).interestRate(rate).termMonths(months)
                .monthlyPayment(monthlyPayment).outstandingBalance(amount)
                .status(status).purpose("Seed data loan")
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(months - 2))
                .build());
        loanCount++;

        // Repayment schedule
        BigDecimal outstanding = amount;
        for (int m = 1; m <= months; m++) {
            BigDecimal interest = outstanding.multiply(monthlyRate).setScale(4, RoundingMode.HALF_UP);
            BigDecimal principal = monthlyPayment.subtract(interest);
            outstanding = outstanding.subtract(principal);
            PaymentStatus pStatus = m <= 2 ? PaymentStatus.COMPLETED : PaymentStatus.PENDING;

            loanPaymentRepository.save(LoanPayment.builder()
                    .loan(loan).amount(monthlyPayment)
                    .principal(principal).interest(interest)
                    .paymentDate(LocalDate.now().minusMonths(2).plusMonths(m))
                    .dueDate(LocalDate.now().minusMonths(2).plusMonths(m))
                    .status(pStatus).build());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper: KYC Documents
    // ═══════════════════════════════════════════════════════════════════

    private void seedKyc(User user) {
        kycDocumentRepository.save(KycDocument.builder()
                .user(user).documentType(DocumentType.ID_CARD)
                .documentUrl("/uploads/kyc/" + user.getId() + "_cnic.pdf")
                .documentNumber("35201-" + ThreadLocalRandom.current().nextInt(1000000, 9999999) + "-1")
                .status(KycStatus.PENDING)
                .expiryDate(LocalDate.now().plusYears(5)).build());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper: Support Tickets
    // ═══════════════════════════════════════════════════════════════════

    private void seedTicket(User user, String subject, TicketCategory category) {
        supportTicketRepository.save(SupportTicket.builder()
                .user(user).subject(subject).category(category)
                .priority(TicketPriority.MEDIUM).status(TicketStatus.OPEN).build());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper: Invoices
    // ═══════════════════════════════════════════════════════════════════

    private void seedInvoices(Business biz, int count) {
        InvoiceStatus[] statuses = {InvoiceStatus.PAID, InvoiceStatus.SENT, InvoiceStatus.OVERDUE,
                InvoiceStatus.PAID, InvoiceStatus.SENT};
        String[] clientNames = {"ABC Trading", "Metro Supplies", "City Motors", "Global Exports", "Star Enterprises"};

        for (int i = 0; i < count; i++) {
            BigDecimal subtotal = pkr(20000 + ThreadLocalRandom.current().nextInt(180000));
            BigDecimal tax = subtotal.multiply(new BigDecimal("0.17")).setScale(4, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(tax);

            invoiceRepository.save(Invoice.builder()
                    .business(biz)
                    .invoiceNumber("INV-" + biz.getId() + "-" + String.format("%04d", i + 1))
                    .customerName(clientNames[i])
                    .customerEmail(clientNames[i].toLowerCase().replace(" ", "") + "@mail.com")
                    .dueDate(LocalDate.now().plusDays(30 - i * 15))
                    .subtotal(subtotal).taxAmount(tax).totalAmount(total)
                    .status(statuses[i])
                    .notes("Invoice for services rendered").build());
            invoiceCount++;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper: Payroll Runs
    // ═══════════════════════════════════════════════════════════════════

    private void seedPayroll(Business biz, User creator, List<User> employees, int runCount) {
        for (int r = 0; r < runCount; r++) {
            LocalDate periodStart = LocalDate.now().minusMonths(r + 1).withDayOfMonth(1);
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);

            List<PayrollItem> items = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            Payroll payroll = Payroll.builder()
                    .business(biz).createdByUser(creator)
                    .payPeriodStart(periodStart).payPeriodEnd(periodEnd)
                    .status(PayrollStatus.PROCESSED)
                    .processedAt(LocalDateTime.now().minusMonths(r))
                    .items(items).build();

            for (User emp : employees) {
                BigDecimal salary = pkr(45000 + ThreadLocalRandom.current().nextInt(55000));
                total = total.add(salary);
                items.add(PayrollItem.builder()
                        .payroll(payroll)
                        .employeeName(emp.getFirstName() + " " + emp.getLastName())
                        .employeeAccount(emp.getEmail())
                        .amount(salary).status(PaymentStatus.COMPLETED).build());
            }
            payroll.setTotalAmount(total);
            payrollRepository.save(payroll);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Audit Logs — 20 entries
    // ═══════════════════════════════════════════════════════════════════

    private void seedAuditLogs(List<User> admins, List<User> allUsers) {
        String[][] actions = {
                {"USER_LOGIN", "User", null},
                {"USER_REGISTER", "User", null},
                {"STATUS_CHANGE", "User", "ACTIVE → SUSPENDED"},
                {"LOAN_APPROVED", "Loan", "Status: PENDING → ACTIVE"},
                {"LOAN_REJECTED", "Loan", "Status: PENDING → REJECTED"},
                {"ACCOUNT_CREATED", "Account", null},
                {"TRANSACTION_FLAGGED", "Transaction", "Status: COMPLETED → FLAGGED"},
                {"KYC_REVIEWED", "KycDocument", "Status: PENDING → APPROVED"},
                {"TICKET_ASSIGNED", "SupportTicket", null},
                {"USER_ROLE_CHANGED", "User", "CUSTOMER → BUSINESS"},
                {"ACCOUNT_CLOSED", "Account", "Status: ACTIVE → CLOSED"},
                {"PAYROLL_PROCESSED", "Payroll", null},
                {"INVOICE_CREATED", "Invoice", null},
                {"PASSWORD_RESET", "User", null},
                {"DOCUMENT_UPLOADED", "Document", null},
                {"TRANSFER_COMPLETED", "Transaction", null},
                {"RATE_LIMIT_TRIGGERED", "Auth", null},
                {"BULK_TRANSACTION", "Transaction", null},
                {"ALERT_CREATED", "Alert", null},
                {"SETTINGS_UPDATED", "System", null}
        };

        for (int i = 0; i < 20; i++) {
            User actor = admins.get(i % admins.size());
            String[] a = actions[i];
            auditLogRepository.save(AuditLog.builder()
                    .user(actor).action(a[0]).entityType(a[1])
                    .entityId((long) (i + 1))
                    .oldValue(a[2] != null ? a[2].split(" → ")[0] : null)
                    .newValue(a[2] != null && a[2].contains("→") ? a[2].split(" → ")[1] : null)
                    .ipAddress("192.168.1." + (10 + i))
                    .userAgent("Mozilla/5.0 VIRTBANK-Seeder").build());
            auditCount++;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Notifications — 5 per user
    // ═══════════════════════════════════════════════════════════════════

    private void seedNotifications(List<User> users) {
        String[][] notifs = {
                {"Welcome to VIRTBANK!", "Your account has been created successfully. Explore your dashboard to get started.", "ACCOUNT"},
                {"New Security Update", "We've enhanced our security features. Please review your security settings.", "SYSTEM"},
                {"Monthly Statement Ready", "Your account statement for the previous month is now available for download.", "ACCOUNT"},
                {"Transfer Received", "You have received a transfer of PKR 25,000 from another VIRTBANK account.", "TRANSACTION"},
                {"Loan EMI Reminder", "Your next loan instalment of PKR 18,500 is due in 3 days.", "LOAN"}
        };

        for (User user : users) {
            for (int n = 0; n < 5; n++) {
                notificationRepository.save(Notification.builder()
                        .user(user)
                        .notificationType(NotificationType.valueOf(notifs[n][2]))
                        .subject(notifs[n][0])
                        .message(notifs[n][1])
                        .isRead(false).build());
                notifCount++;
            }
        }
    }

    // ── Utility ──────────────────────────────────────────────────────

    private BigDecimal pkr(double amount) {
        return new BigDecimal(amount).setScale(4, RoundingMode.HALF_UP);
    }
}
