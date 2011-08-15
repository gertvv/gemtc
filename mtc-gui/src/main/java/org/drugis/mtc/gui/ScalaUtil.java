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
