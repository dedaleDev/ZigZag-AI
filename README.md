### ZigZag AI

ZigZag est un agent conversationnel basé sur GPT4o-Mini. Le projet est segmenté en deux parties : 

- Arduino UNO R3 : Un controlleur pour un usage vocal de ZigZag agréable et intuitif.
- Web Serveur : ne interface graphique pour communiquer avec ZigZag de manière textuelle.

Certaines fonctionnalité requiert l'usage de l'arduino. Cependant, ZigZag est fonctionnel sans.

Fonctionnalités :
- 🔊 reconnaissance vocale -> SpeechToText -> GPT4o-Mini -> textToSpeech (ARDUINO)
- 💡 GPT4o-Mini (SITE WEB)
- 🌤️ Météo service (OpenWeather)
-  Conversations contextuelles
- 🔎 Recherches en lignes avec Qwant.
- 🎛️ Voix IA personalisé.

### Requirement Général :

 - Java 22.0.2 ou ultérieur
 - Python 3.12.1 ou ultérieur :
    pip install edge-tts
    pip install pydub
    pip install PyAudio
    pip install pveagle
    Si python 3.13 installer : 
        pip install audioop-lts
 - Maven
 - Apache
 - mysql ou mariabd
 - ffmpeg  (REQUIS POUR ARDUINO)
    Sous Windows : 
        - Telechargez le fichier : https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip
        - Renommez le dossier extrait en "FFmpeg" et placez le dans C: ou ailleurs
        - Appuyez sur la touche Windows et tapez "variables d'environnement"7.
        - Cliquez sur "Modifier les variables d'environnement système"7.
        - Dans la fenêtre qui s'ouvre, cliquez sur "Variables d'environnement"7.
        - Sous "Variables utilisateur", sélectionnez "Path" et cliquez sur "Modifier".
        - Cliquez sur "Nouveau" et ajoutez le chemin vers le dossier "bin" de FFmpeg (par exemple : C:\FFmpeg\bin\ffmpeg.exe) et nommer le ffmpeg


# Importation dans Eclispe : 
File > Import > Maven > Existing Maven Project > Browse 
And select the folder of ZigZag
Click on "Finish"

# Importation dans VS Code :
File > Open Folder.
And select the folder of ZigZag

# Configuration Général :

Attention :
Afin de rendre le projet fonctionnel, vous devez renseigner compléter le fichier Server\src\main\resources\application.exemple.properties et le RENOMMER en : Server\src\main\resources\application.properties.

### Requirement et branchements Arduino :
- Arduino UNO R3
- EGBO VS1053B
- 2 boutton poussoirs
- 1 led
- 1 breadboard
- 1 carte SD formaté en FAT32. MAX 16 Go
- 12 fils

Attention : 
Le fichier Arduino\v44k1q05.img, doit être placé sur la carte SD pour l'enregistrement audio. 

- Pour compiler le code, utilisez plateformio. Importez le fichier Arduino\ZigZag_Arduino\platformio.ini

#Branchements Arduino 
