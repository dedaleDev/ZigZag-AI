import argparse
import struct
import wave
import os
import pveagle
from pathlib import Path
from pydub import AudioSegment

# Constantes globales
FEEDBACK_TO_DESCRIPTIVE_MSG = {
    pveagle.EagleProfilerEnrollFeedback.AUDIO_OK: 'Bonne qualité audio',
    pveagle.EagleProfilerEnrollFeedback.AUDIO_TOO_SHORT: 'Durée audio insuffisante',
    pveagle.EagleProfilerEnrollFeedback.UNKNOWN_SPEAKER: 'Orateur inconnu dans l\'audio',
    pveagle.EagleProfilerEnrollFeedback.NO_VOICE_FOUND: 'Aucune voix détectée dans l\'audio',
    pveagle.EagleProfilerEnrollFeedback.QUALITY_ISSUE: 'Qualité audio médiocre (microphone ou environnement)'
}


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



def get_caches_dir():
    current_dir = Path.cwd()
    if current_dir.name.endswith("ZigZag"):
        return str(current_dir / "Server/src/caches/")
    return str(current_dir.parent / "flavientech/com/java/caches/")  # à changer plus tard

def read_audio_file(file_name, sample_rate):
    """Lit un fichier audio WAV et retourne les données échantillonnées."""
    try:
        with wave.open(file_name, mode="rb") as wav_file:
            channels = wav_file.getnchannels()
            sample_width = wav_file.getsampwidth()
            num_frames = wav_file.getnframes()

            if wav_file.getframerate() != sample_rate:
                raise ValueError(
                    f"Le fichier audio doit avoir un taux d'échantillonnage de {sample_rate} Hz. Obtenu : {wav_file.getframerate()} Hz")
            if sample_width != 2:
                raise ValueError(f"Le fichier audio doit être en 16 bits. Obtenu : {sample_width * 8} bits")
            if channels > 1:
                print(f"Conversion de {channels} canaux en mono.")
            
            samples = wav_file.readframes(num_frames)
        
        frames = struct.unpack('h' * num_frames * channels, samples)
        
        # Conversion en mono si nécessaire
        if channels > 1:
            audio_samples = []
            for i in range(0, len(frames), channels):
                # Moyenne des canaux
                sample = sum(frames[i:i+channels]) // channels
                audio_samples.append(sample)
        else:
            audio_samples = list(frames)
        
        return audio_samples
    except Exception as e:
        print(f"Erreur lors de la lecture du fichier audio : {e}")
        return []

def list_voice_profiles():
    """Liste tous les fichiers de profil utilisateur dans le répertoire spécifié."""
    try :
        return [file for file in os.listdir(get_caches_dir()+"users") if file.endswith(".eagle")]
    except Exception as e:
        print(f"E_list_voice_profiles: {e}{e.__traceback__.tb_lineno}")

def enroll_user_with_wav(access_key, user_name, audio_file_path, output_profile_path):
    """Enrôlement d'un nouvel utilisateur en utilisant un fichier audio .wav."""
    try:
        if not user_name:
            print("\u001B[31m Le nom d'utilisateur est requis pour l'enrôlement.\u001B[0m")
            return

        if not os.path.exists(audio_file_path):
            print(f"\u001B[31m Le fichier audio spécifié n'existe pas : {audio_file_path}\u001B[0m")
            return

        try:
            # Initialiser le profiler Eagle
            eagle_profiler = pveagle.create_profiler(access_key=access_key)
            print(f'Version d\'Eagle: {eagle_profiler.version}')

            # Lire les samples audio
            audio_samples = read_audio_file(audio_file_path, eagle_profiler.sample_rate)
            if not audio_samples:
                print("\u001B[31m Aucun échantillon audio n'a été lu.\u001B[0m")
                return

            enroll_percentage = 0.0

            # Enrôlement avec les samples audio
            enroll_percentage, feedback = eagle_profiler.enroll(audio_samples)
            print(f'Enrôlement: {enroll_percentage:.2f}% - {FEEDBACK_TO_DESCRIPTIVE_MSG.get(feedback, "Feedback inconnu")}')

            if enroll_percentage >= 100.0:
                # Sauvegarde du profil utilisateur
                speaker_profile = eagle_profiler.export()
                with open(output_profile_path, 'wb') as f:
                    f.write(speaker_profile.to_bytes())
                print(f'Profil de l\'utilisateur sauvegardé dans {output_profile_path}')
            else:
                print(f"\u001B[31m Enrôlement incomplet : {enroll_percentage:.2f}%\u001B[0m")
                print("Veuillez fournir plus d'échantillons audio pour compléter l'enrôlement.")
        
        except pveagle.EagleActivationLimitError:
            print("\u001B[31m La clé d'accès a atteint sa limite de traitement.\u001B[0m")
        except pveagle.EagleError as e:
            print(f"\u001B[31m Erreur lors de l'enrôlement avec Eagle : {e}\u001B[0m")
        finally:
            eagle_profiler.delete()
    except Exception as e:
        print(f"\u001B[31m Erreur: {e}\u001B[0m")


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

if __name__ == '__main__':
    try:
        parser = argparse.ArgumentParser(description="Reconnaissance vocale avec PicoVoice Eagle")
        parser.add_argument("action", choices=["enroll", "test"], help="Action à effectuer: enroll ou test")
        parser.add_argument("access_key", help="Clé d'accès pour Eagle")
        parser.add_argument("--model_path", help="Chemin du fichier modèle (requis pour l'action test)", default=None)
        parser.add_argument("--audio_path", help="Chemin du fichier audio (requis pour l'action enroll et test)", default=None)
        parser.add_argument("--user_name", "-u", help="Nom de l'utilisateur (requis pour l'action enroll)", default=None)
        parser.add_argument("--output_profile_path", help="Chemin pour sauvegarder le profil utilisateur (requis pour l'action enroll)", default=None)
        
        args = parser.parse_args()

        if not args.access_key:
            parser.error("\u001B[31m Une clé d'accès API PicoVoice est requise.\u001B[0m")

        if args.action == "enroll":
            if not args.user_name:
                parser.error("\u001B[31m Le paramètre --user_name est requis pour l'action d'enrôlement.\u001B[0m")
            if not args.audio_path:
                parser.error("\u001B[31m Le paramètre --audio_path est requis pour l'action d'enrôlement.\u001B[0m")
            if not args.output_profile_path:
                parser.error("\u001B[31m Le paramètre --output_profile_path est requis pour l'action d'enrôlement.\u001B[0m")
            enroll_user_with_wav(args.access_key, args.user_name, args.audio_path, args.output_profile_path)
        
        elif args.action == "test":
            if not args.model_path or not os.path.exists(args.model_path):
                parser.error("\u001B[31m Le chemin du modèle n'existe pas ou est manquant.\u001B[0m")
            if not args.audio_path or not os.path.exists(args.audio_path):
                parser.error("\u001B[31m Le chemin du fichier audio n'existe pas ou est manquant.\u001B[0m")
            
            test_voice(args.model_path, args.audio_path, args.access_key)
        
        else:
            print("\u001B[31m Action non valide. Choisissez 'enroll' ou 'test'.\u001B[0m")

    except Exception as e:
        print(f"\u001B[31m Erreur: {e} {e.__traceback__.tb_lineno}\u001B[0m")
        exit(1)