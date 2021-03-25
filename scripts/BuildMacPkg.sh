#!/bin/bash

if [ "$(basename "$(pwd)")" = "scripts" ]; then
  cd ..
fi

if test -f "src/main/resources/Tachidesk.jar"; then
    echo "Tachidesk.jar already exists"
else
    scripts/SetupUnix.sh
fi


echo "Building Pkg package"
./gradlew packagePkg