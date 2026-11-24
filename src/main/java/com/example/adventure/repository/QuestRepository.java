package com.example.adventure.repository;

import com.example.adventure.model.Quest;
import com.example.adventure.model.QuestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    @Query("""
            SELECT q
            FROM Quest q
            WHERE (:status IS NULL OR q.status = :status)
              AND (
                  :keyword IS NULL
                  OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Quest> search(
            @Param("status") QuestStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT q FROM Quest q
            WHERE q.user.id = :userId
              AND (:status IS NULL OR q.status = :status)
              AND (:keyword IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Quest> searchByUserId(
            @Param("userId") Long userId,
            @Param("status") QuestStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Optional<Quest> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM Quest q WHERE q.id = :id AND q.user.id = :userId")
    Optional<Quest> findByIdAndUserIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    long countByUserIdAndStatus(Long userId, QuestStatus status);
}
