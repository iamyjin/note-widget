#!/usr/bin/env bash
# Command-line build without Gradle.
# Needs: aapt, javac (JDK 8+), d8 or dx, zipalign, apksigner, keytool
# and an android.jar of API 31+:  export ANDROID_JAR=/path/to/platforms/android-34/android.jar
# Output: build/Note.apk, signed with the standard Android debug keystore
# (created on first run in ~/.android/debug.keystore, or set KEYSTORE=/path).
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_JAR:?set ANDROID_JAR to the path of android.jar (platforms/android-34/android.jar)}"

SRC=app/src/main
B=build
VC=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' app/build.gradle.kts)
VN=$(sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' app/build.gradle.kts)

rm -rf "$B" && mkdir -p "$B/gen" "$B/obj"

# The Gradle manifest has no package attribute (namespace lives in build.gradle.kts); aapt needs it.
sed 's|<manifest |<manifest package="app.note" |' "$SRC/AndroidManifest.xml" > "$B/AndroidManifest.xml"

aapt package -f -m -J "$B/gen" -M "$B/AndroidManifest.xml" -S "$SRC/res" -I "$ANDROID_JAR" \
     --min-sdk-version 26 --target-sdk-version 34
javac --release 8 -Xlint:-options -cp "$ANDROID_JAR" -d "$B/obj" \
      $(find "$B/gen" "$SRC/java" -name '*.java')

if command -v d8 >/dev/null 2>&1; then
  d8 --min-api 26 --lib "$ANDROID_JAR" --output "$B" $(find "$B/obj" -name '*.class')
else
  "${DX:-dx}" --dex --output="$B/classes.dex" "$B/obj"
fi

aapt package -f -M "$B/AndroidManifest.xml" -S "$SRC/res" -I "$ANDROID_JAR" \
     --min-sdk-version 26 --target-sdk-version 34 \
     --version-code "$VC" --version-name "$VN" -F "$B/unsigned.apk"
( cd "$B" && aapt add unsigned.apk classes.dex >/dev/null )
zipalign -f 4 "$B/unsigned.apk" "$B/aligned.apk"

KS="${KEYSTORE:-$HOME/.android/debug.keystore}"
if [ ! -f "$KS" ]; then
  mkdir -p "$(dirname "$KS")"
  keytool -genkeypair -keystore "$KS" -alias androiddebugkey -storepass android -keypass android \
          -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
fi
apksigner sign --ks "$KS" --ks-pass pass:android --key-pass pass:android \
               --ks-key-alias androiddebugkey --out "$B/Note.apk" "$B/aligned.apk"
echo "Built $B/Note.apk (versionCode $VC, $VN)"
