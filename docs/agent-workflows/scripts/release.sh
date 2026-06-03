#!/usr/bin/env bash
#
# release.sh — Promote `develop` to `main` (git-flow), tag the release on `main`,
# publish a GitHub Release, and bump `develop` to the next minor SNAPSHOT.
#
# Source of truth: docs/agent-workflows/release-to-github.md
#
# Usage:
#   docs/agent-workflows/scripts/release.sh \
#     --bump minor \
#     --title "Foundation / Bootstrap" \
#     --notes-file path/to/notes.md
#
# Required flags:
#   --bump major|minor|patch   Which SemVer component to increment.
#                              The current version is read from build.gradle.kts
#                              (expected as `version = "X.Y.Z-SNAPSHOT"`).
#   --title <text>             Release title suffix. The final GitHub Release
#                              title will be: "<version> — <title>".
#   --notes-file <path>        Path to a Markdown file with the release notes.
#
# Optional flags:
#   --feature-branch <name>    Delete this feature branch (local + remote) after
#                              the release. In git-flow features are normally
#                              already deleted when merged to develop; this flag
#                              is kept for backwards compatibility.
#   --skip-develop-bump        Do not bump `develop` to the next minor SNAPSHOT.
#   --dry-run                  Print commands without executing them.
#
# Preconditions:
#   - You are on a clean working tree.
#   - All features for this release are already merged into `develop`.
#   - `gh` is installed and authenticated.
#   - `origin` points to the GitHub remote.

set -euo pipefail

BUMP=""
TITLE=""
NOTES_FILE=""
FEATURE_BRANCH=""
SKIP_DEVELOP_BUMP="false"
DRY_RUN="false"

usage() {
  sed -n '2,34p' "$0"
  exit "${1:-1}"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bump)                BUMP="$2"; shift 2 ;;
    --title)               TITLE="$2"; shift 2 ;;
    --notes-file)          NOTES_FILE="$2"; shift 2 ;;
    --feature-branch)      FEATURE_BRANCH="$2"; shift 2 ;;
    --skip-develop-bump)   SKIP_DEVELOP_BUMP="true"; shift ;;
    --dry-run)             DRY_RUN="true"; shift ;;
    -h|--help)             usage 0 ;;
    *) echo "Unknown argument: $1" >&2; usage 1 ;;
  esac
done

if [[ -z "$BUMP" || -z "$TITLE" || -z "$NOTES_FILE" ]]; then
  echo "ERROR: --bump, --title and --notes-file are required." >&2
  usage 1
fi

case "$BUMP" in
  major|minor|patch) ;;
  *) echo "ERROR: --bump must be one of: major, minor, patch (got '$BUMP')." >&2; exit 1 ;;
esac

if [[ ! -f "$NOTES_FILE" ]]; then
  echo "ERROR: notes file not found: $NOTES_FILE" >&2
  exit 1
fi

# Sanity checks
command -v gh >/dev/null 2>&1 || { echo "ERROR: gh CLI not installed." >&2; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "ERROR: gh not authenticated." >&2; exit 1; }

REPO_ROOT="$(git rev-parse --show-toplevel)"
GRADLE_FILE="${REPO_ROOT}/build.gradle.kts"

if [[ ! -f "$GRADLE_FILE" ]]; then
  echo "ERROR: build.gradle.kts not found at ${GRADLE_FILE}" >&2
  exit 1
fi

# Extract current version from build.gradle.kts (expects: version = "X.Y.Z[-SNAPSHOT]")
CURRENT_VERSION_RAW="$(grep -E '^version *= *"' "$GRADLE_FILE" | head -n1 | sed -E 's/^version *= *"([^"]+)"/\1/')"
if [[ -z "$CURRENT_VERSION_RAW" ]]; then
  echo "ERROR: could not read 'version = \"...\"' from ${GRADLE_FILE}" >&2
  exit 1
fi

CURRENT_BASE="${CURRENT_VERSION_RAW%-SNAPSHOT}"
if ! [[ "$CURRENT_BASE" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  echo "ERROR: current version '${CURRENT_VERSION_RAW}' does not match X.Y.Z[-SNAPSHOT]" >&2
  exit 1
fi
MAJOR="${BASH_REMATCH[1]}"
MINOR="${BASH_REMATCH[2]}"
PATCH="${BASH_REMATCH[3]}"

case "$BUMP" in
  major) NEW_MAJOR=$((MAJOR + 1)); NEW_MINOR=0;             NEW_PATCH=0 ;;
  minor) NEW_MAJOR=$MAJOR;          NEW_MINOR=$((MINOR + 1)); NEW_PATCH=0 ;;
  patch) NEW_MAJOR=$MAJOR;          NEW_MINOR=$MINOR;         NEW_PATCH=$((PATCH + 1)) ;;
esac

# If the current version is already a SNAPSHOT for the requested bump (typical case
# after a previous release left develop at e.g. 0.3.0-SNAPSHOT and we want minor),
# release the SNAPSHOT base instead of bumping again.
if [[ "$CURRENT_VERSION_RAW" == *-SNAPSHOT ]]; then
  case "$BUMP" in
    minor)
      # develop is X.Y.0-SNAPSHOT → release X.Y.0
      if [[ "$PATCH" == "0" ]]; then
        VERSION="$CURRENT_BASE"
      else
        VERSION="${NEW_MAJOR}.${NEW_MINOR}.${NEW_PATCH}"
      fi
      ;;
    major)
      if [[ "$MINOR" == "0" && "$PATCH" == "0" ]]; then
        VERSION="$CURRENT_BASE"
      else
        VERSION="${NEW_MAJOR}.${NEW_MINOR}.${NEW_PATCH}"
      fi
      ;;
    patch)
      VERSION="${NEW_MAJOR}.${NEW_MINOR}.${NEW_PATCH}"
      ;;
  esac
else
  VERSION="${NEW_MAJOR}.${NEW_MINOR}.${NEW_PATCH}"
fi

# Compute the next minor SNAPSHOT for develop (always next minor, per project policy).
if ! [[ "$VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  echo "ERROR: computed VERSION '${VERSION}' is not X.Y.Z" >&2
  exit 1
fi
RMAJOR="${BASH_REMATCH[1]}"
RMINOR="${BASH_REMATCH[2]}"
NEXT_DEV_VERSION="${RMAJOR}.$((RMINOR + 1)).0-SNAPSHOT"

FULL_TITLE="${VERSION} — ${TITLE}"

echo "==> Current version (build.gradle.kts): ${CURRENT_VERSION_RAW}"
echo "==> Bump type:                          ${BUMP}"
echo "==> Release version:                    ${VERSION}"
echo "==> Release title:                      ${FULL_TITLE}"
echo "==> Next develop SNAPSHOT:              ${NEXT_DEV_VERSION}"

run() {
  echo "+ $*"
  if [[ "$DRY_RUN" == "false" ]]; then
    eval "$@"
  fi
}

set_gradle_version() {
  local new_version="$1"
  echo "+ set build.gradle.kts version to ${new_version}"
  if [[ "$DRY_RUN" == "false" ]]; then
    # Portable in-place edit (works on BSD sed / macOS and GNU sed).
    local tmp
    tmp="$(mktemp)"
    sed -E "s/^version *= *\"[^\"]+\"/version = \"${new_version}\"/" "$GRADLE_FILE" > "$tmp"
    mv "$tmp" "$GRADLE_FILE"
  fi
}

# Ensure clean working tree.
if [[ -n "$(git status --porcelain)" ]]; then
  echo "ERROR: working tree is not clean. Commit or stash changes before releasing." >&2
  exit 1
fi

echo "==> Step 1: Update develop and main locally"
run "git fetch origin"
run "git checkout develop"
run "git pull --ff-only origin develop"
run "git checkout main"
run "git pull --ff-only origin main"

echo "==> Step 2: Set release version in build.gradle.kts on develop"
run "git checkout develop"
set_gradle_version "${VERSION}"
run "git add '${GRADLE_FILE}'"
run "git commit -m 'build: set version to ${VERSION}'"
run "git push origin develop"

echo "==> Step 3: Merge develop into main (--no-ff)"
run "git checkout main"
run "git merge --no-ff develop -m 'chore(release): merge develop into main for ${VERSION}'"
run "git push origin main"

echo "==> Step 4: Create annotated tag ${VERSION} on main"
run "git tag -a '${VERSION}' -m 'Release ${FULL_TITLE}'"
run "git push origin '${VERSION}'"

echo "==> Step 5: Create the GitHub Release"
run "gh release create '${VERSION}' --title '${FULL_TITLE}' --notes-file '${NOTES_FILE}'"

if [[ "$SKIP_DEVELOP_BUMP" == "true" ]]; then
  echo "==> Step 6: Skipped develop SNAPSHOT bump (--skip-develop-bump)"
else
  echo "==> Step 6: Bump develop to next SNAPSHOT (${NEXT_DEV_VERSION})"
  run "git checkout develop"
  run "git pull --ff-only origin develop"
  set_gradle_version "${NEXT_DEV_VERSION}"
  run "git add '${GRADLE_FILE}'"
  run "git commit -m 'build: start next development iteration ${NEXT_DEV_VERSION}'"
  run "git push origin develop"
fi

if [[ -n "$FEATURE_BRANCH" ]]; then
  echo "==> Step 7: Delete legacy feature branch ${FEATURE_BRANCH} (local + remote)"
  run "git branch -d '${FEATURE_BRANCH}' || git branch -D '${FEATURE_BRANCH}'"
  run "git push origin --delete '${FEATURE_BRANCH}' || true"
else
  echo "==> Step 7: Skipped (no --feature-branch provided; git-flow handles this on merge to develop)"
fi

echo "==> Done. Release ${VERSION} published. develop now at ${NEXT_DEV_VERSION}."
