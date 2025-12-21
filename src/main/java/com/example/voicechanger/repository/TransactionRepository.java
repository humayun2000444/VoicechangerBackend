package com.example.voicechanger.repository;

import com.example.voicechanger.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTnxId(String tnxId);
    boolean existsByTnxId(String tnxId);
    List<Transaction> findByIdUser(Long idUser);
    List<Transaction> findByStatus(String status);
    List<Transaction> findByIdUserAndStatus(Long idUser, String status);
}
