/*
  HierarchicalClusterer.java

  Computes hierarchy of clusters.
  NOTES:
  1. The algorithms used here are drawn rather directly from the source code
     for the R package fastCluster.
  2. The version of clusterVectors with a double[][] arg modifies the vectors
     in place. The version with a DoubleMatrix2D arg makes a copy and leaves
     the original intact.
*/

package org.sagres.stats;

import java.util.Arrays;
import java.util.List;
import cern.colt.matrix.DoubleMatrix2D;


//*****************************************************************************


public
class HierarchicalClusterer
{                                                       //HierarchicalClusterer
//-----------------------------------------------------------------------------

    enum Metric { EUCLIDEAN, MANHATTAN, PEARSON };

//=============================================================================
    
    public
    static
    ClusterHierarchy clusterVectors( List< List< Number > > matrix,
                                     Metric metric, boolean clusterRows )
    {
        double[][] array = MathUtil.toArray( matrix, (clusterRows == false) );
        return clusterVectors( array, metric );
    }

//.............................................................................

    public
    static
    ClusterHierarchy clusterVectors( DoubleMatrix2D matrix,
                                     Metric metric, boolean clusterRows )
    {
        double[][] array = MathUtil.toArray( matrix, (clusterRows == false) );
        return clusterVectors( array, metric );
    }
    
//.............................................................................

    public
    static
    ClusterHierarchy clusterVectors( double[][] vectors,
                                     Metric metric )
    {
        int numVecs = vectors.length;
        assert( numVecs > 1 );

        ClusterHierarchy hierarchy = new ClusterHierarchy( numVecs );
        
        DistanceManager distMgr = makeDistanceManager( vectors, metric );
        int[] neighbors = new int[ 2 * numVecs - 2 ];
        double[] minDists = new double[ 2 * numVecs - 2 ];
        IndexList active = new IndexList( 2 * numVecs - 1 );

        for ( int i = 1; i < numVecs; ++i )
        {
            int minJ = 0;
            double minD = distMgr.distance( i, 0 );
            for ( int j = 1; j < i; ++j )
            {
                double d = distMgr.distance( i, j );
                if ( d < minD )
                {
                    minJ = j;
                    minD = d;
                }
            }
            neighbors[ i ] = minJ;
            minDists[ i ] = minD;
        }

        BinaryMinHeap minHeap = new BinaryMinHeap( numVecs -1, 1, minDists );
                            
        for ( int i = numVecs; i < 2 * numVecs - 1; ++i )
        {
            int minJ = minHeap.getSmallest( );
            while ( active.isInactive( neighbors[ minJ ] ) )
            {
                int j = active.begin;
                neighbors[ minJ ] = j;
                double minD = distMgr.distance( minJ, j );
                for ( j = active.succ[ j ]; j < minJ;
                      j = active.succ[ j ] )
                {
                    double d = distMgr.distance( minJ, j );
                    if ( d < minD )
                    {
                        minD = d;
                        neighbors[ minJ ] = j;
                    }
                }
                minHeap.updateGeq( minJ, minD );
                minJ = minHeap.getSmallest( );
            }

            int minK = neighbors[ minJ ];
            double dist = distMgr.postProcess( minDists[ minJ ] );
            hierarchy.append( minK, minJ, dist );

            active.remove( minJ );
            active.remove( minK );

            if ( i < 2 * numVecs - 2 )
            {
                distMgr.merge( minJ, minK, i );
                int s = active.begin;
                neighbors[ i ] = s;
                double minD = distMgr.distance( s, i );
                for ( int j = active.succ[ s ]; j < i;
                      j = active.succ[ j ] )
                {
                    double d = distMgr.distance( j, i );
                    if ( d < minD )
                    {
                        minD = d;
                        neighbors[ i ] = j;
                    }
                }
                if ( minK < active.begin )
                {
                    minHeap.remove( active.begin );
                }
                else
                {
                    minHeap.remove( minK );
                }
                minHeap.replace( minJ, i, minD );
            }
        }
        
        return hierarchy;
    }

//=============================================================================

    private
    static
    DistanceManager makeDistanceManager( double[][] vectors,
                                         Metric metric )
    {
        switch ( metric )
        {
        case EUCLIDEAN:
            return new EuclideanDistanceManager( vectors );
        case MANHATTAN:
            return new ManhattanDistanceManager( vectors );
        case PEARSON:
            return new PearsonDistanceManager( vectors );
        default:
            assert( false ); //Unsupported metric
            return null;
        }        
    }
    
//-----------------------------------------------------------------------------
}                                                       //HierarchicalClusterer


//*****************************************************************************


abstract class DistanceManager
{                                                             //DistanceManager
//-----------------------------------------------------------------------------

    public
    DistanceManager( double[][] vectors )
    {
        m_vectors = vectors;
        m_numVecs = vectors.length;
        assert( m_numVecs > 0 );
        m_dim = vectors[ 0 ].length;
        m_reps = new int[ 2 * m_numVecs - 1 ];
        for ( int i = 0; i < m_numVecs; ++i )
            m_reps[ i ] = i;
        m_weights = new double[ m_numVecs ];
        for ( int i = 0; i < m_numVecs; ++i )
            m_weights[ i ] = 1.0d;
    }

//-----------------------------------------------------------------------------

    public
    abstract
    double distance( int idx0, int idx1 );

//.............................................................................

    public
    abstract
    double postProcess( double rawDist );
    
//-----------------------------------------------------------------------------

    public
    void merge( int idx0, int idx1, int newIdx )
    {
        int i = m_reps[ idx0 ];
        int j = m_reps[ idx1 ];
        double[] vector0 = m_vectors[ i ];
        double[] vector1 = m_vectors[ j ];
        double weight0 = m_weights[ i ];
        double weight1 = m_weights[ j ];
        double totalWt = weight0 + weight1;
        assert( totalWt > 0.0d );
        for ( int k = 0; k < m_dim; ++k )
        {
            vector1[ k ] = (vector0[ k ] * weight0 +
                            vector1[ k ] * weight1) / totalWt;
        }
        m_weights[ j ] = totalWt;
        m_reps[ newIdx ] = j;
    }
    
//=============================================================================

    double[][] m_vectors;
    int m_numVecs;
    int m_dim;
    int[] m_reps;
    double[] m_weights;
    
//-----------------------------------------------------------------------------
}                                                             //DistanceManager


//*****************************************************************************


class EuclideanDistanceManager
extends DistanceManager
{                                                    //EuclideanDistanceManager
//-----------------------------------------------------------------------------

    EuclideanDistanceManager( double[][] vectors )
    {
        super( vectors );
    }

//-----------------------------------------------------------------------------

    @Override
    public
    double distance( int idx0, int idx1 )
    {
        return MathUtil.euclideanDistanceSquared(
            m_vectors[ m_reps[ idx0 ] ], m_vectors[ m_reps[ idx1 ] ] );
    }
    
//.............................................................................

    @Override
    public
    double postProcess( double rawDist )
    {
        return Math.sqrt( rawDist );
    }
    
//-----------------------------------------------------------------------------
}                                                    //EuclideanDistanceManager


//*****************************************************************************


class ManhattanDistanceManager
extends DistanceManager
{                                                    //ManhattanDistanceManager
//-----------------------------------------------------------------------------

    ManhattanDistanceManager( double[][] vectors )
    {
        super( vectors );
    }

//-----------------------------------------------------------------------------

    @Override
    public
    double distance( int idx0, int idx1 )
    {
        return MathUtil.manhattanDistance( 
            m_vectors[ m_reps[ idx0 ] ], m_vectors[ m_reps[ idx1 ] ] );
    }
    
//.............................................................................

    @Override
    public
    double postProcess( double rawDist )
    {
        return rawDist;
    }
    
//-----------------------------------------------------------------------------
}                                                    //ManhattanDistanceManager


//*****************************************************************************


class PearsonDistanceManager
extends DistanceManager
{                                                      //PearsonDistanceManager
//-----------------------------------------------------------------------------

    PearsonDistanceManager( double[][] vectors )
    {
        super( vectors );
    }

//-----------------------------------------------------------------------------

    @Override
    public
    double distance( int idx0, int idx1 )
    {
        return MathUtil.pearsonSqrDistance( 
            m_vectors[ m_reps[ idx0 ] ], m_vectors[ m_reps[ idx1 ] ] );
    }
    
//.............................................................................

    @Override
    public
    double postProcess( double rawDist )
    {
        double a = 1 - rawDist;
        double r = Math.signum( a ) * Math.sqrt( Math.abs( a ) );
        return 1 - r;
    }
    
//-----------------------------------------------------------------------------
}                                                      //PearsonDistanceManager


//*****************************************************************************


class IndexList
{                                                                   //IndexList
//-----------------------------------------------------------------------------

    public
    IndexList( int size )
    {
        begin = 0;
        this.size = size;
        succ = new int[ size + 1 ];
        pred = new int[ size + 1 ];
        for ( int i = 0; i < size; ++i )
        {
            succ[ i ] = i + 1;
            pred[ i + 1 ] = i;
        }
    }

//=============================================================================

    public
    void remove( int index )
    {
        if ( index == begin )
        {
            begin = succ[ index ];
        }
        else
        {
            succ[ pred[ index ] ] = succ[ index ];
            pred[ succ[ index ] ] = pred[ index ];
        }
        succ[ index ] = 0;
    }

//=============================================================================

    public
    boolean isInactive( int index )
    {
        return (succ[ index ] == 0);
    }

//=============================================================================

    @Override
    public
    String toString( )
    {
        String s = "[ ";
        for ( int i = begin; i < size; i = succ[ i ] )
        {
            s += i;
            if ( succ[ i ] < size )
                s += ", ";
        }
        s += " ]";
        return s;
    }
    
//=============================================================================

    public int begin;
    public int size;
    public int[] succ;
    public int[] pred;
    
//-----------------------------------------------------------------------------
}                                                                   //IndexList


//*****************************************************************************


class BinaryMinHeap
{                                                               //BinaryMinHeap
//-----------------------------------------------------------------------------

    public
    BinaryMinHeap( int size, double[] distances )
    {
        m_size = size;
        m_ind = new int[ size ];
        m_rev = new int[ size ];
        for ( int i = 0; i < size; ++i )
        {
            m_ind[ i ] = i;
            m_rev[ i ] = i;
        }
        m_distances = distances;
        heapify( );
    }
    
//-----------------------------------------------------------------------------

    public
    BinaryMinHeap( int size, int start, double[] distances )
    {
        m_size = size;
        m_ind = new int[ size ];
        m_rev = new int[ 2 * size ];
        for ( int i = 0; i < size; ++i )
        {
            m_ind[ i ] = i + start;
            m_rev[ i + start ] = i;
        }
        m_distances = distances;
        heapify( );
    }

//-----------------------------------------------------------------------------

    public
    int getSmallest( )
    {
        return m_ind[ 0 ];
    }

//-----------------------------------------------------------------------------

    public
    void updateGeq( int idx, double dist )
    {
        m_distances[ idx ] = dist;
        updateGeq( m_rev[ idx ] );
    }

//-----------------------------------------------------------------------------

    public
    void remove( int idx )
    {
        --m_size;
        m_rev[ m_ind[ m_size ] ] = m_rev[ idx ];
        m_ind[ m_rev[ idx ] ] = m_ind[ m_size ];
        if ( heapDist( m_size ) <= m_distances[ idx ] )
        {
            updateLeq( m_rev[ idx ] );
        }
        else
        {
            updateGeq( m_rev[ idx ] );
        }
    }

//-----------------------------------------------------------------------------

    public
    void replace( int oldIdx, int newIdx, double dist )
    {
        m_rev[ newIdx ] = m_rev[ oldIdx ];
        m_ind[ m_rev[ newIdx ] ] = newIdx;
        if ( dist <= m_distances[ oldIdx ] )
        {
            updateLeq( newIdx, dist );
        }
        else
        {
            updateGeq( newIdx, dist );
        }
    }
    
//=============================================================================

    private
    void heapify( )
    {
        for ( int i = m_size / 2; i > 0; )
        {
            --i;
            updateGeq( i );
        }
    }

//-----------------------------------------------------------------------------

    private
    void updateGeq( int idx )
    {
        while ( true )
        {
            int j = 2 * idx + 1;
            if ( j >= m_size )
                break;
            if ( heapDist( j ) >= heapDist( idx ) )
            {
                ++j;
                if ( (j >= m_size) || (heapDist( j ) >= heapDist( idx )) )
                {
                    break;
                }
            }
            else if ( (j + 1 < m_size) && (heapDist( j + 1 ) < heapDist( j )) )
            {
                ++j;
            }
            heapSwap( idx, j );
            idx = j;
        }
    }

//-----------------------------------------------------------------------------

    private
    void updateLeq( int idx )
    {
        while ( true )
        {
            if ( idx <= 0 )
                break;
            int j = (idx - 1) / 2;
            if ( heapDist( idx ) >= heapDist( j ) )
                break;
            heapSwap( idx, j );
            idx = j;
        }
    }

//.............................................................................

    private
    void updateLeq( int idx, double dist )
    {
        m_distances[ idx ] = dist;
        updateLeq( m_rev[ idx ] );
    }
    
//-----------------------------------------------------------------------------

    private
    double heapDist( int idx )
    {
        return m_distances[ m_ind[ idx ] ];
    }

//-----------------------------------------------------------------------------

    private
    void heapSwap( int i, int j )
    {
        int t = m_ind[ i ];
        m_ind[ i ] = m_ind[ j ];
        m_ind[ j ] = t;
        m_rev[ m_ind[ i ] ] = i;
        m_rev[ m_ind[ j ] ] = j;
    }

//=============================================================================

    @Override
    public
    String toString( )
    {
        return "{ size=" + m_size + ";\n" +
                "ind=" + Arrays.toString( m_ind ) + ";\n" +
                "rev=" + Arrays.toString( m_rev ) + ";\n" +
                "distances=" + Arrays.toString( m_distances ) + " }";
    }
    
//=============================================================================

    private int m_size;
    private int[] m_ind;
    private int[] m_rev;
    private double[] m_distances;
    
//-----------------------------------------------------------------------------
}                                                               //BinaryMinHeap


//*****************************************************************************
