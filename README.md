DataCleaner
===========

<div style="float: right;">
<img src="http://datacleaner.org/resources/dc-logo-200.png" alt="DataCleaner logo" />
</div>

The premier Open Source Data Quality solution.

Powered by Human Inference, a Neopost Customer Information Management company.

<div style="clear:both;"></div>

## Module structure

First, be sure to also visit the AnalyzerBeans project (https://github.com/datacleaner/AnalyzerBeans) since that contains the "engine" of DataCleaner. This project delivers the UI and gluing on top of AnalyzerBeans that makes for a end-user product.

Modules are:

 * core - this is shared/core/common code between desktop and monitor application.
 * desktop - the Swing-based user interface for desktop users
 * monitor - parent module for the DataCleaner monitor web application
  * api - the API classes and interfaces of DataCleaner monitor
  * services - web services and controllers of DataCleaner monitor
  * widgets - reusable widgets and UI work, based on GWT
  * ui - the actual web user interface, based primarily on GWT and JSF
 * extensions - various extensions for DataCleaner (both desktop and/or monitor)
 * documentation - end-user reference documentation, published on http://datacleaner.org/docs

## Where to go for end-user information?

Please visit the main DataCleaner website http://datacleaner.org for downloads, news, forums etc.

Reference Documentation for users is available at http://datacleaner.org/docs - GitHub wiki and issues are used for developers and technical aspects only.

## License

Licensed under the Lesser General Public License, see http://www.gnu.org/licenses/lgpl.txt
