package com.virtbank.repository;

import com.virtbank.entity.User;
import com.virtbank.entity.enums.UserStatus;
import com.virtbank.entity.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByUserType(UserType userType);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.userType = :type AND u.status = :status")
    List<User> findByUserTypeAndStatus(@Param("type") UserType type, @Param("status") UserStatus status);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.userType = :type")
    List<User> findUnverifiedUsersByType(@Param("type") UserType type);
}
