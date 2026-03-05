#!/usr/bin/env python3
"""
Run Android unit tests for the LanguageCards project.

Executes Gradle test tasks for the shared module (commonTest) and androidApp.
Runs on the JVM—no device or emulator required.
Sets JAVA_HOME if unset, using Android Studio's bundled JDK when available.
"""

import os
import subprocess
import sys
from pathlib import Path
from typing import Optional


def find_java_home() -> Optional[str]:
    """Return JAVA_HOME path, or None if not found."""
    if existing := os.environ.get("JAVA_HOME"):
        if Path(existing).exists():
            return existing

    candidates = []
    if os.name == "nt":
        candidates = [
            Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "Android Studio" / "jbr",
            Path("C:/Program Files/Android/Android Studio/jbr"),
            Path(os.environ.get("PROGRAMFILES", "C:/Program Files")) / "Android" / "Android Studio" / "jbr",
        ]
    else:
        candidates = [
            Path.home() / "Android/Sdk/../Android Studio.app/Contents/jbr",
            Path("/opt/android-studio/jbr"),
        ]

    for path in candidates:
        if path and path.exists():
            return str(path.resolve())
    return None


def main() -> int:
    project_root = Path(__file__).resolve().parent
    os.chdir(project_root)

    env = os.environ.copy()
    if not env.get("JAVA_HOME") or not Path(env.get("JAVA_HOME", "")).exists():
        if java_home := find_java_home():
            env["JAVA_HOME"] = java_home
            print(f"Using JAVA_HOME: {java_home}")
        else:
            print("Warning: JAVA_HOME not set and no JDK found. Gradle may fail.")

    gradlew = "gradlew.bat" if os.name == "nt" else "./gradlew"
    if not Path(gradlew).exists():
        gradlew = "gradlew"

    tasks = [":androidApp:testDebugUnitTest"]

    cmd = [gradlew, "--no-daemon"] + tasks
    print(f"Running: {' '.join(cmd)}")
    result = subprocess.run(cmd, env=env)
    return result.returncode


if __name__ == "__main__":
    sys.exit(main())
