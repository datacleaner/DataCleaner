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

call mvn install
cd DataCleaner-packaging\target\dist
call datacleaner.cmd
cd ..\..\..