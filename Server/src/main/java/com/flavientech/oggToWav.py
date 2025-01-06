import sys
from pydub import AudioSegment
import os


def convert_ogg_to_wav(input_file, output_file):
    try:
        # Charger l'audio
        audio = AudioSegment.from_file(input_file, format="ogg")
        
        # Appliquer les caractéristiques demandées : 16 kHz, Mono, 16 bits
        audio = audio.set_frame_rate(16000)  # Taux d'échantillonnage : 16000 Hz
        audio = audio.set_channels(1)       # Mono : 1 canal
        audio = audio.set_sample_width(2)   # Profondeur de bits : 16 bits (2 octets)
        
        # Exporter en WAV
        audio.export(output_file, format="wav")
        
        print(f"Conversion terminée. Fichier enregistré sous : {output_file}")
    except FileNotFoundError:
        print(f"Erreur : Le fichier {input_file} est introuvable.")
    except Exception as e:
        print(f"Une erreur est survenue lors de la conversion : {e}")

def main():
    if len(sys.argv) != 3:
        print("Usage : python3 oggToWav.py <source.ogg> <destination.wav>")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    convert_ogg_to_wav(input_file, output_file)

if __name__ == "__main__":
    if os.name == "nt":
        if "ffmpeg" not in os.environ["PATH"]:
            os.environ["PATH"] += ";C:\\ffmpeg\\bin"
    main()