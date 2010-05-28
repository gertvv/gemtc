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

final class Treatment(_id: String, _desc: String) extends Ordered[Treatment] {
	val id: String = _id
	val description = _desc
	override def toString = "Treatment(" + id + ")"

	def this(_id: String) = this(_id, "")

	def compare(that: Treatment): Int = id.compare(that.id)

	override def equals(other: Any): Boolean =
		other match {
			case that: Treatment =>
				id == that.id
			case _ => false
		}

	override def hashCode = id.hashCode

	def toXML = <treatment id={id}>{description}</treatment>
}

object Treatment {
	def fromXML(node: scala.xml.Node): Treatment =
		new Treatment((node \ "@id").text, node.text)
}
