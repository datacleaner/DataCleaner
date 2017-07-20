# Release Guide

This guide is here to provide an overview of how to cut releases of DataCleaner community edition.

The steps are outlined in the sections below.

## Ensure the build running and all tests are passing

Check here:

[![Build Status: Linux](https://travis-ci.org/datacleaner/DataCleaner.svg?branch=master)](https://travis-ci.org/datacleaner/DataCleaner)

## Make sure you have privileges to publish DataCleaner Maven artifacts

We use the Maven Central repository to distribute our Maven artifacts. Access to publish to this repository via [Sonatype OSS](https://oss.sonatype.org) is needed.

## Run the Maven release process

This should be a matter of running:

```
> mvn release:prepare
> mvn release:perform
```

## Upload UI zip file to GitHub

Upload the zip file to the release which you will find on the [list on GitHub](https://github.com/datacleaner/DataCleaner/releases). 

## Update the community edition website

Update the releases listed on our [community edition website](https://github.com/datacleaner/datacleaner.github.io) - specifically the `releases` attribute of `_config.yml`.

## Spread the word

Now it's time to let everyone know! Some channels of choice for us:

 * Twitter (announce via personal accounts as well as the @datacleaner handle)
 * LinkedIn (announce it in the DataCleaner group)

## Done

Celebrate with a beverage of your own choice!
