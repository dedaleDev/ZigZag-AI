import asyncio
import edge_tts
import sys 
import subprocess
from pathlib import Path

def get_caches_dir():
    current_dir = Path.cwd()
    if current_dir.name.endswith("ZigZag"):
        return str(current_dir /"Server/src/caches/")
    return str(current_dir.parent /"flavientech/com/java/caches/")#à changer plus tard

VOICES = [str(sys.argv[2])]
#verifier si le texte est vide
if len(sys.argv) < 3 or sys.argv[1] == "":
    print("Text is empty")
    sys.exit(1)
if len(sys.argv) < 3 or sys.argv[2] == "" or sys.argv[2] == "null":
    print("Voice is empty")
    sys.exit(1)
TEXT = sys.argv[1]
VOICE = VOICES[0]
OUTPUT_FILE = get_caches_dir()+"/outputEdgeTTS.mp3"
OUTPUT_FILE_WAV = get_caches_dir() + "/answer.wav"

async def amain():
    communicate = edge_tts.Communicate(TEXT, VOICE)
    await communicate.save(OUTPUT_FILE)

loop = asyncio.get_event_loop_policy().get_event_loop()
try:
    loop.run_until_complete(amain())
finally:
    loop.close()
    
# Convertir le fichier MP3 en WAV
subprocess.run([
    "ffmpeg", "-y", "-loglevel", "error", "-i", OUTPUT_FILE, 
    "-ar", "16000", "-ac", "1", "-c:a", "pcm_s16le", OUTPUT_FILE_WAV
])