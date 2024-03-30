#!/bin/bash

if [[ "$*" == *"preview"* ]]; then
    name="Suwayomi-JUI-Preview"
else
    name="Suwayomi-JUI"
fi

msi="$(find ./ -iname '*.msi' 2>/dev/null)"
if [ -f "$msi" ]; then
  dir="$(dirname "$msi")"
  version=$(tmp="${msi%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$msi")" != "$name-windows-x64-$version.msi" ]; then
    mv "$msi" "$dir/$name-windows-x64-$version.msi"
  fi
fi

dmg_x64="$(find ./runner-package-osx-x64/binaries/\(main-release\|main\)/dmg/ -iname '*.dmg' 2>/dev/null)"
if [ -f "$dmg_x64" ]; then
  dir="$(dirname "$dmg_x64")"
  version=$(tmp="${dmg_x64%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$dmg_x64")" != "$name-macos-x64-$version.dmg" ]; then
    mv "$dmg_x64" "$dir/$name-macos-x64-$version.dmg"
  fi
fi

dmg_arm64="$(find ./runner-package-osx-arm64/binaries/\(main-release\|main\)/dmg/ -iname '*.dmg' 2>/dev/null)"
if [ -f "$dmg_arm64" ]; then
  dir="$(dirname "$dmg_arm64")"
  version=$(tmp="${dmg_arm64%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$dmg_arm64")" != "$name-macos-m1-$version.dmg" ]; then
    mv "$dmg_arm64" "$dir/$name-macos-m1-$version.dmg"
  fi
fi

apk="$(find ./ -iname '*.apk' 2>/dev/null)"
if [ -f "$apk" ]; then
  dir="$(dirname "$apk")"

  if [ "$(basename "$apk")" != "$name-android.apk" ]; then
    mv "$apk" "$dir/$name-android.apk"
  fi
fi
