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
package dk.eobjects.datacleaner.util;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a simplified variant/rewrite of the java.util.Observable class
 * but with the important difference that all observers are stored as weak
 * references. This means that being an observer does not prevent garbage
 * collection.
 */
public class WeakObservable {

	protected transient final Log _log;
	private transient List<WeakReference<WeakObserver>> _observers;
	private transient boolean _changed = false;

	public WeakObservable() {
		_log = LogFactory.getLog(getClass());
		_observers = new LinkedList<WeakReference<WeakObserver>>();
		_changed = false;
	}

	public int getObserverCount() {
		for (Iterator<WeakReference<WeakObserver>> it = _observers.iterator(); it
				.hasNext();) {
			WeakReference<WeakObserver> observerRef = it.next();
			WeakObserver weakObserver = observerRef.get();
			if (weakObserver == null) {
				it.remove();
				if (_log.isDebugEnabled()) {
					_log.debug("Removed weak observer: " + observerRef);
				}
			}
		}
		return _observers.size();
	}

	public void addObserver(WeakObserver observer) {
		_observers.add(new WeakReference<WeakObserver>(observer));
		if (_log.isDebugEnabled()) {
			_log.debug("Added weak observer: " + observer);
			_log.debug("Number of weak observers: " + _observers.size());
		}
	}

	public void deleteObserver(WeakObserver observer) {
		for (Iterator<WeakReference<WeakObserver>> it = _observers.iterator(); it
				.hasNext();) {
			WeakReference<WeakObserver> observerRef = it.next();
			WeakObserver weakObserver = observerRef.get();
			if (weakObserver == null || weakObserver == observer) {
				it.remove();
				if (_log.isDebugEnabled()) {
					_log.debug("Removed weak observer: " + observerRef);
				}
			}
		}
		if (_log.isDebugEnabled()) {
			_log.debug("Number of weak observers: " + _observers.size());
		}
	}

	public void setChanged() {
		_changed = true;
	}

	public void notifyObservers() {
		if (_changed) {
			for (Iterator<WeakReference<WeakObserver>> it = _observers
					.iterator(); it.hasNext();) {
				WeakReference<WeakObserver> observerRef = it.next();
				WeakObserver weakObserver = observerRef.get();
				if (weakObserver == null) {
					it.remove();
					if (_log.isDebugEnabled()) {
						_log.debug("Removed weak observer: " + observerRef);
					}
				} else {
					weakObserver.update(this);
				}
			}
			_changed = false;
		}
	}
}