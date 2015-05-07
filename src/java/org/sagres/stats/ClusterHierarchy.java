/*
  ClusterHierarchy.java

  A hierarchy of clusters of objects (typically vectors). The result of
  a hierarchical clustering algorithm (see HierarchicalClusterer).
*/

package org.sagres.stats;

import java.util.List;
import java.util.ArrayList;


//*****************************************************************************


public
class ClusterHierarchy
{                                                            //ClusterHierarchy
//-----------------------------------------------------------------------------

    ClusterHierarchy( int numItems )
    {
        m_nodes = new ArrayList< BinaryNode >( numItems - 1 );
    }

//-----------------------------------------------------------------------------

    void append( int idx0, int idx1, double distance )
    {
        m_nodes.add( new BinaryNode( idx0, idx1, distance ) );
    }

//=============================================================================

    public
    ClusterNode getTree( )
    {
        int numItems = m_nodes.size() + 1;
        ClusterNode[] treeNodes = new ClusterNode[ 2 * numItems - 1 ];
        for ( int i = 0; i < numItems; ++i )
        {
            treeNodes[ i ] = new ClusterNode( i );
        }
        for ( int i = 0; i < numItems - 1; ++i )
        {
            BinaryNode node = m_nodes.get( i );
            List< ClusterNode > children = new ArrayList< ClusterNode >();
            children.add( treeNodes[ node.idx0 ] );
            children.add( treeNodes[ node.idx1 ] );
            treeNodes[ i + numItems ] =
                    new ClusterNode( children, node.distance );
        }
        ClusterNode root = treeNodes[ 2 * numItems - 2 ];
        root.doRootCalcs( );
        return root;
    }
    
//=============================================================================

    public
    String toString( )
    {
        return m_nodes.toString();
    }
    
//=============================================================================

    List< BinaryNode > m_nodes;

//=============================================================================

    
    static
    class BinaryNode
    {                                                              //BinaryNode
    //-------------------------------------------------------------------------

        BinaryNode( int idx0, int idx1, double distance )
        {
            this.idx0 = idx0;
            this.idx1 = idx1;
            this.distance = distance;
        }

    //=========================================================================

        public
        String toString()
        {
            return "{ " + Integer.toString( idx0 ) + " + "
                    + Integer.toString( idx1 ) +
                    " (" + Double.toString( distance ) + ")}";
        }
    
    //=========================================================================

        public final int idx0;
        public final int idx1;
        public final double distance;

    //-------------------------------------------------------------------------
    }                                                              //BinaryNode


//-----------------------------------------------------------------------------
}                                                            //ClusterHierarchy


//*****************************************************************************
