package com.flavientech;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.flavientech.entity.FlashMemory;
import com.flavientech.entity.LongMemory;
import com.flavientech.service.MemoryService;

public class MemoryController {

    @Autowired
    private static MemoryService memoryService;

    public static void refreshFlashMemory(String newMemory) {
        memoryService.refreshFlashMemory(newMemory);
    }


    /**
     * Refresh long memory and clean answer take the full data, extract : @<username>, <summary>@, and save the summary in the long memory
     * @param fullData
     * @return the full data without the summary
     */
    public static String refreshLongMemoryAndCleanAnswer(String fullData) {
        int startIndex = fullData.indexOf('@'); //first @
        int endIndex = fullData.indexOf('@', startIndex + 1) ; //second @

        if (startIndex != -1 && endIndex != -1) {  
            memoryService.refreshLongMemory(fullData.substring(startIndex + 1, endIndex).split(",")[1], fullData.substring(startIndex + 1, endIndex).split(",")[0]);
            fullData = fullData.substring(0, startIndex) + fullData.substring(endIndex + 1);//remove the summary
        }
        return fullData;
    }

    public static void addFlashMemory(String request, String answer) {
        memoryService.createFlashMemory(request, answer);
    }

    public static void addLongMemory(Long userId, String summary) {
        memoryService.createLongMemory(userId, summary);
    }

    public static List<LongMemory> getLongMemory(String username) {
        return memoryService.getLongMemory(username);
    }

    public static List<FlashMemory> getFlashMemory() {
        return memoryService.getFlashMemory();
    }
}