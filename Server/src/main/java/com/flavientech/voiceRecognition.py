import argparse
import struct
import wave
import os
import pveagle
from pvrecorder import PvRecorder
from pydub import AudioSegment
from pydub import AudioSegment
import os
from pathlib import Path

def get_caches_dir():
    current_dir = Path.cwd()
    if current_dir.name.endswith("ZigZag"):
        return str(current_dir / "Server/src/caches/")
    return str(current_dir.parent / "flavientech/com/java/caches/")#à changer plus tard

# Constantes globales
PV_RECORDER_FRAME_LENGTH = 512
FEEDBACK_TO_DESCRIPTIVE_MSG = {
    pveagle.EagleProfilerEnrollFeedback.AUDIO_OK: 'Bonne qualité audio',
    pveagle.EagleProfilerEnrollFeedback.AUDIO_TOO_SHORT: 'Durée audio insuffisante',
    pveagle.EagleProfilerEnrollFeedback.UNKNOWN_SPEAKER: 'Orateur inconnu dans l\'audio',
    pveagle.EagleProfilerEnrollFeedback.NO_VOICE_FOUND: 'Aucune voix détectée dans l\'audio',
    pveagle.EagleProfilerEnrollFeedback.QUALITY_ISSUE: 'Qualité audio médiocre (microphone ou environnement)'
}

ACCESS_KEY = "NA0NuP+5Orn3NUuj8UHB6Sj1VaolSuM2qvYlQeeWbYs6epGzsACtYA=="

def convert_sample_rate_if_needed(input_file, target_sample_rate=16000, target_bit_depth=16):
    """Convertit un fichier audio vers le taux d'échantillonnage et la profondeur en bits cible si nécessaire."""
    try:
        # Charger le fichier audio
        audio = AudioSegment.from_file(input_file)
        current_sample_rate = audio.frame_rate
        current_bit_depth = audio.sample_width * 8  # sample_width est en octets, donc on le multiplie par 8 pour obtenir les bits

        # Vérifier si une conversion est nécessaire
        conversion_needed = False
        
        if current_sample_rate != target_sample_rate:
            audio = audio.set_frame_rate(target_sample_rate)
            conversion_needed = True
        
        if current_bit_depth != target_bit_depth:
            audio = audio.set_sample_width(target_bit_depth // 8)  # set_sample_width attend des octets, donc on divise par 8
            conversion_needed = True

        # Si une conversion est nécessaire, sauvegarder le fichier converti
        if conversion_needed:
            output_file = os.path.splitext(input_file)[0] + f"_{target_sample_rate}Hz_{target_bit_depth}bits.wav"
            audio.export(output_file, format="wav")
            return output_file
        else:
            return input_file

    except Exception as e:
        print(f"Erreur lors de la conversion du fichier : {e}")
        return None


def optimize_audio_file(input_file, output_file, segment_duration=5000):
    """Optimise un fichier audio en conservant des segments de 5 secondes toutes les 10 secondes."""
    try :
        audio = AudioSegment.from_wav(input_file)
        total_duration = len(audio)
        output_audio = AudioSegment.empty()
        
        for i in range(200, total_duration - 200, 2 * segment_duration):
            segment = audio[i:i + segment_duration]
            output_audio += segment
        
        output_audio.export(output_file, format="wav")
    except Exception as e:
        print(f"E_optimize_audio_file: {e}{e.__traceback__.tb_lineno}")


def list_voice_profiles():
    """Liste tous les fichiers de profil utilisateur dans le répertoire spécifié."""
    try :
        return [file for file in os.listdir("src/caches/users") if file.endswith(".eagle")]
    except Exception as e:
        print(f"E_list_voice_profiles: {e}{e.__traceback__.tb_lineno}")


def read_audio_file(file_name, sample_rate):
    """Lit un fichier audio WAV et retourne les données échantillonnées."""
    try :
        with wave.open(file_name, mode="rb") as wav_file:
            # Validation des propriétés audio
            if wav_file.getframerate() != sample_rate:
                file_name = convert_sample_rate_if_needed(file_name, sample_rate)
            if wav_file.getsampwidth() != 2:
                file_name = convert_sample_rate_if_needed(file_name, target_bit_depth=16)
                
        with wave.open(file_name, mode="rb") as wav_file:
            if wav_file.getframerate() != sample_rate or wav_file.getsampwidth() != 2:
                raise ValueError(f"Le taux d'échantillonnage doit être {sample_rate} Hz, obtenu: {wav_file.getframerate()} Hz. La profondeur en bits doit aussi être de 16 bits, obtenu: {wav_file.getsampwidth() * 8} bits.")
            num_frames = wav_file.getnframes()
            samples = wav_file.readframes(num_frames)
        
        frames = struct.unpack('h' * num_frames * wav_file.getnchannels(), samples)
        return frames[::wav_file.getnchannels()]
    except Exception as e:
        print(f"E_read_audio_file: {e}{e.__traceback__.tb_lineno}")


def test_voice(MODEL_PATH, AUDIO_PATH, ACCESS_KEY):
    """Test de reconnaissance vocale en utilisant le modèle Eagle."""
    try :
        if not os.path.exists(MODEL_PATH):
            print("\u001B[31m ERREUR : Le chemin du modèle n'existe pas.\u001B[0m")
            return 0.0

        try:
            # Charger le modèle Eagle
            with open(MODEL_PATH, 'rb') as f:
                profile = pveagle.EagleProfile.from_bytes(f.read())
            eagle = pveagle.create_recognizer(access_key=ACCESS_KEY, speaker_profiles=[profile])

            # Lire l'audio et préparer les frames
            audio = read_audio_file(AUDIO_PATH, eagle.sample_rate)
            num_frames = len(audio) // eagle.frame_length
            test_results = []

            for i in range(10, num_frames):
                frame = audio[i * eagle.frame_length:(i + 1) * eagle.frame_length]
                scores = eagle.process(frame)
                if scores[0] > 0.2:  # Si l'utilisateur est reconnu
                    test_results.append(scores[0])

            score_result = sum(test_results) / len(test_results) if test_results else 0.0
            print(f"Score final: {score_result:.2f}")
            return score_result

        except pveagle.EagleError as e:
            print(f"Erreur lors du test Eagle: {e}")
            return 0.0

        finally:
            if eagle:
                eagle.delete()
    except Exception as e:
        print(f"E_test_voice: {e}{e.__traceback__.tb_lineno}")


def enroll_user(ACCESS_KEY, user_name):
    """Enrôlement d'un nouvel utilisateur en utilisant un enregistrement audio."""
    try :
        if not user_name:
            print("\u001B[31m Le nom d'utilisateur est requis pour l'enrôlement.\u001B[0m")
            return

        try:
            eagle_profiler = pveagle.create_profiler(access_key=ACCESS_KEY)
            print(f'Version d\'Eagle: {eagle_profiler.version}')
            model_path = f"src/caches/users/{user_name}.eagle"

            # Enregistrement audio
            recorder = PvRecorder(device_index=0, frame_length=PV_RECORDER_FRAME_LENGTH)
            print(f"Enregistrement audio depuis '{recorder.selected_device}'")
            num_enroll_frames = eagle_profiler.min_enroll_samples // PV_RECORDER_FRAME_LENGTH

            enroll_percentage = 0.0
            recorder.start()
            while enroll_percentage < 100.0:
                enroll_pcm = []
                for _ in range(num_enroll_frames):
                    enroll_pcm.extend(recorder.read())
                enroll_percentage, feedback = eagle_profiler.enroll(enroll_pcm)
                print(f'Enrôlement: {enroll_percentage:.2f}% - {FEEDBACK_TO_DESCRIPTIVE_MSG[feedback]}')

            # Sauvegarde du profil utilisateur
            speaker_profile = eagle_profiler.export()
            with open(model_path, 'wb') as f:
                f.write(speaker_profile.to_bytes())
            print(f'Profil de l\'utilisateur sauvegardé dans {model_path}')

        except pveagle.EagleActivationLimitError:
            print("La clé d'accès a atteint sa limite de traitement.")
        except pveagle.EagleError as e:
            print(f"Erreur lors de l'enrôlement Eagle: {e}")
        finally:
            recorder.stop()
            recorder.delete()
            eagle_profiler.delete()
    except Exception as e:
        print(f"E_enroll_user: {e}{e.__traceback__.tb_lineno}")


if __name__ == '__main__':
    try:
        parser = argparse.ArgumentParser(description="Reconnaissance vocale avec PicoVoice Eagle")
        parser.add_argument("action", choices=["enroll", "test"], help="Action à effectuer: enroll ou test")
        parser.add_argument("access_key", help="Clé d'accès pour Eagle")
        parser.add_argument("--model_path", help="Chemin du fichier modèle (requis pour l'action test)", default=None)
        parser.add_argument("--audio_path", help="Chemin du fichier audio (requis pour l'action test)", default=None)
        parser.add_argument("--user_name", "-u", help="Nom de l'utilisateur (requis pour l'action enroll)", default=None)
        
        args = parser.parse_args()

        if not args.access_key:
            parser.error("\u001B[31m Une clé d'accès API PicoVoice est requise.\u001B[0m")

        if args.action == "enroll":
            if not args.user_name:
                parser.error("\u001B[31m Le paramètre --user_name est requis pour l'action d'enrôlement.\u001B[0m")
            enroll_user(args.access_key, args.user_name)
        
        elif args.action == "test":
            if not args.model_path or not os.path.exists(args.model_path):
                parser.error("\u001B[31m Le chemin du modèle n'existe pas ou est manquant.\u001B[0m")
            if not args.audio_path or not os.path.exists(args.audio_path):
                parser.error("\u001B[31m Le chemin du fichier audio n'existe pas ou est manquant.\u001B[0m")
            
            optimize_audio_file(args.audio_path, get_caches_dir()+"users/optimized.wav")
            test_voice(args.model_path, get_caches_dir()+"/users/optimized.wav", args.access_key)
        
        else:
            print("\u001B[31m Action non valide. Choisissez 'enroll' ou 'test'.\u001B[0m")

    except Exception as e:
        print(f"\u001B[31m Erreur: {e} {e.__traceback__.tb_lineno}\u001B[0m")
        exit(1)