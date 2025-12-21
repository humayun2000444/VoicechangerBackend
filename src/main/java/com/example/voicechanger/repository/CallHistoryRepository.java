package com.example.voicechanger.repository;

import com.example.voicechanger.entity.CallHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CallHistoryRepository extends JpaRepository<CallHistory, Long> {

    Optional<CallHistory> findByUuid(String uuid);

    List<CallHistory> findByAparty(String aparty);

    List<CallHistory> findByIdUser(Long idUser);

    List<CallHistory> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CallHistory> findByStatus(String status);
}
