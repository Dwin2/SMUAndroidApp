"""Generate Play Store assets (icon + feature graphic) from the app's brand color.

Run:
    python3 scripts/generate_play_assets.py

Outputs (PNG, no transparency, sRGB):
    play-assets/icon-512.png            -- 512x512 launcher icon
    play-assets/feature-graphic.png     -- 1024x500 feature graphic
"""
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

BRAND = (59, 89, 152)        # #3B5998 — same as ic_launcher_background.xml
WHITE = (255, 255, 255)
SOFT  = (245, 247, 252)

OUT = Path(__file__).resolve().parent.parent / "play-assets"
OUT.mkdir(exist_ok=True)


def _try_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    """Best-effort load of a system font. Falls back to PIL's default at fixed size."""
    candidates = [
        "/System/Library/Fonts/Avenir Next.ttc",
        "/System/Library/Fonts/Helvetica.ttc",
        "/Library/Fonts/Arial.ttf",
        "/System/Library/Fonts/Supplemental/Arial.ttf",
    ]
    for path in candidates:
        try:
            return ImageFont.truetype(path, size, index=1 if bold and path.endswith(".ttc") else 0)
        except (OSError, IOError):
            continue
    return ImageFont.load_default()


def draw_person_silhouette(draw: ImageDraw.ImageDraw, cx: int, cy: int, r: int, color):
    """Material 'account_circle' style person silhouette inside a circle of radius r."""
    head_r = int(r * 0.28)
    head_cy = cy - int(r * 0.22)
    draw.ellipse(
        [(cx - head_r, head_cy - head_r), (cx + head_r, head_cy + head_r)],
        fill=color,
    )
    body_w = int(r * 1.55)
    body_h = int(r * 1.10)
    body_top = cy + int(r * 0.05)
    draw.ellipse(
        [(cx - body_w // 2, body_top), (cx + body_w // 2, body_top + body_h)],
        fill=color,
    )


def make_icon():
    size = 512
    img = Image.new("RGB", (size, size), BRAND)
    draw = ImageDraw.Draw(img)

    cx = cy = size // 2
    outer_r = int(size * 0.36)

    # Outer white circle
    draw.ellipse(
        [(cx - outer_r, cy - outer_r), (cx + outer_r, cy + outer_r)],
        fill=WHITE,
    )

    # Mask the person shape in brand color so it appears "cut out" of the circle
    inner_r = int(outer_r * 0.95)
    person = Image.new("RGB", (size, size), WHITE)
    pdraw = ImageDraw.Draw(person)
    draw_person_silhouette(pdraw, cx, cy, inner_r, BRAND)

    # Composite person into circle area only (everything outside circle stays brand)
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse(
        [(cx - outer_r, cy - outer_r), (cx + outer_r, cy + outer_r)],
        fill=255,
    )
    img.paste(person, (0, 0), mask)

    out_path = OUT / "icon-512.png"
    img.save(out_path, format="PNG")
    print(f"wrote {out_path}")


def make_feature_graphic():
    w, h = 1024, 500
    img = Image.new("RGB", (w, h), BRAND)
    draw = ImageDraw.Draw(img)

    # Simple two-tone — vertical band on the right for visual interest
    band_x = int(w * 0.62)
    draw.rectangle([(band_x, 0), (w, h)], fill=(48, 73, 128))

    # Person-circle icon on the right band
    cx = (band_x + w) // 2
    cy = h // 2
    icon_r = int(h * 0.30)
    draw.ellipse(
        [(cx - icon_r, cy - icon_r), (cx + icon_r, cy + icon_r)],
        fill=WHITE,
    )
    person = Image.new("RGB", (w, h), WHITE)
    pdraw = ImageDraw.Draw(person)
    draw_person_silhouette(pdraw, cx, cy, int(icon_r * 0.95), (48, 73, 128))
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).ellipse(
        [(cx - icon_r, cy - icon_r), (cx + icon_r, cy + icon_r)],
        fill=255,
    )
    img.paste(person, (0, 0), mask)

    # Title + subtitle on the left
    title_font = _try_font(96, bold=True)
    sub_font = _try_font(36)

    title = "SMU Study"
    subtitle = "Harvard research study"

    pad = 80
    draw.text((pad, int(h * 0.30)), title, font=title_font, fill=WHITE)
    draw.text((pad + 6, int(h * 0.58)), subtitle, font=sub_font, fill=SOFT)

    out_path = OUT / "feature-graphic.png"
    img.save(out_path, format="PNG")
    print(f"wrote {out_path}")


if __name__ == "__main__":
    make_icon()
    make_feature_graphic()
