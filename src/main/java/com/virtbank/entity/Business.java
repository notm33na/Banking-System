package com.virtbank.entity;

import com.virtbank.entity.enums.BusinessType;
import com.virtbank.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "businesses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"owner", "members"})
@EqualsAndHashCode(exclude = {"owner", "members"})
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "registration_number", unique = true, length = 100)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 30)
    private BusinessType businessType;

    @Column(length = 100)
    private String industry;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(length = 255)
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Relationships ────────────────────────────────────────────────

    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BusinessMember> members = new ArrayList<>();
}
