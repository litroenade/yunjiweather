param(
    [string] $AdbPath = "",
    [string] $PackageName = "com.litroenade.yunjiweather",
    [int] $WarmupSeconds = 12
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($AdbPath)) {
    $sdkRoot = if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_HOME)) {
        $env:ANDROID_HOME
    } elseif (-not [string]::IsNullOrWhiteSpace($env:ANDROID_SDK_ROOT)) {
        $env:ANDROID_SDK_ROOT
    } else {
        ""
    }
    $AdbPath = if ([string]::IsNullOrWhiteSpace($sdkRoot)) {
        "adb"
    } else {
        Join-Path $sdkRoot "platform-tools\adb.exe"
    }
}

function Invoke-Checked {
    param(
        [string] $FilePath,
        [string[]] $Arguments
    )
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed: $FilePath $($Arguments -join ' ')"
    }
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$artifactDir = Join-Path $root "artifacts\ui_qa"
New-Item -ItemType Directory -Force -Path $artifactDir | Out-Null

$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$devicesOutput = & $AdbPath devices
$devicesOutput | Out-File -FilePath (Join-Path $artifactDir "adb_devices_$stamp.txt") -Encoding utf8
$serials = $devicesOutput |
    Select-String -Pattern "^\S+\s+device$" |
    ForEach-Object { ($_.Line -split "\s+")[0] }

if ($serials.Count -eq 0) {
    $noDeviceFile = Join-Path $artifactDir "NO_DEVICE_$stamp.txt"
    @(
        "No connected Android device was available.",
        "Dynamic weather effects were not claimed as real-device verified.",
        "Required scenarios when a device is attached:",
        "- official theme: sunny/day, rain/day, snow/night",
        "- panorama theme: sunny/day, rain/dusk, snow/night",
        "- custom theme: imported portrait image, top/center/bottom crop anchors",
        "- animation off: hero and atmosphere stop moving",
        "- pull refresh: last update label stays readable over animated background"
    ) | Out-File -FilePath $noDeviceFile -Encoding utf8
    Write-Host "No connected device. Wrote $noDeviceFile"
    exit 0
}

$serial = $serials[0]
Push-Location $root
try {
    Invoke-Checked ".\gradlew.bat" @(":app:installDebug", "--no-daemon")
} finally {
    Pop-Location
}

& $AdbPath -s $serial logcat -c
& $AdbPath -s $serial shell dumpsys gfxinfo $PackageName reset | Out-Null
Invoke-Checked $AdbPath @("-s", $serial, "shell", "monkey", "-p", $PackageName, "1")
Start-Sleep -Seconds 4

$startupScreenshot = Join-Path $artifactDir "startup_$stamp.png"
& $AdbPath -s $serial exec-out screencap -p > $startupScreenshot

Start-Sleep -Seconds $WarmupSeconds
$motionScreenshot = Join-Path $artifactDir "motion_$stamp.png"
& $AdbPath -s $serial exec-out screencap -p > $motionScreenshot

& $AdbPath -s $serial shell dumpsys gfxinfo $PackageName framestats |
    Out-File -FilePath (Join-Path $artifactDir "gfxinfo_$stamp.txt") -Encoding utf8
& $AdbPath -s $serial shell dumpsys meminfo $PackageName |
    Out-File -FilePath (Join-Path $artifactDir "meminfo_$stamp.txt") -Encoding utf8
& $AdbPath -s $serial logcat -d -t 600 |
    Out-File -FilePath (Join-Path $artifactDir "logcat_$stamp.txt") -Encoding utf8

@(
    "Device: $serial",
    "Package: $PackageName",
    "Startup screenshot: $startupScreenshot",
    "Motion screenshot: $motionScreenshot",
    "Next manual QA: open Settings > Developer tools, tap temperature five times, verify official/panorama with sunny/rain/snow and dawn/day/dusk/night combinations."
) | Out-File -FilePath (Join-Path $artifactDir "summary_$stamp.txt") -Encoding utf8

Write-Host "Android UI QA artifacts written to $artifactDir"
