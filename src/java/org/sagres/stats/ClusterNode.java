/*
  ClusterNode.java

  Node of a cluster tree.
*/

package org.sagres.stats;

import java.util.List;
import java.util.ArrayList;


//*****************************************************************************


public
class ClusterNode
{                                                                 //ClusterNode
//-----------------------------------------------------------------------------

    ClusterNode( int item )
    {
        m_item = item;
    }

//-----------------------------------------------------------------------------

    ClusterNode( List< ClusterNode > children, Double distance )
    {
        if ( m_children == null )
        {
            m_children = new ArrayList< ClusterNode >();
        }
        for ( ClusterNode child : children )
        {
            m_children.add( child );
            child.m_parent = this;
        }
        m_intraclusterDistance = distance;
    }

//-----------------------------------------------------------------------------

    void doRootCalcs( )
    {
        //Should be called from root node after tree has been built.
        calcLeafOffsets( );
    }
    
//=============================================================================

    public
    List< ClusterNode > getChildren( )
    {
        return m_children;
    }
    
//-----------------------------------------------------------------------------
    
    public
    Integer getItem( )
    {
        return m_item;
    }

//-----------------------------------------------------------------------------

    public
    Double getIntraclusterDistance( )
    {
        return m_intraclusterDistance;
    }
    
//=============================================================================

    public
    void getItems( List< Integer > items )
    {
        assert( items != null );
        if ( m_item != null )
        {
            items.add( m_item );
        }
        if ( m_children != null )
        {
            for ( ClusterNode child : m_children )
            {
                child.getItems( items );
            }
        }
    }

    public
    void getNodesFromLeaf(List <ClusterNode> nodes, int depth) {
    	assert (nodes != null);
    	int d = getDistanceFromLeaf();
    	if (d > 0 && d <= depth) {
    		nodes.add(this);
    	} else {
    		if (m_children != null)
    		{
    			for (ClusterNode child : m_children)
    			{
    				child.getNodesFromLeaf(nodes, depth);
    			}
    		}
    	}
    }
    public
    void getItems( List< Integer > items, int depth )
    {
        assert( items != null );
        if ( m_item != null && depth <= 0)
        {
            items.add( m_item );
        }
        if ( m_children != null)
        {
            for ( ClusterNode child : m_children )
            {
                child.getItems( items, depth - 1 );
            }
        }
    }

    public
    int maxDepth()
    {
        if (m_children == null) {
            return (0);
        } else {
    		int maxdepth = 0;
            // compute the depth of each subtree
        	for (ClusterNode child: m_children) {

        		int cDepth = child.maxDepth();
        		maxdepth = Math.max(maxdepth, cDepth);
        	}
        	return (maxdepth + 1);
        }
    }


//-----------------------------------------------------------------------------

    public
    int getDistanceFromLeaf( )
    {
        if ( m_distanceFromLeaf == null )
        {
            if ( m_children == null )
            {
                m_distanceFromLeaf = 0;
            }
            else
            {
                int maxDist = 0;
                for ( ClusterNode child : m_children )
                {
                    int dist = child.getDistanceFromLeaf();
                    if ( dist > maxDist )
                        maxDist = dist;
                }
                m_distanceFromLeaf = maxDist + 1;
            }
        }
        return m_distanceFromLeaf;
    }

//-----------------------------------------------------------------------------

    public
    double getOffset( )
    {
        //calcLeafOffsets must already have been run!
        if ( m_offset == null )
        {
            assert( m_children != null );
            int numChildren = m_children.size();
            assert( numChildren > 0 );
            double firstOffset = m_children.get( 0 ).getOffset( );
            double lastOffset =
                    m_children.get( numChildren - 1 ).getOffset( );
            m_offset = (firstOffset + lastOffset) / 2.0d;
        }
        return m_offset;
    }
    
//=============================================================================
    @SuppressWarnings("unused")
    private
    ClusterNode getRoot( )
    {
        ClusterNode root = this;
        while ( root.m_parent != null )
            root = root.m_parent;
        return root;
    }

//-----------------------------------------------------------------------------

    private
    void getLeaves( List< ClusterNode > leaves )
    {
        assert( leaves != null );
        if ( m_children != null )
        {
            for ( ClusterNode child : m_children )
            {
                child.getLeaves( leaves );
            }
        }
        else
        {
            leaves.add( this );
        }
    }

//-----------------------------------------------------------------------------

    private
    void calcLeafOffsets( )
    {
        //Should only be called from root node.
        List< ClusterNode > leaves = new ArrayList< ClusterNode >();
        getLeaves( leaves );
        double offset = 0.5d;
        for ( ClusterNode leaf : leaves )
        {
            leaf.m_offset = offset;
            offset += 1.0d;
        }
    }

//=============================================================================

    List< ClusterNode > m_children; //Null for leaf nodes
    ClusterNode m_parent;           //Null for root node
    Integer m_item;                 //For leaf node: index of corresponding item
    Double m_intraclusterDistance;  //Null for leaf nodes
    Integer m_distanceFromLeaf;
    Double m_offset;
    
//-----------------------------------------------------------------------------
}                                                                 //ClusterNode


//*****************************************************************************
