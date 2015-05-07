/*
  StatsService.groovy

  Provides some basic statistical routines
  NOTE:
  1. Some of these functions sort the list(s). If you have already sorted it,
     or if you call more than one of these functions, you can set
     presorted = true to avoid sorting again.
     An exception is range(), which does not sort, but if the group has been
     sorted, takes advantage of this.
*/

package org.sagres.stats;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;


//*****************************************************************************

public
class StatsService 
{                                                                //StatsService
//-----------------------------------------------------------------------------

    public
    static
    double quantile( List< Number > group, double p )
    {
        return quantile( group, p, false );
    }

//.............................................................................
    
    public
    static
    double quantile( List< Number > group, double p,
                     boolean presorted )
    {
        //This is R's type 7 (default) algorithm. Cf. Wikipedia, "Quantile".
        assert( p >= 0.0 );
        assert( p <= 1.0 );
        List< Double > list = new ArrayList< Double >( group.size() );
        for ( Number n : group )
        {
            list.add( n.doubleValue() );
        }
        if ( presorted == false )
        {
            Collections.sort( list );
        }
        int last = list.size() - 1;
        assert( last >= 0 );
        double h = last * p;
        int i = (int) Math.floor( h );
        assert( i >= 0 );
        if ( i >= last )
        {
            return list.get( last );
        }
        double f = h - i;
        double q = list.get( i )  +  f * (list.get( i + 1 ) - list.get( i ));
        return q;
    }

//.............................................................................

    public
    static
    float quantile( float[] group, float p )
    {
        return quantile( group, p, false );
    }

//.............................................................................
    
    public
    static
    float quantile( float[] group, float p,
                     boolean presorted )
    {
        assert( p >= 0.0 );
        assert( p <= 1.0 );
        if ( presorted == false )
        {
            Arrays.sort( group );
        }
        int last = group.length - 1;
        assert( last >= 0 );
        float h = last * p;
        int i = (int) Math.floor( h );
        assert( i >= 0 );
        if ( i >= last )
        {
            return group[ last ];
        }
        float f = h - i;
        float q = group[ i ]  +  f * (group[ i + 1 ] - group[ i ]);
        return q;
    }
    
//.............................................................................

    public
    static
    double quantile( double[] group, double p )
    {
        return quantile( group, p, false );
    }

//.............................................................................
    
    public
    static
    double quantile( double[] group, double p,
                     boolean presorted )
    {
        assert( p >= 0.0 );
        assert( p <= 1.0 );
        if ( presorted == false )
        {
            Arrays.sort( group );
        }
        int last = group.length - 1;
        assert( last >= 0 );
        double h = last * p;
        int i = (int) Math.floor( h );
        assert( i >= 0 );
        if ( i >= last )
        {
            return group[ last ];
        }
        double f = h - i;
        double q = group[ i ]  +  f * (group[ i + 1 ] - group[ i ]);
        return q;
    }
    
//=============================================================================

    public
    static
    double mean( List< Number > group )
    {
        int size = group.size();
        assert( size > 0 );
        double sum = 0.0;
        for ( Number d : group )
            sum += d.doubleValue();
        return sum / size;
    }

//.............................................................................

    public
    static
    float mean( float[] group )
    {
        int size = group.length;
        assert( size > 0 );
        float sum = 0.0f;
        for ( float d : group )
            sum += d;
        return sum / size;
    }
    
//.............................................................................

    public
    static
    double mean( double[] group )
    {
        int size = group.length;
        assert( size > 0 );
        double sum = 0.0;
        for ( double d : group )
            sum += d;
        return sum / size;
    }
    
//-----------------------------------------------------------------------------

    public
    static
    double median( List< Number > group )
    {
        return median( group, false );
    }

 //.............................................................................
   
    public
    static
    double median( List< Number > group, boolean presorted )
    {
        // = quantile 0.5
        int size = group.size();
        assert( size > 0 );
        List< Double > list = new ArrayList< Double >( group.size() );
        for ( Number n : group )
        {
            list.add( n.doubleValue() );
        }
        if ( (presorted == false) && (size > 2) )
        {
            Collections.sort( list );
        }
        int mid = size / 2;
        if ( (size & 1) == 0 )
        {
            return (list.get( mid - 1 ) + list.get( mid )) * 0.5;
        }
        else
        {
            return list.get( mid ).doubleValue();
        }
    }

//.............................................................................

    public
    static
    float median( float[] group )
    {
        return median( group, false );
    }

 //.............................................................................

    public
    static
    float median( float[] group, boolean presorted )
    {
        if ( presorted == false )
        {
            Arrays.sort( group );
        }
        int size = group.length;
        assert( size > 0 );
        if ( (presorted == false) && (size > 2) )
        {
            Arrays.sort( group );
        }
        int mid = size / 2;
        if ( (size & 1) == 0 )
        {
            return (group[ mid - 1 ] + group[ mid ]) * 0.5f;
        }
        else
        {
            return group[ mid ];
        }
    }
    
//.............................................................................

    public
    static
    double median( double[] group )
    {
        return median( group, false );
    }

 //.............................................................................

    public
    static
    double median( double[] group, boolean presorted )
    {
        if ( presorted == false )
        {
            Arrays.sort( group );
        }
        int size = group.length;
        assert( size > 0 );
        if ( (presorted == false) && (size > 2) )
        {
            Arrays.sort( group );
        }
        int mid = size / 2;
        if ( (size & 1) == 0 )
        {
            return (group[ mid - 1 ] + group[ mid ]) * 0.5;
        }
        else
        {
            return group[ mid ];
        }
    }
    
//-----------------------------------------------------------------------------

    public
    static
    double measureOfCentralTendency( List< Number > group, MeasureOfCT measure )
    {
        return measureOfCentralTendency( group, measure, false );
    }
    
//.............................................................................
    
    public
    static
    double measureOfCentralTendency( List< Number > group, MeasureOfCT measure,
                                     boolean presorted )
    {
        switch ( measure )
        {
        case MEAN:
            return mean( group );
        case MEDIAN:
            return median( group, presorted );
        default:
            assert( false );
            return 0.0;
        }
    }

//.............................................................................

    public
    static
    float measureOfCentralTendency( float[] group,
                                     MeasureOfCT measure )
    {
        return measureOfCentralTendency( group, measure, false );
    }
    
//.............................................................................
    
    public
    static
    float measureOfCentralTendency( float[] group, MeasureOfCT measure,
                                     boolean presorted )
    {
        switch ( measure )
        {
        case MEAN:
            return mean( group );
        case MEDIAN:
            return median( group, presorted );
        default:
            assert( false );
            return 0.0f;
        }
    }

//.............................................................................

    public
    static
    double measureOfCentralTendency( double[] group,
                                     MeasureOfCT measure )
    {
        return measureOfCentralTendency( group, measure, false );
    }
    
//.............................................................................
    
    public
    static
    double measureOfCentralTendency( double[] group, MeasureOfCT measure,
                                     boolean presorted )
    {
        switch ( measure )
        {
        case MEAN:
            return mean( group );
        case MEDIAN:
            return median( group, presorted );
        default:
            assert( false );
            return 0.0;
        }
    }

//=============================================================================

    public
    static
    double variance( List< Number > group )
    {
        return variance( group, null );
    }
    
//.............................................................................
    
    public
    static
    double variance( List< Number > group, Double average )
    {
        int size = group.size();
        assert( size > 1 );
        double avg = (average != null) ? average : mean( group );
        //Chan, T.F., et al., American Statistician, vol 37 (1983), p 242-47.
        double diffSum = 0.0;
        double diffSqrSum = 0.0;
        for ( Number d : group )
        {
            double diff = d.doubleValue() - avg;
            diffSum += diff;
            diffSqrSum += diff * diff;
        }
        return ( diffSqrSum  -  (diffSum * diffSum) / size ) / (size - 1.0);
    }

//.............................................................................

    public
    static
    float variance( float[] group )
    {
        return variance( group, null );
    }
    
//.............................................................................

    public
    static
    float variance( float[] group, Float average )
    {
        int size = group.length;
        assert( size > 1 );
        float avg = (average != null) ? average : mean( group );
        float diffSum = 0.0f;
        float diffSqrSum = 0.0f;
        for ( int i = 0; i < size; ++i )
        {
            float diff = group[ i ] - avg;
            diffSum += diff;
            diffSqrSum += diff * diff;
        }
        return ( diffSqrSum  -  (diffSum * diffSum) / size ) / (size - 1.0f);
    }
    
//.............................................................................

    public
    static
    double variance( double[] group )
    {
        return variance( group, null );
    }
    
//.............................................................................

    public
    static
    double variance( double[] group, Double average )
    {
        int size = group.length;
        assert( size > 1 );
        double avg = (average != null) ? average : mean( group );
        double diffSum = 0.0;
        double diffSqrSum = 0.0;
        for ( int i = 0; i < size; ++i )
        {
            double diff = group[ i ] - avg;
            diffSum += diff;
            diffSqrSum += diff * diff;
        }
        return ( diffSqrSum  -  (diffSum * diffSum) / size ) / (size - 1.0);
    }
    
//-----------------------------------------------------------------------------

    public
    static
    double stdDev( List< Number > group )
    {
        return stdDev( group, null );
    }
    
//.............................................................................
    
    public
    static
    double stdDev( List< Number > group, Double avg )
    {
        return Math.sqrt( variance( group, avg ) );
    }

//.............................................................................
    
    public
    static
    float stdDev( float[] group )
    {
        return stdDev( group, null );
    }
    
//.............................................................................
    
    public
    static
    float stdDev( float[] group, Float avg )
    {
        return (float) Math.sqrt( variance( group, avg ) );
    }

//.............................................................................
    
    public
    static
    double stdDev( double[] group )
    {
        return stdDev( group, null );
    }
    
//.............................................................................
    
    public
    static
    double stdDev( double[] group, Double avg )
    {
        return Math.sqrt( variance( group, avg ) );
    }

//-----------------------------------------------------------------------------

    public
    static
    double range( List< Number > group )
    {
        int size = group.size();
        assert( size > 0 );
        double min = group.get( 0 ).doubleValue();
        double max = min;
        for ( int i = 1; i < size; ++i )
        {
            double d = group.get( i ).doubleValue();
            if ( d < min )
                min = d;
            if ( d > max )
                max = d;
        }
        return max - min;
    }

//.............................................................................

    public
    static
    double range( List< Number > group, boolean presorted )
    {
        if ( presorted )
        {
            int size = group.size();
            assert( size > 0 );
            double min = group.get( 0 ).doubleValue();
            double max = group.get( size - 1 ).doubleValue();
            return max - min;
        }
        else
        {
            return range( group );
        }
    }
    
//.............................................................................
    
    public
    static
    float range( float[] group )
    {
        int size = group.length;
        assert( size > 0 );
        float min = group[ 0 ];
        float max = min;
        for ( int i = 1; i < size; ++i )
        {
            float d = group[ i ];
            if ( d < min )
                min = d;
            if ( d > max )
                max = d;
        }
        return max - min;
    }

//.............................................................................

    public
    static
    float range( float[] group, boolean presorted )
    {
        if ( presorted )
        {
            int size = group.length;
            assert( size > 0 );
            float min = group[ 0 ];
            float max = group[ size - 1 ];
            return max - min;
        }
        else
        {
            return range( group );
        }
    }
    
//.............................................................................
    
    public
    static
    double range( double[] group )
    {
        int size = group.length;
        assert( size > 0 );
        double min = group[ 0 ];
        double max = min;
        for ( int i = 1; i < size; ++i )
        {
            double d = group[ i ];
            if ( d < min )
                min = d;
            if ( d > max )
                max = d;
        }
        return max - min;
    }

//.............................................................................

    public
    static
    double range( double[] group, boolean presorted )
    {
        if ( presorted )
        {
            int size = group.length;
            assert( size > 0 );
            double min = group[ 0 ];
            double max = group[ size - 1 ];
            return max - min;
        }
        else
        {
            return range( group );
        }
    }
    
//-----------------------------------------------------------------------------

    public
    static
    double interQuartileRange( List< Number > group )
    {
        return interQuartileRange( group, false );
    }
    
//.............................................................................
    
    public
    static
    double interQuartileRange( List< Number > group, boolean presorted )
    {
        double q3 = quantile( group, 0.75, presorted );
        double q1 = quantile( group, 0.25, presorted );
        return q3 - q1;
    }

//.............................................................................

    public
    static
    float interQuartileRange( float[] group )
    {
        return interQuartileRange( group, false );
    }
    
//.............................................................................
    
    public
    static
    float interQuartileRange( float[] group, boolean presorted )
    {
        float q3 = quantile( group, 0.75f, presorted );
        float q1 = quantile( group, 0.25f, true );
        return q3 - q1;
    }

//.............................................................................

    public
    static
    double interQuartileRange( double[] group )
    {
        return interQuartileRange( group, false );
    }
    
//.............................................................................
    
    public
    static
    double interQuartileRange( double[] group, boolean presorted )
    {
        double q3 = quantile( group, 0.75, presorted );
        double q1 = quantile( group, 0.25, true );
        return q3 - q1;
    }

//=============================================================================

    public
    static
    double correlationCoefficient( List< Number > group0,
                                   List< Number > group1 )
    {   //Pearson product-moment C.C.
        assert( group0.size() == group1.size() );
        int sampleSize = group0.size();
        assert( sampleSize > 1 );
        double mean0 = 0.0;
        double mean1 = 0.0;
        for ( int i = 0; i < sampleSize; ++i )
        {
            mean0 += group0.get( i ).doubleValue();
            mean1 += group1.get( i ).doubleValue();
        }
        mean0 /= sampleSize;
        mean1 /= sampleSize;
        double sum0 = 0.0;
        double sum1 = 0.0;
        double sum01 = 0.0;
        double sum00 = 0.0;
        double sum11 = 0.0;
        for ( int i = 0; i < sampleSize; ++i )
        {
            double diff0 = group0.get( i ).doubleValue() - mean0;
            double diff1 = group1.get( i ).doubleValue() - mean1;
            sum0 += diff0;
            sum1 += diff1;
            sum01 += diff0 * diff1;
            sum00 += diff0 * diff0;
            sum11 += diff1 * diff1;
        }
        //Chan, T.F., et al., American Statistician, vol 37 (1983), p 242-47.
        double var0 = sum00  -  (sum0 * sum0) / sampleSize;
        double var1 = sum11  -  (sum1 * sum1) / sampleSize;
        double cov = sum01 - (sum0 * sum1) / sampleSize;
        double denom = Math.sqrt( var0 * var1 );
        if ( denom == 0.0 )
            return (cov < 0) ? -1.0 : 1.0;
        else
            return cov / denom;
    }

//.............................................................................

    public
    static
    float correlationCoefficient( float[] group0, float[] group1 )
    {
        assert( group0.length == group1.length );
        int sampleSize = group0.length;
        assert( sampleSize > 1 );
        float mean0 = 0.0f;
        float mean1 = 0.0f;
        for ( int i = 0; i < sampleSize; ++i )
        {
            mean0 += group0[ i ];
            mean1 += group1[ i ];
        }
        mean0 /= sampleSize;
        mean1 /= sampleSize;
        float sum0 = 0.0f;
        float sum1 = 0.0f;
        float sum01 = 0.0f;
        float sum00 = 0.0f;
        float sum11 = 0.0f;
        for ( int i = 0; i < sampleSize; ++i )
        {
            float diff0 = group0[ i ] - mean0;
            float diff1 = group1[ i ] - mean1;
            sum0 += diff0;
            sum1 += diff1;
            sum01 += diff0 * diff1;
            sum00 += diff0 * diff0;
            sum11 += diff1 * diff1;
        }
        //Chan, T.F., et al., American Statistician, vol 37 (1983), p 242-47.
        float var0 = sum00  -  (sum0 * sum0) / sampleSize;
        float var1 = sum11  -  (sum1 * sum1) / sampleSize;
        float cov = sum01 - (sum0 * sum1) / sampleSize;
        float denom = (float) Math.sqrt( var0 * var1 );
        if ( denom == 0.0f )
            return (cov < 0) ? -1.0f : 1.0f;
        else
            return cov / denom;
    }
    
//.............................................................................

    public
    static
    double correlationCoefficient( double[] group0, double[] group1 )
    {
        assert( group0.length == group1.length );
        int sampleSize = group0.length;
        assert( sampleSize > 1 );
        double mean0 = 0.0;
        double mean1 = 0.0;
        for ( int i = 0; i < sampleSize; ++i )
        {
            mean0 += group0[ i ];
            mean1 += group1[ i ];
        }
        mean0 /= sampleSize;
        mean1 /= sampleSize;
        double sum0 = 0.0;
        double sum1 = 0.0;
        double sum01 = 0.0;
        double sum00 = 0.0;
        double sum11 = 0.0;
        for ( int i = 0; i < sampleSize; ++i )
        {
            double diff0 = group0[ i ] - mean0;
            double diff1 = group1[ i ] - mean1;
            sum0 += diff0;
            sum1 += diff1;
            sum01 += diff0 * diff1;
            sum00 += diff0 * diff0;
            sum11 += diff1 * diff1;
        }
        //Chan, T.F., et al., American Statistician, vol 37 (1983), p 242-47.
        double var0 = sum00  -  (sum0 * sum0) / sampleSize;
        double var1 = sum11  -  (sum1 * sum1) / sampleSize;
        double cov = sum01 - (sum0 * sum1) / sampleSize;
        double denom = Math.sqrt( var0 * var1 );
        if ( denom == 0.0 )
            return (cov < 0) ? -1.0 : 1.0;
        else
            return cov / denom;
    }

//=============================================================================

    public
    static
    Map<String,?> spearmansRankCorrelation( List< Number > group0,
								  List< Number > group1 )
    {
        assert( group0.size() == group1.size() );
        int sampleSize = group0.size();
        assert( sampleSize > 1 );
        double[][] ranks = new double[ 2 ][];
        ranks[ 0 ] = new double[ sampleSize ];
        ranks[ 1 ] = new double[ sampleSize ];
        for ( int i = 0; i < sampleSize; ++i )
        {
            ranks[ 0 ][ i ] = group0.get( i ).doubleValue();
            ranks[ 1 ][ i ] = group1.get( i ).doubleValue();
        }
        return computeSpearman( ranks );
    }

//-----------------------------------------------------------------------------

    public
    static
    Map<String,?> spearmansRankCorrelation( float[] group0,
								  float[] group1 )
    {
        assert( group0.length == group1.length );
        int sampleSize = group0.length;
        assert( sampleSize > 1 );
        double[][] ranks = new double[ 2 ][];
        ranks[ 0 ] = new double[ sampleSize ];
        ranks[ 1 ] = new double[ sampleSize ];
        for ( int i = 0; i < sampleSize; ++i )
        {
            ranks[ 0 ][ i ] = (double) group0[ i ];
            ranks[ 1 ][ i ] = (double) group1[ i ];
        }
        return computeSpearman( ranks );
    }

//-----------------------------------------------------------------------------

    public
    static
    Map<String,?> spearmansRankCorrelation( double[] group0,
								  double[] group1 )
    {
        assert( group0.length == group1.length );
        int sampleSize = group0.length;
        assert( sampleSize > 1 );
        double[][] ranks = new double[ 2 ][];
        ranks[ 0 ] = new double[ sampleSize ];
        ranks[ 1 ] = new double[ sampleSize ];
        for ( int i = 0; i < sampleSize; ++i )
        {
            ranks[ 0 ][ i ] = group0[ i ];
            ranks[ 1 ][ i ] = group1[ i ];
        }
        return computeSpearman( ranks );
    }

//-----------------------------------------------------------------------------

    private
    static
    Map<String,?> computeSpearman( double[][] ranks )
    {
		Map<String,Object> result = new HashMap<String,Object>( );
        assert( ranks.length == 2 );
        assert( ranks[ 0 ].length == ranks[ 1 ].length );
        int sampleSize = ranks[ 0 ].length;
		boolean noTies = true;
        for ( int i = 0; i < 2; ++i )
        {
            Integer[] index = new Integer[ sampleSize ];
            for ( int j = 0; j < sampleSize; ++j )
                index[ j ] = j;
            Arrays.sort( index, new IndexComparator( ranks[ i ] ) );
            int j = 1;
            while ( j < sampleSize )
            {
                if ( ranks[ i ][ index[ j - 1 ] ] !=
                     ranks[ i ][ index[ j ] ] )
                {
                    ranks[ i ][ index[ j - 1 ] ] = (double) j;
                    ++j;
                }
                else
                {
					noTies = false;
                    int k = j + 1;
                    while ( (k <= sampleSize) &&
                            (ranks[ i ][ index[ k -1 ] ] ==
                             ranks[ i ][ index[ j - 1 ] ]) )
                    {
                        ++k;
                    }
                    double avgRank = 0.5f * (j + k - 1);
                    for ( int m = j; m <= k - 1; ++m )
                    {
                        ranks[ i ][ index[ m - 1 ] ] = avgRank;
                    }
                    j = k;
                }
            }
            if ( j == sampleSize )
                ranks[ i ][ index[ j - 1 ] ] = (double) j;
        }
        double rho = correlationCoefficient( ranks[ 0 ], ranks[ 1 ] );
		double prob = spearmanProbability( rho, sampleSize, noTies );
		result.put( "statistic", rho );
		result.put( "pValue", prob );
		result.put( "sampleSize", sampleSize );
		result.put( "noTies", noTies );
		return result;
    }

//-----------------------------------------------------------------------------
	
	public
	static
	double spearmanProbability( double rho, int n, boolean exact )
	{
		return spearmanProbability( rho, n, "both", exact );
	}
	
//.............................................................................

	public
	static
	double spearmanProbability( double rho, int n, String tail, boolean exact )
	{
		//Essentially translated from R cor.test and pspearman source code.
		if ( rho <= -1.0 )
		{
			return (tail != "upper")  ?  0.0  :  1.0;
		}
		else if ( rho >= 1.0 )
		{
			return (tail != "lower")  ?  0.0  :  1.0;
		}
		if ( tail == "both" )
		{
			double prob;
			if ( rho < 0.0 )
			{
				prob = spearmanProbability( rho, n, "lower", exact );
			}
			else
			{
				prob = spearmanProbability( rho, n, "upper", exact );
			}
			return Math.min( 2.0 * prob, 1.0 );
		}
		int smallN = 9;
		if ( (n <= smallN) && exact )
		{
			double q = (1 - rho) * n * (n*n - 1) / 6.0;
			q = Math.round( q )  +  (tail == "upper"  ?  2.0  :  0.0);
			int nFact = 1;
			int[] l = new int[ smallN ];
			for ( int i = 1; i <= n; ++i )
			{
				nFact *= i;
				l[ i - 1 ] = i;
			}
			double n3 = (double) n;
			n3 *= (n3 * n3  -  1.0) / 3.0;
			int ifr;
			if ( q == n3 )
			{
				ifr = 1;
			}
			else
			{
				ifr = 0;
				for ( int m = 0; m < nFact; ++m )
				{
					int ise = 0;
					int n1;
					for ( int i = 0; i < n; ++i )
					{
						n1 = i + 1 - l[ i ];
						ise += n1 * n1;
					}
					if ( q <= ise )
					{
						++ifr;
					}
					n1 = n;
					int mt;
					do {
						mt = l[ 0 ];
						for ( int i = 1; i < n1; ++i )
						{
							l[ i - 1 ] = l[ i ];
						}
						--n1;
						l[ n1 ] = mt;
					} while ( (mt == n1 + 1) && (n1 > 1) );
				}
			}
			return ((tail == "upper") ? (nFact - ifr) : ifr) / (double)nFact;
		}
		else if ( (n <= 1290) && exact )
		{
			/* Edgeworth coefficients : */
			final double
					c1 = .2274,
					c2 = .2531,
					c3 = .1745,
					c4 = .0758,
					c5 = .1033,
					c6 = .3932,
					c7 = .0879,
					c8 = .0151,
					c9 = .0072,
					c10= .0831,
					c11= .0131,
					c12= .00046;// 4.6e-4;
			double q = (1 - rho) * n * (n*n - 1) / 6.0;
			q = Math.round( q )  +  (tail == "upper"  ?  2.0  :  0.0);
			double y = (double) n;
			double b = 1.0 / y;
			double x = (6.0 * (q - 1.0) * b / (y * y - 1.0)  -  1.0) *
					Math.sqrt( y - 1.0 );
			y = x * x;
			double u = x * b * (c1 + b * (c2 + c3 * b) +
								y * (-c4 + b * (c5 + c6 * b) -
									 y * b * (c7 + c8 * b -
											  y * (c9 - c10 * b +
												   y * b * (c11 - c12 * y)))));
			y = u / Math.exp( y / 2.0 );
			double prob;
			if ( tail == "lower" )
			{
				prob = y + (1.0 - ProbDist.normal_DF( x, 0.0, 1.0 ));
			}
			else
			{
				prob = -y + ProbDist.normal_DF( x, 0.0, 1.0 );
			}
			return Math.max( 0.0, Math.min( prob, 1.0 ) );
		}
		else
		{
			double t = rho * Math.sqrt( (n - 2.0) / (1.0 - rho*rho) );
			double prob = ProbDist.t_DF( t, n - 2 );
			if ( tail == "upper" )
				prob = 1.0 - prob;
			return prob;
		}
	}

//=============================================================================

	public
	static
	double[] fdrAdjust( double[] probs )
	{
		int n = probs.length;
		Integer[] index = new Integer[ n ];
		for ( int i = 0; i < n; ++i )
		{
			index[ i ] = i;
		}
		Arrays.sort( index, new IndexComparator( probs ) );
		double[] adjPs = new double[ n ];
		adjPs[ index[ n - 1 ] ] = probs[ index[ n - 1 ] ];
		for ( int i = n - 2; i >= 0; --i )
		{
			double j = (double)(i + 1);
			adjPs[ index[ i ] ] =
					Math.min( (n / j) * probs[ index[ i ] ],
											adjPs[ index[ i + 1 ] ] );
		}
		return adjPs;
	}
	
//=============================================================================

    private
    static
    class IndexComparator
        implements Comparator< Integer >
    {                                                         //IndexComparator
    //-------------------------------------------------------------------------

        public
        IndexComparator( double[] values )
        {
            m_values = values;
        }
        
    //-------------------------------------------------------------------------

        public int compare( Integer i1, Integer i2 )
        {
            if ( m_values[ i1 ] < m_values[ i2 ] )
                return -1;
            if ( m_values[ i1 ] > m_values[ i2 ] )
                return 1;
            return 0;
        }
        
    //-------------------------------------------------------------------------

        private double[] m_values;
        
    //-------------------------------------------------------------------------
    }                                                         //IndexComparator

    
//=============================================================================

    public
    static
    double difference( List< Number > group0, List< Number > group1,
                       MeasureOfCT measure )
    {
        return difference( group0, group1, measure, true, false );
    }

//.............................................................................

    public
    static
    double difference( List< Number > group0, List< Number > group1,
                       MeasureOfCT measure, boolean absolute )
    {
        return difference( group0, group1, measure, absolute, false );
    }
    
//.............................................................................
    
    public
    static
    double difference( List< Number > group0, List< Number > group1,
                       MeasureOfCT measure, boolean absolute,
                       boolean presorted )
    {
        double stat1 = measureOfCentralTendency( group0, measure, presorted );
        double stat2 = measureOfCentralTendency( group1, measure, presorted );
        double diff = stat1 - stat2;
        if ( absolute && (diff < 0.0) )
        {
            diff = - diff;
        }
        return diff;
    }

//.............................................................................

    public
    static
    float difference( float[] group0, float[] group1,
                       MeasureOfCT measure )
    {
        return difference( group0, group1, measure, true, false );
    }

//.............................................................................

    public
    static
    float difference( float[] group0, float[] group1,
                       MeasureOfCT measure, boolean absolute )
    {
        return difference( group0, group1, measure, absolute, false );
    }
    
//.............................................................................
    
    public
    static
    float difference( float[] group0, float[] group1,
                       MeasureOfCT measure, boolean absolute,
                       boolean presorted )
    {
        float stat1 = measureOfCentralTendency( group0, measure, presorted );
        float stat2 = measureOfCentralTendency( group1, measure, presorted );
        float diff = stat1 - stat2;
        if ( absolute && (diff < 0.0f) )
        {
            diff = - diff;
        }
        return diff;
    }

//.............................................................................

    public
    static
    double difference( double[] group0, double[] group1,
                       MeasureOfCT measure )
    {
        return difference( group0, group1, measure, true, false );
    }

//.............................................................................

    public
    static
    double difference( double[] group0, double[] group1,
                       MeasureOfCT measure, boolean absolute )
    {
        return difference( group0, group1, measure, absolute, false );
    }
    
//.............................................................................
    
    public
    static
    double difference( double[] group0, double[] group1,
                       MeasureOfCT measure, boolean absolute,
                       boolean presorted )
    {
        double stat1 = measureOfCentralTendency( group0, measure, presorted );
        double stat2 = measureOfCentralTendency( group1, measure, presorted );
        double diff = stat1 - stat2;
        if ( absolute && (diff < 0.0) )
        {
            diff = - diff;
        }
        return diff;
    }

//-----------------------------------------------------------------------------

    //"Fold change" is what the English-speaking world calls "ratio."
    
    public
    static
    double foldChange( List< Number > group0, List< Number > group1,
                       MeasureOfCT measure )
    {
        return foldChange( group0, group1, measure, true, false );
    }

//.............................................................................

    public
    static
    double foldChange( List< Number > group0, List< Number > group1,
                       MeasureOfCT measure, boolean absolute )
    {
        return foldChange( group0, group1, measure, absolute, false );
    }
    
//.............................................................................
    
    public
    static
    double foldChange( List< Number > group0, List< Number > group1,
                       MeasureOfCT measure, boolean absolute,
                       boolean presorted )
    {
        double stat1 = measureOfCentralTendency( group0, measure, presorted );
        double stat2 = measureOfCentralTendency( group1, measure, presorted );
        assert( stat2 != 0.0 );
        double ratio = stat1 / stat2;
        if ( absolute && (ratio < 1.0) )
        {
            assert( ratio != 0.0 );
            ratio = 1.0 / ratio;
        }
        return ratio;
    }

//.............................................................................

    public
    static
    float foldChange( float[] group0, float[] group1,
                       MeasureOfCT measure )
    {
        return foldChange( group0, group1, measure, true, false );
    }

//.............................................................................

    public
    static
    float foldChange( float[] group0, float[] group1,
                       MeasureOfCT measure, boolean absolute )
    {
        return foldChange( group0, group1, measure, absolute, false );
    }
    
//.............................................................................
    
    public
    static
    float foldChange( float[] group0, float[] group1,
                       MeasureOfCT measure, boolean absolute,
                       boolean presorted )
    {
        float stat1 = measureOfCentralTendency( group0, measure, presorted );
        float stat2 = measureOfCentralTendency( group1, measure, presorted );
        assert( stat2 != 0.0f );
        float ratio = stat1 / stat2;
        if ( absolute && (ratio < 1.0f) )
        {
            assert( ratio != 0.0f );
            ratio = 1.0f / ratio;
        }
        return ratio;
    }
    
//.............................................................................

    public
    static
    double foldChange( double[] group0, double[] group1,
                       MeasureOfCT measure )
    {
        return foldChange( group0, group1, measure, true, false );
    }

//.............................................................................

    public
    static
    double foldChange( double[] group0, double[] group1,
                       MeasureOfCT measure, boolean absolute )
    {
        return foldChange( group0, group1, measure, absolute, false );
    }
    
//.............................................................................
    
    public
    static
    double foldChange( double[] group0, double[] group1,
                       MeasureOfCT measure, boolean absolute,
                       boolean presorted )
    {
        double stat1 = measureOfCentralTendency( group0, measure, presorted );
        double stat2 = measureOfCentralTendency( group1, measure, presorted );
        assert( stat2 != 0.0 );
        double ratio = stat1 / stat2;
        if ( absolute && (ratio < 1.0) )
        {
            assert( ratio != 0.0 );
            ratio = 1.0 / ratio;
        }
        return ratio;
    }
    
//=============================================================================

    enum MeasureOfCT { MEAN, MEDIAN }; //measures of central tendency
    enum MeasureOfSpread { STDDEV, RANGE, IQR /*inter-quartile range*/ };

//-----------------------------------------------------------------------------
}                                                                //StatsService


//*****************************************************************************
