package com.virtbank.repository;

import com.virtbank.entity.Investment;
import com.virtbank.entity.enums.InvestmentStatus;
import com.virtbank.entity.enums.InvestmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserId(Long userId);

    List<Investment> findByUserIdAndStatus(Long userId, InvestmentStatus status);

    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.investmentType = :type AND i.status = 'ACTIVE'")
    List<Investment> findActiveByUserIdAndType(@Param("userId") Long userId, @Param("type") InvestmentType type);

    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.status = 'ACTIVE' ORDER BY i.purchasedAt DESC")
    List<Investment> findActiveInvestmentsByUserIdOrderByDate(@Param("userId") Long userId);

    @Query("SELECT i FROM Investment i WHERE i.symbol = :symbol AND i.status = 'ACTIVE'")
    List<Investment> findActiveBySymbol(@Param("symbol") String symbol);
}
