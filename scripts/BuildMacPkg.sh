#!/bin/bash

if [ "$(basename "$(pwd)")" = "scripts" ]; then
  cd ..
fi

if test -f "src/main/resources/Tachidesk.jar"; then
    echo "Tachidesk.jar exists, removing as MacOS installers cannot run it"
    rm "src/main/resources/Tachidesk.jar"
fi

echo "Building Pkg package"
./gradlew packagePkg "$@"