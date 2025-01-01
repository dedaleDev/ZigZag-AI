package com.flavientech;

import com.fazecast.jSerialComm.*;
import java.nio.ByteBuffer;

public class ArduinoSerial {

    private SerialPort comPort;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static final byte PREAMBLE_EXPECTED = (byte) 0b11100001;

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
            Thread.sleep(1000); // attendre 2 secondes
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
            if (preamble != PREAMBLE_EXPECTED) {
                //System.out.println("Préambule incorrect : " + Integer.toBinaryString(preamble & 0xFF)); //ingnorer l'avertissement, cela indique simplement que l'arduino n'a pas encore envoyé de données
                continue; 
            }
            if (buffer.remaining() < 4) {
                buffer.reset();
                break;
            }
            int frameNumber = ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
            int dataLength = ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);

            if (buffer.remaining() < dataLength) {
                buffer.reset();
                break;
            }

            byte[] data = new byte[dataLength];
            buffer.get(data, 0, dataLength);
            processMessage(preamble, frameNumber, dataLength, data);
        }
        buffer.compact();
    }

    private void processMessage(byte preamble, int frameNumber, int dataLength, byte[] data) {
        System.out.println("Preamble : " + Integer.toBinaryString(preamble & 0xFF));
        System.out.println("Frame Number: " + frameNumber);
        System.out.println("Data Length: " + dataLength);
        System.out.println("Data: " + new String(data));
    }
}