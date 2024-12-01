import asyncio
import edge_tts
import sys 

VOICES = [ 'fr-FR-DeniseNeural']
TEXT = sys.argv[1]
VOICE = VOICES[0]
OUTPUT_FILE = "src/caches/outputEdgeTTS.mp3"

async def amain():
    communicate = edge_tts.Communicate(TEXT, VOICE)
    await communicate.save(OUTPUT_FILE)

loop = asyncio.get_event_loop_policy().get_event_loop()
try:
    loop.run_until_complete(amain())
finally:
    loop.close()