package com.example.voicechanger.repository;

import com.example.voicechanger.entity.VoiceUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceUserMappingRepository extends JpaRepository<VoiceUserMapping, Long> {

    List<VoiceUserMapping> findByIdUser(Long idUser);

    Optional<VoiceUserMapping> findByIdUserAndIdVoiceType(Long idUser, Long idVoiceType);

    boolean existsByIdUserAndIdVoiceType(Long idUser, Long idVoiceType);

    List<VoiceUserMapping> findByIdUserAndIsPurchased(Long idUser, Boolean isPurchased);

    // Find active voice types for user (either no trial expiry or trial not yet expired)
    @Query("SELECT vum FROM VoiceUserMapping vum WHERE vum.idUser = :idUser " +
           "AND (vum.trialExpiryDate IS NULL OR vum.trialExpiryDate > :currentTime)")
    List<VoiceUserMapping> findActiveByIdUser(@Param("idUser") Long idUser, @Param("currentTime") LocalDateTime currentTime);

    // Find expired trials for user
    @Query("SELECT vum FROM VoiceUserMapping vum WHERE vum.idUser = :idUser " +
           "AND vum.trialExpiryDate IS NOT NULL AND vum.trialExpiryDate <= :currentTime " +
           "AND vum.isPurchased = false")
    List<VoiceUserMapping> findExpiredTrialsByIdUser(@Param("idUser") Long idUser, @Param("currentTime") LocalDateTime currentTime);

    // Find all mappings with user and voiceType eagerly fetched
    @Query("SELECT vum FROM VoiceUserMapping vum " +
           "LEFT JOIN FETCH vum.user " +
           "LEFT JOIN FETCH vum.voiceType")
    List<VoiceUserMapping> findAllWithUserAndVoiceType();
}
