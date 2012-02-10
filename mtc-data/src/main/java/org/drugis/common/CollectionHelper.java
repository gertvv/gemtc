package org.drugis.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections15.Transformer;

public class CollectionHelper {

	/**
	 * Transform the elements of coll using transform, producing a new collection containing the transformed elements.
	 * @param <I> Input elements type.
	 * @param <O> Output elements type.
	 * @param coll Collection to apply transformation to.
	 * @param transform The transformation to apply.
	 * @return A new collection, containing the transformed elements.
	 */
	public static <I, O> Collection<O> transform(Collection<? extends I> coll, Transformer<? super I, ? extends O> transform) {
	    List<O> list = new ArrayList<O>(coll.size());
	    for (Iterator<? extends I> it = coll.iterator(); it.hasNext();) {
	        list.add(transform.transform(it.next()));
	    }
	    return list;
	}

}
