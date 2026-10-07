$env:JAVA_HOME="C:\Tools\jdk21\jdk-21.0.2"
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
if (Test-Path ".env") {
    Get-Content .env | Where-Object { $_ -match '^([^#=]+)="?([^"]*)"?$' } | ForEach-Object {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        Set-Item -Path Env:\$name -Value $value
    }
}
mvn clean spring-boot:run
