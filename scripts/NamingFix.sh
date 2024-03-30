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

# Find DMG
dmg_dirs="$(find ./ -type d -iname '*-macos-*' 2>/dev/null)"
for dir in $dmg_dirs; do
  arch=$(basename "$dir" | cut -d'-' -f3) # Extract architecture from directory name
  dmg="$dir/*.dmg"
  if [ "$(ls -A $dir/*.dmg 2>/dev/null)" ]; then
    version=$(tmp=$(basename $dir/*.dmg .dmg) && echo "${tmp##*-}")

    if [ "$arch" == "x64" ]; then
      if [ "$(basename $dir/*.dmg)" != "$name-macos-x64-$version.dmg" ]; then
        mv $dir/*.dmg "$dir/$name-macos-x64-$version.dmg"
      fi
    elif [ "$arch" == "arm64" ]; then
      if [ "$(basename $dir/*.dmg)" != "$name-macos-arm64-$version.dmg" ]; then
        mv $dir/*.dmg "$dir/$name-macos-arm64-$version.dmg"
      fi
    fi
  fi
done

apk="$(find ./ -iname '*.apk' 2>/dev/null)"
if [ -f "$apk" ]; then
  dir="$(dirname "$apk")"

  if [ "$(basename "$apk")" != "$name-android.apk" ]; then
    mv "$apk" "$dir/$name-android.apk"
  fi
fi
