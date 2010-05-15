package dk.eobjects.datacleaner.profiler.matching;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import dk.eobjects.datacleaner.profiler.AbstractProfile;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.util.FileHelper;

// this profile is currently not used, so warnings are suppressed
@SuppressWarnings("unused")
public class LevenshteinDistanceProfile extends AbstractProfile {


	private List<String> _inputList = new ArrayList<String>();
	private Environment _environment;
	private final int THRESHOLD_PERCENTAGE = 95;
	private final int THRESHOLD_LEVENSHTEIN_DISTANCE = 10;

	@Override
	public void initialize(Column... columns) {

		super.initialize(columns);
		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);

		try {
			File tempDir = FileHelper.getTempDir();
			_environment = new Environment(tempDir, environmentConfig);
		} catch (DatabaseException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected List<IMatrix> getResultMatrices() {
		String[] data = _inputList.toArray(new String[_inputList.size()]) ;
		Iterator<String> primary = _inputList.iterator();
		Iterator<String> secondary;
		
		MatrixBuilder matrixBuilder = new MatrixBuilder();
		
		List<IMatrix> result = new ArrayList<IMatrix>();
		
		matrixBuilder.addColumn("master"); //FIXME: write proper name
		matrixBuilder.addColumn("candidate");
		matrixBuilder.addColumn("Levenshtein Distance");
		matrixBuilder.addColumn("Relative match");
		

		String master;
		for (int i=0;i< data.length;i++){
			
			master = data[i];
			for(int j=i+1;j<data.length;j++){
				String candidate = data[j];
				int score = StringUtils.getLevenshteinDistance(master, candidate);
				double relativeMatch = (1-(((double)score)/master.length()))*100;
				//TODO: do as a percentage. So if a string is 100 char the 3 is 97% match. While if a string is only 10 chars it is only a 70% match
				if (score < THRESHOLD_LEVENSHTEIN_DISTANCE ) {
					System.out.println(master + ":" + candidate + ":" + score+":"+ relativeMatch);					
					matrixBuilder.addRow("Row", master, candidate, new Integer(score),relativeMatch); 					
				}
			}
		}
		result.add(matrixBuilder.getMatrix());
		return result;
	}

	@Override
	protected void processValue(Column column, Object value, long valueCount,
			Row row) {
		_inputList.add(value.toString());
	}

}
