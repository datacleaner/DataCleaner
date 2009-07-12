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

import java.util.Iterator;
import java.util.Map.Entry;

import dk.eobjects.datacleaner.profiler.valuedist.ScoredList;

import junit.framework.TestCase;

public class ScoredListTest extends TestCase {

	public void testGetLowestOrHighestScoreOnEmptyList() throws Exception {
		ScoredList sl = new ScoredList(true, 4);
		assertNull(sl.getLowestScore());
		assertNull(sl.getHighestScore());
	}

	public void testAsTopScore() throws Exception {
		ScoredList sl = new ScoredList(true, 4);
		sl.register("a", 4l);
		assertEquals("[ScoredList: a=4]", sl.toString());
		sl.register("b", 3l);
		assertEquals("[ScoredList: b=3 a=4]", sl.toString());
		sl.register("c", 6l);
		sl.register("d", 5l);
		assertEquals("[ScoredList: b=3 a=4 d=5 c=6]", sl.toString());
		sl.register("e", 2l);
		sl.register("f", 1l);
		assertEquals("[ScoredList: b=3 a=4 d=5 c=6]", sl.toString());
		sl.register("g", 8l);
		assertEquals("[ScoredList: a=4 d=5 c=6 g=8]", sl.toString());
	}

	public void testRemoveAbove() throws Exception {
		ScoredList sl1 = new ScoredList(true, 4);
		ScoredList sl2 = new ScoredList(false, 4);
		sl1.register("n", 2l);
		sl1.register("foo", 40l);
		sl2.register("n", 2l);
		sl2.register("foo", 40l);

		Long lowestScore = sl1.getLowestScore();
		assertEquals(2l, lowestScore.longValue());
		sl2.removeAbove(1);

		assertEquals("[ScoredList:]", sl2.toString());
	}

	public void testAsLowScore() throws Exception {
		ScoredList sl = new ScoredList(false, 4);
		sl.register("a", 4l);
		assertEquals("[ScoredList: a=4]", sl.toString());
		sl.register("b", 3l);
		assertEquals("[ScoredList: b=3 a=4]", sl.toString());
		sl.register("c", 6l);
		assertEquals("[ScoredList: b=3 a=4 c=6]", sl.toString());
		sl.register("d", 5l);
		assertEquals("[ScoredList: b=3 a=4 d=5 c=6]", sl.toString());
		sl.register("e", 2l);
		sl.register("f", 1l);
		assertEquals("[ScoredList: e=2 b=3 a=4 d=5]", sl.toString());
		sl.register("g", 8l);
		assertEquals("[ScoredList: e=2 b=3 a=4 d=5]", sl.toString());
	}

	public void testNullKey() throws Exception {
		ScoredList sl = new ScoredList(true, 4);
		sl.register("a", 6l);
		sl.register(null, 4l);
		sl.register("b", 2l);
		assertEquals("[ScoredList: b=2 null=4 a=6]", sl.toString());
	}

	public void testIterateHighToLow() throws Exception {
		ScoredList sl = new ScoredList(true, 4);
		sl.register("a", 6l);
		sl.register("b", 5l);
		sl.register("c", 4l);
		sl.register("d", 3l);
		Iterator<Entry<String, Long>> it = sl.iterateHighToLow();
		assertEquals(true, it.hasNext());
		assertEquals("a", it.next().getKey());
		assertEquals(true, it.hasNext());
		assertEquals("b", it.next().getKey());
		assertEquals(true, it.hasNext());
		assertEquals("c", it.next().getKey());
		assertEquals(true, it.hasNext());
		assertEquals("d", it.next().getKey());
		assertEquals(false, it.hasNext());
	}
}
