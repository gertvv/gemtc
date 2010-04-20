package org.drugis.mtc

object Matrix {
	def swap[T](l: List[T], i: Int, j: Int): List[T] = {
		def aux(l0: List[T], k: Int): List[T] = l0 match {
			case Nil => Nil
			case e :: l1 =>
				(if (k == i) l(j)
				 else if (k == j) l(i)
				 else e) :: aux(l1, k + 1)
		}
		aux(l, 0)
	}

	def replace[T](l: List[T], i: Int, obj: T): List[T] = {
		def aux(l0: List[T], k: Int): List[T] = l0 match {
			case Nil => Nil
			case e :: l1 => (if (k == i) obj else e) :: aux(l1, k + 1)
		}
		aux(l, 0)
	}

	// currently does only forward-elimination, as backward-elimination doesn't
	// affect the number of non-zero rows (i.e., the dimensionality is known
	// after just the forward step).
	def gaussElimGF2(m: Matrix[Boolean]): Matrix[Boolean] = {
		def exchange(m: Matrix[Boolean], row: Int, col: Int)
		: Matrix[Boolean] = {
			if (m.elements(row)(col) == true) m
			else {
				val idx = m.col(col).slice(row, m.nRows).indexOf(true)
				m.exchangeRows(row, idx + row)
			}
		}

		def addRows(m: Matrix[Boolean], addTo: Int, addThis: Int) = {
			m.replaceRow(addTo,
				m.elements(addTo).zip(m.elements(addThis))
				.map(p => p._1 ^ p._2))
		}

		def eliminateRow(m: Matrix[Boolean], row: Int, col: Int, i: Int)
		: Matrix[Boolean] = {
			if (m.elements(i)(col) == false) m
			else addRows(m, i, row)
		}

		def eliminate(m: Matrix[Boolean], row: Int, col: Int)
		: Matrix[Boolean] = {
			// eliminate non-zero values in (i->nRows, col) by using (row, *)
			def aux(m: Matrix[Boolean], row: Int, col: Int, i: Int)
			: Matrix[Boolean] = {
				if (i >= m.nRows) m
				else aux(eliminateRow(m, row, col, i), row, col, i + 1)
			}
			aux(exchange(m, row, col), row, col, col + 1)
		}

		def nonZeroRowsRemain(m: Matrix[Boolean], startRow: Int) = {
			{for {row <- startRow to m.nRows - 1} yield m.rowOnly(row, false)
			}.contains(false)
		}

		// Recursively perform the forward step of Gauss elimination
		def forwardGauss(m: Matrix[Boolean], row: Int, col: Int)
		: Matrix[Boolean] = {
			if (col == m.nCols) m
			else if (!m.col(col).slice(row, m.nRows).contains(true))
				forwardGauss(m, row, col + 1)
			else if (nonZeroRowsRemain(m, row))
				forwardGauss(eliminate(m, row, col), row + 1, col + 1)
			else m
		}


		def backPropagate(m: Matrix[Boolean], row: Int): Matrix[Boolean] = {
			def aux(m: Matrix[Boolean], row: Int, col: Int, i: Int)
			: Matrix[Boolean] = {
				if (i < 0) m
				else aux(eliminateRow(m, row, col, i),
					row, col, i - 1)
			}
			aux(m, row, m.firstColWithout(row, false), row - 1)
		}

		def backwardGauss(m: Matrix[Boolean], row: Int)
		: Matrix[Boolean] = {
			if (row <= 0) m
			else backwardGauss(backPropagate(m, row), row - 1)
		}

		def lastNonZeroRow(m: Matrix[Boolean]): Int = {
			def aux(m: Matrix[Boolean], row: Int): Int = 
				if (row == -1) -1
				else if (m.rowContains(row, true)) row
				else aux(m, row - 1)
			aux(m, m.nRows - 1)
		}

		val fw = forwardGauss(m, 0, 0)
		backwardGauss(fw, lastNonZeroRow(fw))
	}
}

import Matrix._

class Matrix[T](_elements: List[List[T]]) {
	val elements: List[List[T]] = _elements
	val nRows: Int = elements.size
	val nCols: Int = if (elements.isEmpty) 0 else elements.head.size
	require(elements.forall(_.size == nCols))

	def col(col: Int): List[T] = elements.map(r => r(col))

	def row(row: Int): List[T] = elements(row)
	
	def rowContains(row: Int, obj: T): Boolean = 
		_elements(row).contains(obj)

	def colContains(col: Int, obj: T): Boolean = 
		_elements.exists(l => l(col) == obj)

	def rowOnly(row: Int, obj: T): Boolean =
		_elements(row).forall(t => t == obj)

	def colOnly(col: Int, obj: T): Boolean =
		_elements.forall(l => l(col) == obj)

	def firstRowWithout(col: Int, obj: T): Int =
		_elements.findIndexOf(l => l(col) != obj)

	def firstColWithout(row: Int, obj: T): Int =
		_elements(row).findIndexOf(l => l != obj)

	def exchangeRows(r1: Int, r2: Int): Matrix[T] = {
		new Matrix(swap(elements, r1, r2))
	}

	def replaceRow(r1: Int, row: List[T]): Matrix[T] = {
		require(row.size == nCols)
		new Matrix(replace(elements, r1, row))
	}

	override def toString = elements.toString

	override def equals(other: Any) = other match {
		case that: Matrix[_] =>
			(that canEqual this) &&
			this.elements == that.elements
		case _ => false
	}

	def canEqual(other: Any) = other.isInstanceOf[Matrix[_]]
}
