package com.virtbank.repository;

import com.virtbank.entity.BusinessMember;
import com.virtbank.entity.enums.BusinessMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessMemberRepository extends JpaRepository<BusinessMember, Long> {

    List<BusinessMember> findByBusinessId(Long businessId);

    List<BusinessMember> findByUserId(Long userId);

    Optional<BusinessMember> findByBusinessIdAndUserId(Long businessId, Long userId);

    @Query("SELECT bm FROM BusinessMember bm WHERE bm.business.id = :businessId AND bm.role = :role")
    List<BusinessMember> findByBusinessIdAndRole(@Param("businessId") Long businessId, @Param("role") BusinessMemberRole role);

    @Query("SELECT COUNT(bm) FROM BusinessMember bm WHERE bm.business.id = :businessId AND bm.status = 'ACTIVE'")
    long countActiveMembers(@Param("businessId") Long businessId);
}
