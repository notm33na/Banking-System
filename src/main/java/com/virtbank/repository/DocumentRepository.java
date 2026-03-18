package com.virtbank.repository;

import com.virtbank.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserId(Long userId);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.documentType = :type")
    List<Document> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId ORDER BY d.createdAt DESC")
    List<Document> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
