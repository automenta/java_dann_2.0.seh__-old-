/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package com.syncleus.dann.graphicalmodel.bayesian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.syncleus.dann.graph.DirectedEdge;
import com.syncleus.dann.graphicalmodel.GraphicalModelNode;

public class ImmutableBayesianAdjacencyNetwork<N extends GraphicalModelNode, E extends DirectedEdge<N>> extends AbstractBayesianAdjacencyNetwork<N, E>
{
	private static final long serialVersionUID = -2944480631125238463L;

	public ImmutableBayesianAdjacencyNetwork()
	{
		super();
	}

	// TODO we cant copy because right now each bayesian node is hard linked to a parent graph, this should be fixed
	/*
	public ImmutableMarkovRandomFieldAdjacencyGraph(final Graph<N, E> copyGraph)
	{
		super(copyGraph.getNodes(), copyGraph.getEdges());
	}
	*/

	public ImmutableBayesianAdjacencyNetwork(final Set<N> nodes, final Set<E> edges)
	{
		super(nodes, edges);
	}

	@Override
	protected Set<E> getInternalEdges()
	{
		return Collections.unmodifiableSet(super.getInternalEdges());
	}

	@Override
	protected Map<N, Set<E>> getInternalAdjacencyEdges()
	{
		final Map<N, Set<E>> newAdjacentEdges = new HashMap<N, Set<E>>();
		for(final Entry<N, Set<E>> neighborEdgeEntry : super.getInternalAdjacencyEdges().entrySet())
			newAdjacentEdges.put(neighborEdgeEntry.getKey(), new HashSet<E>(neighborEdgeEntry.getValue()));
		return newAdjacentEdges;
	}

	@Override
	protected Map<N, List<N>> getInternalAdjacencyNodes()
	{
		final Map<N, List<N>> newAdjacentNodes = new HashMap<N, List<N>>();
		for(final Entry<N, List<N>> neighborNodeEntry : super.getInternalAdjacencyNodes().entrySet())
			newAdjacentNodes.put(neighborNodeEntry.getKey(), new ArrayList<N>(neighborNodeEntry.getValue()));
		return newAdjacentNodes;
	}

	@Override
	public ImmutableBayesianAdjacencyNetwork<N, E> cloneAdd(final E newEdge)
	{
		return (ImmutableBayesianAdjacencyNetwork<N, E>) super.cloneAdd(newEdge);
	}

	@Override
	public ImmutableBayesianAdjacencyNetwork<N, E> cloneAdd(final N newNode)
	{
		return (ImmutableBayesianAdjacencyNetwork<N, E>) super.cloneAdd(newNode);
	}

	@Override
	public ImmutableBayesianAdjacencyNetwork<N, E> cloneAdd(final Set<N> newNodes, final Set<E> newEdges)
	{
		return (ImmutableBayesianAdjacencyNetwork<N, E>) super.cloneAdd(newNodes, newEdges);
	}

	@Override
	public ImmutableBayesianAdjacencyNetwork<N, E> cloneRemove(final E edgeToRemove)
	{
		return (ImmutableBayesianAdjacencyNetwork<N, E>) super.cloneRemove(edgeToRemove);
	}

	@Override
	public ImmutableBayesianAdjacencyNetwork<N, E> cloneRemove(final N nodeToRemove)
	{
		return (ImmutableBayesianAdjacencyNetwork<N, E>) super.cloneRemove(nodeToRemove);
	}

	@Override
	public ImmutableBayesianAdjacencyNetwork<N, E> cloneRemove(final Set<N> deleteNodes, final Set<E> deleteEdges)
	{
		return (ImmutableBayesianAdjacencyNetwork<N, E>) super.cloneRemove(deleteNodes, deleteEdges);
	}

	@Override
	public ImmutableBayesianAdjacencyNetwork<N, E> clone()
	{
		return (ImmutableBayesianAdjacencyNetwork<N, E>) super.clone();
	}
}
