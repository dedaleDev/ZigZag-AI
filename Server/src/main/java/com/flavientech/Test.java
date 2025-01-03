package com.flavientech;

import com.fazecast.jSerialComm.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class Test {

    private final String serialPortName;
    private final int baudRate = 115200; // Doit correspondre à celui défini sur l'Arduino
    private SerialPort serialPort;
    private int frameNumberSend = 0;
    private static final int CHUNK_SIZE = 57;

    public Test(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    private void sendFrame(byte preamble, byte[] data, int length) throws IOException, InterruptedException {
        if (length > CHUNK_SIZE) {
            throw new IllegalArgumentException("La taille des données dépasse la limite de 57 octets.");
        }
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(preamble);
        frame.write((frameNumberSend >> 8) & 0xFF);
        frame.write(frameNumberSend & 0xFF);
        frame.write((length >> 8) & 0xFF);
        frame.write(length & 0xFF);
        if (length > 0 && data != null) {
            frame.write(data);
        }
        frame.write(0xAE);
        byte[] frameBytes = frame.toByteArray();
        serialPort.writeBytes(frameBytes, frameBytes.length);
        
        // Ajouter un délai après l'envoi
        Thread.sleep(10); // Ajuste cette valeur en fonction des performances
        frameNumberSend = (frameNumberSend + 1) & 0xFFFF;
    
        System.out.println("Trame envoyée n° " + frameNumberSend);
    }

    public void sendAudioFileToArduino(String audioFilePath) {
        this.frameNumberSend = 0;
        System.out.println("Sending audio file to Arduino...");
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            System.err.println("Erreur : Le fichier audio spécifié n'existe pas.");
            return;
        }
        try (FileInputStream fis = new FileInputStream(audioFile)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            // Envoyer les chunks du fichier audio
            while ((bytesRead = fis.read(buffer)) != -1) {
                if (bytesRead > 0) {
                    byte[] dataToSend = Arrays.copyOf(buffer, bytesRead);
                    sendFrame((byte) 0xE4, dataToSend, bytesRead); 
                }
            }
            // Envoyer la trame de fin de fichier audio
            sendFrame((byte) 0xA1, new byte[0], 0);
            System.out.println("Fichier envoyé !");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi du fichier audio : " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
                    e.printStackTrace();
        }
    }

    public void start() {
        try {
            // Ouvrir le port série
            serialPort = SerialPort.getCommPort(serialPortName);
            serialPort.setBaudRate(baudRate);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            
            // Activer le contrôle de flux RTS/CTS
            serialPort.setFlowControl(SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED);

            if (!serialPort.openPort()) {
                System.err.println("Erreur : Impossible d'ouvrir le port série.");
                return;
            }

            System.out.println("Port série ouvert avec succès : " + serialPortName);
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration du port série : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Port série fermé.");
        }
    }

    public static void main(String[] args) {
        Test test = new Test("/dev/tty.usbmodem14301");
        test.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        test.sendAudioFileToArduino("Server/src/caches/request.wav");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        test.stop();
    }
}