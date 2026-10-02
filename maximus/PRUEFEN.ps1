# MAXIMUS: prueft, ob der Projektordner vollstaendig ist.
# Aufruf in PowerShell im Ordner "maximus" (dort, wo settings.gradle.kts liegt):
#   powershell -ExecutionPolicy Bypass -File .\PRUEFEN.ps1
$expected = Get-Content -Path ".\DATEILISTE-KOMPLETT.txt" | Where-Object { $_.Trim() -ne "" }
$missing = @()
foreach ($f in $expected) {
    if (-not (Test-Path -LiteralPath $f)) { $missing += $f }
}
Write-Host ("Erwartet: {0} Dateien" -f $expected.Count)
if ($missing.Count -eq 0) {
    Write-Host "Alles vorhanden." -ForegroundColor Green
} else {
    Write-Host ("FEHLEN: {0} Dateien" -f $missing.Count) -ForegroundColor Red
    $missing | ForEach-Object { Write-Host "  $_" }
}
# Veraltete Dateien aus fruehen Paketen, die nicht mehr gebraucht werden:
$stale = @(
  "app\src\main\res\font\cormorant_bold.ttf",
  "app\src\main\res\font\cormorant_medium.ttf",
  "app\src\main\res\font\cormorant_semibold.ttf",
  "docs\licenses\OFL-CormorantGaramond.txt"
)
foreach ($s in $stale) { if (Test-Path -LiteralPath $s) { Write-Host "Veraltet, bitte loeschen: $s" -ForegroundColor Yellow } }
