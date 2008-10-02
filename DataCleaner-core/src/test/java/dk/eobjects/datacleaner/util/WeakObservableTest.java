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

import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.util.WeakObserver;
import junit.framework.TestCase;

public class WeakObservableTest extends TestCase {

	private MyWeakObserver _o1;
	private MyWeakObserver _o2;
	private MyWeakObserver _o3;

	public void testGarbageCollection() throws Exception {
		WeakObservable observable = new WeakObservable();
		_o1 = new MyWeakObserver();
		_o2 = new MyWeakObserver();
		_o3 = new MyWeakObserver();
		observable.addObserver(_o1);
		observable.addObserver(_o2);
		observable.addObserver(_o3);

		System.gc();
		System.runFinalization();
		assertEquals(3, observable.getObserverCount());

		_o1 = null;

		System.gc();
		System.runFinalization();
		assertEquals(2, observable.getObserverCount());

		_o2 = null;
		_o3 = null;

		System.gc();
		System.runFinalization();
		assertEquals(0, observable.getObserverCount());
	}

	private class MyWeakObserver implements WeakObserver {
		public void update(WeakObservable observable) {
			System.out.println("foobar");
		}
	}
}