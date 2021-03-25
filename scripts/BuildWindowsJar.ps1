if ($(Split-Path -Path (Get-Location) -Leaf) -eq "scripts" ) {
    Set-Location ..
}

if (Test-Path "src/main/resources/Tachidesk.jar" -PathType leaf)
{
    Write-Output "Tachidesk.jar already exists"
}
else
{
    &"./scripts/SetupWindows.ps1"
}

Write-Output "Building Jar package"
&"./gradlew" packageUberJarForCurrentOS