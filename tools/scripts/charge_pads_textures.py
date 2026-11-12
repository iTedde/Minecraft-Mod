"""Cut the Charge Pads textures and the GUI out of ftb ultimate/mods/ChargePads.zip (spec 47 par. 2.1, par. 9).

Index = row * 16 + column of myrathi/ic2/chargepads/gfx/blocks.png (16 x 16 px tiles). Per pad (static, crystalizor,
lapotronic): bottom 0-2, top inactive 16-18, side 32-34 (only rows 12-15 are drawn), top active 48-50.
Items (phase 47.2): upgrade kits 64-66, efficiency upgrade 82, modules 80 + offset (original enum u).
"""
import io
import sys
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(sys.argv[1])
ASSETS = ROOT / "src/main/resources/assets/craft"
ZIP = ROOT / "ftb ultimate/mods/ChargePads.zip"
GFX = "myrathi/ic2/chargepads/gfx/"

PADS = ["static", "crystalizor", "lapotronic"]
BLOCKS = {}
for column, pad in enumerate(PADS):
    BLOCKS[column] = pad + "_charge_pad_bottom"
    BLOCKS[16 + column] = pad + "_charge_pad_top"
    BLOCKS[32 + column] = pad + "_charge_pad_side"
    BLOCKS[48 + column] = pad + "_charge_pad_top_active"

ITEMS = {
    64: "crystalizor_upgrade_kit", 65: "mfs_unit_upgrade", 66: "lapotronic_upgrade_kit", 82: "efficiency_upgrade",
    80: "drain_conversion_module", 81: "damage_conversion_module", 98: "armour_priority_module",
    96: "proximity_booster_module", 97: "wide_band_booster_module", 112: "field_expansion_module_alpha",
    113: "field_expansion_module_delta", 114: "field_expansion_module_theta",
}


def tile(sheet, index):
    x, y = index % 16 * 16, index // 16 * 16
    return sheet.crop((x, y, x + 16, y + 16))


def save(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


with zipfile.ZipFile(ZIP) as archive:
    sheet = Image.open(io.BytesIO(archive.read(GFX + "blocks.png"))).convert("RGBA")
    for index, name in BLOCKS.items():
        save(tile(sheet, index), ASSETS / "textures/block/chargepads" / (name + ".png"))
    for index, name in ITEMS.items():
        save(tile(sheet, index), ASSETS / "textures/item/chargepads" / (name + ".png"))
    gui = Image.open(io.BytesIO(archive.read(GFX + "GUIChargePad.png"))).convert("RGBA")
    save(gui, ASSETS / "textures/gui/chargepads/charge_pad.png")
print("done")
