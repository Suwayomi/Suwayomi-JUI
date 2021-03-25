if ($(Split-Path -Path (Get-Location) -Leaf) -eq "scripts" ) {
  Set-Location ..
}

Remove-Item -Recurse -Force "tmp" | Out-Null
New-Item -ItemType Directory -Force -Path "tmp"

Write-Output "Getting latest Tachidesk build files"
$zipball = (Invoke-WebRequest -Uri "https://api.github.com/repos/Suwayomi/Tachidesk/releases/latest").content | Select-String -Pattern 'https[\.:\/A-Za-z0-9]*zipball\/[a-zA-Z0-9.]*' -CaseSensitive

Invoke-WebRequest -Uri $zipball.Matches.Value -OutFile tmp/Tachidesk.zip

Expand-Archive -Path "tmp/Tachidesk.zip" -DestinationPath "tmp"

$tachidesk_folder = Get-ChildItem -Path "tmp" | Where-Object {$_.Name -match ".*Tachidesk-[a-z0-9]*"} | Select-Object FullName

Push-Location $tachidesk_folder.FullName

Write-Output "Setting up android.jar"
&"./AndroidCompat/getAndroid.ps1"

Write-Output "Building Tachidesk.jar"
&"./gradlew" :server:shadowJar -x :webUI:copyBuild

$tachidesk_jar = $(Get-ChildItem "server/build" | Where-Object { $_.Name -match '.*\.jar' })[0].FullName

Pop-Location

Write-Output "Copying Tachidesk.jar to resources folder..."
Move-Item -Force $tachidesk_jar "src/main/resources/Tachidesk.jar"

Write-Output "Cleaning up..."
Remove-Item -Recurse -Force "tmp" | Out-Null

Write-Output "Done!"