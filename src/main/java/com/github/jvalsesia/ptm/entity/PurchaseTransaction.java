package com.github.jvalsesia.ptm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
public class PurchaseTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    @Column(nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Purchase amount is required")
    @Positive(message = "Purchase amount must be positive")
    @Digits(integer = 17, fraction = 2,
            message = "Purchase amount must be rounded to the nearest cent")
    private BigDecimal purchaseAmount;

    // Ensure the amount is always rounded to 2 decimal places
    public void setPurchaseAmount(BigDecimal purchaseAmount) {
        this.purchaseAmount = purchaseAmount != null
                ? purchaseAmount.setScale(2, RoundingMode.HALF_UP)
                : null;
    }

}
