package com.example.voicechanger.repository;

import com.example.voicechanger.entity.VoicePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoicePurchaseRepository extends JpaRepository<VoicePurchase, Long> {

    List<VoicePurchase> findByIdUser(Long idUser);

    // Find purchases by user ordered by purchase date descending (latest first)
    List<VoicePurchase> findByIdUserOrderByPurchaseDateDesc(Long idUser);

    Optional<VoicePurchase> findByIdUserAndIdVoiceType(Long idUser, Long idVoiceType);

    boolean existsByIdUserAndIdVoiceType(Long idUser, Long idVoiceType);

    List<VoicePurchase> findByIdVoiceType(Long idVoiceType);

    // Find purchases by status
    List<VoicePurchase> findByStatus(String status);

    // Find purchases by user and status
    List<VoicePurchase> findByIdUserAndStatus(Long idUser, String status);

    // Find approved purchases for a user (to check if already purchased)
    boolean existsByIdUserAndIdVoiceTypeAndStatus(Long idUser, Long idVoiceType, String status);
}
