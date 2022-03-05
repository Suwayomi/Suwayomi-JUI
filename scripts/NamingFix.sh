#!/bin/bash

msi="$(find ./ -iname '*.msi' 2>/dev/null)"
if [ -f "$msi" ]; then
  dir="$(dirname "$msi")"
  version=$(tmp="${msi%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$msi")" != "Tachidesk-JUI-windows-x64-$version.msi" ]; then
    mv "$msi" "$dir/Tachidesk-JUI-windows-x64-$version.msi"
  fi
fi

dmg="$(find ./ -iname '*.dmg' 2>/dev/null)"
if [ -f "$dmg" ]; then
  dir="$(dirname "$dmg")"
  version=$(tmp="${dmg%.*}" && echo "${tmp##*-}")

  if [ "$(basename "$dmg")" != "Tachidesk-JUI-macos-x64-$version.dmg" ]; then
    mv "$dmg" "$dir/Tachidesk-JUI-macos-x64-$version.dmg"
  fi
fi

apk="$(find ./ -iname '*.apk' 2>/dev/null)"
if [ -f "$apk" ]; then
  dir="$(dirname "$apk")"

  if [ "$(basename "$apk")" != "Tachidesk-JUI-android.apk" ]; then
    mv "$apk" "$dir/Tachidesk-JUI-android.apk"
  fi
fi
