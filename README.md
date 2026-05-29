# HealthFitness

## Health &amp; Fitness cordova plugin for OutSystems applications.

## Release process

Releases are automated by the `Trigger semantic-release for plugin` workflow, which opens a PR from `release` to `main` when a new version is produced by semantic-release.

> [!WARNING]
> **Release PRs must be merged with "Create a merge commit".** Do not squash or rebase — semantic-release tags point at specific commits on the `release` branch, and squashing or rebasing rewrites those commits so the tags would no longer be reachable from `main`.