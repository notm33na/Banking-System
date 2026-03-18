package com.virtbank.repository;

import com.virtbank.entity.Account;
import com.virtbank.entity.enums.AccountStatus;
import com.virtbank.entity.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserId(Long userId);

    List<Account> findByUserIdAndStatus(Long userId, AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.accountType = :type AND a.status = :status")
    List<Account> findByTypeAndStatus(@Param("type") AccountType type, @Param("status") AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.balance < :threshold AND a.status = 'ACTIVE'")
    List<Account> findLowBalanceAccounts(@Param("threshold") BigDecimal threshold);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}
