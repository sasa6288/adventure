package com.example.adventure.repository;

import com.example.adventure.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    boolean existsByUserIdAndCheckInDate(Long userId, LocalDate date);
    Optional<CheckIn> findTopByUserIdOrderByCheckInDateDesc(Long userId);
}
