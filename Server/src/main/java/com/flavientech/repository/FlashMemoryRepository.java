package com.flavientech.repository;

import com.flavientech.entity.FlashMemory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashMemoryRepository extends JpaRepository<FlashMemory, Long> {
    List<FlashMemory> findByUserId(Long userId);
}