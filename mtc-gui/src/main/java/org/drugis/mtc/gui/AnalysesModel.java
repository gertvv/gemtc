package org.drugis.mtc.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.collections15.CollectionUtils;
import org.drugis.common.CollectionUtil;
import org.drugis.mtc.presentation.MCMCPresentation;


public class AnalysesModel implements TreeModel {
	public static class RootNode {
		@Override
		public String toString() {
			return "Models";
		}
	}

	private RootNode d_root = new RootNode();
	private SortedMap<ModelType, SortedSet<MCMCPresentation>> d_nodes = new TreeMap<ModelType, SortedSet<MCMCPresentation>>();
	private Set<TreeModelListener> d_listeners = new HashSet<TreeModelListener>();

	/**
	 * Add a model of the given type.
	 * @param type The model type.
	 * @param model An MCMCPresentation wrapping the concrete model.
	 */
	public void add(ModelType type, MCMCPresentation model) {
		if (!d_nodes.containsKey(type)) {
			d_nodes.put(type, new TreeSet<MCMCPresentation>(new Comparator<MCMCPresentation>() {
				@Override
				public int compare(MCMCPresentation o1, MCMCPresentation o2) {
					return o1.getName().compareTo(o2.getName());
				}
			}));
			fireTreeNodeAdded(d_root, type);
		}
		final SortedSet<MCMCPresentation> set = d_nodes.get(type);
		set.add(model);
		fireTreeNodeAdded(type, model);
	}


	public void replace(ModelType type, MCMCPresentation oldPresentation, MCMCPresentation newPresentation) {
		d_nodes.get(type).remove(oldPresentation);
		d_nodes.get(type).add(newPresentation);
		fireNodeReplaced(type, oldPresentation, newPresentation);
	}

	/**
	 * Remove all models.
	 */
	public void clear() {
		final ArrayList<ModelType> nodes = new ArrayList<ModelType>(d_nodes.keySet());
		d_nodes.clear();
		fireTreeNodesRemoved(nodes);
	}

	//
	// TreeModel methods
	//

	@Override
	public Object getRoot() {
		return d_root;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == d_root) {
			return CollectionUtils.get(d_nodes.keySet(), index);
		} else if (parent instanceof ModelType && d_nodes.containsKey(parent)) {
			return CollectionUtils.get(d_nodes.get(parent), index);
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == d_root) {
			return d_nodes.size();
		}
		if (parent instanceof ModelType) {
			SortedSet<MCMCPresentation> children = d_nodes.get(parent);
			return children == null ? 0 : children.size();
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == d_root) {
			return d_nodes.isEmpty();
		}
		return !(node instanceof ModelType);
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == d_root) {
			return CollectionUtil.getIndexOfElement(d_nodes.keySet(), child);
		}
		if (parent instanceof ModelType && d_nodes.containsKey(parent)) {
			return CollectionUtil.getIndexOfElement(d_nodes.get(parent), child);
		}
		return -1;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		throw new UnsupportedOperationException();
	}

	private void fireTreeNodesRemoved(ArrayList<ModelType> nodes) {
		int idx[] = new int[nodes.size()];
		for (int i = 0; i < idx.length; ++i) {
			idx[i] = i;
		}
		TreeModelEvent event = new TreeModelEvent(this, new Object[] { d_root }, idx, nodes.toArray());
		for (TreeModelListener l : new ArrayList<TreeModelListener>(d_listeners)) {
			l.treeNodesRemoved(event);
		}
	}

	private void fireNodeReplaced(ModelType type, MCMCPresentation oldPresentation, MCMCPresentation newPresentation) {
		int idx = getIndexOfChild(type, newPresentation);
		TreeModelEvent event = createTreeEvent(type, newPresentation, idx);
		for (TreeModelListener l : new ArrayList<TreeModelListener>(d_listeners)) {
			l.treeNodesChanged(event);
		}
	}


	private void fireTreeNodeAdded(Object parent, Object newObj) {
		int index = getIndexOfChild(parent, newObj);
		TreeModelEvent event = createTreeEvent(parent, newObj, index);
		for (TreeModelListener l : new ArrayList<TreeModelListener>(d_listeners)) {
			l.treeNodesInserted(event);
		}
	}


	private TreeModelEvent createTreeEvent(Object parent, Object newObj, int index) {
		TreeModelEvent event;
		if (parent == d_root) {
			event = new TreeModelEvent(this, new Object[] { d_root }, new int[] { index }, new Object[] { newObj });
		} else {
			event = new TreeModelEvent(this, new Object[] { d_root, parent }, new int[] { index }, new Object[] { newObj });
		}
		return event;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		d_listeners.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		d_listeners.remove(l);
	}

	public SortedSet<MCMCPresentation> getModels(ModelType type) {
		return d_nodes.get(type);
	}
}
