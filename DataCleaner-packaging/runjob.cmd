@echo off
rem  This file is part of DataCleaner.
rem
rem  DataCleaner is free software: you can redistribute it and/or modify
rem  it under the terms of the GNU General Public License as published by
rem  the Free Software Foundation, either version 3 of the License, or
rem  (at your option) any later version.
rem
rem  DataCleaner is distributed in the hope that it will be useful,
rem  but WITHOUT ANY WARRANTY; without even the implied warranty of
rem  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem  GNU General Public License for more details.
rem
rem  You should have received a copy of the GNU General Public License
rem  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.

set DATACLEANER_HOME=%~dp0
cd /d %DATACLEANER_HOME%
echo Using DATACLEANER_HOME: %DATACLEANER_HOME%

call java -Xmx1024m -XX:MaxPermSize=256m -cp datacleaner.jar dk.eobjects.datacleaner.gui.DataCleanerCli %*