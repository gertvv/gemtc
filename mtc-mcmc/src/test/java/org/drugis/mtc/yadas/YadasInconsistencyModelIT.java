package org.drugis.mtc.yadas;

import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.InconsistencyModelTestBase;
import org.drugis.mtc.model.Network;

public class YadasInconsistencyModelIT extends InconsistencyModelTestBase {
	@Override
	protected InconsistencyModel createModel(Network network) {
		return new YadasInconsistencyModel(network);
	}
}
