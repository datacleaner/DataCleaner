/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.regexswap;

import java.util.ArrayList;
import java.util.List;

public class Category {

	private String _name;
	private String _description;
	private String _detailsUrl;
	private List<Regex> _regexes = new ArrayList<Regex>();
	
	public Category(String name) {
		_name = name;
	}
	
	public String getName() {
		return _name;
	}
	public void setName(String name) {
		_name = name;
	}
	public String getDescription() {
		return _description;
	}
	public void setDescription(String description) {
		_description = description;
	}
	public List<Regex> getRegexes() {
		return _regexes;
	}
	public void setRegexes(List<Regex> regexes) {
		_regexes = regexes;
	}
	
	public String getDetailsUrl() {
		return _detailsUrl;
	}
	
	public void setDetailsUrl(String detailsUrl) {
		_detailsUrl = detailsUrl;
	}
	
	public boolean containsRegex(Regex regexes) {
		return _regexes.contains(regexes);
	}

	public void addRegex(Regex regex) {
		_regexes.add(regex);
	}
}
