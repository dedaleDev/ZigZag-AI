package com.flavientech.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.flavientech.entity.FlashMemory;
import com.flavientech.entity.LongMemory;
import com.flavientech.service.MemoryService;

@Controller
public class MemoryController {

    @Autowired
    private MemoryService memoryService;

    public void refreshFlashMemory(String newMemory) {
        memoryService.refreshFlashMemory(newMemory);
    }

    /**
     * Refresh long memory and clean answer take the full data, extract : @<username>, <summary>@, and save the summary in the long memory
     * @param fullData
     * @return the full data without the summary
     */
    public String refreshLongMemoryAndCleanAnswer(String fullData) {
        int startIndex = fullData.indexOf('@'); //first @
        int endIndex = fullData.indexOf('@', startIndex + 1) ; //second @

        if (startIndex != -1 && endIndex != -1) {  
            memoryService.refreshLongMemory(fullData.substring(startIndex + 1, endIndex).split(",")[1], fullData.substring(startIndex + 1, endIndex).split(",")[0]);
            fullData = fullData.substring(0, startIndex) + fullData.substring(endIndex + 1);//remove the summary
        }
        return fullData;
    }

    public void addFlashMemory(String request, String answer) {
        memoryService.createFlashMemory(request, answer);
    }

    public void addLongMemory(Long userId, String summary) {
        memoryService.createLongMemory(userId, summary);
    }

    public List<LongMemory> getLongMemory(String username) {
        return memoryService.getLongMemory(username);
    }

    public List<FlashMemory> getFlashMemory() {
        return memoryService.getFlashMemory();
    }
}