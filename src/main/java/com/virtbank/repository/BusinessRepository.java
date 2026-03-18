package com.virtbank.repository;

import com.virtbank.entity.Business;
import com.virtbank.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    List<Business> findByOwnerId(Long ownerId);

    Optional<Business> findByRegistrationNumber(String registrationNumber);

    List<Business> findByStatus(UserStatus status);

    @Query("SELECT b FROM Business b WHERE LOWER(b.businessName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Business> searchByName(@Param("name") String name);

    @Query("SELECT b FROM Business b WHERE b.industry = :industry AND b.status = 'ACTIVE'")
    List<Business> findActiveByIndustry(@Param("industry") String industry);
}
