#!/usr/bin/env bash
#
# Publishes the library to Maven Central. All secrets are pulled from Bitwarden at run time -
# nothing sensitive is stored on disk.
#
# Usage:
#   ./publish.sh             upload to the Central Portal for manual release
#   ./publish.sh --release   upload and auto-release once validation passes
#   ./publish.sh --local     publish to the local Maven repo only (dry run)
#
# Requires the Bitwarden CLI (`bw`), logged in, with two vault items:
#   "maven-central" - login: username/password = Central Portal token
#   "gpg-signing"   - login: username = key id, password = key passphrase, notes = ASCII secret key
#
set -euo pipefail
cd "$(dirname "$0")/.." # repo root, where gradlew lives

# Unlock the vault (prompts for the master password), reusing an active session
# if BW_SESSION is already exported in the current shell.
BW_SESSION="${BW_SESSION:-$(bw unlock --raw)}"
export BW_SESSION

export ORG_GRADLE_PROJECT_mavenCentralUsername="$(bw get username maven-central)"
export ORG_GRADLE_PROJECT_mavenCentralPassword="$(bw get password maven-central)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId="$(bw get username gpg-signing)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="$(bw get password gpg-signing)"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(bw get notes gpg-signing)"

task=":library:publishToMavenCentral"
case "${1:-}" in
    --release) task=":library:publishAndReleaseToMavenCentral"; shift ;;
    --local)   task=":library:publishMavenPublicationToMavenLocal"; shift ;;
esac

./gradlew clean "$task" "$@"
