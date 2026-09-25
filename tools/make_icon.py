#!/usr/bin/env python3
"""
NoteVault app icon generator.

Pure Python, standard library only (zlib + struct). No Pillow, no ImageMagick,
no pip install needed. Draws two variants:

  legacy      - full-bleed rounded square with the glyph (used on old launchers)
  foreground  - transparent background, glyph inside the adaptive-icon safe
                zone (used on Android 8+ with mipmap-anydpi-v26)

Design: a white note page with three violet text lines, and an amber padlock
overlapping the bottom-right corner. Notes + privacy.

Usage:
    python3 make_icon.py legacy     192 mipmap-xxxhdpi/ic_launcher.png
    python3 make_icon.py foreground 432 mipmap-xxxhdpi/ic_launcher_foreground.png
"""

import math
import struct
import sys
import zlib

# ---- palette -----------------------------------------------------------
BG_TOP = (76, 29, 149)     # deep violet   #4C1D95
BG_BOT = (124, 58, 237)    # bright violet #7C3AED
PAPER = (255, 255, 255)
INK = (109, 40, 217)       # violet for the text lines
AMBER = (251, 191, 36)     # #FBBF24 padlock
AMBER_DARK = (180, 83, 9)  # #B45309 keyhole / shackle shade


def chunk(tag, data):
    return (struct.pack('>I', len(data)) + tag + data +
            struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff))


def write_png(path, w, h, px):
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    raw = b''.join(
        b'\x00' + bytes(px[y * w * 4:(y + 1) * w * 4]) for y in range(h)
    )
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n' +
                chunk(b'IHDR', ihdr) +
                chunk(b'IDAT', zlib.compress(raw, 9)) +
                chunk(b'IEND', b''))


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def in_round_rect(x, y, x0, y0, x1, y1, r):
    if x < x0 or x > x1 or y < y0 or y > y1:
        return False
    cx = min(max(x, x0 + r), x1 - r)
    cy = min(max(y, y0 + r), y1 - r)
    return (x - cx) ** 2 + (y - cy) ** 2 <= r * r


def in_ring(x, y, cx, cy, r_in, r_out):
    """Filled annulus - used for the padlock shackle."""
    d2 = (x - cx) ** 2 + (y - cy) ** 2
    return r_in * r_in <= d2 <= r_out * r_out


def in_circle(x, y, cx, cy, r):
    return (x - cx) ** 2 + (y - cy) ** 2 <= r * r


def sample(sx, sy, mode, g, size):
    """Return the RGB colour at this supersample point, or None for transparent."""
    # g = (gx0, gy0, gx1, gy1) glyph bounding box in pixels
    gx0, gy0, gx1, gy1 = g
    gw = gx1 - gx0
    gh = gy1 - gy0

    def u(px, py):
        """Map absolute px into the glyph's 0..1 space."""
        return ((px - gx0) / gw, (py - gy0) / gh)

    # --- glyph geometry in 0..1 space ---
    page = (0.07, 0.04, 0.85, 0.93)
    lock_body = (0.47, 0.55, 0.97, 1.00)
    shack_cx, shack_cy = 0.72, 0.60
    shack_r_out, shack_r_in = 0.165, 0.093

    ux, uy = u(sx, sy)

    # 1) the paper and its text lines
    c = None
    if in_round_rect(sx, sy, gx0 + page[0] * gw, gy0 + page[1] * gh,
                     gx0 + page[2] * gw, gy0 + page[3] * gh, 0.09 * gw):
        c = PAPER
        for ly, right in [(0.28, 0.69), (0.43, 0.69), (0.58, 0.55)]:
            if in_round_rect(sx, sy,
                             gx0 + 0.19 * gw, gy0 + ly * gh,
                             gx0 + right * gw, gy0 + (ly + 0.062) * gh,
                             0.031 * gw):
                c = INK

    # 2) the padlock, drawn ON TOP of the paper
    if uy <= shack_cy and in_ring(sx, sy,
                                  gx0 + shack_cx * gw, gy0 + shack_cy * gh,
                                  shack_r_in * gw, shack_r_out * gw):
        return AMBER_DARK

    if in_round_rect(sx, sy, gx0 + lock_body[0] * gw, gy0 + lock_body[1] * gh,
                     gx0 + lock_body[2] * gw, gy0 + lock_body[3] * gh, 0.10 * gw):
        if in_circle(sx, sy, gx0 + 0.72 * gw, gy0 + 0.73 * gh, 0.055 * gw):
            return AMBER_DARK
        return AMBER

    if c is not None:
        return c

    if mode == 'foreground':
        return None

    # legacy: violet gradient background behind everything, spanning the canvas
    return lerp(BG_TOP, BG_BOT, min(1.0, max(0.0, sy / size)))


def build(size, mode):
    if mode == 'legacy':
        g = (0.20 * size, 0.20 * size, 0.80 * size, 0.80 * size)
        r = 0.22 * size
    else:
        # adaptive icons are masked to a circle covering the middle 72/108 of
        # the canvas, so the glyph is kept a little smaller to survive it.
        g = (0.22 * size, 0.22 * size, 0.78 * size, 0.78 * size)
        r = 0.0

    j = 3
    offs = [(dx / j, dy / j) for dx in range(j) for dy in range(j)]
    px = bytearray(size * size * 4)
    n = len(offs)

    for y in range(size):
        for x in range(size):
            rr = gg = bb = aa = 0
            for dx, dy in offs:
                sx, sy = x + dx, y + dy
                if r > 0 and not in_round_rect(sx, sy, 0, 0, size, size, r):
                    continue
                c = sample(sx, sy, mode, g, size)
                if c is None:
                    continue
                rr += c[0]
                gg += c[1]
                bb += c[2]
                aa += 1
            if aa == 0:
                continue
            i = (y * size + x) * 4
            px[i] = rr // aa
            px[i + 1] = gg // aa
            px[i + 2] = bb // aa
            px[i + 3] = 255 * aa // n
    return px


def main():
    if len(sys.argv) != 4:
        print(__doc__)
        sys.exit(1)
    mode = sys.argv[1]
    if mode not in ('legacy', 'foreground'):
        print('mode must be "legacy" or "foreground"')
        sys.exit(1)
    size = int(sys.argv[2])
    out = sys.argv[3]
    write_png(out, size, size, build(size, mode))
    print('wrote', out, size, 'x', size, '(' + mode + ')')


if __name__ == '__main__':
    main()
