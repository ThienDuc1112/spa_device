param(
    [Parameter(Mandatory = $true)]
    [string]$CredentialPath
)

$ErrorActionPreference = 'Stop'
$credentialFile = (Resolve-Path -LiteralPath $CredentialPath).Path
$credentials = Get-Content -LiteralPath $credentialFile -Raw | ConvertFrom-Json
if ($credentials.type -ne 'service_account' -or !$credentials.project_id -or !$credentials.private_key) {
    throw 'Use a Firebase Admin service-account JSON, not the Android google-services.json.'
}
$androidConfig = Join-Path $PSScriptRoot '../pda-android/app/google-services.json'
if (Test-Path -LiteralPath $androidConfig) {
    $clientProject = (Get-Content -LiteralPath $androidConfig -Raw | ConvertFrom-Json).project_info.project_id
    if ($clientProject -and $clientProject -ne $credentials.project_id) {
        throw 'Firebase Admin and Android google-services.json use different Firebase projects.'
    }
}
$war = Join-Path $PSScriptRoot 'target/pda-management-1.0.0.war'
if (!(Test-Path -LiteralPath $war)) {
    throw 'Build the backend first: mvn -f pda-management/pom.xml package'
}
$javaExe = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
$env:GOOGLE_APPLICATION_CREDENTIALS = $credentialFile
$env:FCM_ENABLED = 'true'
$env:APP_SCHEDULER_ENABLED = 'true'
# Command-line flags also override dev-profile defaults in an older WAR.
& $javaExe -jar $war --app.fcm-enabled=true --app.scheduler-enabled=true
exit $LASTEXITCODE
