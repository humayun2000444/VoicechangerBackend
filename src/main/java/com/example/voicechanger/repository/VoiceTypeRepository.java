package com.example.voicechanger.repository;

import com.example.voicechanger.entity.VoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoiceTypeRepository extends JpaRepository<VoiceType, Long> {
    Optional<VoiceType> findByCode(String code);
    boolean existsByCode(String code);
}
