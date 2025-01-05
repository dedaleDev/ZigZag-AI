package com.flavientech.repository;

import com.flavientech.entity.FlashMemory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashMemoryRepository extends JpaRepository<FlashMemory, Long> {
}