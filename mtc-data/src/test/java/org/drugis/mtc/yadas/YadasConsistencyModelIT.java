package org.drugis.mtc.yadas;

import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.ConsistencyModelTestBase;
import org.drugis.mtc.model.Network;

public class YadasConsistencyModelIT extends ConsistencyModelTestBase {
	@Override
	protected ConsistencyModel createModel(Network network) {
		return new YadasConsistencyModel(network);
	}
}
