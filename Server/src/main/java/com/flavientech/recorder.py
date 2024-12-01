import pyaudio
import wave

def record_audio(device_index=2, record_seconds=5, output_filename="src/caches/request.wav"):
    # Lister les périphériques audio disponibles
    audio = pyaudio.PyAudio()
    for i in range(audio.get_device_count()):
        info = audio.get_device_info_by_index(i)
        print(f"Device {i}: {info['name']}")

    # Paramètres
    FORMAT = pyaudio.paInt16
    CHANNELS = 2  # Changer le nombre de canaux à 2
    RATE = 16000
    CHUNK = 1024

    # Ouvrir le flux audio
    stream = audio.open(format=FORMAT, channels=CHANNELS, rate=RATE, input=True, input_device_index=device_index, frames_per_buffer=CHUNK)
    frames = []

    # Enregistrer l'audio
    print("Recording started... Speak please")
    for i in range(0, int(RATE / CHUNK * record_seconds)):
        data = stream.read(CHUNK)
        frames.append(data)

    print("Recording finished")
    # Arrêter le flux
    stream.stop_stream()
    stream.close()
    audio.terminate()

    # Sauvegarder l'audio dans le fichier
    with wave.open(output_filename, 'wb') as wf:
        wf.setnchannels(CHANNELS)
        wf.setsampwidth(audio.get_sample_size(FORMAT))
        wf.setframerate(RATE)
        wf.writeframes(b''.join(frames))

    print(f"Audio saved to the file {output_filename}")

# Appel de la fonction
record_audio(device_index=2, record_seconds=7, output_filename="src/caches/request.wav")