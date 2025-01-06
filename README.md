# ZigZag AI: The Conversational Assistant

![ZigZag Logo](Server/src/main/resources/static/img/ZigZagLogo.png)

ZigZag AI is a conversational assistant powered by **GPT4o-Mini** and designed for voice and text-based interactions. Its architecture combines hardware (Arduino) and software (Java, Python, SQL) to provide a seamless user experience. Whether you're interacting with ZigZag through voice commands or a web interface, its features ensure robust performance, extensibility, and security.

Designed to provide both **voice-controlled** and **text-based interactions**. The project is divided into two main components:  

1. **Arduino UNO R3**: A hardware controller enabling intuitive voice-based interactions.  
2. **Web Server**: A graphical interface for text-based communication with ZigZag.

While some features require the Arduino setup, ZigZag also functions independently for basic interactions.  

---

## 🌟 Features

- **Voice Interaction**: Speak to ZigZag and hear it respond with a natural voice. (REQUIRED ARDUINO)
- **User Voice Detection**: Identify users based on their voice tone.  (REQUIRED ARDUINO)
- **LLM Integration**: Leverages OpenAI's GPT4o-Mini for intelligent conversation responses.
- **Online Search**: Completes responses using **Qwant** search engine.
- **Weather Service**: Retrieve current weather conditions via **OpenWeather** API.
- **Contextual Conversations**: Stores and manages conversation contexts in an SQL database.
- **Web Interface**: Text-based interaction via a web browser.
- **Customizable AI Voices**: Personalize the assistant’s voice.

---

## 📂 Project Structure

The project is divided into two main components: **Arduino** and **Server**.

### Project Tree

```plaintext
├── Arduino
│   ├── ZigZag_Arduino
│   │   ├── include
│   │   │   └── README
│   │   ├── lib
│   │   │   └── README
│   │   ├── platformio.ini
│   │   ├── src
│   │   │   ├── main.cpp
│   │   │   ├── mainRecorder.bak
│   │   │   └── mainSender.bak
│   │   └── test
│   │       └── README
│   └── v44k1q05.img
├── LICENSE
├── README.md
└── Server
    ├── pom.xml
    ├── src
    │   └── main
    │       ├── caches
    │       │   ├── users
    │       ├── java
    │       │   └── com
    │       │       └── flavientech
    │       │           ├── App.java //THE MAIN
    │       │           ├── ArduinoSerial.java
    │       │           ├── AudioFileListener.java
    │       │           ├── EagleController.java
    │       │           ├── InteractWithOpenAI.java
    │       │           ├── LoadConf.java
    │       │           ├── OnlineAPITools.java
    │       │           ├── OnlineSearch.java
    │       │           ├── OpenAI.java
    │       │           ├── PathChecker.java
    │       │           ├── PhysicalInfo.java
    │       │           ├── PythonController.java
    │       │           ├── SoundPlayer.java
    │       │           ├── SpeechToText.java
    │       │           ├── TextToSpeech.java
    │       │           ├── Time.java
    │       │           ├── WeatherService.java
    │       │           ├── WebController.java
    │       │           ├── WebServer.java
    │       │           ├── edgeTTS.py
    │       │           ├── oggToWav.py
    │       │           ├── recorder.py
    │       │           ├── service
    │       │           │   ├── DatabaseController.java
    │       │           │   └── DatabaseInitializer.java
    │       │           └── voiceRecognition.py
    │       ├── models
    │       │   ├── ZigZag-leopard-v2.0.0-24-09-13--08-11-50.pv
    │       │   ├── eagleVoices.txt
    │       │   └── promptGPT4o-Mini.txt
    │       ├── resources
    │       │   ├── application.exemple.properties
    │       │   ├── application.properties //WRITE YOUR CONFIGURATION HERE
    │       │   ├── static
    │       │   │   ├── css
    │       │   │   │   ├── index.css
    │       │   │   │   └── settings.css
    │       │   │   ├── img
    │       │   │   │   ├── ZigZagLogo.png
    │       │   │   │   ├── back.svg
    │       │   │   │   ├── send_button.svg
    │       │   │   │   └── settings_button.svg
    │       │   │   └── js
    │       │   │       ├── index.js
    │       │   │       └── settings.js
    │       │   ├── templates
    │       │   │   ├── error.html
    │       │   │   ├── index.html
    │       │   │   └── settings.html
    │       │   └── zigzag.sql
    │       └── sounds


```
### Explanation of the Project Tree

Server
/src/main/java/com/flavientech: Core server-side logic, including API integration, database management, and speech-to-text processing.
In this case, you have mainly : 
- App.java : the main of the project.
- EagleController.java : Speacker detection managment.
- ArduinoSerial.java : Serial link with the Arduino UNO R3
- OpenAI.java and InteractWithOpenAI.java : manage and process  communication with GPT4o-Mini
- WebController.java : Spring boot website controller.
- DatabaseController.java : JDBC database managment.
- OnlineAPITools.java : interact with differents API.

/src/main/resources: Configuration files, web assets (HTML, CSS, JS).
/src/main/caches: Temporary files for processing audio.


Arduino
ZigZag_Arduino: Contains the Arduino code for handling audio recording, playback, and interaction with ZigZag Server by Serial link.
platformio.ini: Configuration file for compiling the Arduino code using PlatformIO.
v44k1q05.img: Pre-loaded audio processing file required for recording. ADD THIS IN YOUR SD CARD FOR RECORD.


---

## 🛠️ Installation and Setup

While some features require the Arduino setup, ZigZag also functions independently for basic interactions.  

### Prerequisites
**Hardware Requirements (optional) :**
- Arduino UNO R3
- EGBO VS1053B audio module
- 2 push buttons
- 1 LED
- 1 SD card (FAT32, max 16GB)
- Breadboard and jumper wires

**Software Requirements:**
- Java: Version 22.0.2 or later
- Python: Version 3.12.1 or later
    Required Python packages:
    
    ```plaintext
    pip install edge-tts pydub PyAudio pveagle
    pip install audioop-lts  # For Python 3.13+
    ```

- Maven, Apache, MySQL/MariaDB
- FFmpeg (ONLY FOR ARDUINO COMPLETE SETUP):
    Download: FFmpeg on windows : https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-full.7z
    Add bin folder to environment variables.

- Plateformio (ONLY FOR ARDUINO COMPLETE SETUP)

1. Configure the Server
Navigate to Server/src/main/resources.
Copy application.exemple.properties and rename it to application.properties.
Update the file with your configurations (e.g., database credentials, API keys).

2. Configure Arduino (optional)
Place  Arduino/v44k1q05.img on the SD card.
Open Arduino/ZigZag_Arduino/platformio.ini in PlatformIO and upload the code to your Arduino board.

3. Run the Application
Start the backend server on Eclipse :
    - File > Import > Maven > Existing Maven Project > Browse 
    - Select the folder of ZigZag
    - Click on "Finish"
    - Go to App.java and run it.

Select "Serveur Web" ou "Arduino + Serveur Web" and access on the configured url, default : http://localhost:8080.

🤖 Arduino Connections
Here’s how to set up the Arduino hardware:

- EGBO VS1053B Audio Module:
    Connect to Arduino following this Pinout Guide. If you have shield, you just have to plug it. DON'T FORGET PLUGIN
Push Buttons:
Button 1: Start/End recording.
Button 2: Enroll new user
LED: Indicates system status.

![links](https://github.com/user-attachments/assets/ccf79922-ece2-4f68-b99e-373805b47cbf)

📜 API Credits
OpenAI: Language model for intelligent conversations.
Qwant: Privacy-focused search engine for online queries.
OpenWeather: Weather updates.
Picovoice: Voice recognition models.
Edge TTS: Text-to-speech.
FFmpeg: Audio processing.

Enjoy ZigZag AI! 🚀
