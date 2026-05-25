package com.finance.manager.entity;

import com.finance.manager.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a transaction category.
 * Can be a default system category or a user-defined custom category.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /**
     * True if this is a user-created custom category; false if it's a system default.
     */
    @Column(nullable = false)
    private boolean isCustom;

    /**
     * The user who owns this custom category. Null for default categories.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
