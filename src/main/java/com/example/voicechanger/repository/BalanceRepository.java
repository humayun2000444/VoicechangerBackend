package com.example.voicechanger.repository;

import com.example.voicechanger.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findByIdUser(Long idUser);
    boolean existsByIdUser(Long idUser);
}
