"""Cut the MFFS textures and GUIs out of ftb ultimate/mods/modularforcefieldsystem.zip (spec 48 par. 1, par. 13).

Pack GraphicStyle=1: machine textures *_ue.png (512 px, 32 px tiles): 0 side, 1 front, 2 back, 4-6 the same active;
the projector has one row per module type (ProTyp 0-8). Force field: blocks.png tiles 0-3, camouflage 180 (empty).
Items: items.png (16 px tiles, index = row * 16 + column). Ore: forciciumore.png tile 0; the deepslate variant puts
the green ore pixels onto the vanilla deepslate texture (argument 2: the Minecraft client jar).
"""
import io
import sys
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(sys.argv[1])
CLIENT_JAR = Path(sys.argv[2])
ASSETS = ROOT / "src/main/resources/assets/craft"
ZIP = ROOT / "ftb ultimate/mods/modularforcefieldsystem.zip"
SPRITES = "chb/mods/mffs/sprites/"

MACHINES = {"Extractor_ue.png": "mffs_extractor", "Capacitor_ue.png": "mffs_capacitor",
            "Converter_ue.png": "mffs_converter"}
FACES = {0: "side", 1: "front", 2: "back", 4: "side_active", 5: "front_active", 6: "back_active"}
ITEMS = {
    97: "forcicium", 98: "forcicium_cell", 66: "projector_focus_matrix", 64: "field_modulator_distance",
    65: "field_modulator_strength", 37: "mffs_extractor_booster", 32: "mffs_capacitor_upgrade_capacity",
    33: "mffs_capacitor_upgrade_range", 16: "mffs_card_blank", 17: "mffs_card_power_link",
    21: "mffs_card_infinite_power", 0: "mffs_multitool_wrench", 1: "mffs_multitool_switch",
    49: "projector_module_wall", 50: "projector_module_deflector", 51: "projector_module_tube",
    52: "projector_module_sphere", 53: "projector_module_cube", 54: "projector_module_containment",
    55: "projector_module_adv_cube", 56: "projector_module_diagonal_wall",
    36: "projector_upgrade_touch_damage", 35: "projector_upgrade_sponge", 34: "projector_upgrade_field_manipulator",
    38: "projector_upgrade_block_breaker", 41: "projector_upgrade_field_jammer", 42: "projector_upgrade_camouflage",
    43: "projector_upgrade_field_fusion", 40: "projector_upgrade_npc_defense", 39: "projector_upgrade_defense_station",
    4: "mffs_multitool_field_teleporter",
}
FIELD_TYPES = {180: "camouflage", 0: "default", 1: "zapper", 2: "area", 3: "containment"}
CRYSTAL_STEPS = range(5)  # icons 112-116
GUIS = {"GuiExtractor.png": "extractor", "GuiCapacitor.png": "capacitor", "GuiConvertor.png": "converter",
        "GuiProjector.png": "projector", "items.png": "items"}


def tile(sheet, index, size):
    x, y = index % 16 * size, index // 16 * size
    return sheet.crop((x, y, x + size, y + size))


def save(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def is_ore_pixel(pixel):
    r, g, b, _ = pixel
    return max(r, g, b) - min(r, g, b) > 24


with zipfile.ZipFile(ZIP) as archive:
    def sheet(name):
        return Image.open(io.BytesIO(archive.read(SPRITES + name))).convert("RGBA")

    for file, name in MACHINES.items():
        image = sheet(file)
        for index, face in FACES.items():
            save(tile(image, index, 32), ASSETS / "textures/block/mffs" / f"{name}_{face}.png")
    projector = sheet("projector_ue.png")
    for typ in range(9):
        for index, face in FACES.items():
            save(tile(projector, typ * 16 + index, 32),
                 ASSETS / "textures/block/mffs" / f"mffs_projector_{typ}_{face}.png")
    fields = sheet("blocks.png")
    for index, name in FIELD_TYPES.items():
        save(tile(fields, index, 16), ASSETS / "textures/block/mffs" / f"force_field_{name}.png")
    items = sheet("items.png")
    for index, name in ITEMS.items():
        save(tile(items, index, 16), ASSETS / "textures/item/mffs" / (name + ".png"))
    for step in CRYSTAL_STEPS:
        save(tile(items, 112 + step, 16), ASSETS / "textures/item/mffs" / f"force_energy_crystal_{step}.png")
    for file, name in GUIS.items():
        save(sheet(file), ASSETS / "textures/gui/mffs" / (name + ".png"))
    ore = tile(sheet("forciciumore.png"), 0, 16)
    save(ore, ASSETS / "textures/block/mffs/monazit_ore.png")

with zipfile.ZipFile(CLIENT_JAR) as jar:
    deepslate = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/block/deepslate.png"))).convert("RGBA")
deep = deepslate.copy()
for y in range(16):
    for x in range(16):
        pixel = ore.getpixel((x, y))
        if is_ore_pixel(pixel):
            deep.putpixel((x, y), pixel)
save(deep, ASSETS / "textures/block/mffs/deepslate_monazit_ore.png")
print("done")
