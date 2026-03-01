#!/usr/bin/env python3
"""
Build the androidApp module for LanguageCards.
Run from the project root or from any subdirectory.
"""
import os
import platform
import subprocess
import sys
from pathlib import Path


def find_project_root() -> Path:
    """Find the LanguageCards project root (contains gradlew)."""
    script_dir = Path(__file__).resolve().parent
    if (script_dir / "gradlew").exists() or (script_dir / "gradlew.bat").exists():
        return script_dir
    # If script is in a subdir, go up
    for parent in script_dir.parents:
        if (parent / "gradlew").exists() or (parent / "gradlew.bat").exists():
            return parent
    return script_dir


def get_java_home() -> str | None:
    """Return JAVA_HOME for Android builds. Prefer Android Studio JBR on Windows."""
    if env := os.environ.get("JAVA_HOME"):
        return env
    if platform.system() == "Windows":
        candidates = [
            Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "Android Studio" / "jbr",
            Path("C:/Program Files/Android/Android Studio/jbr"),
            Path(os.environ.get("PROGRAMFILES", "C:/Program Files")) / "Android" / "Android Studio" / "jbr",
        ]
        for p in candidates:
            if p and p.exists() and (p / "bin" / "java.exe").exists():
                return str(p)
    return None


def main() -> int:
    root = find_project_root()
    os.chdir(root)

    java_home = get_java_home()
    env = os.environ.copy()
    if java_home:
        env["JAVA_HOME"] = java_home
        print(f"Using JAVA_HOME: {java_home}")
    else:
        print("JAVA_HOME not set; using system default")

    gradlew = "gradlew.bat" if platform.system() == "Windows" else "gradlew"
    cmd = [str(root / gradlew), ":androidApp:assembleDebug"]

    print(f"Running: {' '.join(cmd)}")
    result = subprocess.run(cmd, env=env, cwd=root)
    if result.returncode == 0:
        apk = root / "androidApp" / "build" / "outputs" / "apk" / "debug" / "androidApp-debug.apk"
        print(f"\nBuild successful. APK: {apk}")
    return result.returncode


if __name__ == "__main__":
    sys.exit(main())
