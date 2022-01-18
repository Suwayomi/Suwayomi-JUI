#!/bin/bash

rpm="$(find ./build/compose/binaries/main/rpm/*.rpm)"
dir="$(dirname "$rpm")"

echo "Adding java dependency to rpm file $rpm in $dir"

rpmrebuild -d "$dir" -np --change-spec-requires="echo Requires: java-1.8.0-openjdk-headless" "$rpm"

echo "Cleaning up"
rm -f "$rpm"
mv "$dir"/*/*'.rpm' "$dir"