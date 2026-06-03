# 🤖 Auto GitHub PR — Agent Workflow

> **⚠️ This workflow is NOT referenced in the project's agent structure (`AGENTS.md`,
> `docs/ARCHITECTURE.md`, etc.) and must be invoked manually.**

---

## 📋 Description

This workflow guides an AI agent to implement a **simple task** from start to finish,
following all project conventions, and culminating with the **automatic creation of a
Pull Request on GitHub**.

The goal is for the agent to:

1. Implement the requested change respecting code and architecture conventions.
2. Create commits with messages in **Conventional Commits** format.
3. Create a complete PR, assigned and with a reviewer, ready for review.

---

## 🔄 Workflow

### 1️⃣ Implement the task

- The agent implements the task requested by the user following the project conventions
  defined in [`AGENTS.md`](../../AGENTS.md) and [`docs/ARCHITECTURE.md`](../ARCHITECTURE.md).
- Commit messages **must** follow the **Conventional Commits** format:
  ```
  type(scope): description
  ```
  Example:
  ```
  feat(tasks): add priority field to task model
  ```
- Every commit made by the agent **must** include the co-authorship trailer:
  ```
  --trailer "Co-authored-by: <AgentName> <<agent-email>>"
  ```

### 2️⃣ Ask for the PR reviewer

- Before creating the PR, the agent **must ask the user** which GitHub user should be
  set as **reviewer**.
- If the user responds with something like _"the creator"_, _"the owner"_, _"the repo owner"_
  or similar, the agent will set as reviewer: **`jorge-aranda`**.

### 3️⃣ Assign the PR to the current user

- The PR will be **assigned** (_assignee_) to the user interacting with the agent (the
  current session user).

### 4️⃣ PR title

- The **PR title** will be the **commit description** (the part after `type(scope):`).
- Example: if the commit is `feat(tasks): add priority field to task model`, the PR title
  will be:
  > **add priority field to task model**

### 5️⃣ Source and target branch

| Concept              | Value       |
|----------------------|-------------|
| **Source branch**    | `develop`   |
| **Target branch (base)** | `develop` |

- The feature branch is created **from `develop`**.
- The PR must be merged **back into `develop`**.

### 6️⃣ PR description

- The agent **will generate a temporary Markdown file** (`.md`) with the detailed PR
  description.
- This temporary file:
  - Must be as **detailed as possible**.
  - Must use **attractive visual styles**: headings, tables, lists, emojis, badges, etc.
  - Will be used as the PR body when creating it with `gh pr create --body-file`.
- **🌐 The PR body must be written in English.**
- **🚨 This is very important:** the description must be generated as a temporary `.md` file
  and passed to the PR creation command.

#### Suggested structure for the description:

```markdown
## 🎯 Objective

Brief description of the change and its motivation.

## 📝 Changes made

- ✅ Change 1
- ✅ Change 2
- ✅ Change 3

## 🧪 Testing

| Test type          | Status  |
|--------------------|---------|
| Unit tests         | ✅ Pass |
| Integration tests  | ⏭️ N/A  |

## 📎 Additional notes

- Any relevant context for the reviewer.
```

---

## 🛠️ Execution example (key commands)

```bash
# 1. Create branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/my-change

# 2. Implement changes and commit
git add .
git commit -m "feat(tasks): add priority field to task model" \
  --trailer "Co-authored-by: Junie <junie@jetbrains.com>"

# 3. Push the branch
git push origin feature/my-change

# 4. Generate temporary file with the PR description
# (the agent generates /tmp/pr-description.md with detailed content)

# 5. Create the PR
gh pr create \
  --base develop \
  --title "add priority field to task model" \
  --body-file /tmp/pr-description.md \
  --assignee @me \
  --reviewer jorge-aranda
```

---

## ⚙️ Requirements

- GitHub CLI (`gh`) authenticated in the agent's environment.
- Write access to the repository.
- Up-to-date `develop` branch.

---

## 📌 Reminders

| Rule | Detail |
|------|--------|
| 🔀 Source branch | Always from `develop` |
| 🎯 Target branch | Always into `develop` |
| 📝 PR title | Commit description (without `type(scope):`) |
| 👤 Assignee | Current session user |
| 👁️ Reviewer | Ask the user; default `jorge-aranda` |
| 📄 Description | Temporary `.md` file, detailed and visual |
| 🏷️ Commits | Conventional Commits + co-author trailer |
| 🌐 Body language | Always in **English** |
| 🚫 Not referenced | This workflow is invoked **manually** |
