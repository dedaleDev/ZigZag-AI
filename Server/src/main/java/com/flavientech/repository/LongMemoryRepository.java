package com.flavientech.repository;

import com.flavientech.entity.LongMemory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LongMemoryRepository extends JpaRepository<LongMemory, Long> {
    List<LongMemory> findByUserId(Long userId);
    List<LongMemory> findByUsername(String username);
}