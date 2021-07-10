
![image](https://github.com/Suwayomi/Tachidesk/raw/master/server/src/main/resources/icon/faviconlogo.png)
# TachideskJUI
A free and open source manga reader to read manga from a [Tachidesk](https://github.com/Suwayomi/Tachidesk) server.

TachideskJUI can run the Tachidesk server on its own, or connect to an already hosted server. 

Any platform that runs Java can run it. On most platforms are binaries available if you don't want to install Java yourself

## Is this application usable? Should I test it?
Here is a list of current features for interaction with Tachidesk:

- Installing and uninstalling Extensions.
- Interaction with your library.
- Browsing installed sources.
- Viewing manga and chapter info.

**Note:** Keep in mind that TachideskJUI and Tachidesk are alpha software and can rarely break and/or with each update, so you may have to delete your data to fix it. See [General troubleshooting](#general-troubleshooting) and [Support and help](#support-and-help) if it happens.

## Downloading and Running the app
### All Operating Systems (x64, Java Not Included)
You should have The [Java Runtime Environment(JRE) 15](https://jdk.java.net/15/) or newer.

Download the latest jar release for your OS from [the releases section](https://github.com/Suwayomi/TachideskJUI/releases).

Double-click on the jar file or run `java -jar TachideskJUI-os-arch-X.Y.Z.jar` from a Terminal/Command Prompt window to run the app.

### Windows (x64, Java 8+ required for server)
#### Installer
Download the latest msi release from [the releases section](https://github.com/Suwayomi/TachideskJUI/releases).
#### Scoop
1. Add Witchilich's Scoop bucket - `scoop bucket add witchilich https://github.com/Witchilich/scoop-witchilich`
2. Add the Java Scoop bucket - `scoop bucket add java`
3. Install TachideskJUI - `scoop install tachideskjui`

### MacOS (x64, Java 8+ required for server)
Download the latest dmg release from [the releases section](https://github.com/Suwayomi/TachideskJUI/releases).

### Debian based Linux (x64, Java 8+ required for server)
Download the latest deb release from [the releases section](https://github.com/Suwayomi/TachideskJUI/releases).

### Fedora based Linux (x64, Java 8+ required for server)
Download the latest rpm release from [the releases section](https://github.com/Suwayomi/TachideskJUI/releases).

### Arch based Linux (x64, Java Included)
Download the latest release from [the aur](https://aur.archlinux.org/packages/tachidesk-jui/).

If you use yay, you can run `yay -S tachidesk-jui` inside a terminal window.

## General troubleshooting
### I'm having issues starting the application
Make sure you have used either an installer, or you have Java 15 installed.

### It says server failed to start
Make sure that if you used an installer, that you have at least Java 8 installed.

## Support and help
Join Tachidesk's [discord server](https://discord.gg/wgPyb7hE5d) to hang out with the community and receive support and help.

## Building from source
### Prerequisite: Getting Tachidesk.jar
#### Linux
Run `./scripts/SetupUnix.sh` in bash from project's root directory to download and rebuild the Tachidesk jar without the WebUI.
#### Windows
Run `&"./scripts/SetupWindows.ps1"` in powershell from project's root directory to download and rebuild the Tachidesk jar without the WebUI.
### Prerequisite: Software dependencies
You need this software packages installed in order to build this project:
- Java Development Kit and Java Runtime Environment version 15, this can be handled by IntelliJ
### building a jar for your OS
Run `./gradlew packageUberJarForCurrentOS`, the resulting built jar file will be `build/compose/TachideskJUI-os-arch-X.Y.Z.jar`.


## Translation
Feel free to translate the project on [Weblate](https://hosted.weblate.org/projects/tachideskjui/desktop/)

<details><summary>Translation Progress</summary>
<a href="https://hosted.weblate.org/engage/tachideskjui/">
<img src="https://hosted.weblate.org/widgets/tachideskjui/-/desktop/multi-auto.svg" alt="Translation status" />
</a>
</details>

## Credit
The `Tachidesk` project is developed by [@AriaMoradi](https://github.com/AriaMoradi) and contributors, a link for [Tachidesk is provided here](https://github.com/Suwayomi/Tachidesk) and is licensed under `Mozilla Public License v2.0`.

Parts of [Tachiyomi-1.x](https://github.com/tachiyomiorg/tachiyomi-1.x) is adopted into this codebase, also licensed under `Mozilla Public License v2.0`.

You can obtain a copy of `Mozilla Public License v2.0` from https://mozilla.org/MPL/2.0/

## License

    Copyright (C) 2020-2021 Syer and contributors

    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.
