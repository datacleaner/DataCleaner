# Release Guide

This guide is here to provide an overview of how to cut releases of DataCleaner community edition.

The steps are outlined in the sections below.

## Ensure the build running and all tests are passing

Check GitHub Actions.

## Run the Maven release process

This should be a matter of running:

```
> mvn release:prepare
> mvn release:perform
```

## Upload UI zip file to GitHub

Upload the zip file (in `desktop/ui/target`) to the release which you will find on the [list on GitHub](https://github.com/datacleaner/DataCleaner/releases). 

## Update the community edition website

Update the releases listed on our [community edition website](https://github.com/datacleaner/datacleaner.github.io) - specifically the `releases` attribute of `_config.yml`.

## Done

Celebrate with a beverage of your own choice!
