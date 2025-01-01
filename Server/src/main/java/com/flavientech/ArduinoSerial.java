package com.flavientech;
import com.fazecast.jSerialComm.SerialPort;

import java.util.Scanner;

public class ArduinoSerial {

    private String portName;
    private SerialPort serialPort;

    public int ArduinoSerial(String portName) {
        this.portName = portName;
        this.serialPort = SerialPort.getCommPort(portName);
        this.serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        if (!serialPort.openPort()) {
            System.err.println("\u001B[31m ❌ Impossible d'ouvrir le port série : " + portName + "\n Communication avec l'Arduino impossible.\u001B[0m");
            return -1;
        }

        System.out.println("\u001B[32m ✅ Port série ouvert avec succès : " + portName + "\u001B[0m");

        // Écouter les messages reçus depuis l'Arduino
        new Thread(() -> {
            Scanner scanner = new Scanner(serialPort.getInputStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println("Reçu depuis Arduino : " + line);
            }
            scanner.close();
        }).start();

        return 0;
    }

    public void send(String message){
        try (Scanner userInput = new Scanner(message)) {
                message = userInput.nextLine();
                serialPort.getOutputStream().write((message + "\n").getBytes());
                serialPort.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        serialPort.closePort();
    }
}