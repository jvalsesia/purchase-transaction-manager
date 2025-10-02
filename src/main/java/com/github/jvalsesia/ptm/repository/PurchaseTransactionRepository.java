package com.github.jvalsesia.ptm.repository;

import com.github.jvalsesia.ptm.entity.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, UUID> {
}
