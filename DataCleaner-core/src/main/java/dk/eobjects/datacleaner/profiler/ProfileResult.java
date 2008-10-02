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
package dk.eobjects.datacleaner.profiler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class ProfileResult implements IProfileResult {

	private List<IMatrix> _matrices = new ArrayList<IMatrix>();
	private IProfileDescriptor _profileDescriptor;
	private Exception _error;

	public ProfileResult(IProfileDescriptor profileDescriptor) {
		_profileDescriptor = profileDescriptor;
	}

	public ProfileResult(Class<? extends IProfile> profileClass) {
		_profileDescriptor = ProfilerManager
				.getProfileDescriptorByProfileClass(profileClass);
	}

	public IMatrix[] getMatrices() {
		return _matrices.toArray(new IMatrix[_matrices.size()]);
	}

	public void addMatrix(IMatrix matrix) {
		_matrices.add(matrix);
	}

	@Override
	public String toString() {
		return "ProfileResult[profileDescriptor=" + _profileDescriptor
				+ ",matrices=" + ArrayUtils.toString(getMatrices()) + "]";
	}

	public IProfileDescriptor getDescriptor() {
		return _profileDescriptor;
	}

	public void setMatrices(List<IMatrix> matrices) {
		_matrices = matrices;
	}

	public Exception getError() {
		return _error;
	}

	public void setError(Exception error) {
		_error = error;
	}
}