package com.flavientech.repository;

import com.flavientech.entity.LongMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LongMemoryRepository extends JpaRepository<LongMemory, Long> {
    List<LongMemory> findByUserId(Long userId);
    @Query("SELECT lm FROM LongMemory lm WHERE lm.user.username = :username")
    List<LongMemory> findByUserUsername(@Param("username") String username);
}