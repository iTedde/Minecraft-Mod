"""Cut the Nuclear Control textures, GUIs and sounds out of ftb ultimate/mods/IC2NuclearControl.zip (spec 46 §9).

Index = row * 16 + column of img/texture_thermo.png (16 x 16 px tiles), as in BlockNuclearControlMain.sideMapping
and the item icon indices.
"""
import io
import sys
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(sys.argv[1])
ASSETS = ROOT / "src/main/resources/assets/craft"
ZIP = ROOT / "ftb ultimate/mods/IC2NuclearControl.zip"

BLOCKS = {
    0: "thermal_monitor_normal", 16: "thermal_monitor_hot", 32: "thermal_monitor_none",
    1: "thermal_monitor_back", 17: "thermal_monitor_side",
    3: "industrial_alarm_lamp_off", 19: "industrial_alarm_lamp_dim", 35: "industrial_alarm_lamp_bright",
    4: "industrial_alarm_back", 5: "industrial_alarm_side",
    7: "howler_alarm_front", 8: "howler_alarm_back", 9: "howler_alarm_side",
    25: "remote_thermal_monitor_front", 23: "remote_thermal_monitor_back", 24: "remote_thermal_monitor_side",
    26: "thermo_bar",
}
ITEMS = {
    2: "thermometer", 18: "digital_thermometer", 34: "remote_sensor_kit",
    50: "reactor_sensor_location_card", 66: "range_upgrade",
}
GUIS = {
    "GUIThermalMonitor": "thermal_monitor", "GUIRemoteThermo": "remote_thermal_monitor",
    "GUIHowlerAlarm": "howler_alarm", "GUIIndustrialAlarm": "industrial_alarm",
}
SOUNDS = {"alarm-default": "alarm_default", "alarm-sci-fi": "alarm_sci_fi"}


def tile(sheet, index):
    x, y = index % 16 * 16, index // 16 * 16
    return sheet.crop((x, y, x + 16, y + 16))


def save(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


with zipfile.ZipFile(ZIP) as archive:
    sheet = Image.open(io.BytesIO(archive.read("img/texture_thermo.png"))).convert("RGBA")
    for index, name in BLOCKS.items():
        save(tile(sheet, index), ASSETS / "textures/block/nuclearcontrol" / f"{name}.png")
    for index, name in ITEMS.items():
        save(tile(sheet, index), ASSETS / "textures/item/nuclearcontrol" / f"{name}.png")
    for source, name in GUIS.items():
        gui = Image.open(io.BytesIO(archive.read(f"img/{source}.png"))).convert("RGBA")
        save(gui, ASSETS / "textures/gui/nuclearcontrol" / f"{name}.png")
    for source, name in SOUNDS.items():
        target = ASSETS / "sounds/nuclearcontrol" / f"{name}.ogg"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(archive.read(f"sound/{source}.ogg"))
print("ok")
