package com.flavientech;

import com.fazecast.jSerialComm.*;
import java.nio.ByteBuffer;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArduinoSerial {

    private SerialPort comPort;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static final byte PREAMBLE_EXPECTED = (byte) 0b11100001;
    private static final byte PREAMBLE_STOP = (byte) 0b10100001;
    private FileOutputStream fileOutputStream = null;
    private String outputFilePath = "request.ogg";
    private int expectedFrameNumber = 0; // Ajouté pour suivre la séquence des trames

    public ArduinoSerial(String comArduino) {
        comPort = SerialPort.getCommPort(comArduino);
        comPort.setBaudRate(9600);
        comPort.openPort();
        
        // Vider le buffer initial
        while (comPort.bytesAvailable() > 0) {
            comPort.readBytes(new byte[comPort.bytesAvailable()], comPort.bytesAvailable());
        }
        
        // Attendre que l'Arduino soit prêt
        try {
            Thread.sleep(2000); // attendre 2 secondes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        comPort.addDataListener(new MessageListener());
    }

    private class MessageListener implements SerialPortDataListener {

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] newData = new byte[comPort.bytesAvailable()];
            int numRead = comPort.readBytes(newData, newData.length);
            buffer.put(newData, 0, numRead);
            processBuffer();
        }
    }

    private void processBuffer() {
        buffer.flip();
        while (buffer.remaining() >= 5) { // Préambule + en-tête
            buffer.mark();
            byte preamble = buffer.get();
            if (preamble == PREAMBLE_EXPECTED) {
                if (buffer.remaining() < 4) {
                    buffer.reset();
                    break;
                }
                int frameNumber = ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
                int dataLength = ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
    
                // Ajouter vérification de la longueur des données
                if (dataLength > 57) {
                    System.out.println("Avertissement : Longueur des données trop grande : " + dataLength);
                    buffer.position(buffer.position() + dataLength); // Ignorer les données incorrectes
                    continue;
                }
    
                if (buffer.remaining() < dataLength) {
                    buffer.reset();
                    break;
                }
    
                byte[] data = new byte[dataLength];
                buffer.get(data, 0, dataLength);
                processMessage(preamble, frameNumber, dataLength, data);
            } else if (preamble == PREAMBLE_STOP) {
                System.out.println("Transmission terminée.");
                closeFile();
            } else {
                //System.out.println("Avertissement : Préambule incorrect : " + Integer.toBinaryString(preamble & 0xFF));
            }
        }
        buffer.compact();
    }

    private void processMessage(byte preamble, int frameNumber, int dataLength, byte[] data) {
        if (preamble == PREAMBLE_EXPECTED) {
            // Vérifier la séquence des numéros de trame
            if (frameNumber != expectedFrameNumber) {
                System.out.println("Avertissement : Numéro de trame inattendu. Attendu " + expectedFrameNumber + " mais reçu " + frameNumber);
                expectedFrameNumber = frameNumber + 1; // Synchronisation
            } else {
                expectedFrameNumber++;
            }

            // Vérifier la longueur des données
            if (dataLength > 57) {
                System.out.println("Avertissement : Longueur des données dépasse la limite de 57 octets. Longueur reçue : " + dataLength);
                return;
            }

            try {
                if (fileOutputStream == null) {
                    fileOutputStream = new FileOutputStream(outputFilePath);
                    System.out.println("Fichier " + outputFilePath + " ouvert pour l'écriture.");
                }
                fileOutputStream.write(data);
                System.out.println("Frame AUDIO Number : " + frameNumber + ", Data Length: " + dataLength);
            } catch (IOException e) {
                System.out.println("Erreur lors de l'écriture dans le fichier : " + e.getMessage());
            }
        }
    }

    private void closeFile() {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
                System.out.println("Fichier " + outputFilePath + " fermé.");
                fileOutputStream = null;
            } catch (IOException e) {
                System.out.println("Erreur lors de la fermeture du fichier : " + e.getMessage());
            }
        }
    }
}