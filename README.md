# ZigZag AI: The Conversational Assistant

![ZigZag Logo](Server/src/main/resources/static/img/ZigZagLogo.png)

ZigZag AI is a conversational assistant powered by **GPT4o-Mini** and designed for voice and text-based interactions. Its architecture combines hardware (Arduino) and software (Java, Python, SQL) to provide a seamless user experience. Whether you're interacting with ZigZag through voice commands or a web interface, its features ensure robust performance, extensibility, and security.

---

## 🌟 Features

- **Voice Interaction**: Speak to ZigZag and hear it respond with a natural voice.
- **User Voice Detection**: Identify users based on their voice tone.
- **LLM Integration**: Leverages OpenAI's GPT4o-Mini for intelligent conversation responses.
- **Online Search**: Completes responses using **Qwant** search engine.
- **Weather Service**: Retrieve current weather conditions via **OpenWeather** API.
- **Contextual Conversations**: Stores and manages conversation contexts in an SQL database.
- **Web Interface**: Text-based interaction via a web browser.
- **Customizable AI Voices**: Personalize the assistant’s voice.

---

## 🚀 Performance Goals

- **Fast Response Times**: Optimized for minimal latency.
- **Cost Efficiency**: Designed to minimize operational costs.

---

## 🔒 Security and Privacy

- **Local Data Processing**: Prioritizes local processing to avoid unnecessary external API calls.
- **GDPR Compliance**: Ensures secure handling of user data while avoiding model training with user-specific data.

---

## 🔧 Extensibility

ZigZag is built with future scalability in mind. Its modular design allows for:

- Adding new features without disrupting existing services.
- Potential integration with additional APIs (e.g., Spotify, Matter IoT) in the future.

---

## 📂 Project Structure

The project is divided into two main components: **Arduino** and **Server**.

### Project Tree

```plaintext
├── Arduino
│   ├── ZigZag_Arduino
│   │   ├── include
│   │   │   └── README
│   │   ├── lib
│   │   │   └── README
│   │   ├── platformio.ini
│   │   ├── src
│   │   │   ├── main.cpp
│   │   │   ├── mainRecorder.bak
│   │   │   └── mainSender.bak
│   │   └── test
│   │       └── README
│   └── v44k1q05.img
├── LICENSE
├── README.md
└── Server
    ├── pom.xml
    ├── src
    │   ├── main
    │   │   ├── caches
    │   │   ├── java/com/flavientech
    │   │   │   ├── App.java
    │   │   │   ├── ArduinoSerial.java
    │   │   │   └── ...
    │   │   ├── resources
    │   │   │   ├── application.properties
    │   │   │   ├── templates
    │   │   │   │   ├── index.html
    │   │   │   │   └── settings.html
    │   │   │   └── static
    │   │   └── sounds
    │   │       └── ...
Explanation of the Project Tree
Arduino
ZigZag_Arduino: Contains the Arduino code for handling audio recording, playback, and interaction with ZigZag.
platformio.ini: Configuration file for compiling the Arduino code using PlatformIO.
v44k1q05.img: Pre-loaded audio processing file required for recording.
Server
/src/main/java/com/flavientech: Core server-side logic, including API integration, database management, and speech-to-text processing.
/src/main/resources: Configuration files, web assets (HTML, CSS, JS), and SQL scripts for database setup.
/src/main/caches: Temporary files for processing audio and conversation context.
🛠️ Installation and Setup
Prerequisites
Hardware Requirements:
Arduino UNO R3
VS1053B audio module
2 push buttons
1 LED
1 SD card (FAT32, max 16GB)
Breadboard and jumper wires
Software Requirements:
Java: Version 22.0.2 or later
Python: Version 3.12.1 or later
Required Python packages:
bash

Copier
pip install edge-tts pydub PyAudio pveagle
pip install audioop-lts  # For Python 3.13+
Maven, Apache, MySQL/MariaDB
FFmpeg (for audio processing):
Download: FFmpeg Essentials
Add bin folder to environment variables.
Setup Steps
1. Clone the Repository
bash

Copier
git clone https://github.com/yourusername/ZigZag.git
cd ZigZag
2. Configure the Server
Navigate to Server/src/main/resources.
Copy application.exemple.properties and rename it to application.properties.
Update the file with your configurations (e.g., database credentials, API keys).
3. Install Dependencies
Backend:
bash

Copier
mvn install
Frontend: Ensure all static assets are correctly configured.
4. Configure Arduino
Place v44k1q05.img on the SD card.
Open Arduino/ZigZag_Arduino/platformio.ini in PlatformIO and upload the code to your Arduino board.
5. Run the Application
Start the backend server:
bash

Copier
java -jar target/zigzag-server.jar
Access the web interface at http://localhost:8080.
🤖 Arduino Connections
Here’s how to set up the Arduino hardware:

VS1053B Audio Module:
Connect to Arduino following this Pinout Guide.
Push Buttons:
Button 1: Start recording.
Button 2: Stop recording.
LED:
Indicates system status.
Tip: Use online services like Fritzing to create a visual wiring diagram for your setup.

📜 API Credits
OpenAI: Language model for intelligent conversations.
Qwant: Privacy-focused search engine for online queries.
OpenWeather: Weather updates.
Picovoice: Voice recognition models.
Edge TTS: Text-to-speech.
FFmpeg: Audio processing.
🖋️ How to Contribute
Contributions are welcome! To get started:

Fork the repository.
Create a new branch:
bash

Copier
git checkout -b feature-name
Submit a pull request.
📜 License
This project is licensed under the MIT License.

Enjoy building with ZigZag AI! 🚀
