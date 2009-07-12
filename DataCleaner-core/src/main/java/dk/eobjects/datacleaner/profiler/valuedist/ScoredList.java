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
package dk.eobjects.datacleaner.profiler.valuedist;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

public class ScoredList {

	private LinkedList<Entry<String, Long>> _entries = new LinkedList<Entry<String, Long>>();
	private int _capacity;
	private boolean _topBased;

	public ScoredList(boolean topBased, int capacity) {
		_topBased = topBased;
		_capacity = capacity;
	}

	public int size() {
		return _entries.size();
	}

	public int getCapacity() {
		return _capacity;
	}

	public boolean isTopBased() {
		return _topBased;
	}

	public boolean isBottomBased() {
		return !_topBased;
	}

	public boolean register(final String key, final Long value) {
		return register(new Entry<String, Long>() {

			public String getKey() {
				return key;
			}

			public Long getValue() {
				return value;
			}

			public Long setValue(Long value) {
				throw new UnsupportedOperationException("setValue");
			}
		});
	}

	public boolean register(Entry<String, Long> entry) {
		if (_entries.isEmpty()) {
			_entries.add(entry);
			return true;
		}
		long entryScore = entry.getValue();
		if (entryScore == 1) {
			return false;
		}
		int numEntries = _entries.size();
		boolean replaced = false;
		if (_topBased) {
			// Find the first entry that has a higher score and insert this
			// entry before it
			if (numEntries < _capacity || entryScore > getLowestScore()) {
				for (int i = 0; i < numEntries && !replaced; i++) {
					Entry<String, Long> currentEntry = _entries.get(i);
					Long currentEntryValue = currentEntry.getValue();
					if (currentEntryValue > entryScore) {
						_entries.add(i, entry);
						replaced = true;
					}
				}
				if (!replaced) {
					_entries.addLast(entry);
				}
				if (numEntries == _capacity) {
					_entries.removeFirst();
				}
				return true;
			}
			return false;
		} else {
			if (numEntries < _capacity || entryScore < getHighestScore()) {
				for (int i = numEntries - 1; i >= 0 && !replaced; i--) {
					Long currentEntryValue = _entries.get(i).getValue();
					if (currentEntryValue < entryScore) {
						_entries.add(i + 1, entry);
						replaced = true;
					}
				}
				if (!replaced) {
					_entries.addFirst(entry);
				}
				if (numEntries == _capacity) {
					_entries.removeLast();
				}
				return true;
			}
			return false;
		}
	}

	public Iterator<Entry<String, Long>> iterateLowToHigh() {
		return _entries.iterator();
	}

	public Iterator<Entry<String, Long>> iterateHighToLow() {
		return new Iterator<Entry<String, Long>>() {

			private int _currentIndex = _entries.size();

			public boolean hasNext() {
				return (_currentIndex > 0);
			}

			public Entry<String, Long> next() {
				_currentIndex--;
				Entry<String, Long> entry = _entries.get(_currentIndex);
				return entry;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public Set<Entry<String, Long>> getEntriesWithScore(long score) {
		Set<Entry<String, Long>> result = new HashSet<Entry<String, Long>>();
		for (Entry<String, Long> entry : _entries) {
			if (entry.getValue() == score) {
				result.add(entry);
			}
		}
		return result;
	}

	public Long getHighestScore() {
		if (_entries.isEmpty()) {
			return null;
		}
		Entry<String, Long> lastEntry = _entries.getLast();
		if (lastEntry == null) {
			return null;
		} else {
			return lastEntry.getValue();
		}
	}

	public Long getLowestScore() {
		if (_entries.isEmpty()) {
			return null;
		}
		Entry<String, Long> firstEntry = _entries.getFirst();
		if (firstEntry == null) {
			return null;
		} else {
			return firstEntry.getValue();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[ScoredList:");
		for (Entry<String, Long> entry : _entries) {
			sb.append(' ');
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(entry.getValue());
		}
		sb.append("]");
		return sb.toString();
	}

	public void decrementCapacity() {
		if (_entries.size() == _capacity) {
			if (_topBased) {
				_entries.removeFirst();
			} else {
				_entries.removeLast();
			}
		}
		_capacity--;
	}

	public void removeAbove(long score) {
		Iterator<Entry<String, Long>> it = iterateLowToHigh();
		while (it.hasNext()) {
			Entry<String, Long> entry = it.next();
			if (entry.getValue() > score) {
				it.remove();
			}
		}
	}
}
