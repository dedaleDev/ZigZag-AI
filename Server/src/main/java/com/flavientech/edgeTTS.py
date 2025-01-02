import asyncio
import edge_tts
import sys 
from pathlib import Path

def get_caches_dir():
    current_dir = Path.cwd()
    if current_dir.name.endswith("ZigZag"):
        return str(current_dir /"Server/src/caches/")
    return str(current_dir.parent /"flavientech/com/java/caches/")#à changer plus tard

VOICES = [ 'fr-FR-DeniseNeural']
TEXT = sys.argv[1]
VOICE = VOICES[0]
OUTPUT_FILE = get_caches_dir()+"/outputEdgeTTS.mp3"

async def amain():
    communicate = edge_tts.Communicate(TEXT, VOICE)
    await communicate.save(OUTPUT_FILE)

loop = asyncio.get_event_loop_policy().get_event_loop()
try:
    loop.run_until_complete(amain())
finally:
    loop.close()