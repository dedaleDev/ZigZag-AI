package com.flavientech;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        // Renvoie la page "index.html" située dans le répertoire /templates
        return "index";
    }
    @GetMapping("/settings")
    public String settings() {
        // Renvoie la page "settings.html" située dans le répertoire /templates
        return "settings";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404() {
        // Renvoie la page "error.html" située dans le répertoire /templates
        return "error";
    }

    
    @PostMapping("/api/request")
    public ResponseEntity<Map<String, String>> receiveRequestJson(@RequestBody Map<String, String> requestJson) {
        String apiKeyOpenAi = null;
        apiKeyOpenAi = LoadConf.getApiKeyOpenAi();
        if (apiKeyOpenAi == null) {
            //send error message
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "OpenAI API key not found");
            return ResponseEntity.ok(response);
        }
        // Traitez les données JSON ici
        System.out.println("Données reçues : " + requestJson);
        
        // Envoyer la requête à ZigZag : 
        String openAIResponse = InteractWithOpenAI.run(apiKeyOpenAi, requestJson.get("name").toString(), requestJson.get("message").toString());
        // Créez une réponse JSON
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", openAIResponse);

        return ResponseEntity.ok(response);
    }

    //api get users 
    @GetMapping("/api/getUsers")
    public ResponseEntity<Map<String, Object>> getUsers() {
        // Créez une réponse JSON
        List<String> users = EagleController.getUsersVoiceList();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("users", users);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/getVoiceList")
    public ResponseEntity<Map<String, Object>> getVoicesList() {
        List<String> voiceList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("models/eaglesVoices.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                voiceList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to read voices list");
            return ResponseEntity.status(500).body(response);
        }
        // Créez une réponse JSON
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("voices", voiceList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/getArduinoCom")
    public ResponseEntity<Map<String, String>> getArduinoCom() {
        // Créez une réponse JSON
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("comArduino", LoadConf.getComArduino());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/deleteUser")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestBody Map<String, String> requestJson) {
        if (requestJson.get("name") == null || requestJson.get("name").contains("/") || requestJson.get("name").contains("\\")) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Name not found or incorrect");
            return ResponseEntity.ok(response);
        }
        EagleController.deleteUser(requestJson.get("name").toString());
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User deleted");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/selectVoiceAI")
    public ResponseEntity<Map<String, String>> selectVoiceAI(@RequestBody Map<String, String> requestJson) {
        if (requestJson.get("voice") == null || requestJson.get("voice").split(" ").length > 1) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Voice not found or incorrect");
            return ResponseEntity.ok(response);
        }
        List<String> voiceList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("models/eaglesVoices.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                voiceList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Voice name incorrect");
            return ResponseEntity.ok(response);
        }
        if (!voiceList.contains(requestJson.get("voice"))) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Voice not found or incorrect");
            return ResponseEntity.ok(response);
        }
        LoadConf.writeEagleVoice(requestJson.get("voice"));

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        System.out.println("Voice changed to " + requestJson.get("voice"));
        response.put("message", "voice changed");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/selectArduinoCom")
    public ResponseEntity<Map<String, String>> selectArduinoCom(@RequestBody Map<String, String> requestJson) {
        if (requestJson.get("comArduino") == null || requestJson.get("comArduino").split(" ").length > 1) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Com not found or incorrect");
            return ResponseEntity.ok(response);
        }
        LoadConf.writeArduinoCom(requestJson.get("comArduino"));

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        System.out.println("Com changed to " + requestJson.get("comArduino"));
        response.put("message", "com changed");
        return ResponseEntity.ok(response);
    }


}
