package com.github.jvalsesia.ptm;

import com.github.jvalsesia.ptm.entity.PurchaseTransaction;
import com.github.jvalsesia.ptm.repository.PurchaseTransactionRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@DataJpaTest // Loads JPA context and H2 DB

public class PurchaseTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PurchaseTransactionRepository repository;

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private PurchaseTransaction createAndSaveTransaction(String description, LocalDate date, BigDecimal amount) {
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription(description);
        transaction.setTransactionDate(date);
        transaction.setPurchaseAmount(amount);
        return entityManager.persistAndFlush(transaction);
    }


    @Test
    void shouldSavePurchaseTransaction() {
        // Given data
        String description  = "Office Supplies";
        LocalDate date = LocalDate.of(2025, 10, 1);
        BigDecimal amount = new BigDecimal("150.75");

        // When save date
        PurchaseTransaction saved = createAndSaveTransaction(description, date, amount);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Office Supplies");
        assertThat(saved.getPurchaseAmount())
                .isEqualByComparingTo(new BigDecimal("150.75"));
    }

    @Test
    void shouldFindTransactionById() {

        // Given data
        String description  = "Equipment";
        LocalDate date = LocalDate.now();
        BigDecimal amount = new BigDecimal("301.84");

        // When save date
        PurchaseTransaction saved = createAndSaveTransaction(description, date, amount);
        UUID id = saved.getId();

        // When find data
        Optional<PurchaseTransaction> found = repository.findById(id);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Equipment");
    }


    @Test
    void shouldRejectNullDescription() {
        // Given data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription(null);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setPurchaseAmount(new BigDecimal("100.00"));

        // When validating
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("Description is required"));
    }

    @Test
    void shouldRejectBlankDescription() {
        // Given data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("   ");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setPurchaseAmount(new BigDecimal("100.00"));

        // When validating
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("Description is required"));
    }

    @Test
    void shouldRejectNullTransactionDate() {
        // Given bad data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Test");
        transaction.setTransactionDate(null);
        transaction.setPurchaseAmount(new BigDecimal("100.00"));

        // When validating
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("Transaction date is required"));
    }

    @Test
    void shouldRejectFutureTransactionDate() {
        // Given bad data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Future Transaction");
        transaction.setTransactionDate(LocalDate.now().plusDays(1));
        transaction.setPurchaseAmount(new BigDecimal("100.00"));

        // When validating
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("cannot be in the future"));
    }


    @Test
    void shouldAcceptPastTransactionDate() {
        // Given bad data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Past Transaction");
        transaction.setTransactionDate(LocalDate.now().minusDays(30));
        transaction.setPurchaseAmount(new BigDecimal("100.00"));

        // When validating
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isEmpty();
    }


    @Test
    void shouldRejectNullPurchaseAmount() {
        // Given bad data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Test");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setPurchaseAmount(null);

        // When validating
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("Purchase amount is required"));
    }

    @Test
    void shouldRejectZeroPurchaseAmount() {
        // Given bad data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Test");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setPurchaseAmount(BigDecimal.ZERO);

        // When validating
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRejectNegativePurchaseAmount() {
        // Given bad data
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setDescription("Test");
        transaction.setTransactionDate(LocalDate.now());
        transaction.setPurchaseAmount(new BigDecimal("-50.00"));

        // When validate
        Set<ConstraintViolation<PurchaseTransaction>> violations =
                validator.validate(transaction);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("must be positive"));
    }

    @Test
    void shouldRoundPurchaseAmountToNearestCent() {
        // Given odd data
        PurchaseTransaction tx1 = new PurchaseTransaction();
        tx1.setPurchaseAmount(new BigDecimal("100.126")); // Should round to 100.13

        PurchaseTransaction tx2 = new PurchaseTransaction();
        tx2.setPurchaseAmount(new BigDecimal("100.124")); // Should round to 100.12

        PurchaseTransaction tx3 = new PurchaseTransaction();
        tx3.setPurchaseAmount(new BigDecimal("100.125")); // Should round to 100.13

        // Then
        assertThat(tx1.getPurchaseAmount()).isEqualByComparingTo("100.13");
        assertThat(tx2.getPurchaseAmount()).isEqualByComparingTo("100.12");
        assertThat(tx3.getPurchaseAmount()).isEqualByComparingTo("100.13");
    }

    @Test
    void shouldStoreExactlyTwoDecimalPlaces() {
        // Given odd data
        String description  = "Office Supplies";
        LocalDate date = LocalDate.of(2025, 10, 1);
        BigDecimal amount = new BigDecimal("150.5"); // Input with 1 decimal

        PurchaseTransaction transaction = createAndSaveTransaction( description, date, amount );
        entityManager.clear();

        // When
        Optional<PurchaseTransaction> foundPurchase = repository.findById(transaction.getId());
        if(foundPurchase.isPresent()) {
            // Then
            assertThat(foundPurchase.get().getPurchaseAmount().scale()).isEqualTo(2);
            assertThat(foundPurchase.get().getPurchaseAmount()).isEqualByComparingTo("150.50");
        }

    }
}
