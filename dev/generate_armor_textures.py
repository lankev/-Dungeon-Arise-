"""
Génère les textures d'armures Minecraft (64x32) pour Solo Gates
Basé sur les références visuelles fournies (Layer 1 + Layer 2 par rang)
"""
from PIL import Image
import os

OUT = r"c:\Users\matis\Documents\GitHub\-Dungeon-Arise-\dev\solo-gates-v2\src\main\resources\assets\sologates\textures\models\armor"

# ── helpers ──────────────────────────────────────────────────────────────────

def new():
    return Image.new("RGBA", (64, 32), (0, 0, 0, 0))

def p(img, x, y, c):
    if 0 <= x < 64 and 0 <= y < 32:
        img.putpixel((x, y), c + (255,) if len(c) == 3 else c)

def r(img, x1, y1, x2, y2, c):
    cc = c + (255,) if len(c) == 3 else c
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            img.putpixel((x, y), cc)

def dk(c, f=0.55): return tuple(max(0, int(v*f)) for v in c)
def lt(c, f=1.45): return tuple(min(255, int(v*f)) for v in c)
def mx(a, b, t=0.5): return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))

# ── UV regions (Minecraft standard 64×32) ────────────────────────────────────
# Layer 1: helmet, chest, right arm, right boot
# Layer 2: right leg + left leg

# HELMET
HT = (8,0,15,7)   # top
HB = (16,0,23,7)  # bottom (inside)
HRI= (0,8,7,15)   # right
HF = (8,8,15,15)  # front ← visible
HL = (16,8,23,15) # left
HBK= (24,8,31,15) # back

# CHEST
CT = (20,16,27,19) # top
CB = (28,16,35,19) # bottom
CRI= (16,20,19,31) # right
CF = (20,20,27,31) # front ← visible
CL = (28,20,31,31) # left
CBK= (32,20,39,31) # back

# RIGHT ARM
AT = (44,16,47,19)
AB = (48,16,51,19)
ARI= (40,20,43,31)
AF = (44,20,47,31) # front ← visible
ALL= (48,20,51,31)
ABK= (52,20,55,31)

# RIGHT BOOT
BT = (4,16,7,19)
BB = (8,16,11,19)
BRI= (0,20,3,31)
BF = (4,20,7,31)  # front ← visible
BL = (8,20,11,31)
BBK= (12,20,15,31)

# LEGGINGS Layer 2 — right leg
LRT = (4,16,7,19)
LRB = (8,16,11,19)
LRRI= (0,20,3,31)
LRF = (4,20,7,31)
LRL = (8,20,11,31)
LRBK= (12,20,15,31)

# LEGGINGS Layer 2 — left leg
LLT = (20,16,23,19)
LLB = (24,16,27,19)
LLLI= (16,20,19,31)
LLF = (20,20,23,31)
LLL = (24,20,27,31)
LLBK= (28,20,31,31)

def fill_region(img, region, base, shade_l=None, shade_r=None):
    """Fill a UV region with base + optional left/right edge shading."""
    x1,y1,x2,y2 = region
    r(img, x1,y1,x2,y2, base)
    if shade_l:
        for y in range(y1, y2+1): p(img, x1, y, shade_l)
    if shade_r:
        for y in range(y1, y2+1): p(img, x2, y, shade_r)

def shade_panel(img, region, mid, shadow, highlight, top_trim=None, bot_trim=None):
    x1,y1,x2,y2 = region
    w = x2-x1+1
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            t = (x-x1)/(w-1) if w > 1 else 0.5
            col = mx(shadow, highlight, t)
            col = mx(col, mid, 0.4)
            img.putpixel((x,y), col+(255,))
    if top_trim:
        for x in range(x1, x2+1): p(img, x, y1, top_trim)
    if bot_trim:
        for x in range(x1, x2+1): p(img, x, y2, bot_trim)

# ── RANK E ────────────────────────────────────────────────────────────────────
# Dark charcoal iron + blue visor + brown leather straps

def rank_e_l1():
    img = new()
    BAS = (44, 44, 50)   # dark charcoal
    LTH = (80, 58, 36)   # leather brown
    BLU = (30, 60, 105)  # blue accent
    RVT = (130,130,140)  # rivet silver
    SHD = dk(BAS)
    HI  = lt(BAS, 1.3)

    # HELMET
    r(img,*HT, dk(BAS))
    r(img,*HB, dk(BAS,0.4))
    shade_panel(img, HRI, BAS, SHD, BAS)
    shade_panel(img, HF,  BAS, SHD, HI, top_trim=BLU, bot_trim=SHD)
    shade_panel(img, HL,  BAS, BAS, SHD)
    r(img,*HBK, dk(BAS,0.7))
    # blue visor strip on helmet front
    for x in range(8,16): p(img,x,11,(30,65,115))
    for x in range(8,16): p(img,x,12,(20,50,95))
    # rivets
    for rx,ry in [(9,9),(14,9),(9,14),(14,14)]:
        p(img,rx,ry,RVT)

    # CHEST
    r(img,*CT, dk(BAS))
    r(img,*CB, dk(BAS,0.4))
    shade_panel(img, CRI, BAS, SHD, BAS)
    shade_panel(img, CF,  BAS, SHD, HI, top_trim=BLU, bot_trim=SHD)
    shade_panel(img, CL,  BAS, BAS, SHD)
    r(img,*CBK, dk(BAS,0.7))
    # leather diagonal strap
    for i in range(8):
        p(img, 20+i, 21+i//2, LTH)
        p(img, 20+i, 22+i//2, dk(LTH))
    # leather belt horizontal
    for x in range(20,28): p(img,x,27,LTH); p(img,x,28,dk(LTH))
    # plate rivets on chest
    for rx,ry in [(21,20),(26,20),(21,31),(26,31),(23,24),(24,24)]:
        p(img,rx,ry,RVT)

    # RIGHT ARM
    r(img,*AT, dk(BAS))
    r(img,*AB, dk(BAS,0.4))
    shade_panel(img, ARI, BAS, SHD, BAS)
    shade_panel(img, AF,  BAS, SHD, HI, top_trim=BLU, bot_trim=SHD)
    shade_panel(img, ALL, BAS, BAS, SHD)
    r(img,*ABK, dk(BAS,0.7))
    for x in range(44,48): p(img,x,25,LTH)
    for rx,ry in [(45,21),(46,21)]: p(img,rx,ry,RVT)

    # BOOT
    r(img,*BT, dk(BAS))
    r(img,*BB, dk(BAS,0.4))
    shade_panel(img, BRI, BAS, SHD, BAS)
    shade_panel(img, BF,  BAS, SHD, HI, top_trim=BLU, bot_trim=SHD)
    shade_panel(img, BL,  BAS, BAS, SHD)
    r(img,*BBK, dk(BAS,0.7))
    for x in range(4,8): p(img,x,27,LTH)

    return img

def rank_e_l2():
    img = new()
    BAS = (38, 38, 44)
    LTH = (70, 50, 30)
    SHD = dk(BAS)
    HI  = lt(BAS, 1.3)

    for leg_x in [0, 16]:
        r(img, leg_x+4,16, leg_x+7,19, dk(BAS))
        r(img, leg_x+8,16, leg_x+11,19, dk(BAS,0.4))
        shade_panel(img, (leg_x,20,leg_x+3,31), BAS, SHD, BAS)
        shade_panel(img, (leg_x+4,20,leg_x+7,31), BAS, SHD, HI)
        shade_panel(img, (leg_x+8,20,leg_x+11,31), BAS, BAS, SHD)
        r(img, leg_x+12,20, leg_x+15,31, dk(BAS,0.7))
        for x in range(leg_x+4, leg_x+8):
            p(img,x,26,LTH); p(img,x,27,dk(LTH))
        p(img,leg_x+5,22,(120,120,132))
        p(img,leg_x+6,22,(120,120,132))

    return img

# ── RANK C ────────────────────────────────────────────────────────────────────
# Dark forest green + bronze/gold trim + brown wood texture

def rank_c_l1():
    img = new()
    GRN = (34, 55, 30)
    BRZ = (115, 82, 16)
    WOD = (70, 50, 20)
    SHD = dk(GRN)
    HI  = lt(GRN, 1.4)
    RVT = (180,145,40)

    panels = [
        (HRI, GRN), (HF, GRN), (HL, GRN), (HBK, dk(GRN,0.75)),
        (CRI, GRN), (CF, GRN), (CL, GRN), (CBK, dk(GRN,0.75)),
        (ARI, GRN), (AF, GRN), (ALL, GRN), (ABK, dk(GRN,0.75)),
        (BRI, GRN), (BF, GRN), (BL, GRN), (BBK, dk(GRN,0.75)),
    ]
    tops = [HT,CT,AT,BT]
    bots = [HB,CB,AB,BB]
    for reg,col in panels:
        shade_panel(img, reg, col, SHD, lt(col,1.2))
    for reg in tops: r(img,*reg, dk(GRN))
    for reg in bots: r(img,*reg, dk(GRN,0.4))

    # bronze trim lines
    for x in range(8,16):  p(img,x,8,BRZ); p(img,x,15,dk(BRZ))   # helmet
    for x in range(20,28): p(img,x,20,BRZ); p(img,x,31,dk(BRZ))  # chest
    for x in range(44,48): p(img,x,20,BRZ); p(img,x,31,dk(BRZ))  # arm
    for x in range(4,8):   p(img,x,20,BRZ); p(img,x,31,dk(BRZ))  # boot

    # wood/leather texture on chest front
    for y in range(22,31,2):
        for x in range(20,28): p(img,x,y,mx(GRN,WOD,0.35))
    # bronze rivets
    for rx,ry in [(21,20),(26,20),(21,31),(26,31),(8,8),(15,8)]:
        p(img,rx,ry,RVT)
    # diagonal bronze strap
    for i in range(6): p(img,21+i,22+i,BRZ)
    # belt
    for x in range(20,28): p(img,x,27,BRZ); p(img,x,28,dk(BRZ))
    # arm stripe
    for y in range(22,30): p(img,44,y,BRZ); p(img,47,y,BRZ)
    # helmet visor: bronze bar
    for x in range(9,15): p(img,x,12,BRZ)

    return img

def rank_c_l2():
    img = new()
    GRN = (30, 48, 26)
    BRZ = (105, 75, 14)
    WOD = (65, 45, 18)
    SHD = dk(GRN)
    HI  = lt(GRN, 1.35)

    for leg_x in [0, 16]:
        r(img, leg_x+4,16, leg_x+7,19, dk(GRN))
        r(img, leg_x+8,16, leg_x+11,19, dk(GRN,0.4))
        shade_panel(img,(leg_x,20,leg_x+3,31), GRN, SHD, GRN)
        shade_panel(img,(leg_x+4,20,leg_x+7,31), GRN, SHD, HI)
        shade_panel(img,(leg_x+8,20,leg_x+11,31), GRN, GRN, SHD)
        r(img, leg_x+12,20, leg_x+15,31, dk(GRN,0.75))
        for x in range(leg_x+4, leg_x+8):
            p(img,x,20,BRZ); p(img,x,31,dk(BRZ))
        for y in range(22,30,2):
            for x in range(leg_x+4,leg_x+8): p(img,x,y,mx(GRN,WOD,0.4))
        for x in range(leg_x+4,leg_x+8): p(img,x,27,BRZ)
        p(img,leg_x+5,22,(170,130,30)); p(img,leg_x+6,22,(170,130,30))

    return img

# ── RANK B ────────────────────────────────────────────────────────────────────
# Dark navy steel + teal/cyan highlights

def rank_b_l1():
    img = new()
    STL = (30, 50, 72)
    CYN = (50, 95, 145)
    SHD = dk(STL)
    HI  = (70,115,165)
    RVT = (120,175,220)

    tops = [HT,CT,AT,BT]
    bots = [HB,CB,AB,BB]
    for reg in tops: r(img,*reg, dk(STL))
    for reg in bots: r(img,*reg, dk(STL,0.4))

    for reg in [HRI,HF,HL,CRI,CF,CL,ARI,AF,ALL,BRI,BF,BL]:
        shade_panel(img, reg, STL, SHD, HI, top_trim=CYN, bot_trim=SHD)
    for reg in [HBK,CBK,ABK,BBK]:
        r(img,*reg, dk(STL,0.7))

    # helmet: cyan visor
    for x in range(9,15): p(img,x,11,CYN); p(img,x,12,dk(CYN))
    # chest: vertical & horizontal blue lines
    for y in range(21,31): p(img,24,y,CYN)
    for x in range(20,28): p(img,x,24,CYN); p(img,x,25,dk(CYN))
    # arm band
    for x in range(44,48): p(img,x,25,CYN)
    # boot top stripe
    for x in range(4,8): p(img,x,21,CYN)
    # rivets
    for rx,ry in [(21,20),(26,20),(23,22),(24,22),(45,20),(46,20)]:
        p(img,rx,ry,RVT)

    return img

def rank_b_l2():
    img = new()
    STL = (26, 44, 66)
    CYN = (45, 88, 138)
    SHD = dk(STL)
    HI  = (62,105,155)

    for leg_x in [0, 16]:
        r(img,leg_x+4,16,leg_x+7,19, dk(STL))
        r(img,leg_x+8,16,leg_x+11,19, dk(STL,0.4))
        shade_panel(img,(leg_x,20,leg_x+3,31), STL, SHD, STL)
        shade_panel(img,(leg_x+4,20,leg_x+7,31), STL, SHD, HI, top_trim=CYN, bot_trim=SHD)
        shade_panel(img,(leg_x+8,20,leg_x+11,31), STL, STL, SHD)
        r(img,leg_x+12,20,leg_x+15,31, dk(STL,0.7))
        for x in range(leg_x+4,leg_x+8):
            p(img,x,25,CYN); p(img,x,26,dk(CYN))
        p(img,leg_x+5,22,CYN); p(img,leg_x+6,22,CYN)

    return img

# ── RANK A ────────────────────────────────────────────────────────────────────
# Deep purple near-black + bright purple runes/diamonds

def rank_a_l1():
    img = new()
    PRP = (28, 6, 52)
    BPR = (90, 18, 160)
    LPR = (140, 50, 220)
    SHD = dk(PRP)
    CLO = (15, 3, 28)   # cloak/dark
    RVT = (170, 80, 255)

    tops = [HT,CT,AT,BT]
    bots = [HB,CB,AB,BB]
    for reg in tops: r(img,*reg, dk(PRP))
    for reg in bots: r(img,*reg, dk(PRP,0.3))

    for reg in [HRI,HF,HL,HBK]:
        shade_panel(img, reg, PRP, SHD, BPR, top_trim=BPR, bot_trim=SHD)
    for reg in [CRI,CF,CL,CBK,ARI,AF,ALL,ABK,BRI,BF,BL,BBK]:
        shade_panel(img, reg, PRP, SHD, mx(PRP,BPR,0.5))

    # cloak-like dark sections on sides
    for reg in [CRI,CL,ARI,ALL]:
        shade_panel(img, reg, CLO, dk(CLO), mx(CLO,PRP,0.4))

    # rune diamond on chest front
    cx,cy = 23,25
    for dx,dy,col in [
        (0,-2,BPR),(0,-3,LPR),
        (-1,-1,BPR),(0,-1,LPR),(1,-1,BPR),
        (-2,0,BPR),(-1,0,LPR),(0,0,LPR),(1,0,LPR),(2,0,BPR),
        (-1,1,BPR),(0,1,LPR),(1,1,BPR),
        (0,2,BPR),(0,3,LPR),
    ]:
        p(img,cx+dx,cy+dy,col)

    # helmet visor: dark with purple glow edge
    for x in range(9,15): p(img,x,11,CLO); p(img,x,12,CLO)
    for x in range(9,15): p(img,x,10,BPR)
    # purple trim lines
    for x in range(8,16): p(img,x,8,BPR); p(img,x,15,SHD)
    # rivets / eye glow
    p(img,9,12,RVT); p(img,10,12,RVT)
    p(img,13,12,RVT); p(img,14,12,RVT)
    # arm rune
    p(img,45,24,LPR); p(img,46,24,LPR)
    p(img,45,25,BPR); p(img,46,25,BPR)
    p(img,45,26,LPR); p(img,46,26,LPR)

    return img

def rank_a_l2():
    img = new()
    PRP = (22, 4, 42)
    BPR = (80, 15, 145)
    LPR = (130, 45, 210)
    SHD = dk(PRP)

    for leg_x in [0, 16]:
        r(img,leg_x+4,16,leg_x+7,19, dk(PRP))
        r(img,leg_x+8,16,leg_x+11,19, dk(PRP,0.3))
        shade_panel(img,(leg_x,20,leg_x+3,31), PRP, SHD, PRP)
        shade_panel(img,(leg_x+4,20,leg_x+7,31), PRP, SHD, mx(PRP,BPR,0.6), top_trim=BPR)
        shade_panel(img,(leg_x+8,20,leg_x+11,31), PRP, PRP, SHD)
        r(img,leg_x+12,20,leg_x+15,31, dk(PRP,0.7))

        # large diamond rune on each leg front
        cx = leg_x+5
        for dx,dy,col in [
            (0,0,LPR),
            (-1,1,BPR),(0,1,LPR),(1,1,BPR),
            (-1,2,BPR),(0,2,LPR),(1,2,BPR),
            (-1,3,BPR),(0,3,LPR),(1,3,BPR),
            (0,4,LPR),
            (-1,5,BPR),(0,5,LPR),(1,5,BPR),
            (-1,6,BPR),(0,6,LPR),(1,6,BPR),
            (0,7,BPR),(0,8,BPR),
        ]:
            p(img,cx+dx,21+dy,col)
        # side purple lines
        for y in range(20,32): p(img,leg_x,y,BPR if y%2==0 else SHD)

    return img

# ── RANK S ────────────────────────────────────────────────────────────────────
# Near-black + glowing red/orange cracks and runes

def rank_s_l1():
    img = new()
    BLK = (12, 4, 4)
    DRD = (45, 8, 8)
    RED = (190, 28, 0)
    ORG = (240, 62, 0)
    SHD = (6, 2, 2)
    RVT = (255, 120, 40)

    tops = [HT,CT,AT,BT]
    bots = [HB,CB,AB,BB]
    for reg in tops: r(img,*reg, DRD)
    for reg in bots: r(img,*reg, SHD)

    for reg in [HRI,HF,HL,HBK,CRI,CF,CL,CBK,ARI,AF,ALL,ABK,BRI,BF,BL,BBK]:
        shade_panel(img, reg, DRD, SHD, mx(DRD,BLK,0.3))

    # HELMET: demon horns hint + red visor
    for x in range(8,16): p(img,x,8,RED)    # top trim = red
    for x in range(9,15): p(img,x,11,RED); p(img,x,12,(180,20,0))
    # glowing eyes
    p(img,9,12,ORG); p(img,10,12,ORG); p(img,13,12,ORG); p(img,14,12,ORG)
    # horn pixels at very top
    p(img,10,8,RED); p(img,10,7,RED); p(img,13,8,RED); p(img,13,7,RED)
    p(img,10,9,ORG); p(img,13,9,ORG)

    # CHEST cracks
    cracks_chest = [
        (21,21),(22,22),(21,23),(22,23),
        (25,22),(26,21),(26,23),(25,24),
        (23,26),(24,26),(23,27),(24,27),
        (22,29),(23,29),(22,30),(21,30),
        (25,28),(26,29),(25,30),
    ]
    for cx,cy in cracks_chest:
        p(img,cx,cy,RED)
    # inner glow (slightly brighter center)
    for cx,cy in [(22,22),(25,23),(23,27),(25,29)]:
        p(img,cx,cy,ORG)

    # chest center S-rune
    for dx,dy in [(0,0),(0,1),(-1,1),(0,2),(1,2),(0,3),(0,4)]:
        p(img,24+dx,22+dy,ORG)

    # ARM cracks
    for ax,ay in [(45,22),(46,22),(44,24),(45,25),(46,26),(45,28),(44,29)]:
        p(img,ax,ay,RED)
    p(img,45,25,ORG)

    # BOOT red trim
    for x in range(4,8): p(img,x,20,RED); p(img,x,28,RED)
    p(img,5,24,ORG); p(img,6,24,ORG)

    # Lava-glow edge on chest front top
    for x in range(20,28): p(img,x,20,RED)

    return img

def rank_s_l2():
    img = new()
    BLK = (10, 3, 3)
    DRD = (38, 7, 7)
    RED = (180, 25, 0)
    ORG = (230, 58, 0)
    SHD = (5, 2, 2)

    for leg_x in [0, 16]:
        r(img,leg_x+4,16,leg_x+7,19, DRD)
        r(img,leg_x+8,16,leg_x+11,19, SHD)
        shade_panel(img,(leg_x,20,leg_x+3,31), DRD, SHD, DRD)
        shade_panel(img,(leg_x+4,20,leg_x+7,31), DRD, SHD, mx(DRD,BLK,0.3))
        shade_panel(img,(leg_x+8,20,leg_x+11,31), DRD, DRD, SHD)
        r(img,leg_x+12,20,leg_x+15,31, dk(DRD,0.6))

        # large fire-rune diamond
        cx = leg_x+5
        for dx,dy,col in [
            (0,0,ORG),
            (-1,1,RED),(0,1,ORG),(1,1,RED),
            (-1,2,RED),(0,2,ORG),(1,2,RED),
            (-2,3,RED),(-1,3,ORG),(0,3,ORG),(1,3,ORG),(2,3,RED),
            (-1,4,RED),(0,4,ORG),(1,4,RED),
            (-1,5,RED),(0,5,ORG),(1,5,RED),
            (0,6,ORG),
            (0,7,RED),(0,8,RED),
        ]:
            p(img,cx+dx,21+dy,col)

        # crack lines on sides
        for y in range(20,32,3):
            p(img,leg_x,y,RED)
            p(img,leg_x+3,y,RED)
        # top lava glow
        for x in range(leg_x+4,leg_x+8): p(img,x,20,RED)

    return img

# ── MAIN ─────────────────────────────────────────────────────────────────────

GENERATORS = {
    "rank_e": (rank_e_l1, rank_e_l2),
    "rank_c": (rank_c_l1, rank_c_l2),
    "rank_b": (rank_b_l1, rank_b_l2),
    "rank_a": (rank_a_l1, rank_a_l2),
    "rank_s": (rank_s_l1, rank_s_l2),
}

for rank, (gen1, gen2) in GENERATORS.items():
    img1 = gen1()
    img2 = gen2()
    img1.save(os.path.join(OUT, f"{rank}_layer_1.png"))
    img2.save(os.path.join(OUT, f"{rank}_layer_2.png"))
    print(f"  {rank} done")

print("\nTerminé !")
