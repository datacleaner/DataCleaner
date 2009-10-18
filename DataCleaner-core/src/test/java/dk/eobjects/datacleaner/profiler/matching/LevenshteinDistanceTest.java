package dk.eobjects.datacleaner.profiler.matching;

import java.util.List;

import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.ProfileManagerTest;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class LevenshteinDistanceTest extends DataCleanerTestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ProfileManagerTest.initProfileManager();
	}
	
	public void testSimpleScore(){
		Table t = new Table("foobar");
		Column[] columns = new Column[] { new Column("foo").setTable(t) };
		t.addColumn(columns[0]);
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(columns[0]) };
		LevenshteinDistanceProfile profile = new LevenshteinDistanceProfile();
		profile.initialize(columns);
		profile.process(new Row(selectItems, new Object[] { "foo" }), 2);
		profile.process(new Row(selectItems, new Object[] { "fooo" }), 1);
		profile.process(new Row(selectItems, new Object[] { "foooo" }), 1);

		List<IMatrix> result= profile.getResultMatrices();
//		assertEquals("Matrix[columnNames={master,candidate,L Score},Row={foo,fooo,1},Row={foo,foooo,2},Row={fooo,foooo,1}]",result.get(0));
		
		
	}
	
	public void testThreshold(){
		Table t = new Table("foobar");
		Column[] columns = new Column[] { new Column("foo").setTable(t) };
		t.addColumn(columns[0]);
		SelectItem[] selectItems = new SelectItem[] { new SelectItem(columns[0]) };
		LevenshteinDistanceProfile profile = new LevenshteinDistanceProfile();
		profile.initialize(columns);
		profile.process(new Row(selectItems, new Object[] { "foo" }), 1);
		profile.process(new Row(selectItems, new Object[] { "bar" }), 2);
		List<IMatrix> result= profile.getResultMatrices();
		assertNotNull(result);
		
		
	}

}
