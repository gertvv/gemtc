/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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

package org.drugis.mtc.gui;

public class ScalaUtil {
	@SuppressWarnings("unchecked")
	public static <E> scala.collection.immutable.Set<E> toScalaSet(java.util.Collection<E> col) {
		scala.collection.Iterable<E> asIterable = scala.collection.JavaConversions.asIterable(col);
		return (scala.collection.immutable.Set<E>)new scala.collection.immutable.HashSet<E>().$plus$plus(asIterable);
	}
	
	@SuppressWarnings("unchecked")
	public static <A,B> scala.collection.immutable.Map<A,B> toScalaMap(java.util.Map<A,B> map) {
		scala.collection.mutable.Map<A,B> asMap = scala.collection.JavaConversions.asMap(map);
		return (scala.collection.immutable.HashMap<A,B>)new scala.collection.immutable.HashMap<A,B>().$plus$plus(asMap);
	}
	
	public static <E> java.util.Set<E> toJavaSet(scala.collection.immutable.Set<E> set) {
		return scala.collection.JavaConversions.asSet(set);
	}
}
