package com.flavientech;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ArduinoSerial {

    private final String serialPortName;
    private final int baudRate = 500000; // Doit correspondre à celui défini sur l'Arduino
    private final String outputFileName = pathChecker.getCachesDir() + "RECEIVED.ogg"; // Nom du fichier de sortie

    private SerialPort serialPort;
    private int previousFrameNumber = -1; // Dernier numéro de trame reçu
    private long startTime = 0;
    private AudioFileListener audioFileListener;
    private int frameNumber = 0;
    private int frameNumberSend = 0;
    private static final int CHUNK_SIZE = 57;

    public ArduinoSerial(String serialPortName) {
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
        frameNumberSend = (frameNumberSend + 1) & 0xFFFF;
        System.out.println("Trame envoyée : " + frameNumberSend + ", taille : " + length + " octets");
        //Thread.sleep(10); 
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

    public void setAudioFileListener(AudioFileListener listener) {
        this.audioFileListener = listener;
    }

    public void start() {
        try {
            // Ouvrir le port série
            serialPort = SerialPort.getCommPort(serialPortName);
            serialPort.setBaudRate(baudRate);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

            if (!serialPort.openPort()) {
                System.err.println("Erreur : Impossible d'ouvrir le port série.");
                return;
            }

            System.out.println("Port série ouvert avec succès : " + serialPortName);

            // Ajouter un écouteur pour les données entrantes
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        processIncomingData();
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration du port série : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void processIncomingData() {
        try (FileOutputStream outputStream = new FileOutputStream(outputFileName, (previousFrameNumber != -1))) {
            while (serialPort.bytesAvailable() > 0) {
                // Lire le préambule (1 octet)
                byte[] preamble = new byte[1];
                serialPort.readBytes(preamble, 1);
    
                if (preamble[0] == (byte) 0xE1) { // Préambule attendu : 1110 0001 -> FICHIER AUDIO OGG
                    // Réinitialiser le fichier si c'est la première trame
                    if (previousFrameNumber == -1) {
                        startTime = System.currentTimeMillis();
                        // Ouvrir le fichier en mode écrasement (sans append)
                        new FileOutputStream(outputFileName, false).close(); // Réinitialisation du fichier
                    }
    
                    // Lire l'en-tête (4 octets)
                    byte[] header = new byte[4];
                    serialPort.readBytes(header, 4);
    
                    if (header.length < 4) {
                        System.err.println("Erreur : En-tête incomplet.");
                        continue;
                    }
    
                    // Extraire le numéro de trame et la longueur des données
                    int frameNumber = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);
                    int dataLength = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
    
                    // Vérifier la cohérence du numéro de trame
                    if (previousFrameNumber != -1 && frameNumber != previousFrameNumber + 1) {
                        System.err.println("Erreur : Trame attendue " + (previousFrameNumber + 1) +
                                ", reçue " + frameNumber + ". Temps écoulé : " + (System.currentTimeMillis() - startTime) + " ms");
                        break;
                    }
    
                    // Lire les données
                    byte[] data = new byte[dataLength];
                    int bytesRead = serialPort.readBytes(data, dataLength);
    
                    if (bytesRead != dataLength) {
                        System.err.println("Erreur : Longueur des données incorrecte.");
                        break;
                    }
    
                    // Lire le caractère de stop (1 octet)
                    byte[] stopByte = new byte[1];
                    serialPort.readBytes(stopByte, 1);
    
                    if (stopByte[0] != (byte) 0xAE) { // Stop attendu : 1010 1110
                        System.err.println("Erreur : Caractère de fin de trame incorrect.");
                        break;
                    }
    
                    // Écrire les données dans le fichier de sortie
                    outputStream.write(data);
                    System.out.println("Trame " + frameNumber + " reçue, taille : " + dataLength + " octets. Temps écoulé : " +
                            (System.currentTimeMillis() - startTime) + " ms");
    
                    // Mettre à jour le numéro de trame précédent
                    previousFrameNumber = frameNumber;
                } else if (preamble[0] == (byte) 0xA1) { // Préambule attendu : 1010 0001 -> FIN DE FICHIER AUDIO
                    byte[] header = new byte[4];
                    serialPort.readBytes(header, 4);
                    if (header.length < 4) {
                        System.err.println("Erreur : En-tête incomplet.");
                        continue;
                    }
                    // Extraire le numéro de trame et la longueur des données
                    int frameNumber = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);
                    int dataLength = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
    
                    // Vérifier la cohérence du numéro de trame
                    if (previousFrameNumber != -1 && frameNumber != previousFrameNumber + 1) {
                        System.err.println("Erreur : Trame attendue " + (previousFrameNumber + 1) +
                                ", reçue " + frameNumber + ". Temps écoulé : " + (System.currentTimeMillis() - startTime) + " ms");
                        break;
                    }
    
                    // Vérifier la longueur des données
                    if (dataLength != 0) {
                        System.err.println("Erreur : Longueur des données incorrecte.");
                        break;
                    }
    
                    // Lire le caractère de stop (1 octet)
                    byte[] stopByte = new byte[1];
                    serialPort.readBytes(stopByte, 1);
                    if (stopByte[0] != (byte) 0xAE) { // Stop attendu : 1010 1110
                        System.err.println("Erreur : Caractère de fin de trame incorrect.");
                        break;
                    }
    
                    // Écrire les données dans le fichier de sortie
                    System.out.println("Fin du fichier audio reçue. Temps écoulé : " + (System.currentTimeMillis() - startTime) + " ms");
    
                    // Réinitialiser les variables
                    previousFrameNumber = -1;

                    // Notifier le listener
                    if (audioFileListener != null) {
                        audioFileListener.onAudioFileReceived(outputFileName);
                    }
                } else if (preamble[0] == (byte) 0xE2) { // Préambule attendu : 1110 0010 -> NOUVEAU UTILISATEUR
                    System.out.println("Nouveau utilisateur 0");
                    byte[] header = new byte[4];
                    serialPort.readBytes(header, 4);
                    if (header.length < 4) {
                        System.err.println("Erreur : En-tête incomplet.");
                        continue;
                    }
                    int dataLength = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
                    byte[] data = new byte[dataLength];
                    serialPort.readBytes(data, dataLength); 

                    if (data[0] == (byte) 0x01) {
                        soundPlayer sound = new soundPlayer(pathChecker.checkPath("userCreation1.mp3"));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Port série fermé.");
        }
    }
}