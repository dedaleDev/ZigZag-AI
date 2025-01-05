package com.flavientech.service;

import com.flavientech.entity.FlashMemory;
import com.flavientech.entity.LongMemory;
import com.flavientech.repository.FlashMemoryRepository;
import com.flavientech.repository.LongMemoryRepository;
import com.flavientech.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class MemoryService {

    private final LongMemoryRepository longMemoryRepository;
    private final FlashMemoryRepository flashMemoryRepository;
    private final UserRepository userRepository;

    public MemoryService(LongMemoryRepository longMemoryRepository, FlashMemoryRepository flashMemoryRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.longMemoryRepository = longMemoryRepository;
        this.flashMemoryRepository = flashMemoryRepository;
    }

    public void cleanLongMemory() {
        if (longMemoryRepository.count() > 40) {
            longMemoryRepository.deleteAll();
        }
    }

    public void cleanFlashMemory() {
        List<FlashMemory> flashMemories = flashMemoryRepository.findAll();
        if (flashMemories.size() > 2 || flashMemories.stream().mapToInt(f -> f.getRequest().length() + f.getAnswer().length()).sum() > 3000) {
            flashMemoryRepository.deleteAll();
        }
    }

    public void refreshFlashMemory(String newMemory) {
        cleanFlashMemory();
        FlashMemory flashMemory = new FlashMemory();
        flashMemory.setRequest(newMemory);
        flashMemory.setAnswer(newMemory);
        flashMemoryRepository.save(flashMemory);
    }

    public void refreshLongMemory(String newMemory, String username) {
        cleanLongMemory();
        LongMemory longMemory = new LongMemory();
        longMemory.setSummary(newMemory);
        longMemory.setUser(userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found")));
        longMemoryRepository.save(longMemory);
    }

    public List<LongMemory> getLongMemoryByUserId(Long userId) {
        return longMemoryRepository.findByUserId(userId);
    }

    public List<FlashMemory> getFlashMemory() {
        return flashMemoryRepository.findAll();
    }

    public List<LongMemory> getLongMemory(String username) {
        return longMemoryRepository.findByUsername(username);
    }

    public LongMemory createLongMemory(Long userId, String summary) {
        LongMemory longMemory = new LongMemory();
        longMemory.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        longMemory.setDate(new Date(System.currentTimeMillis()));
        longMemory.setSummary(summary);
        return longMemoryRepository.save(longMemory);
    }
    
    public FlashMemory createFlashMemory(Long userId, String request, String answer) {
        FlashMemory flashMemory = new FlashMemory();
        flashMemory.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        flashMemory.setRequest(request);
        flashMemory.setAnswer(answer);
        return flashMemoryRepository.save(flashMemory);
    }
}