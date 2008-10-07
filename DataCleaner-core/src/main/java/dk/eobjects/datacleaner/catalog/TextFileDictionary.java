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
package dk.eobjects.datacleaner.catalog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.profiler.pattern.ITokenizer;
import dk.eobjects.datacleaner.profiler.pattern.Token;
import dk.eobjects.datacleaner.profiler.pattern.WordAndNumberTokenizer;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.FileHelper;

/**
 * A dictionary that reads text files with one word per line
 */
public class TextFileDictionary implements IDictionary {

	private static final long serialVersionUID = 6123460352476468574L;
	private static final Log _log = LogFactory.getLog(TextFileDictionary.class);
	private transient ArrayList<String> _wordList;
	private File _dictionaryFile;
	private String _name;

	public static TextFileDictionary createTextFileDictionary(String name,
			Column column, DataContext dc, File file) {
		BufferedWriter writer = FileHelper.getBufferedWriter(file);
		try {
			Query q = new Query().select(column).from(column.getTable())
					.orderBy(column);
			q.getSelectClause().setDistinct(true);
			DataSet dataSet = dc.executeQuery(q);
			while (dataSet.next()) {
				Row row = dataSet.getRow();
				Object value = row.getValue(column);
				if (value != null) {
					writer.write(value.toString());
					writer.write('\n');
				}
			}
			dataSet.close();
		} catch (IOException e) {
			_log.info("Couldn't write line: " + e.getMessage());
			_log.debug(e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				_log.info("Couldn't close writer: " + e.getMessage());
			}
		}
		
		return new TextFileDictionary(name, file);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public File getDictionaryFile() {
		return _dictionaryFile;
	}

	public void setDictionaryFile(File dictionaryFile) {
		_dictionaryFile = dictionaryFile;
	}

	public TextFileDictionary() {
	}

	public TextFileDictionary(File dictionaryFile) {
		setDictionaryFile(dictionaryFile);
	}

	public TextFileDictionary(String name, File file) {
		setName(name);
		setDictionaryFile(file);
	}

	private void initWordlist() {
		if (_dictionaryFile == null) {
			throw new IllegalStateException(
					"Cannot validate without dictionaryFile.");
		}

		try {
			BufferedReader br = new BufferedReader(FileHelper
					.getReader(_dictionaryFile));
			_wordList = new ArrayList<String>();
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				line = line.trim();
				_wordList.add(line);
			}
			br.close();
		} catch (IOException e) {
			_log.error(e);
			throw new RuntimeException(e);
		}
		Collections.sort(_wordList);
	}

	public boolean[] isValid(String... values) {
		boolean debugEnabled = _log.isDebugEnabled();
		if (_wordList == null) {
			initWordlist();
		}
		ITokenizer tokenizer = new WordAndNumberTokenizer();
		boolean[] results = new boolean[values.length];

		for (int i = 0; i < values.length; i++) {
			results[i] = true;
			String sentence = values[i];
			if (sentence == null) {
				results[i] = false;
			} else {
				// If the whole sentence is valid, then we don't need to do
				// anymore
				boolean sentenceValid = existsInFile(sentence);
				if (!sentenceValid) {
					// Tokenize the sentence to check individual token validity
					Token[] tokens = tokenizer.tokenize(sentence);
					if (debugEnabled) {
						_log.debug("Spell checking sentence: " + sentence);
						_log.debug("Found tokens: "
								+ ArrayUtils.toString(tokens));
					}
					for (int j = 0; j < tokens.length && results[i]; j++) {
						if (tokens[j].isWord() || tokens[j].isMixed()) {
							String tokenString = tokens[j].toString();
							results[i] = existsInFile(tokenString);
							if (debugEnabled && !results[i]) {
								_log
										.debug("Token '"
												+ tokenString
												+ "' was not valid according to spell checker.");
							}
						}
					}
				}

			}
		}
		return results;
	}

	private boolean existsInFile(String word) {
		int binarySearch = Collections.binarySearch(_wordList, word);
		return binarySearch >= 0;
	}

	@Override
	public String toString() {
		return "TextFileDictionary[name=" + _name + "]";
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}