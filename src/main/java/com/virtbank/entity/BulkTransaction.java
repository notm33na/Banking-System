package com.virtbank.entity;

import com.virtbank.entity.enums.BulkTransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bulk_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"business", "initiatedByUser", "items"})
@EqualsAndHashCode(exclude = {"business", "initiatedByUser", "items"})
public class BulkTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by", nullable = false)
    private User initiatedByUser;

    @Column(length = 500)
    private String description;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "total_count")
    private Integer totalCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BulkTransactionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // ── Relationships ────────────────────────────────────────────────

    @OneToMany(mappedBy = "bulkTransaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<BulkTransactionItem> items = new ArrayList<>();
}
