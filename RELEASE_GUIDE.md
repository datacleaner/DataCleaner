# Release Guide

This guide is here to provide an overview of how to cut releases of DataCleaner community edition.

The steps are outlined in the sections below.

## Ensure the build running and all tests are passing

Check here:

[![Build Status: Linux](https://travis-ci.org/datacleaner/DataCleaner.svg?branch=master)](https://travis-ci.org/datacleaner/DataCleaner)

## Make sure you have privileges to publish DataCleaner Maven artifacts

We use the Maven Central repository to distribute our Maven artifacts. Access to publish to this repository via [Sonatype OSS](https://oss.sonatype.org) is needed.

If you don't, someone else will have to drive the release process. If you feel you should have this privilege, contact a DataCleaner admin.

## Call for a VOTE to release the new version of DataCleaner

A few ground rules about release VOTEs:

* A vote is raised as an issue in GitHub. Remember to include the "Vote" label.
* Unless explicitly specified, the voting period is 72 hours (3 days). This duration should only be deviated from in case of special urgency. Examples of special urgencies are when there are security critical bugfixes to be delivered or when a release artifact based on a very minimal change is corrupted or otherwise failed.
* Votes are cast simply by commenting on the issue.
* DataCleaner admins have the final say regardless of the voting outcome. But we do prefer to address any concerns or disputes and will usually call for a re-vote once that is done.

[Create the VOTE issue here](https://github.com/datacleaner/DataCleaner/issues/new?labels=VOTE).

Here's a template you can use for raising the vote (replace $VERSION with the version number that you plan to use):

```
Issue title: [VOTE] Release DataCleaner community edition $VERSION

Hi All,

Please vote on releasing DataCleaner version $VERSION based on the current *master* branch.

The vote is open for 72 hours.

[ ] +1 Release DataCleaner %VERSION
[ ] -1 Do not release because ...

Thank you in advance for participating.
```

## Run the Maven release process

This should be a matter of running:

```
> mvn release:prepare
> mvn release:perform
```

## 'Close' and 'Release' the staging repository

Go to [Sonatype OSS](https://oss.sonatype.org) to close and release the staged repository:

* Log in
* Click "Staging repositories"
* Identify the repository that represents the new release (usually has the words orgeobjects in it's name)
* Click 'Close'
* When closed, click 'Release'.

## Upload UI zip file to GitHub

Upload the zip file (in `desktop/ui/target`) to the release which you will find on the [list on GitHub](https://github.com/datacleaner/DataCleaner/releases). 

## Update the community edition website

Update the releases listed on our [community edition website](https://github.com/datacleaner/datacleaner.github.io) - specifically the `releases` attribute of `_config.yml`.

## Spread the word

Now it's time to let everyone know! Some channels of choice for us:

 * Twitter (announce via personal accounts as well as the @datacleaner handle)
 * LinkedIn (announce it in the DataCleaner group)

## Done

Celebrate with a beverage of your own choice!
