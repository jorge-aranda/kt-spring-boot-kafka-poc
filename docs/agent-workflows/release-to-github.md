# Workflow: Release to GitHub

Tag a merged milestone on `main` and publish the corresponding GitHub Release, then clean up
the feature branch.

## When to use

After a `feature/*` pull request has been **merged into `main`** on GitHub and you want to
ship a versioned release (e.g. `0.1.0`, `0.2.0`, …).

## Preconditions

- The PR for the milestone is **already merged** into `main` on GitHub.
- Local clone has `origin` pointing to the GitHub remote.
- [`gh`](https://cli.github.com/) is installed and authenticated (`gh auth status`).
- You know:
  - The **version tag** to create (e.g. `0.1.0`) — follow SemVer.
  - The **release title** (e.g. `0.1.0 — Foundation / Bootstrap`).
  - The **release notes** in English (Markdown).
  - The **feature branch name** that was merged (e.g. `feature/foundation`).

## Steps

### 1. Update `main` locally

```bash
git checkout main
git pull --ff-only origin main
git log --oneline -5
```

Verify the merge commit of the PR is at the tip of `main`. If `pull --ff-only` fails, your
local `main` has diverged — resolve before continuing.

### 2. Create an annotated tag

```bash
VERSION="0.1.0"
TITLE="Release ${VERSION} — <milestone name>"

git tag -a "${VERSION}" -m "${TITLE}"
```

Tags are **annotated** (not lightweight) so they carry author, date and message.

### 3. Push the tag

```bash
git push origin "${VERSION}"
```

### 4. Create the GitHub Release

Write the notes in English, Markdown, with the same sections used across the project
(`Highlights` / `Changes` / `Verification` / `Notes` — adapt as needed).

```bash
gh release create "${VERSION}" \
  --title "${VERSION} — <milestone name>" \
  --notes "$(cat <<'EOF'
### Highlights
- ...

### Changes
- ...

### Verification
- ...

### Notes
- ...
EOF
)"
```

The command prints the release URL on success.

### 5. Delete the merged feature branch (local + remote)

```bash
FEATURE_BRANCH="feature/<name>"

git branch --show-current                # must be 'main'
git branch -d "${FEATURE_BRANCH}"        # safe delete (refuses if not merged)
git push origin --delete "${FEATURE_BRANCH}"
```

If `git branch -d` refuses because the branch is not detected as merged (e.g. squash merge),
verify the PR was indeed merged on GitHub and then force with `git branch -D`.

## Verification checklist

- `git tag --list "${VERSION}"` shows the tag locally.
- `git ls-remote --tags origin | grep "${VERSION}"` shows the tag on `origin`.
- `gh release view "${VERSION}"` returns the release with the expected title and notes.
- `git branch -a | grep "${FEATURE_BRANCH}"` returns nothing (local + remote gone).

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

## Automation

A convenience wrapper exists at [`scripts/release.sh`](scripts/release.sh). It performs steps
1–5 with the same commands documented above. Markdown remains the source of truth — if the
script and this document drift, this document wins.
