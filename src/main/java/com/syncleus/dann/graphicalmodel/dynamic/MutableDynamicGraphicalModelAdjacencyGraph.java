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
package com.syncleus.dann.graphicalmodel.dynamic;

import java.util.*;
import com.syncleus.dann.graph.BidirectedEdge;
import com.syncleus.dann.graph.Graph;
import com.syncleus.dann.graph.context.ContextGraphElement;

public class MutableDynamicGraphicalModelAdjacencyGraph<N extends DynamicGraphicalModelNode, E extends BidirectedEdge<N>> extends AbstractDynamicGraphicalModel<N, E> implements MutableDynamicGraphicalModel<N, E>
{
	private static final long serialVersionUID = -7951102585507791756L;

	public MutableDynamicGraphicalModelAdjacencyGraph()
	{
		super();
	}

	public MutableDynamicGraphicalModelAdjacencyGraph(final Graph<N, E> copyGraph)
	{
		super(copyGraph.getNodes(), copyGraph.getEdges());
	}

	public MutableDynamicGraphicalModelAdjacencyGraph(final Set<N> nodes, final Set<E> edges)
	{
		super(nodes, edges);
	}

	@Override
	public boolean add(final E newEdge)
	{
		if( newEdge == null )
			throw new IllegalArgumentException("newEdge can not be null");
		if( !this.getNodes().containsAll(newEdge.getNodes()) )
			throw new IllegalArgumentException("newEdge has a node as an end point that is not part of the graph");

		// if context is enabled lets check if it can join
		if( this.isContextEnabled() && (newEdge instanceof ContextGraphElement)
				&& !((ContextGraphElement)newEdge).joiningGraph(this) )
			return false;

		if( this.getInternalEdges().add(newEdge) )
		{
			for(final N currentNode : newEdge.getNodes())
			{
				this.getInternalAdjacencyEdges().get(currentNode).add(newEdge);

				final List<N> newAdjacentNodes = new ArrayList<N>(newEdge.getNodes());
				newAdjacentNodes.remove(currentNode);
				for(final N newAdjacentNode : newAdjacentNodes)
					this.getInternalAdjacencyNodes().get(currentNode).add(newAdjacentNode);
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean add(final N newNode)
	{
		if( newNode == null )
			throw new IllegalArgumentException("newNode can not be null");

		if( this.getInternalAdjacencyEdges().containsKey(newNode) )
			return false;

		// if context is enabled lets check if it can join
		if( this.isContextEnabled() && (newNode instanceof ContextGraphElement)
				&& !((ContextGraphElement)newNode).joiningGraph(this) )
			return false;

		this.getInternalAdjacencyEdges().put(newNode, new HashSet<E>());
		this.getInternalAdjacencyNodes().put(newNode, new ArrayList<N>());
		return true;
	}

	@Override
	public boolean remove(final E edgeToRemove)
	{
		if( edgeToRemove == null )
			throw new IllegalArgumentException("removeSynapse can not be null");

		if( !this.getInternalEdges().contains(edgeToRemove) )
			return false;

		// if context is enabled lets check if it can join
		if( this.isContextEnabled()
				&& (edgeToRemove instanceof ContextGraphElement)
				&& !((ContextGraphElement)edgeToRemove).leavingGraph(this) )
			return false;

		if( !this.getInternalEdges().remove(edgeToRemove) )
			return false;

		for(final N removeNode : edgeToRemove.getNodes())
		{
			this.getInternalAdjacencyEdges().get(removeNode).remove(edgeToRemove);

			final List<N> removeAdjacentNodes = new ArrayList<N>(edgeToRemove.getNodes());
			removeAdjacentNodes.remove(removeNode);
			for(final N removeAdjacentNode : removeAdjacentNodes)
				this.getInternalAdjacencyNodes().get(removeNode).remove(removeAdjacentNode);
		}
		return true;
	}

	@Override
	public boolean remove(final N nodeToRemove)
	{
		if( nodeToRemove == null )
			throw new IllegalArgumentException("node can not be null");

		if( !this.getInternalAdjacencyEdges().containsKey(nodeToRemove) )
			return false;

		// if context is enabled lets check if it can join
		if( this.isContextEnabled()
				&& (nodeToRemove instanceof ContextGraphElement)
				&& !((ContextGraphElement)nodeToRemove).leavingGraph(this) )
			return false;

		final Set<E> removeEdges = this.getInternalAdjacencyEdges().get(nodeToRemove);

		//remove all the edges
		for(final E removeEdge : removeEdges)
			this.remove(removeEdge);

		//modify edges by removing the node to remove
		final Set<E> newEdges = new HashSet<E>();
		for(final E removeEdge : removeEdges)
		{
			E newEdge = (E) removeEdge.disconnect(nodeToRemove);
			while( (newEdge != null) && (newEdge.getNodes().contains(nodeToRemove)) )
				newEdge = (E) removeEdge.disconnect(nodeToRemove);
			if( newEdge != null )
				newEdges.add(newEdge);
		}

		//add the modified edges
		for(final E newEdge : newEdges)
			this.add(newEdge);

		//remove the node itself
		this.getInternalAdjacencyEdges().remove(nodeToRemove);
		this.getInternalAdjacencyNodes().remove(nodeToRemove);

		return true;
	}

	@Override
	public boolean clear()
	{
		boolean removedSomething = false;

		//first lets remove all the edges
		for(E edge : this.getEdges())
		{
			//lets just make sure we arent some how getting an we dont actually own, this shouldnt be possible so its
			//an assert. This ensures that if remove() comes back false it must be because the context didnt allow it.
			assert this.getInternalEdges().contains(edge);

			if( !this.remove(edge) )
				throw new IllegalStateException("one of the edges will not allow itself to leave this graph");

			removedSomething = true;
		}

		//now lets remove all the nodes
		for(N node : this.getNodes())
		{
			//lets just make sure we arent some how getting an we dont actually own, this shouldnt be possible so its
			//an assert. This ensures that if remove() comes back false it must be because the context didnt allow it.
			assert ( !this.getInternalAdjacencyEdges().containsKey(node) );

			if( !this.remove(node) )
				throw new IllegalStateException("one of the nodes will not allow itself to leave this graph");

			removedSomething = true;
		}

		return removedSomething;
	}

	@Override
	public MutableDynamicGraphicalModelAdjacencyGraph<N, E> cloneAdd(final E newEdge)
	{
		return (MutableDynamicGraphicalModelAdjacencyGraph<N, E>) super.cloneAdd(newEdge);
	}

	@Override
	public MutableDynamicGraphicalModelAdjacencyGraph<N, E> cloneAdd(final N newNode)
	{
		return (MutableDynamicGraphicalModelAdjacencyGraph<N, E>) super.cloneAdd(newNode);
	}

	@Override
	public MutableDynamicGraphicalModelAdjacencyGraph<N, E> cloneAdd(final Set<N> newNodes, final Set<E> newEdges)
	{
		return (MutableDynamicGraphicalModelAdjacencyGraph<N, E>) super.cloneAdd(newNodes, newEdges);
	}

	@Override
	public MutableDynamicGraphicalModelAdjacencyGraph<N, E> cloneRemove(final E edgeToRemove)
	{
		return (MutableDynamicGraphicalModelAdjacencyGraph<N, E>) super.cloneRemove(edgeToRemove);
	}

	@Override
	public MutableDynamicGraphicalModelAdjacencyGraph<N, E> cloneRemove(final N nodeToRemove)
	{
		return (MutableDynamicGraphicalModelAdjacencyGraph<N, E>) super.cloneRemove(nodeToRemove);
	}

	@Override
	public MutableDynamicGraphicalModelAdjacencyGraph<N, E> cloneRemove(final Set<N> deleteNodes, final Set<E> deleteEdges)
	{
		return (MutableDynamicGraphicalModelAdjacencyGraph<N, E>) super.cloneRemove(deleteNodes, deleteEdges);
	}

	@Override
	public MutableDynamicGraphicalModelAdjacencyGraph<N, E> clone()
	{
		return (MutableDynamicGraphicalModelAdjacencyGraph<N, E>) super.clone();
	}
}
