#!/usr/bin/env bash
#
# release.sh — Tag a merged milestone on main and publish a GitHub Release.
#
# Source of truth: docs/agent-workflows/release-to-github.md
#
# Usage:
#   docs/agent-workflows/scripts/release.sh \
#     --version 0.1.0 \
#     --title "0.1.0 — Foundation / Bootstrap" \
#     --notes-file path/to/notes.md \
#     --feature-branch feature/foundation
#
# Optional flags:
#   --skip-branch-delete   Do not delete the feature branch (local + remote).
#   --dry-run              Print commands without executing them.
#
# Preconditions:
#   - The PR for the milestone is already merged into main on GitHub.
#   - `gh` is installed and authenticated.
#   - `origin` points to the GitHub remote.

set -euo pipefail

VERSION=""
TITLE=""
NOTES_FILE=""
FEATURE_BRANCH=""
SKIP_BRANCH_DELETE="false"
DRY_RUN="false"

usage() {
  sed -n '2,22p' "$0"
  exit "${1:-1}"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version)             VERSION="$2"; shift 2 ;;
    --title)               TITLE="$2"; shift 2 ;;
    --notes-file)          NOTES_FILE="$2"; shift 2 ;;
    --feature-branch)      FEATURE_BRANCH="$2"; shift 2 ;;
    --skip-branch-delete)  SKIP_BRANCH_DELETE="true"; shift ;;
    --dry-run)             DRY_RUN="true"; shift ;;
    -h|--help)             usage 0 ;;
    *) echo "Unknown argument: $1" >&2; usage 1 ;;
  esac
done

if [[ -z "$VERSION" || -z "$TITLE" || -z "$NOTES_FILE" ]]; then
  echo "ERROR: --version, --title and --notes-file are required." >&2
  usage 1
fi

if [[ ! -f "$NOTES_FILE" ]]; then
  echo "ERROR: notes file not found: $NOTES_FILE" >&2
  exit 1
fi

if [[ "$SKIP_BRANCH_DELETE" == "false" && -z "$FEATURE_BRANCH" ]]; then
  echo "ERROR: --feature-branch is required unless --skip-branch-delete is set." >&2
  exit 1
fi

run() {
  echo "+ $*"
  if [[ "$DRY_RUN" == "false" ]]; then
    eval "$@"
  fi
}

# Sanity checks
command -v gh >/dev/null 2>&1 || { echo "ERROR: gh CLI not installed." >&2; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "ERROR: gh not authenticated." >&2; exit 1; }

echo "==> Step 1: Update main locally"
run "git checkout main"
run "git pull --ff-only origin main"
run "git log --oneline -5"

echo "==> Step 2: Create annotated tag ${VERSION}"
run "git tag -a '${VERSION}' -m 'Release ${VERSION} — ${TITLE}'"

echo "==> Step 3: Push the tag to origin"
run "git push origin '${VERSION}'"

echo "==> Step 4: Create the GitHub Release"
run "gh release create '${VERSION}' --title '${TITLE}' --notes-file '${NOTES_FILE}'"

if [[ "$SKIP_BRANCH_DELETE" == "true" ]]; then
  echo "==> Step 5: Skipped (--skip-branch-delete)"
else
  echo "==> Step 5: Delete merged feature branch ${FEATURE_BRANCH} (local + remote)"
  run "git branch -d '${FEATURE_BRANCH}'"
  run "git push origin --delete '${FEATURE_BRANCH}'"
fi

echo "==> Done. Release ${VERSION} published."
