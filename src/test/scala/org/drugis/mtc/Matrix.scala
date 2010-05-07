/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2010 Gert van Valkenhoef.
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

package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class MatrixTest extends ShouldMatchersForJUnit {
	@Test def testIllegalInitialization() {
		intercept[IllegalArgumentException] {
			new Matrix[Int](List[List[Int]](
				List[Int](1, 2, 3),
				List[Int](4, 5),
				List[Int](1, 2, 3)))
		}
	}

	@Test def testDim() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 2, 3),
			List[Int](4, 5, 6)))
	
		m.nRows should be (2)
		m.nCols should be (3)
	}

	@Test def testRowContains() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 2, 3),
			List[Int](4, 5, 6),
			List[Int](1, 2, 3)))

		assert(m.rowContains(0, 1))
		assert(m.rowContains(0, 3))
		assert(m.rowContains(1, 5))
		assert(!m.rowContains(0, 4))
	}

	@Test def testColContains() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 2, 3),
			List[Int](4, 5, 6),
			List[Int](1, 2, 3)))

		assert(m.colContains(0, 1))
		assert(m.colContains(0, 4))
		assert(m.colContains(1, 5))
		assert(!m.colContains(2, 4))
	}

	@Test def testRowOnly() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 1, 3),
			List[Int](4, 4, 4),
			List[Int](1, 2, 1)))

		assert(m.rowOnly(1, 4))
		assert(!m.rowOnly(0, 1))
		assert(!m.rowOnly(2, 1))
	}

	@Test def testColOnly() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 1, 3),
			List[Int](1, 4, 4),
			List[Int](1, 2, 1)))

		assert(m.colOnly(0, 1))
		assert(!m.colOnly(1, 1))
	}

	@Test def testFirstRowWithout() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 1, 3),
			List[Int](4, 4, 3),
			List[Int](1, 2, 1)))

		m.firstRowWithout(0, 1) should be (1)
		m.firstRowWithout(0, 4) should be (0)
		m.firstRowWithout(2, 3) should be (2)
	}

	@Test def testExchangeRows() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 1, 3),
			List[Int](4, 4, 3),
			List[Int](1, 2, 1)))

		val e = new Matrix[Int](List[List[Int]](
			List[Int](4, 4, 3),
			List[Int](1, 1, 3),
			List[Int](1, 2, 1)))

		m.exchangeRows(0, 1) should be (e)
	}
	
	@Test def testReplaceRow() {
		val m = new Matrix[Int](List[List[Int]](
			List[Int](1, 1, 3),
			List[Int](4, 4, 3),
			List[Int](1, 2, 1)))

		val r = List[Int](5, 5, 5)

		val e = new Matrix[Int](List[List[Int]](
			List[Int](1, 1, 3),
			List[Int](5, 5, 5),
			List[Int](1, 2, 1)))

		m.replaceRow(1, r) should be (e)
	}

	// test Gauss elimination on matrix that requires forward pass only
	@Test def testGaussElimForward() {
		val matrix = new Matrix(List[List[Boolean]](
			List[Boolean](false, true, false),
			List[Boolean](true, false, false),
			List[Boolean](true, true, false)))
		val echelon = new Matrix(List[List[Boolean]](
			List[Boolean](true, false, false),
			List[Boolean](false, true, false),
			List[Boolean](false, false, false)))

		Matrix.gaussElimGF2(matrix) should be (echelon)
	}

	// test Gauss elimination on matrix that requires forward and backward pass
	@Test def testGaussElimBackward() {
		val matrix = new Matrix(List[List[Boolean]](
			List[Boolean](true, true, false, false),
			List[Boolean](true, true, false, true),
			List[Boolean](false, true, true, true),
			List[Boolean](false, false, true, false)))
		val echelon = new Matrix(List[List[Boolean]](
			List[Boolean](true, false, false, false),
			List[Boolean](false, true, false, false),
			List[Boolean](false, false, true, false),
			List[Boolean](false, false, false, true)))

		Matrix.gaussElimGF2(matrix) should be (echelon)
	}
}
