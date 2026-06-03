# Agent Workflows

Reusable, agent-friendly runbooks for recurring repository operations.

Each workflow documents the exact sequence of commands an AI agent (or a human) should run
to perform a routine task end-to-end. They are intentionally **explicit**, **idempotent where
possible**, and **safe** (no destructive defaults).

## Available workflows

| Workflow          | Doc                                            | Script                                       |
|-------------------|------------------------------------------------|----------------------------------------------|
| Release to GitHub | [`release-to-github.md`](release-to-github.md) | [`scripts/release.sh`](scripts/release.sh)   |

- **Release to GitHub** — Tag a merged milestone on `main` and publish a GitHub Release.

## Conventions

- Workflows assume the repo root as the working directory.
- All commands are non-interactive; flags are spelled out.
- Markdown is the source of truth — scripts are a convenience wrapper over the documented steps.
- When an AI agent runs these, it must still apply the global rules in
  [`AGENTS.md`](../../AGENTS.md) (co-author trailer on commits, Conventional Commits, etc.).
