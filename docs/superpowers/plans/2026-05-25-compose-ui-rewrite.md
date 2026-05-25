# Superseded Compose UI Rewrite Plan

Status: superseded on 2026-05-25.

This file is retained only as historical context. Do not execute the original bottom-tab/auth-preserving plan.

Current direction:

- No login/auth/session gate.
- No user-scoped local data.
- No Fragment/ViewBinding/layout navigation shell.
- Main UI is a single Compose weather page inspired by Huawei Weather's city weather page.
- City management, search, alerts, life index, and settings open from top actions or in-page service entries.
- Current implementation status lives in `docs/refactor-implementation-status.md`.

Use `README.md` and `docs/refactor-implementation-status.md` as the active implementation references.
