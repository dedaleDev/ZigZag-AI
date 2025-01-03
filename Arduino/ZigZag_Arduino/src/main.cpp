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
int8_t createNewUserState = 0;
bool toRecord = false;

bool receiveMinimalFrame(uint8_t preamble) {
    while (true) {
        if (Serial.available() >= 6) { // Ensure there are enough bytes to read
            uint8_t buffer[6];
            Serial.readBytes(buffer, 6);
            if (buffer[0] == preamble && buffer[5] == 0b10101110) {
                return true;
            }
        }
    }
}

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

void sendAudio(String path){
    oggFile = SD.open(path, FILE_READ);
    if (!oggFile) {
        Serial.println("Erreur : Fichier RECORD.ogg introuvable !");
        while (1);
    }
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
    if (!musicPlayer.prepareRecordOgg("v44k1q05.img")) {
        Serial.println("Couldn't load plugin!");
        while (1);
    }
}

void setup() {
    Serial.begin(500000);
    Serial.println("ZigZag");
    if (!musicPlayer.begin()) {
        Serial.println("VS1053 not found");
        while (1);
    }
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
    pinMode(3, INPUT);   // Bouton poussoir enroll
    Serial.println("Ready !");
}

void record(const char* filename, unsigned long duration = 0){
    Serial.println("starting recording...");
    digitalWrite(10, HIGH);
    if (SD.exists(filename)) {
        SD.remove(filename);
    }
    recording = SD.open(filename, FILE_WRITE);
    if (!recording) {
        Serial.println("Couldn't open file to record!");
        while (1);
    }
    musicPlayer.startRecordOgg(true);
    unsigned long startTime = millis();
    while ((duration == 0 && digitalRead(4) != LOW) || (duration > 0 && (millis() - startTime) < duration * 1000)) {
        uint16_t written = saveRecordedData(true);
        if (written < 32) {
            delay(1);
        }
        //faire clignoter la LED toutes les secondes sans ralentir l'enregistrement
        if ((millis() - startTime) % 1000 < 500) {
            digitalWrite(10, HIGH);
        } else {
            digitalWrite(10, LOW);
        }
    }
    musicPlayer.stopRecordOgg();
    saveRecordedData(false);
    recording.flush();
    recording.close();
    digitalWrite(10, LOW);
    sendAudio(filename);
}

void loop() {
    digitalWrite(10, LOW);
    if (digitalRead(4) == HIGH) {
        record("RECORD.ogg");
    } else if (digitalRead(3) == HIGH and createNewUserState == 0) {
        uint8_t data[1] = {0b00000001};
        sendFrame(0b11100010, data, 1); 
        receiveMinimalFrame(0b11101000);
        record("nameUser.ogg", 3);
        receiveMinimalFrame(0b11101001);
        record("enroll.ogg", 20);
    }
}