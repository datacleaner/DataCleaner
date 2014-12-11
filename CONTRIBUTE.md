## Thank you!

Before anything else, thank you. We are honored that you're even considering contributing to DataCleaner.

This guide will help you get started with DataCleaner's development environment. You'll also find the set of rules you're expected to follow in order to submit improvements and fixes.

### Where to find issues?

Our issue list is avialable on GitHub: [DataCleaner issues](https://github.com/datacleaner/DataCleaner/issues). It's non-exhaustive and anyone is invited to post their own wishes and requirements as issues.

We generally use these communication tools to discuss features:

 * The issue list itself. We want you to comment on it, post new issues etc.
 * The [online DataCleaner discussion forum](http://datacleaner.org/forum). Especially if it is more of a question or a use case issue rather than a bug or technical feature description.

### Build the code

Fork and clone the repository:

```
> git clone https://github.com/datacleaner/DataCleaner.git DataCleaner
```

Try your first build:

```
> cd DataCleaner
> mvn clean install
```

### Report issues and ideas

Please use our [issue list](https://github.com/datacleaner/DataCleaner/issues) to report issues and ideas that you might work on. This is a great way to bounce ideas and get input to your contributions.

### Submitting your patch

When submitting your patch, keep in mind that it should be easy for others to review. Therefore consider:

* Don't change spaces to tabs or stuff like that. Clean up your patch so that it is readable.
* We prefer you to post your patch as a [GitHub Pull Request](https://github.com/datacleaner/DataCleaner/pulls), and if possible please mention any relevant issue numbers in the Pull Request description.
* If a Pull Request is NOT possible for whatever reason, use "git diff" to make a .diff file and post it as a Gist or something else, and add a comment with a link to the diff on the relevant issue.

### About tests

Your patches receive extra points if there's a good set of unittests with it. If you're contributing a core utility/service/function then it is a requirement.

That said, especially the UI aspects (Swing or web frontend) are generally accepted as "not always feasible to test".

By default the build will include all self-contained tests, including some quick integration tests.

### Coding guidelines

If you plan on submitting code, read this carefully. Please note it is not yet complete.

Our code style looks a lot like any other Java coding guidelines. Distinctive choices though:

* We format indentation using spaces, not tabs. We use 4 spaces for each indentation.
* We often prefix instance variables with an underscore (_). This to easily distinguish between method local and instance variables, as well as avoiding the overuse of the 'this' keyword in e.g. setter methods.
* We format line wrapping using a desired max line length of 120 characters.
* When possible, we tend to like the "final" keyword. This makes for more robust code and immutability is especially important in batch processing applications like DataCleaner.