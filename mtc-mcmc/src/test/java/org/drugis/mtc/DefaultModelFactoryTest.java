/*
 * This file is part of the GeMTC software for MTC model generation and
 * analysis. GeMTC is distributed from http://drugis.org/gemtc.
 * Copyright (C) 2009-2012 Gert van Valkenhoef.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.mtc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DefaultModelFactoryTest {
	private static final String YADAS =
		"org.drugis.mtc.yadas.YadasModelFactory";

	@Test
	public void testDefaultInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance();
		assertEquals(Class.forName(YADAS), fac.getClass());
	}

	@Test
	public void testOtherInstance() throws Exception {
		ModelFactory fac = DefaultModelFactory.instance(YADAS);
		assertEquals(Class.forName(YADAS), fac.getClass());
	}
}
