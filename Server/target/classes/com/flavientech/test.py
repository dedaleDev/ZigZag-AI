from pathlib import Path

def get_caches_dir():
    current_dir = Path.cwd()
    if current_dir.name.endswith("ZigZag"):
        return str(current_dir /"Server/src/caches/")
    return str(current_dir.parent /"flavientech/com/java/caches/")#à changer plus tard

print(get_caches_dir())