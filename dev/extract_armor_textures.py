"""
Extrait les textures d'armures Minecraft (64x32) depuis armor_reference.png.
Bornes calibrees depuis scan pixel de l'image 1536x1024.
"""
from PIL import Image
import os

SRC = r"c:\Users\matis\Documents\GitHub\-Dungeon-Arise-\dev\armor_reference.png"
OUT = r"c:\Users\matis\Documents\GitHub\-Dungeon-Arise-\dev\solo-gates-v2\src\main\resources\assets\sologates\textures\models\armor"

# Bornes calibrees par analyse de l'image source
COLUMNS = {
    "e": (13,  299),
    "c": (312, 607),
    "b": (621, 909),
    "a": (923, 1204),
    "s": (1217, 1521),
}
LAYER_Y = {
    1: (158, 468),
    2: (550, 800),
}

def is_checker(r, g, b):
    avg = (r + g + b) // 3
    return abs(r - avg) < 15 and abs(g - avg) < 15 and abs(b - avg) < 15 and 130 < avg < 248

def extract_region(px, x1, y1, x2, y2, W, H):
    w = x2 - x1 + 1
    h = y2 - y1 + 1
    sx = w / 64.0
    sy = h / 32.0
    out = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for v in range(32):
        for u in range(64):
            cx = min(x2, x1 + int((u + 0.5) * sx))
            cy = min(y2, y1 + int((v + 0.5) * sy))
            r, g, b, a = px[cx, cy]
            if not is_checker(r, g, b):
                out.putpixel((u, v), (r, g, b, 255))
    return out

src = Image.open(SRC).convert("RGBA")
px = src.load()
W, H = src.size
print(f"Image source: {W}x{H}")

for rank, (x1, x2) in COLUMNS.items():
    for layer_num, (y1, y2) in LAYER_Y.items():
        img = extract_region(px, x1, y1, x2, y2, W, H)
        fname = f"rank_{rank}_layer_{layer_num}.png"
        img.save(os.path.join(OUT, fname))
        print(f"  Sauvegarde: {fname}  ({x1},{y1} -> {x2},{y2})")

print("\nTermine !")
