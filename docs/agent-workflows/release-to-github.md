# Workflow: Release to GitHub (git-flow)

Promote the `develop` branch to `main` following **git-flow**, tag the release on `main`,
publish the corresponding GitHub Release, and leave `develop` ready for the next iteration
with a fresh `-SNAPSHOT` version.

## When to use

When all the `feature/*` branches that compose the next milestone have already been
**merged into `develop`** and you want to ship a versioned release
(e.g. `0.1.0`, `0.2.0`, …).

## Preconditions

- All features for the milestone are already merged into `develop` on GitHub.
- Local clone has `origin` pointing to the GitHub remote, and `develop` and `main` are both
  in sync with `origin`.
- Working tree is clean (`git status` shows nothing to commit).
- [`gh`](https://cli.github.com/) is installed and authenticated (`gh auth status`).
- You know:
  - The **bump type** for this release (`major`, `minor` or `patch`) — follow SemVer.
    The agent **must ask the user** which one to use before starting.
  - The **release title** suffix (e.g. `Foundation / Bootstrap`). The final GitHub Release
    title will be `<version> — <suffix>`.
  - The **release notes** in English (Markdown).

## Versioning rules

- The current version lives in [`build.gradle.kts`](../../build.gradle.kts) using the standard
  Gradle convention (`version = "X.Y.Z-SNAPSHOT"`), even though the project uses the simple
  `0.1.0` format for tags and releases.
- The **release version** is derived from the bump type:
  - `major` → `(X+1).0.0`
  - `minor` → `X.(Y+1).0`
  - `patch` → `X.Y.(Z+1)`
  - If `develop` is already at the `-SNAPSHOT` of the target release (e.g. `0.3.0-SNAPSHOT`
    when bumping `minor` from `0.2.x`), the SNAPSHOT base is released as-is (`0.3.0`).
- After publishing the release, `develop` is **always** bumped to the **next minor**
  `-SNAPSHOT` (e.g. after releasing `0.2.0` → `develop` becomes `0.3.0-SNAPSHOT`),
  regardless of the bump type that was used for the release.

## Steps

### 1. Ask the user the bump type

The agent must ask the user whether the release is `major`, `minor` or `patch` and use
that value as the `--bump` flag of the script (or to compute the version manually).

### 2. Update `develop` and `main` locally

```bash
git fetch origin
git checkout develop && git pull --ff-only origin develop
git checkout main    && git pull --ff-only origin main
```

If `pull --ff-only` fails on either branch, your local branch has diverged — resolve before
continuing.

### 3. Set the release version in `build.gradle.kts` on `develop`

Compute the release version from the current `develop` version and the bump type
(see [Versioning rules](#versioning-rules)).

```bash
VERSION="0.2.0"

git checkout develop
# In build.gradle.kts, replace:
#   version = "0.2.0-SNAPSHOT"
# with:
#   version = "0.2.0"
git add build.gradle.kts
git commit -m "build: set version to ${VERSION}"
git push origin develop
```

### 4. Merge `develop` into `main` with `--no-ff`

```bash
git checkout main
git merge --no-ff develop -m "chore(release): merge develop into main for ${VERSION}"
git push origin main
```

### 5. Create an annotated tag on `main` and push it

```bash
git tag -a "${VERSION}" -m "Release ${VERSION} — <milestone name>"
git push origin "${VERSION}"
```

Tags are **annotated** (not lightweight) so they carry author, date and message.

### 6. Create the GitHub Release

Write the notes in English, Markdown, with the same sections used across the project
(`Highlights` / `Changes` / `Verification` / `Notes` — adapt as needed).

```bash
gh release create "${VERSION}" \
  --title "${VERSION} — <milestone name>" \
  --notes-file path/to/notes.md
```

The command prints the release URL on success.

### 7. Bump `develop` to the next minor `-SNAPSHOT`

```bash
NEXT_DEV_VERSION="0.3.0-SNAPSHOT"

git checkout develop
git pull --ff-only origin develop
# In build.gradle.kts, replace the release version with the next minor SNAPSHOT.
git add build.gradle.kts
git commit -m "build: start next development iteration ${NEXT_DEV_VERSION}"
git push origin develop
```

### 8. (Optional) Delete a legacy feature branch

In git-flow, `feature/*` branches are normally deleted when they are merged into `develop`,
so this step is usually unnecessary. Only run it if a stale branch remains:

```bash
FEATURE_BRANCH="feature/<name>"
git branch -d "${FEATURE_BRANCH}" || git branch -D "${FEATURE_BRANCH}"
git push origin --delete "${FEATURE_BRANCH}"
```

## Verification checklist

- `git tag --list "${VERSION}"` shows the tag locally.
- `git ls-remote --tags origin | grep "${VERSION}"` shows the tag on `origin`.
- `gh release view "${VERSION}"` returns the release with the expected title and notes.
- On `main`, `build.gradle.kts` contains `version = "${VERSION}"` (no `-SNAPSHOT`).
- On `develop`, `build.gradle.kts` contains `version = "${NEXT_DEV_VERSION}"` (`-SNAPSHOT`).
- `git log --oneline main` shows the `--no-ff` merge commit at the tip.

## Rollback

- Delete a wrong tag (local + remote):
  ```bash
  git tag -d "${VERSION}"
  git push origin --delete "${VERSION}"
  ```
- Delete a wrong release (keeps or removes the tag depending on flag):
  ```bash
  gh release delete "${VERSION}" --yes              # keeps the tag
  gh release delete "${VERSION}" --yes --cleanup-tag # also deletes the tag
  ```
- Revert the `--no-ff` merge on `main` (only if not yet released to consumers):
  ```bash
  git checkout main
  git revert -m 1 <merge-commit-sha>
  git push origin main
  ```

## Automation

A convenience wrapper exists at [`scripts/release.sh`](scripts/release.sh). It performs
steps 2–7 with the same commands documented above and computes the release version and
the next `develop` SNAPSHOT from `build.gradle.kts` automatically.

```bash
docs/agent-workflows/scripts/release.sh \
  --bump minor \
  --title "Foundation / Bootstrap" \
  --notes-file path/to/notes.md
```

Optional flags:

- `--feature-branch <name>` — delete a stale feature branch (local + remote) at the end.
- `--skip-develop-bump` — do not bump `develop` to the next minor SNAPSHOT.
- `--dry-run` — print commands without executing them.

Markdown remains the source of truth — if the script and this document drift, this document
wins.
