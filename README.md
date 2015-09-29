# DataCleaner

[![Build Status: Linux](https://travis-ci.org/datacleaner/DataCleaner.svg?branch=master)](https://travis-ci.org/datacleaner/DataCleaner)
<div>
<img src="http://datacleaner.org/resources/dc-logo-100.png" alt="DataCleaner logo" />
</div>

The premier Open Source Data Quality solution.

Powered by Neopost and Human Inference

## Module structure

The main application modules are:

 * api - The public API of DataCleaner. Mostly interfaces and annotations that you should use to build your own extensions.
 * testware - Useful classes for unit testing of DataCleaner and extension code.
 * engine
  * core - The core engine piece which allows execution of jobs and components as per the API.
  * xml-config - Contains utilities for reading and writing job files and configuration files of DataCleaner.
 * components
  * ... - many sub modules containing built-in as well as additional components/extensions to use with DataCleaner.
 * desktop
  * api - The public API for the DataCleaner desktop application.
  * ui - The Swing-based user interface for desktop users
 * monitor
  * api - the API classes and interfaces of DataCleaner monitor
  * services - web services and controllers of DataCleaner monitor
  * widgets - reusable widgets and UI work, based on GWT
  * ui - the actual web user interface, based primarily on GWT and JSF
 * documentation - end-user reference documentation, published on http://datacleaner.org/docs

## Continuous Integration

There's a public build of DataCleaner that can be found on Travis CI:

https://travis-ci.org/datacleaner/DataCleaner

## Where to go for end-user information?

Please visit the main DataCleaner website http://datacleaner.org for downloads, news, forums etc.

Reference Documentation for users is available at http://datacleaner.org/docs - GitHub wiki and issues are used for developers and technical aspects only.

## License

Licensed under the Lesser General Public License, see http://www.gnu.org/licenses/lgpl.txt
