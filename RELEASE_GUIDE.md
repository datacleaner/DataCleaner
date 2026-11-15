# Release Guide

This guide explains how DataCleaner releases are produced.

Releases are automated by the [release workflow](.github/workflows/release.yml) using
[semantic-release](https://semantic-release.gitbook.io/). There is no manual Maven
release step and no Maven artifacts are published anywhere - the end-user distribution
is attached to the GitHub release instead.

## Triggering a release

A release is triggered when commits are pushed to `main` with a message that follows
[Conventional Commits](https://www.conventionalcommits.org/):

| Commit type | Version bump | Example |
|-------------|--------------|---------|
| `fix:` | Patch | `fix: correct null handling in CSV reader` |
| `feat:` | Minor | `feat: add support for Excel 365 files` |
| `feat!:` or `BREAKING CHANGE:` | Major | `feat!: require Java 21` |
| `chore:`, `docs:`, etc. | No release | `docs: update build instructions` |

The workflow can also be run manually from the Actions tab via `workflow_dispatch`.

## What the workflow does

On a release, the workflow will:

1. Determine the next version from the commits since the last release.
2. Update all `pom.xml` versions and `CHANGELOG.md`.
3. Build the desktop distribution, which produces
   `desktop/ui/target/DataCleaner-<version>.zip`.
4. Create a GitHub release tagged `DataCleaner-<version>` and upload the zip as a
   release asset.
5. Commit and push the next `-SNAPSHOT` version.

Maven artifact deployment is disabled (`maven.deploy.skip`), so only the GitHub
release assets are published.

## After a release

Update the releases listed on our [website](https://github.com/datacleaner/datacleaner.github.io) -
specifically the `releases` attribute of `_config.yml`.

## Done

Celebrate with a beverage of your own choice!
