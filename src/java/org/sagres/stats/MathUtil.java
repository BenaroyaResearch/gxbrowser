/*
  MathUtil.java

  Useful math-related routines.
*/

package org.sagres.stats;


import java.util.List;
import cern.colt.matrix.DoubleMatrix2D;


//*****************************************************************************


class MathUtil
{                                                                    //MathUtil
//-----------------------------------------------------------------------------

    public
    static
    double[][] toArray( List< List< Number > > matrix, boolean transpose )
    {
        double[][] array;
        if ( transpose == false )
        {
            int numVecs = matrix.size();
            assert( numVecs > 0 );
            int dim = matrix.get( 0 ).size();
            array = new double[ numVecs ][ dim ];
            for ( int i = 0; i < numVecs; ++i )
                for ( int j = 0; j < dim; ++j )
                {
                    array[ i ][ j ] = matrix.get( i ).get( j ).doubleValue();
                }
        }
        else
        {
            int dim = matrix.size();
            assert( dim > 0 );
            int numVecs = matrix.get( 0 ).size();
            array = new double[ numVecs ][ dim ];
            for ( int i = 0; i < numVecs; ++i )
                for ( int j = 0; j < dim; ++j )
                {
                    array[ i ][ j ] = matrix.get( j ).get( i ).doubleValue();
                }
        }
        return array;
    }

//.............................................................................

    public
    static
    double[][] toArray( DoubleMatrix2D matrix, boolean transpose )
    {
        double[][] array;
        if ( transpose == false )
        {
            array = matrix.toArray();
        }
        else
        {
            int numVecs = matrix.columns();
            int dim = matrix.rows();
            array = new double[ numVecs ][ dim ];
            for ( int i = 0; i < numVecs; ++i )
                for ( int j = 0; j < dim; ++j )
                {
                    array[ i ][ j ] = matrix.get( j, i );
                }
        }
        return array;
    }
    
//=============================================================================

    public
    static
    double euclideanDistance( List< Number > vector0, List< Number > vector1 )
    {
        return Math.sqrt( euclideanDistanceSquared( vector0, vector1 ) );
    }

//.............................................................................

    public
    static
    double euclideanDistanceSquared( List< Number > vector0,
                                     List< Number > vector1 )
    {
        assert( vector0.size() == vector1.size() );
        int dim = vector0.size();
        double sum = 0.0;
        for ( int i = 0; i < dim; ++i )
        {
            double diff = vector0.get( i ).doubleValue() -
                    vector1.get( i ).doubleValue();
            sum += diff * diff;
        }
        return sum;
    }

//.............................................................................

    public
    static
    float euclideanDistance( float[] vector0, float[] vector1 )
    {
        return (float) Math.sqrt( euclideanDistanceSquared( vector0,
                                                            vector1 ) );
    }

//.............................................................................
    
    public
    static
    float euclideanDistanceSquared( float[] vector0, float[] vector1 )
    {
        assert( vector0.length == vector1.length );
        int dim = vector0.length;
        float sum = 0.0f;
        for ( int i = 0; i < dim; ++i )
        {
            float diff = vector0[ i ] - vector1[ i ];
            sum += diff * diff;
        }
        return sum;
    }
    
//.............................................................................

    public
    static
    double euclideanDistance( double[] vector0, double[] vector1 )
    {
        return Math.sqrt( euclideanDistanceSquared( vector0, vector1 ) );
    }

//.............................................................................
    
    public
    static
    double euclideanDistanceSquared( double[] vector0, double[] vector1 )
    {
        assert( vector0.length == vector1.length );
        int dim = vector0.length;
        double sum = 0.0;
        for ( int i = 0; i < dim; ++i )
        {
            double diff = vector0[ i ] - vector1[ i ];
            sum += diff * diff;
        }
        return sum;
    }
    
//-----------------------------------------------------------------------------

    public
    static
    double manhattanDistance( List< Number > vector0, List< Number > vector1 )
    {
        assert( vector0.size() == vector1.size() );
        int dim = vector0.size();
        double sum = 0.0;
        for ( int i = 0; i < dim; ++i )
        {
            sum += Math.abs( vector0.get( i ).doubleValue() -
                             vector1.get( i ).doubleValue() );
        }
        return sum;
    }

//.............................................................................

    public
    static
    float manhattanDistance( float[] vector0, float[] vector1 )
    {
        assert( vector0.length == vector1.length );
        int dim = vector0.length;
        float sum = 0.0f;
        for ( int i = 0; i < dim; ++i )
        {
            sum += Math.abs( vector0[ i ] - vector1[ i ] );
        }
        return sum;
    }

//.............................................................................

    public
    static
    double manhattanDistance( double[] vector0, double[] vector1 )
    {
        assert( vector0.length == vector1.length );
        int dim = vector0.length;
        double sum = 0.0;
        for ( int i = 0; i < dim; ++i )
        {
            sum += Math.abs( vector0[ i ] - vector1[ i ] );
        }
        return sum;
    }

//-----------------------------------------------------------------------------

    public
    static
    float pearsonSqrDistance( float[] vector0, float[] vector1 )
    {
        //This is a monotonic function of Pearson's correlation coefficient, r.
        // Specificially, psd = 1 - signum( r ) * r^2,
        // so r = signum( 1 - psd ) * sqrt( abs( 1 - psd ) ).
        // psd ranges from 0 for identical or completely correlated vectors
        // to 2. It's a bit faster to compute than r because it avoids sqrt.
        assert( vector0.length == vector1.length );
        int dim = vector0.length;
        assert( dim > 1 );
        float mean0 = 0.0f;
        float mean1 = 0.0f;
        for ( int i = 0; i < dim; ++i )
        {
            mean0 += vector0[ i ];
            mean1 += vector1[ i ];
        }
        mean0 /= dim;
        mean1 /= dim;
        float sum0 = 0.0f;
        float sum1 = 0.0f;
        float sum01 = 0.0f;
        float sum00 = 0.0f;
        float sum11 = 0.0f;
        for ( int i = 0; i < dim; ++i )
        {
            float diff0 = vector0[ i ] - mean0;
            float diff1 = vector1[ i ] - mean1;
            sum0 += diff0;
            sum1 += diff1;
            sum01 += diff0 * diff1;
            sum00 += diff0 * diff0;
            sum11 += diff1 * diff1;
        }
        float var0 = sum00  -  (sum0 * sum0) / dim;
        float var1 = sum11  -  (sum1 * sum1) / dim;
        float cov = sum01  -  (sum0 * sum1) / dim;
        float sign = Math.signum( cov );
        float num = sign * cov * cov;
        float denom = var0 * var1;
        if ( denom == 0.0 )
            return (cov < 0) ? 2.0f : 0.0f;
        else
            return 1 - (num / denom);
    }
    
//.............................................................................

    public
    static
    double pearsonSqrDistance( double[] vector0, double[] vector1 )
    {
        //This is a monotonic function of Pearson's correlation coefficient, r.
        // Specificially, psd = 1 - signum( r ) * r^2,
        // so r = signum( 1 - psd ) * sqrt( abs( 1 - psd ) ).
        // psd ranges from 0 for identical or completely correlated vectors
        // to 2. It's a bit faster to compute than r because it avoids sqrt.
        assert( vector0.length == vector1.length );
        int dim = vector0.length;
        assert( dim > 1 );
        double mean0 = 0.0;
        double mean1 = 0.0;
        for ( int i = 0; i < dim; ++i )
        {
            mean0 += vector0[ i ];
            mean1 += vector1[ i ];
        }
        mean0 /= dim;
        mean1 /= dim;
        double sum0 = 0.0;
        double sum1 = 0.0;
        double sum01 = 0.0;
        double sum00 = 0.0;
        double sum11 = 0.0;
        for ( int i = 0; i < dim; ++i )
        {
            double diff0 = vector0[ i ] - mean0;
            double diff1 = vector1[ i ] - mean1;
            sum0 += diff0;
            sum1 += diff1;
            sum01 += diff0 * diff1;
            sum00 += diff0 * diff0;
            sum11 += diff1 * diff1;
        }
        double var0 = sum00  -  (sum0 * sum0) / dim;
        double var1 = sum11  -  (sum1 * sum1) / dim;
        double cov = sum01  -  (sum0 * sum1) / dim;
        double sign = Math.signum( cov );
        double num = sign * cov * cov;
        double denom = var0 * var1;
        if ( denom == 0.0 )
            return (cov < 0) ? 2.0 : 0.0;
        else
            return 1 - (num / denom);
    }
    
//-----------------------------------------------------------------------------
}                                                                    //MathUtil


//*****************************************************************************
