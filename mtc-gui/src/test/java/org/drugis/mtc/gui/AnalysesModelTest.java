package org.drugis.mtc.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.drugis.common.JUnitUtil;
import org.drugis.common.event.TreeModelEventMatcher;
import org.drugis.mtc.presentation.ConsistencyWrapper;
import org.drugis.mtc.presentation.MCMCModelWrapper;
import org.drugis.mtc.presentation.MCMCPresentation;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class AnalysesModelTest {
	private MCMCModelWrapper d_wrapper;

	@Before
	public void setUp() {
		d_wrapper = EasyMock.createNiceMock(ConsistencyWrapper.class);
		EasyMock.expect(d_wrapper.isSaved()).andReturn(true).anyTimes();
		EasyMock.replay(d_wrapper);
	}

	@Test
	public void testRootNode() {
		final AnalysesModel model = new AnalysesModel();
		assertTrue(model.getRoot() instanceof AnalysesModel.RootNode);
		assertTrue(model.isLeaf(model.getRoot()));
		assertEquals(0, model.getChildCount(model.getRoot()));
		model.add(ModelType.Consistency, new MCMCPresentation(d_wrapper, "Naam"));
		assertFalse(model.isLeaf(model.getRoot()));
		assertEquals(1, model.getChildCount(model.getRoot()));
	}

	@Test
	public void testTypes() {
		final AnalysesModel model = new AnalysesModel();

		model.add(ModelType.Consistency, new MCMCPresentation(d_wrapper, "cons1"));
		model.add(ModelType.Inconsistency, new MCMCPresentation(d_wrapper, "inco1"));
		model.add(ModelType.Inconsistency, new MCMCPresentation(d_wrapper, "inco2"));
		model.add(ModelType.NodeSplit, new MCMCPresentation(d_wrapper, "splt1"));

		assertEquals(ModelType.Consistency, model.getChild(model.getRoot(), 0));
		assertEquals(ModelType.Inconsistency, model.getChild(model.getRoot(), 1));
		assertEquals(ModelType.NodeSplit, model.getChild(model.getRoot(), 2));

		assertEquals(0, model.getIndexOfChild(model.getRoot(), ModelType.Consistency));
		assertEquals(1, model.getIndexOfChild(model.getRoot(), ModelType.Inconsistency));
		assertEquals(2, model.getIndexOfChild(model.getRoot(), ModelType.NodeSplit));

		assertFalse(model.isLeaf(ModelType.Consistency));
		assertFalse(model.isLeaf(ModelType.Inconsistency));
		assertFalse(model.isLeaf(ModelType.NodeSplit));

		assertEquals(1, model.getChildCount(ModelType.Consistency));
		assertEquals(2, model.getChildCount(ModelType.Inconsistency));
		assertEquals(1, model.getChildCount(ModelType.NodeSplit));
	}

	@Test
	public void testModels() {
		final AnalysesModel model = new AnalysesModel();
		final MCMCPresentation wrap1 = new MCMCPresentation(d_wrapper, "inco1");
		model.add(ModelType.Inconsistency, wrap1);
		final MCMCPresentation wrap2 = new MCMCPresentation(d_wrapper, "inco2");
		model.add(ModelType.Inconsistency, wrap2);

		assertSame(wrap1, model.getChild(ModelType.Inconsistency, 0));
		assertSame(wrap2, model.getChild(ModelType.Inconsistency, 1));

		assertEquals(0, model.getIndexOfChild(ModelType.Inconsistency, wrap1));
		assertEquals(1, model.getIndexOfChild(ModelType.Inconsistency, wrap2));

		assertTrue(model.isLeaf(wrap1));
		assertTrue(model.isLeaf(wrap2));

		assertEquals(0, model.getChildCount(wrap1));
		assertEquals(0, model.getChildCount(wrap2));
	}

	@Test
	public void testClear() {
		final AnalysesModel model = new AnalysesModel();
		final MCMCPresentation inco1 = new MCMCPresentation(d_wrapper, "inco1");
		final MCMCPresentation cons1 = new MCMCPresentation(d_wrapper, "cons1");
		final MCMCPresentation splt1 = new MCMCPresentation(d_wrapper, "splt1");
		final MCMCPresentation splt2 = new MCMCPresentation(d_wrapper, "splt2");
		model.add(ModelType.Inconsistency, inco1);
		model.add(ModelType.Consistency, cons1);
		model.add(ModelType.NodeSplit, splt2);
		model.add(ModelType.NodeSplit, splt1);
		model.clear();

		assertTrue(model.isLeaf(model.getRoot()));
		assertEquals(0, model.getChildCount(model.getRoot()));

		model.add(ModelType.NodeSplit, splt1);
		assertFalse(model.isLeaf(model.getRoot()));
		assertEquals(1, model.getChildCount(model.getRoot()));
		assertEquals(1, model.getChildCount(ModelType.NodeSplit));
	}

	@Test
	public void testListeners() {
		final AnalysesModel model = new AnalysesModel();
		final MCMCPresentation inco1 = new MCMCPresentation(d_wrapper, "inco1");
		final MCMCPresentation cons1 = new MCMCPresentation(d_wrapper, "cons1");
		final MCMCPresentation splt1 = new MCMCPresentation(d_wrapper, "splt1");
		final MCMCPresentation splt2 = new MCMCPresentation(d_wrapper, "splt2");

		TreeModelListener listener = EasyMock.createStrictMock(TreeModelListener.class);
		TreeModelEvent event1a = new TreeModelEvent(model, new Object[] { model.getRoot() }, new int[] { 0 }, new Object[] { ModelType.Inconsistency });
		listener.treeNodesInserted(TreeModelEventMatcher.eqTreeModelEvent(event1a));
		TreeModelEvent event1b = new TreeModelEvent(model, new Object[] { model.getRoot(), ModelType.Inconsistency }, new int[] { 0 }, new Object[] { inco1 });
		listener.treeNodesInserted(TreeModelEventMatcher.eqTreeModelEvent(event1b));
		TreeModelEvent event2a = new TreeModelEvent(model, new Object[] { model.getRoot() }, new int[] { 0 }, new Object[] { ModelType.Consistency });
		listener.treeNodesInserted(TreeModelEventMatcher.eqTreeModelEvent(event2a));
		TreeModelEvent event2b = new TreeModelEvent(model, new Object[] { model.getRoot(), ModelType.Consistency }, new int[] { 0 }, new Object[] { cons1 });
		listener.treeNodesInserted(TreeModelEventMatcher.eqTreeModelEvent(event2b));
		TreeModelEvent event3a = new TreeModelEvent(model, new Object[] { model.getRoot() }, new int[] { 2 }, new Object[] { ModelType.NodeSplit });
		listener.treeNodesInserted(TreeModelEventMatcher.eqTreeModelEvent(event3a));
		TreeModelEvent event3b = new TreeModelEvent(model, new Object[] { model.getRoot(), ModelType.NodeSplit }, new int[] { 0 }, new Object[] { splt2 });
		listener.treeNodesInserted(TreeModelEventMatcher.eqTreeModelEvent(event3b));
		TreeModelEvent event4 = new TreeModelEvent(model, new Object[] { model.getRoot(), ModelType.NodeSplit }, new int[] { 0 }, new Object[] { splt1 });
		listener.treeNodesInserted(TreeModelEventMatcher.eqTreeModelEvent(event4));
		TreeModelEvent event5 = new TreeModelEvent(model, new Object[] { model.getRoot() }, new int[] { 0, 1, 2 }, new Object[] { ModelType.Consistency, ModelType.Inconsistency, ModelType.NodeSplit });
		listener.treeNodesRemoved(TreeModelEventMatcher.eqTreeModelEvent(event5));
		EasyMock.replay(listener);

		model.addTreeModelListener(listener);
		model.add(ModelType.Inconsistency, inco1);
		model.add(ModelType.Consistency, cons1);
		model.add(ModelType.NodeSplit, splt2);
		model.add(ModelType.NodeSplit, splt1);
		model.clear();
		model.removeTreeModelListener(listener);

		model.add(ModelType.Inconsistency, inco1);

		EasyMock.verify(listener);
	}

	@Test
	public void testReplace() {
		final AnalysesModel model = new AnalysesModel();
		final MCMCPresentation inco1 = new MCMCPresentation(d_wrapper, "inco1");
		final MCMCPresentation inco2 = new MCMCPresentation(d_wrapper, "inco2");

		model.add(ModelType.Inconsistency, inco1);
		JUnitUtil.assertAllAndOnly(model.getModels(ModelType.Inconsistency), Collections.singleton(inco1));

		TreeModelListener listener = EasyMock.createStrictMock(TreeModelListener.class);

		TreeModelEvent event1a = new TreeModelEvent(model, new Object[] { model.getRoot(), ModelType.Inconsistency }, new int[] { 0 }, new Object[] { inco2 });
		listener.treeNodesChanged(TreeModelEventMatcher.eqTreeModelEvent(event1a));

		EasyMock.replay(listener);
		model.addTreeModelListener(listener);
		model.replace(ModelType.Inconsistency, inco1, inco2);
		EasyMock.verify(listener);

		JUnitUtil.assertAllAndOnly(model.getModels(ModelType.Inconsistency), Collections.singleton(inco2));


	}

}
