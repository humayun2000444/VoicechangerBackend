package com.example.voicechanger.repository;

import com.example.voicechanger.entity.VoiceMappingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoiceMappingHistoryRepository extends JpaRepository<VoiceMappingHistory, Long> {

    /**
     * Find all history records for a specific user
     */
    List<VoiceMappingHistory> findByIdUser(Long idUser);

    /**
     * Find all history records for a specific voice type
     */
    List<VoiceMappingHistory> findByIdVoiceType(Long idVoiceType);

    /**
     * Find history records by expiry reason
     */
    List<VoiceMappingHistory> findByExpiryReason(String expiryReason);

    /**
     * Find history records for a user within a date range
     */
    @Query("SELECT vmh FROM VoiceMappingHistory vmh WHERE vmh.idUser = :idUser " +
           "AND vmh.expiredAt BETWEEN :startDate AND :endDate")
    List<VoiceMappingHistory> findByUserAndDateRange(
            @Param("idUser") Long idUser,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all history records with user and voice type details
     */
    @Query("SELECT vmh FROM VoiceMappingHistory vmh " +
           "LEFT JOIN FETCH vmh.user " +
           "LEFT JOIN FETCH vmh.voiceType")
    List<VoiceMappingHistory> findAllWithDetails();

    /**
     * Find history records for a user with full details
     */
    @Query("SELECT vmh FROM VoiceMappingHistory vmh " +
           "LEFT JOIN FETCH vmh.user " +
           "LEFT JOIN FETCH vmh.voiceType " +
           "WHERE vmh.idUser = :idUser")
    List<VoiceMappingHistory> findByUserWithDetails(@Param("idUser") Long idUser);

    /**
     * Count total expired mappings by reason
     */
    @Query("SELECT vmh.expiryReason, COUNT(vmh) FROM VoiceMappingHistory vmh " +
           "GROUP BY vmh.expiryReason")
    List<Object[]> countByExpiryReason();

    /**
     * Count expired mappings for a user
     */
    Long countByIdUser(Long idUser);

    /**
     * Find recent expired mappings (last N days)
     */
    @Query("SELECT vmh FROM VoiceMappingHistory vmh " +
           "WHERE vmh.expiredAt >= :sinceDate " +
           "ORDER BY vmh.expiredAt DESC")
    List<VoiceMappingHistory> findRecentExpired(@Param("sinceDate") LocalDateTime sinceDate);
}
