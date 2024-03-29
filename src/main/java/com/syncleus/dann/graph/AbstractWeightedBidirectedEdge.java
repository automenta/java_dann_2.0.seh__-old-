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

public abstract class AbstractWeightedBidirectedEdge<N> extends AbstractBidirectedEdge<N> implements WeightedBidirectedEdge<N>
{
	private final double weight;

	protected AbstractWeightedBidirectedEdge(final double weight)
	{
		super();
		this.weight = weight;
	}

	protected AbstractWeightedBidirectedEdge(final double weight, final boolean allowJoiningMultipleGraphs, final boolean contextEnabled)
	{
		super(allowJoiningMultipleGraphs, contextEnabled);
		this.weight = weight;
	}

	protected AbstractWeightedBidirectedEdge(final N leftNode, final EndState leftEndState, final N rightNode, final EndState rightEndState, final double ourWeight)
	{
		super(leftNode, leftEndState, rightNode, rightEndState);

		this.weight = ourWeight;
	}

	protected AbstractWeightedBidirectedEdge(final N leftNode, final EndState leftEndState, final N rightNode, final EndState rightEndState, final double ourWeight, final boolean allowJoiningMultipleGraphs, final boolean contextEnabled)
	{
		super(leftNode, leftEndState, rightNode, rightEndState, allowJoiningMultipleGraphs, contextEnabled);

		this.weight = ourWeight;
	}

	@Override
	public double getWeight()
	{
		return this.weight;
	}

	@Override
	public AbstractWeightedBidirectedEdge<N> clone()
	{
		return (AbstractWeightedBidirectedEdge<N>) super.clone();
	}
}
