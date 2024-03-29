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
package com.syncleus.dann.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.syncleus.dann.UnexpectedDannError;
import com.syncleus.dann.graph.context.ContextGraphElement;
import com.syncleus.dann.graph.xml.*;
import com.syncleus.dann.xml.NameXml;
import com.syncleus.dann.xml.NamedValueXml;
import com.syncleus.dann.xml.Namer;
import com.syncleus.dann.xml.XmlSerializable;
import org.apache.log4j.Logger;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * An AbstractAdjacencyGraph is a Graph implemented using adjacency lists.
 * @param <N> The node type
 * @param <E> The type of edge for the given node type
 */
@XmlJavaTypeAdapter( com.syncleus.dann.xml.XmlSerializableAdapter.class )
public abstract class AbstractAdjacencyGraph<N, E extends Edge<N>> implements Graph<N, E>
{
	private static final Logger LOGGER = Logger.getLogger(AbstractAdjacencyGraph.class);
	private Set<E> edges;
	private Map<N, Set<E>> adjacentEdges = new HashMap<N, Set<E>>();
	private Map<N, List<N>> adjacentNodes = new HashMap<N, List<N>>();
	private final boolean contextEnabled;


	/**
	 * Creates a new AbstractAdjacencyGraph with no edges and no adjacencies.
	 * nodeContext and edgeContext is enabled.
	 */
	protected AbstractAdjacencyGraph()
	{
		this(true);
	}

	/**
	 * Creates a new AbstractAdjacencyGraph with no edges and no adjacencies.
	 */
	protected AbstractAdjacencyGraph(final boolean contextEnabled)
	{
		this.edges = new HashSet<E>();
		this.contextEnabled = contextEnabled;
	}

	/**
	 * Creates a new AbstractAdjacencyGraph as a copy of the current Graph.
	 * nodeContext is enabled.
	 * @param copyGraph The Graph to copy
	 */
	protected AbstractAdjacencyGraph(final Graph<N, E> copyGraph)
	{
		this(copyGraph.getNodes(), copyGraph.getEdges(), true);
	}

	/**
	 * Creates a new AbstractAdjacencyGraph as a copy of the current Graph.
	 * @param copyGraph The Graph to copy
	 */
	protected AbstractAdjacencyGraph(final Graph<N, E> copyGraph, final boolean contextEnabled)
	{
		this(copyGraph.getNodes(), copyGraph.getEdges(), contextEnabled);
	}

	/**
	 * Creates a new AbstractAdjacencyGraph from the given list of nodes, and
	 * the given list of ourEdges.
	 * The adjacency lists are created from this structure. nodeContext is
	 * enabled.
	 *
	 * @param nodes The set of all nodes
	 * @param edges The set of all ourEdges
	 */
	protected AbstractAdjacencyGraph(final Set<N> nodes, final Set<E> edges)
	{
		this(nodes, edges, true);
	}

	/**
	 * Creates a new AbstractAdjacencyGraph from the given list of nodes, and
	 * the given list of ourEdges.
	 * The adjacency lists are created from this structure.
	 *
	 * @param attemptNodes The set of all nodes
	 * @param attemptEdges The set of all ourEdges
	 */
	protected AbstractAdjacencyGraph(final Set<N> attemptNodes, final Set<E> attemptEdges, final boolean contextEnabled)
	{
		if(attemptNodes == null)
			throw new IllegalArgumentException("attemptNodes can not be null");
		if(attemptEdges == null)
			throw new IllegalArgumentException("attemptEdges can not be null");
		//make sure all the edges only connect to contained nodes
		for( E attemptEdge : attemptEdges )
			if( !attemptNodes.containsAll(attemptEdge.getNodes()) )
			    throw new IllegalArgumentException("A node that is an end point in one of the attemptEdges was not in the nodes list");

		this.contextEnabled = contextEnabled;

		//add all the nodes before we worry about edges. check for NodeContext
		for(final N attemptNode : attemptNodes)
		{
			// lets see if this ContextEdge will allow itself to join the graph
			if(this.contextEnabled
					&& (attemptNode instanceof ContextGraphElement)
					&& !((ContextGraphElement)attemptNode).joiningGraph(this) )
				continue;

			this.adjacentNodes.put(attemptNode, new ArrayList<N>());
			this.adjacentEdges.put(attemptNode, new HashSet<E>());
		}

		//Add the edges checking for Edge Context.
		if( this.contextEnabled )
		{
			this.edges = new HashSet<E>(attemptEdges.size());
			for(E attemptEdge : attemptEdges)
			{
				// lets see if this ContextEdge will allow itself to join the graph
				if(this.contextEnabled
						&& (attemptEdge instanceof ContextGraphElement)
						&& !((ContextGraphElement)attemptEdge).joiningGraph(this) )
					continue;


				this.edges.add(attemptEdge);
				//populate adjacency maps
				for(N currentNode : attemptEdge.getNodes())
				{
					boolean passedCurrent = false;
					for(N neighborNode : attemptEdge.getNodes())
					{
						if( !passedCurrent && (neighborNode == currentNode))
						{
							passedCurrent = true;
							continue;
						}

						//this is a neighbor node
						if( !this.adjacentNodes.containsKey(currentNode) )
							throw new IllegalStateException("After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");

						this.adjacentNodes.get(currentNode).add(neighborNode);
					}

					//this is a neighbor edge
					if( !this.adjacentEdges.containsKey(currentNode) )
						throw new IllegalStateException("After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");
					this.adjacentEdges.get(currentNode).add(attemptEdge);
				}
			}
		}
		else
		{
			this.edges = new HashSet<E>(attemptEdges);
		}
	}

	/**
	 * Gets the internal edges of the list.
	 * @return The set of internal edges
	 */
	protected Set<E> getInternalEdges()
	{
		return this.edges;
	}

	/**
	 * Gets the map of nodes to their associated set of edges.
	 * @return The internal adjacency edges to nodes
	 */
	protected Map<N, Set<E>> getInternalAdjacencyEdges()
	{
		return this.adjacentEdges;
	}

	/**
	 * Gets each node's list of adjacent nodes.
	 * @return The map of nodes to adjacent nodes
	 */
	protected Map<N, List<N>> getInternalAdjacencyNodes()
	{
		return this.adjacentNodes;
	}

	@Override
	public boolean isContextEnabled()
	{
		return this.contextEnabled;
	}

	/**
	 * Gets all nodes in the map.
	 * @return The unmodifiable set of nodes
	 */
	@Override
	public Set<N> getNodes()
	{
		return Collections.unmodifiableSet(this.adjacentEdges.keySet());
	}

	/**
	 * Gets all edges in the map.
	 * @return The unmodifiable set of edges
	 */
	@Override
	public Set<E> getEdges()
	{
		return Collections.unmodifiableSet(this.edges);
	}

	/**
	 * Gets all edges adjacent to a given node.
	 * @param node the end point for all edges to retrieve.
	 * @return The edges adjacent to that node.
	 */
	@Override
	public Set<E> getAdjacentEdges(final N node)
	{
		if( this.adjacentEdges.containsKey(node) )
			return Collections.unmodifiableSet(this.adjacentEdges.get(node));
		else
			return Collections.<E>emptySet();
	}

	/**
	 * Gets all nodes adjacent to the given node.
	 * @param node The whose neighbors are to be returned.
	 * @return All adjacent nodes to the given node
	 */
	@Override
	public List<N> getAdjacentNodes(final N node)
	{
		return Collections.unmodifiableList(new ArrayList<N>(this.adjacentNodes.get(node)));
	}

	/**
	 * Gets the traversable nodes adjacent to the given node.
	 * @param node The whose traversable neighbors are to be returned.
	 * @return The traversable nodes adjacent to the given node
	 * @see com.syncleus.dann.graph.Edge#getTraversableNodes(Object)
	 */
	@Override
	public List<N> getTraversableNodes(final N node)
	{
		final List<N> traversableNodes = new ArrayList<N>();
		for(final E adjacentEdge : this.getAdjacentEdges(node))
			traversableNodes.addAll(adjacentEdge.getTraversableNodes(node));
		return Collections.unmodifiableList(traversableNodes);
	}

	/**
	 * Gets the traversable edges from this node.
	 * @param node edges returned will be traversable from this node.
	 * @return The traversable edges from the given node
	 * @see com.syncleus.dann.graph.Edge#isTraversable(Object)
	 */
	@Override
	public Set<E> getTraversableEdges(final N node)
	{
		final Set<E> traversableEdges = new HashSet<E>();
		for(final E adjacentEdge : this.getAdjacentEdges(node))
			if( adjacentEdge.isTraversable(node) )
				traversableEdges.add(adjacentEdge);
		return Collections.unmodifiableSet(traversableEdges);
	}

	/**
	 * Adds the given edge to a clone of this object. Returns null if the given
	 * edge could not be added.
	 * @param newEdge the edge to add to the cloned graph.
	 * @return A clone, with the given edge added
	 */
	@Override
	public AbstractAdjacencyGraph<N, E> cloneAdd(final E newEdge)
	{
		if( newEdge == null )
			throw new IllegalArgumentException("newEdge can not be null");
		if( !this.getNodes().containsAll(newEdge.getNodes()) )
			throw new IllegalArgumentException("newEdge has a node as an end point that is not part of the graph");

		final Set<E> newEdges = new HashSet<E>(this.edges);
		if( newEdges.add(newEdge) )
		{
			final Map<N, Set<E>> newAdjacentEdges = new HashMap<N, Set<E>>();
			for(final Map.Entry<N, Set<E>> neighborEdgeEntry : this.adjacentEdges.entrySet())
				newAdjacentEdges.put(neighborEdgeEntry.getKey(), new HashSet<E>(neighborEdgeEntry.getValue()));
			final Map<N, List<N>> newAdjacentNodes = new HashMap<N, List<N>>();
			for(final Map.Entry<N, List<N>> neighborNodeEntry : this.adjacentNodes.entrySet())
				newAdjacentNodes.put(neighborNodeEntry.getKey(), new ArrayList<N>(neighborNodeEntry.getValue()));

			for(final N currentNode : newEdge.getNodes())
			{
				newAdjacentEdges.get(currentNode).add(newEdge);

				final List<N> currentAdjacentNodes = new ArrayList<N>(newEdge.getNodes());
				currentAdjacentNodes.remove(currentNode);
				for(final N currentAdjacentNode : currentAdjacentNodes)
					newAdjacentNodes.get(currentNode).add(currentAdjacentNode);
			}

			final AbstractAdjacencyGraph<N, E> copy = this.clone();
			copy.edges = newEdges;
			copy.adjacentEdges = newAdjacentEdges;
			copy.adjacentNodes = newAdjacentNodes;
			return copy;
		}

		return null;
	}

	/**
	 * Creates a clone of this graph with the given node added.
	 * NOTE: Returns null.
	 * @param newNode the node to add to the cloned graph.
	 * @return null
	 */
	@Override
	public AbstractAdjacencyGraph<N, E> cloneAdd(final N newNode)
	{
		// TODO Implement this method
		return null;
	}

	@Override
	public AbstractAdjacencyGraph<N, E> cloneAdd(final Set<N> newNodes, final Set<E> newEdges)
	{
		// TODO Implement this method
		return null;
	}

	@Override
	public AbstractAdjacencyGraph<N, E> cloneRemove(final E edgeToRemove)
	{
		// TODO Implement this method
		return null;
	}

	@Override
	public AbstractAdjacencyGraph<N, E> cloneRemove(final N nodeToRemove)
	{
		// TODO Implement this method
		return null;
	}

	@Override
	public AbstractAdjacencyGraph<N, E> cloneRemove(final Set<N> deleteNodes, final Set<E> deleteEdges)
	{
		// TODO Implement this method
		return null;
	}

	/**
	 * Clones the current object.
	 * @return A clone of the current object, with no changes
	 */
	@Override
	public AbstractAdjacencyGraph<N, E> clone()
	{
		try
		{
			final AbstractAdjacencyGraph<N, E> cloneGraph = (AbstractAdjacencyGraph<N, E>) super.clone();

			//lets instantiate some new data structures for our clone
			cloneGraph.adjacentEdges = new HashMap<N, Set<E>>();
			cloneGraph.adjacentNodes = new HashMap<N, List<N>>();

			//add all the nodes before we worry about edges. check for NodeContext
			for(N attemptNode : this.getNodes())
			{
				// lets see if this ContextEdge will allow itself to join the graph
				if(this.contextEnabled
						&& (attemptNode instanceof ContextGraphElement)
						&& !((ContextGraphElement)attemptNode).joiningGraph(cloneGraph) )
					continue;

				cloneGraph.adjacentNodes.put(attemptNode, new ArrayList<N>());
				cloneGraph.adjacentEdges.put(attemptNode, new HashSet<E>());
			}

			//Add the edges checking for Edge Context.
			if( this.contextEnabled )
			{
				cloneGraph.edges = new HashSet<E>(this.getEdges().size());
				for(E attemptEdge : this.getEdges())
				{
					// lets see if this ContextEdge will allow itself to join the graph
					if(this.contextEnabled
							&& (attemptEdge instanceof ContextGraphElement)
							&& !((ContextGraphElement)attemptEdge).joiningGraph(cloneGraph) )
						continue;

					cloneGraph.edges.add(attemptEdge);
					//populate adjacency maps
					for(N currentNode : attemptEdge.getNodes())
					{
						boolean passedCurrent = false;
						for(N neighborNode : attemptEdge.getNodes())
						{
							if( !passedCurrent && (neighborNode == currentNode))
							{
								passedCurrent = true;
								continue;
							}

							//this is a neighbor node
							if( !cloneGraph.adjacentNodes.containsKey(currentNode) )
								throw new IllegalStateException("After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");

							cloneGraph.adjacentNodes.get(currentNode).add(neighborNode);
						}

						//this is a neighbor edge
						if( !cloneGraph.adjacentEdges.containsKey(currentNode) )
							throw new IllegalStateException("After edges and nodes have applied their context restrictions an edge remained connected to a node not in this graph");
						cloneGraph.adjacentEdges.get(currentNode).add(attemptEdge);
					}
				}
			}
			else
			{
				cloneGraph.edges = new HashSet<E>(this.getEdges());
			}

			return cloneGraph;
		}
		catch(CloneNotSupportedException caught)
		{
			LOGGER.error("Unexpectedly could not clone Graph.", caught);
			throw new UnexpectedDannError("Unexpectedly could not clone graph", caught);
		}
	}

	/**
	 * Converts the current AbstractAdjacencyGraph to a GraphXML.
	 * @return The GraphXML representation of this AbstractAdjacencyGraph
	 */
	@Override
	public GraphXml toXml()
	{
		final GraphElementXml xml = new GraphElementXml();
		final Namer<Object> namer = new Namer<Object>();

		xml.setNodeInstances(new GraphElementXml.NodeInstances());
		for(N node : this.adjacentEdges.keySet())
		{
			final String nodeName = namer.getNameOrCreate(node);

			final Object nodeXml;
			if(node instanceof XmlSerializable)
				nodeXml = ((XmlSerializable)node).toXml(namer);
			else
				// if the object is not XmlSerializable lets try to just
				// serialize it as a regular JAXB object
				nodeXml = node;

			final NamedValueXml encapsulation = new NamedValueXml();
			encapsulation.setName(nodeName);
			encapsulation.setValue(nodeXml);

			xml.getNodeInstances().getNodes().add(encapsulation);
		}

		this.toXml(xml, namer);
		return xml;
	}

	/**
	 * Converts a given Namer to its GraphXML representation.
	 * @param namer The namer to convert
	 * @return The GraphXML representation of this namer
	 */
	@Override
	public GraphXml toXml(final Namer<Object> namer)
	{
		if(namer == null)
			throw new IllegalArgumentException("namer can not be null");

		final GraphXml xml = new GraphXml();
		this.toXml(xml, namer);
		return xml;
	}

	/**
	 * Adds a current Namer to the given GraphXML object.
	 * @param jaxbObject The graph to add the object to
	 * @param namer THe namer to add to the GraphXML
	 */
	@Override
	public void toXml(final GraphXml jaxbObject, final Namer<Object> namer)
	{
		if(namer == null)
			throw new IllegalArgumentException("nodeNames can not be null");
		if(jaxbObject == null)
			throw new IllegalArgumentException("jaxbObject can not be null");

		for(N node : this.adjacentEdges.keySet())
		{
			final String nodeName = namer.getNameOrCreate(node);

			final Object nodeXml;
			if(node instanceof XmlSerializable)
				nodeXml = ((XmlSerializable)node).toXml(namer);
			else
				// if the object is not XmlSerializable lets try to just
				// serialize it as a regular JAXB object
				nodeXml = node;

			final NameXml encapsulation = new NameXml();
			encapsulation.setName(nodeName);

			if( jaxbObject.getNodes() == null )
				jaxbObject.setNodes(new GraphXml.Nodes());
			jaxbObject.getNodes().getNodes().add(encapsulation);
		}

		for(E edge : this.edges)
		{
			final EdgeXml edgeXml = edge.toXml(namer);

			if( jaxbObject.getEdges() == null )
				jaxbObject.setEdges(new GraphXml.Edges());
			jaxbObject.getEdges().getEdges().add(edgeXml);
		}
	}
}
