param(
    [string]$BaseUrl = "http://localhost:8080"
)

$script = "financial_manager_tests.sh"
$cwd = (Get-Location).Path

function Run-With-Bash {
    Write-Output "Using bash to run the script..."
    bash -lc "cd \"$cwd\" && chmod +x $script && ./$script $BaseUrl"
}

function Run-With-Docker {
    Write-Output "Using Docker to run the script inside ubuntu:24.04 container..."
    docker run --rm -v "${PWD}":/work -w /work ubuntu:24.04 bash -lc "apt-get update -qq && apt-get install -y curl grep sed bc coreutils && chmod +x $script && ./$script $BaseUrl"
}

if (Get-Command bash -ErrorAction SilentlyContinue) {
    Run-With-Bash
    exit $LASTEXITCODE
}

if (Get-Command docker -ErrorAction SilentlyContinue) {
    Run-With-Docker
    exit $LASTEXITCODE
}

Write-Error "Neither 'bash' nor 'docker' were found in PATH. Install Git Bash or WSL, or Docker Desktop."
exit 2
