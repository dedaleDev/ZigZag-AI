#include <Arduino.h>
#include <SPI.h>
#include <Adafruit_VS1053.h>
#include <SD.h>

#define SD_CS 9
#define MISO 12
#define MOSI 11
#define SCK 13
#define XCS 6
#define XRESET 8
#define XDSC 7
#define DREQ 2

#define RECBUFFSIZE 32
#define CHUNK_SIZE 57// Taille maximale des données dans une trame

Adafruit_VS1053_FilePlayer musicPlayer = Adafruit_VS1053_FilePlayer(XRESET, XCS, XDSC, DREQ, SD_CS);
File recording;
uint8_t recording_buffer[RECBUFFSIZE];

File oggFile;
uint16_t frameNumber = 0; // Numéro de trame

File receivedFile;
bool createNewUser = false;

uint16_t saveRecordedData(boolean isrecord) {
    // Fonction pour enregistrer les données sur la carte SD
    uint16_t written = 0;
    uint16_t wordsWaiting = musicPlayer.recordedWordsWaiting();
    while (wordsWaiting > 0) {
        uint16_t toRead = min(wordsWaiting, RECBUFFSIZE / 2);
        for (uint16_t addr = 0; addr < toRead * 2; addr += 2) {
            uint16_t t = musicPlayer.recordedReadWord();
            recording_buffer[addr] = t >> 8;
            recording_buffer[addr + 1] = t;
        }
        if (!recording.write(recording_buffer, toRead * 2)) {
            Serial.println("Couldn't write to file!");
            while (1);
        }
        written += toRead;
        wordsWaiting -= toRead;
    }
    if (!isrecord) {
        musicPlayer.sciRead(VS1053_SCI_AICTRL3);
        if (!(musicPlayer.sciRead(VS1053_SCI_AICTRL3) & (1 << 2))) {
            uint16_t lastWord = musicPlayer.recordedReadWord();
            recording.write(lastWord & 0xFF);
            written++;
        }
    }
    return written;
}

void sendFrame(uint8_t preamble, uint8_t *data, size_t length) {
    Serial.write(preamble);
    Serial.write((uint8_t)(frameNumber >> 8));    // Numéro de trame (MSB)
    Serial.write((uint8_t)(frameNumber & 0xFF)); // Numéro de trame (LSB)
    Serial.write((uint8_t)(length >> 8));        // Longueur des données (MSB)
    Serial.write((uint8_t)(length & 0xFF));      // Longueur des données (LSB)
    Serial.write(data, length);
    Serial.write(0b10101110); 
    frameNumber++;
}

void sendAudio(){
    while (true){
        if (oggFile.available()) {
            uint8_t buffer[CHUNK_SIZE];
            size_t bytesRead = oggFile.read(buffer, CHUNK_SIZE);
            sendFrame(0b11100001, buffer, bytesRead); 
        } else {
            uint8_t dummyData[0] = {};
            sendFrame(0b10100001, dummyData, 0);
            oggFile.close();
            break;
        }
    }
    Serial.println("Fichier envoyé !");
}

void setup() {
    Serial.begin(500000);
    Serial.println("Adafruit VS1053 Ogg record");

    if (!musicPlayer.begin()) {
        Serial.println("VS1053 not found");
        while (1);
    }

    musicPlayer.sineTest(0x44, 500);

    if (!SD.begin(SD_CS)) {
        Serial.println("SD failed, or not present");
        while (1);
    }
    Serial.println("SD OK!");

    musicPlayer.setVolume(10, 10);

    if (!musicPlayer.prepareRecordOgg("v44k1q05.img")) {
        Serial.println("Couldn't load plugin!");
        while (1);
    }
    pinMode(10, OUTPUT); // LED de contrôle
    pinMode(4, INPUT);   // Bouton poussoir AI
    pinMode(3, INPUT);   // Bouton poussoir user train
    Serial.println("Ready to record!");
}

uint8_t isRecording = false;


void loop() {
    digitalWrite(10, LOW);
    if (digitalRead(4) == HIGH) {
        Serial.println("Button pressed, starting recording...");
        digitalWrite(10, HIGH);
        isRecording = true;

        const char* filename = "RECORD.OGG";

        Serial.print("Recording to ");
        Serial.println(filename);

        // Supprimer le fichier existant
        if (SD.exists(filename)) {
            SD.remove(filename);
            Serial.println("Fichier RECORD.OGG supprimé.");
        }

        recording = SD.open(filename, FILE_WRITE);
        if (!recording) {
            Serial.println("Couldn't open file to record!");
            while (1);
        }
        musicPlayer.startRecordOgg(true);
    } else if (digitalRead(3) == HIGH and createNewUser == false) {
        uint8_t data[1] = {0b00000001};
        sendFrame(0b11100010, data, 1); 
        digitalWrite(10, HIGH);
        isRecording = false;
        createNewUser = true;
    }
    if (isRecording) {
        while (digitalRead(4) != LOW) {
            uint16_t written = saveRecordedData(isRecording);
            if (written < 32) {
                delay(1);
            }
        }
        musicPlayer.stopRecordOgg();
        isRecording = false;
        saveRecordedData(isRecording);
        recording.flush();
        recording.close();
        Serial.println("End recording");
        digitalWrite(10, LOW);

        oggFile = SD.open("RECORD.ogg", FILE_READ);
        if (!oggFile) {
            Serial.println("Erreur : Fichier RECORD.ogg introuvable !");
            while (1);
        }
        sendAudio();

        if (!musicPlayer.prepareRecordOgg("v44k1q05.img")) {
            Serial.println("Couldn't load plugin!");
            while (1);
        }
    }
}