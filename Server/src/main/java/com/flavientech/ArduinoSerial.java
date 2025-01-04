package com.flavientech;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArduinoSerial {

    private final String serialPortName;
    private final int baudRate = 500000; // Doit correspondre à celui défini sur l'Arduino
    private final String outputFileName = PathChecker.getCachesDir() + "RECEIVED.ogg"; // Nom du fichier de sortie

    private SerialPort serialPort;
    private int previousFrameNumber = -1; // Dernier numéro de trame reçu
    private long startTime = 0;
    private AudioFileListener audioFileListener;
    private int frameNumberSend = 0;
    private static final int CHUNK_SIZE = 57;


    private final String apiKeyPicoVoice;
    private short newUser = 0;
    private String nameUser = "";

    public ArduinoSerial(String serialPortName, String apiKeyPicoVoice) {
        this.serialPortName = serialPortName;
        this.apiKeyPicoVoice = apiKeyPicoVoice;
    }

    private void sendFrame(byte preamble, byte[] data, int length)  {
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
            try {
                frame.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        frame.write(0xAE);
        byte[] frameBytes = frame.toByteArray();
        serialPort.writeBytes(frameBytes, frameBytes.length);
        frameNumberSend = (frameNumberSend + 1) & 0xFFFF;
        System.out.println("Trame envoyée : " + frameNumberSend + ", taille : " + length + " octets");
        //Thread.sleep(10); 
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
            System.err.println("\u001B[31mErreur lors de la connection au port série : " + e.getMessage() + "\u001B[0m");
            System.err.println("\u001B[31mIndice de correction : Vérifiez les branchements et assurez-vous que le port série est correct et que l'Arduino est branché.\u001B[0m");
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
                    if (audioFileListener != null && this.newUser == 0) {
                        audioFileListener.onAudioFileReceived(outputFileName);
                    } else if (audioFileListener != null && this.newUser ==1) { //Suite procédure state 1 nouveau utilisateur, j'ai conscience que c'est pas très propre, mais je n'ai pas trouvé d'autre solution
                        SpeechToText recognizer = new SpeechToText(apiKeyPicoVoice);
                        new PythonController(PathChecker.checkPath("oggToWav.py")).runPythonScript(outputFileName, PathChecker.getCachesDir() + "nameUser.wav");
                        String result = recognizer.run(PathChecker.getCachesDir() + "nameUser.wav");
                        if (result == null || result.isEmpty() || result.split(" ").length < 1) {
                            new SoundPlayer(PathChecker.checkPath("userCreation3_Error.wav"));
                            this.newUser = 0;
                            this.nameUser = "";
                            return;
                        }
                        this.nameUser = result.split(" ")[0].trim();
                        System.out.println("Nom utilisateur : " + this.nameUser);
                        this.newUser = 2;
                        new SoundPlayer(PathChecker.checkPath("userCreation2.wav"));
                        sendFrame((byte) 0xE9, new byte[0],  0);
                    } else if (audioFileListener != null && this.newUser == 2) { //Suite procédure state 2 nouveau utilisateur
                        new PythonController(PathChecker.checkPath("oggToWav.py")).runPythonScript(outputFileName, PathChecker.getCachesDir() + "enroll.wav");
                        EagleController.runEnroll(apiKeyPicoVoice, this.nameUser);
                        new SoundPlayer(PathChecker.checkPath("userCreation4.wav"));
                        this.newUser = 0;
                        this.nameUser = "";
                    }
                } else if (preamble[0] == (byte) 0xE2) { // Préambule attendu : 1110 0010 -> NOUVEAU UTILISATEUR
                    System.out.println("Nouveau utilisateur State : 0");
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
                        new SoundPlayer(PathChecker.checkPath("userCreation1.wav"));
                        this.newUser = 1;
                        sendFrame((byte) 0xE8, new byte[0], 0);
                        System.out.println("Nouveau utilisateur State : 1");
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