param(
    [Parameter(Mandatory = $true)][string]$DependencyList,
    [string]$Report = 'target/osv-report.json'
)
$ErrorActionPreference = 'Stop'
$packages = @(Get-Content -LiteralPath $DependencyList | ForEach-Object {
    if ($_ -match '^\s+([^:\s]+):([^:\s]+):jar:([^:\s]+):(compile|provided|runtime)\b') {
        [pscustomobject]@{ name = ($Matches[1] + ':' + $Matches[2]); version = $Matches[3]; scope = $Matches[4] }
    }
})
if ($packages.Count -eq 0) { throw 'Dependency inventory is empty' }
$queries = @($packages | ForEach-Object {
    @{ package = @{ name = $_.name; ecosystem = 'Maven' }; version = $_.version }
})
$response = Invoke-RestMethod -Method Post -Uri 'https://api.osv.dev/v1/querybatch' -ContentType 'application/json' -Body (@{ queries = $queries } | ConvertTo-Json -Depth 6) -TimeoutSec 60
if (@($response.results).Count -ne $packages.Count) { throw 'Incomplete OSV response' }
@{ packages = $packages; response = $response; checkedAtUtc = [DateTime]::UtcNow.ToString('o') } | ConvertTo-Json -Depth 15 | Set-Content -LiteralPath $Report -Encoding UTF8
$findings = @()
for ($index = 0; $index -lt $packages.Count; $index++) {
    if ($response.results[$index].vulns -or $response.results[$index].next_page_token) {
        $findings += [pscustomobject]@{ package = $packages[$index].name; version = $packages[$index].version; scope = $packages[$index].scope; advisories = ($response.results[$index].vulns.id -join ',') }
    }
}
if ($findings.Count -gt 0) {
    $findings | Format-Table | Out-Host
    throw 'OSV returned vulnerability advisories; inspect the report'
}
Write-Output ("OSV checked " + $packages.Count + " exact Maven package versions: no advisories returned.")
