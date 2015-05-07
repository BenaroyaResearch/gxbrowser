//Test with:
// grails test-app unit: org.sagres.stats.StatsService

package org.sagres.stats;

import grails.test.*
import cern.colt.list.DoubleArrayList;

class StatsServiceTests 
extends GrailsUnitTestCase 
{                                                           //StatsServiceTests
//-----------------------------------------------------------------------------

    protected void setUp() {
        super.setUp()
    }

//-----------------------------------------------------------------------------

    protected void tearDown() {
        super.tearDown()
    }

//=============================================================================

	void testProbDist( )
	{
		assertEquals( 0.398942280401433d,
					  ProbDist.normal_PDF( 0.0d, 0.0d, 1.0d ),
					  0.000000000000001d );
		assertEquals( 0.129517595665892d,
					  ProbDist.normal_PDF( 1.5d, 0.0d, 1.0d ),
					  0.000000000000001d );
		assertEquals( 0.009871153794751d,
					  ProbDist.normal_PDF( -2.72d, 0.0d, 1.0d ),
					  0.000000000000001d );
		assertEquals( 0.01d, ProbDist.normal_DF( -2.32635d, 0.0d, 1.0d ),
					  0.0000001d );
		assertEquals( 0.5d, ProbDist.normal_DF( 0.0d, 0.0d, 1.0d ),
					  0.0000001d );
		assertEquals( 0.84134474606855d,
					  ProbDist.normal_DF( 1.0d, 0.0d, 1.0d ),
					  0.0000001d );
		assertEquals( 0.9d, ProbDist.normal_DF( 1.28155d, 0.0d, 1.0d ),
					  0.000001d );
		assertEquals( 0.9772498680518d,
					  ProbDist.normal_DF( 2.0d, 0.0d, 1.0d ),
					  0.0000001d );
		assertEquals( 0.99865010196835d,
					  ProbDist.normal_DF( 3.0d, 0.0d, 1.0d ),
					  0.0000001d );

		assertEquals( 0.6d, ProbDist.t_DF( 0.325d, 1 ), 0.001d );
		assertEquals( 0.75d, ProbDist.t_DF( 1.0d, 1 ), 0.001d );
		assertEquals( 0.99d, ProbDist.t_DF( 31.821d, 1 ), 0.00001d );
		assertEquals( 0.9995d, ProbDist.t_DF( 636.619d, 1 ), 0.000000001d );
		assertEquals( 0.6d, ProbDist.t_DF( 0.267d, 5 ), 0.001d );
		assertEquals( 0.975d, ProbDist.t_DF( 2.571d, 5 ), 0.0001d );
		assertEquals( 0.5d, ProbDist.t_DF( 0.0d, 10 ) );
		assertEquals( 0.05d, ProbDist.t_DF( -1.725d, 20 ), 0.0006d );
	}

//-----------------------------------------------------------------------------

    void testDescriptiveStats( )
    {
        List< Float > fNums = [ 20.0f, 40.0f, 10.0f, 50.0f, 30.0f ];
        assertEquals( 30.0f, StatsService.median( fNums ) );
        assertEquals( 10.0f, StatsService.quantile( fNums, 0.0f ) );
        assertEquals( 20.0f, StatsService.quantile( fNums, 0.25f ) );
        assertEquals( 30.0f, StatsService.quantile( fNums, 0.50f ) );
        assertEquals( 40.0f, StatsService.quantile( fNums, 0.75f ) );
        assertEquals( 50.0f, StatsService.quantile( fNums, 1.00f ) );
        assertEquals( 30.0f, StatsService.mean( fNums ) );
        assertEquals( 250.0f, StatsService.variance( fNums ) );
        assertEquals( 15.8114f, StatsService.stdDev( fNums ), 0.0001f );
        assertEquals( 40.0f, StatsService.range( fNums ) );
        assertEquals( 20.0f, StatsService.interQuartileRange( fNums ) );

        float[] fArr = fNums.toArray();
        assertEquals( 30.0f, StatsService.median( fArr ) );
        assertEquals( 10.0f, StatsService.quantile( fArr, 0.0f ) );
        assertEquals( 20.0f, StatsService.quantile( fArr, 0.25f ) );
        assertEquals( 30.0f, StatsService.quantile( fArr, 0.50f ) );
        assertEquals( 40.0f, StatsService.quantile( fArr, 0.75f ) );
        assertEquals( 50.0f, StatsService.quantile( fArr, 1.00f ) );
        assertEquals( 30.0f, StatsService.mean( fArr ) );
        assertEquals( 250.0f, StatsService.variance( fArr ) );
        assertEquals( 15.8114f, StatsService.stdDev( fArr ), 0.0001f );
        assertEquals( 40.0f, StatsService.range( fArr ) );
        assertEquals( 20.0f, StatsService.interQuartileRange( fArr ) );
        
        fNums = [ 30.0f, 50.0f, 10.0f, 60.0f, 40.0f, 20.0f ];
        assertEquals( 35.0f, StatsService.median( fNums ) );
        assertEquals( 10.0f, StatsService.quantile( fNums, 0.0f, false ) );
        assertEquals( 22.5f, StatsService.quantile( fNums, 0.25f, false ) );
        assertEquals( 35.0f, StatsService.quantile( fNums, 0.50f ) );
        assertEquals( 47.5f, StatsService.quantile( fNums, 0.75f, false ) );
        assertEquals( 60.0f, StatsService.quantile( fNums, 1.00f ) );
        assertEquals( 35.0f, StatsService.mean( fNums ) );
        assertEquals( 350.0f, StatsService.variance( fNums, 35.0f ) );
        assertEquals( 18.7083f, StatsService.stdDev( fNums, 35.0f ), 0.0001f );
        assertEquals( 50.0f, StatsService.range( fNums ) );
        assertEquals( 25.0f, StatsService.interQuartileRange( fNums, false ) );

        fArr = fNums.toArray();
        assertEquals( 35.0f, StatsService.median( fArr ) );
        assertEquals( 10.0f, StatsService.quantile( fArr, 0.0f, true ) );
        assertEquals( 22.5f, StatsService.quantile( fArr, 0.25f, true ) );
        assertEquals( 35.0f, StatsService.quantile( fArr, 0.50f ) );
        assertEquals( 47.5f, StatsService.quantile( fArr, 0.75f, true ) );
        assertEquals( 60.0f, StatsService.quantile( fArr, 1.00f ) );
        assertEquals( 35.0f, StatsService.mean( fArr ) );
        assertEquals( 350.0f, StatsService.variance( fArr, 35.0f ) );
        assertEquals( 18.7083f, StatsService.stdDev( fArr, 35.0f ), 0.0001f );
        assertEquals( 50.0f, StatsService.range( fArr ) );
        assertEquals( 25.0f, StatsService.interQuartileRange( fArr, true ) );

        List< Float > fGroup0 = [ 68.0f, 68.0f, 67.0f, 70.0f, 71.0f, 73.0f,
                                  76.0f, 81.0f, 83.0f, 84.0f ];
        List< Float > fGroup1 = [ 53.0f, 38.0f, 35.0f, 49.0f, 42.0f, 60.0f,
                                  54.0f, 67.0f, 82.0f, 78.0f ];
        assertEquals( 0.922f,
                      StatsService.correlationCoefficient( fGroup0, fGroup1 ),
                      0.001f );
        float[] fArr0 = fGroup0.toArray();
        float[] fArr1 = fGroup1.toArray();
        assertEquals( 0.922f,
                      StatsService.correlationCoefficient( fArr0, fArr1 ),
                      0.001f );
        assertEquals( 0.150f,
                      MathUtil.pearsonSqrDistance( fArr0, fArr1 ),
                      0.001f );

        List< Double > dNums = [ 20.0d, 40.0d, 10.0d, 50.0d, 30.0d ];
        assertEquals( 30.0d, StatsService.median( dNums ) );
        assertEquals( 10.0d, StatsService.quantile( dNums, 0.0d ) );
        assertEquals( 20.0d, StatsService.quantile( dNums, 0.25d ) );
        assertEquals( 30.0d, StatsService.quantile( dNums, 0.50d ) );
        assertEquals( 40.0d, StatsService.quantile( dNums, 0.75d ) );
        assertEquals( 50.0d, StatsService.quantile( dNums, 1.00d ) );
        assertEquals( 30.0d, StatsService.mean( dNums ) );
        assertEquals( 250.0d, StatsService.variance( dNums ) );
        assertEquals( 15.8114d, StatsService.stdDev( dNums ), 0.0001d );
        assertEquals( 40.0d, StatsService.range( dNums ) );
        assertEquals( 20.0d, StatsService.interQuartileRange( dNums ) );

        double[] dArr = dNums.toArray();
        assertEquals( 30.0d, StatsService.median( dArr ) );
        assertEquals( 10.0d, StatsService.quantile( dArr, 0.0d ) );
        assertEquals( 20.0d, StatsService.quantile( dArr, 0.25d ) );
        assertEquals( 30.0d, StatsService.quantile( dArr, 0.50d ) );
        assertEquals( 40.0d, StatsService.quantile( dArr, 0.75d ) );
        assertEquals( 50.0d, StatsService.quantile( dArr, 1.00d ) );
        assertEquals( 30.0d, StatsService.mean( dArr ) );
        assertEquals( 250.0d, StatsService.variance( dArr ) );
        assertEquals( 15.8114d, StatsService.stdDev( dArr ), 0.0001d );
        assertEquals( 40.0d, StatsService.range( dArr ) );
        assertEquals( 20.0d, StatsService.interQuartileRange( dArr ) );
        
        dNums = [ 30.0d, 50.0d, 10.0d, 60.0d, 40.0d, 20.0d ];
        assertEquals( 35.0d, StatsService.median( dNums ) );
        assertEquals( 10.0d, StatsService.quantile( dNums, 0.0d, false ) );
        assertEquals( 22.5d, StatsService.quantile( dNums, 0.25d, false ) );
        assertEquals( 35.0d, StatsService.quantile( dNums, 0.50d ) );
        assertEquals( 47.5d, StatsService.quantile( dNums, 0.75d, false ) );
        assertEquals( 60.0d, StatsService.quantile( dNums, 1.00d ) );
        assertEquals( 35.0d, StatsService.mean( dNums ) );
        assertEquals( 350.0d, StatsService.variance( dNums, 35.0d ) );
        assertEquals( 18.7083d, StatsService.stdDev( dNums, 35.0d ), 0.0001d );
        assertEquals( 50.0d, StatsService.range( dNums ) );
        assertEquals( 25.0d, StatsService.interQuartileRange( dNums, false ) );

        dArr = dNums.toArray();
        assertEquals( 35.0d, StatsService.median( dArr ) );
        assertEquals( 10.0d, StatsService.quantile( dArr, 0.0d, true ) );
        assertEquals( 22.5d, StatsService.quantile( dArr, 0.25d, true ) );
        assertEquals( 35.0d, StatsService.quantile( dArr, 0.50d ) );
        assertEquals( 47.5d, StatsService.quantile( dArr, 0.75d, true ) );
        assertEquals( 60.0d, StatsService.quantile( dArr, 1.00d ) );
        assertEquals( 35.0d, StatsService.mean( dArr ) );
        assertEquals( 350.0d, StatsService.variance( dArr, 35.0d ) );
        assertEquals( 18.7083d, StatsService.stdDev( dArr, 35.0d ), 0.0001d );
        assertEquals( 50.0d, StatsService.range( dArr ) );
        assertEquals( 25.0d, StatsService.interQuartileRange( dArr, true ) );

        List< Double > dGroup0 = [ 68.0d, 68.0d, 67.0d, 70.0d, 71.0d, 73.0d,
                                  76.0d, 81.0d, 83.0d, 84.0d ];
        List< Double > dGroup1 = [ 53.0d, 38.0d, 35.0d, 49.0d, 42.0d, 60.0d,
                                  54.0d, 67.0d, 82.0d, 78.0d ];
        assertEquals( 0.922d,
                      StatsService.correlationCoefficient( dGroup0, dGroup1 ),
                      0.001d );
        double[] dArr0 = dGroup0.toArray();
        double[] dArr1 = dGroup1.toArray();
        assertEquals( 0.922d,
                      StatsService.correlationCoefficient( dArr0, dArr1 ),
                      0.001d );
        assertEquals( 0.150d,
                      MathUtil.pearsonSqrDistance( dArr0, dArr1 ),
                      0.001d );
    }

//-----------------------------------------------------------------------------

	void testSpearman( )
	{
		Map corrRslt;
		double rho, prob;
		List< Float > fGroup0 = [ 71.0f, 70.5f, 71.0f, 72.0f, 70.0f,
								  70.0f, 66.5f, 70.0f, 71.0f ];
        List< Float > fGroup1 = [ 125.0f, 119.0f, 128.0f, 128.0f, 119.0f,
								  127.0f, 105.0f, 123.0f, 115.0f ];
		corrRslt = StatsService.spearmansRankCorrelation( fGroup0, fGroup1 );
		rho = corrRslt.statistic;
		prob = corrRslt.pValue;
        assertEquals( 0.5567112d, rho, 0.0000001d );
		assertEquals( 0.1195d, prob, 0.0001d );
		assertEquals( fGroup0.size(), corrRslt.sampleSize );
		assertEquals( false, corrRslt.noTies );
		prob = StatsService.spearmanProbability( rho, fGroup0.size(),
												 "lower", false );
		assertEquals( 0.9402525d, prob, 0.0000001d );
		prob = StatsService.spearmanProbability( rho, fGroup0.size(),
												 "upper", false );
		assertEquals( 0.05974755d, prob, 0.00000001d );
		prob = StatsService.spearmanProbability( rho, fGroup0.size(),
												 "both", false );
		assertEquals( 0.1195d, prob, 0.0001d );
        float[] fArr0 = fGroup0.toArray();
        float[] fArr1 = fGroup1.toArray();
		corrRslt = StatsService.spearmansRankCorrelation( fArr0, fArr1 );
		rho = corrRslt.statistic;
		prob = corrRslt.pValue;
        assertEquals( 0.5567112d, rho, 0.0000001d );
		assertEquals( 0.1195d, prob, 0.0001d );
		assertEquals( fArr0.size(), corrRslt.sampleSize );
		assertEquals( false, corrRslt.noTies );

        List< Double > dGroup0 = [ 71.0d, 70.5d, 71.0d, 72.0d, 70.0d,
								   70.0d, 66.5d, 70.0d, 71.0d ];
        List< Double> dGroup1 = [ 125.0d, 119.0d, 128.0d, 128.0d, 119.0d,
								  127.0d, 105.0d, 123.0d, 115.0d ];
		corrRslt = StatsService.spearmansRankCorrelation( dGroup0, dGroup1 );
		rho = corrRslt.statistic;
		prob = corrRslt.pValue;
        assertEquals( 0.5567112d, rho, 0.0000001d );
		assertEquals( 0.1195d, prob, 0.0001d );
		assertEquals( dGroup0.size(), corrRslt.sampleSize );
		assertEquals( false, corrRslt.noTies );
        double[] dArr0 = dGroup0.toArray();
        double[] dArr1 = dGroup1.toArray();
		corrRslt = StatsService.spearmansRankCorrelation( fArr0, fArr1 );
		rho = corrRslt.statistic;
		prob = corrRslt.pValue;
        assertEquals( 0.5567112d, rho, 0.0000001d );
		assertEquals( 0.1195d, prob, 0.0001d );
		assertEquals( dArr0.size(), corrRslt.sampleSize );
		assertEquals( false, corrRslt.noTies );

		dGroup0 = [ 1.0d, 2.0d, 3.0d, 4.0d ];
		dGroup1 = [ 1.0d, 3.0d, 2.0d, 4.0d ];
		corrRslt = StatsService.spearmansRankCorrelation( dGroup0, dGroup1 );
		rho = corrRslt.statistic;
		prob = corrRslt.pValue;
        assertEquals( 0.8d, rho, 0.0000001d );
		assertEquals( 0.3333333d, prob, 0.0000001d );
		assertEquals( dGroup0.size(), corrRslt.sampleSize );
		assertEquals( true, corrRslt.noTies );
		prob = StatsService.spearmanProbability( rho, dGroup0.size(),
												 "lower", true );
		assertEquals( 0.9583333d, prob, 0.0000001d );
		prob = StatsService.spearmanProbability( rho, dGroup0.size(),
												 "upper", true );
		assertEquals( 0.1666667d, prob, 0.0000001d );
		prob = StatsService.spearmanProbability( rho, dGroup0.size(),
												 "both", true );
		assertEquals( 0.3333333d, prob, 0.0000001d );

		dGroup0 = [ 1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 6.0d, 7.0d, 8.0d, 9.0d,
					10.0d, 11.0d ];
		dGroup1 = [ 6.0d, 5.0d, 4.0d, 3.0d, 2.0d, 1.0d, 7.0d, 11.0d, 10.0d,
					9.0d, 8.0d ];
		corrRslt = StatsService.spearmansRankCorrelation( dGroup0, dGroup1 );
		rho = corrRslt.statistic;
		prob = corrRslt.pValue;
        assertEquals( 0.5909091d, rho, 0.0000001d );
		assertEquals( 0.06072825d, prob, 0.0001d );
		assertEquals( dGroup0.size(), corrRslt.sampleSize );
		assertEquals( true, corrRslt.noTies );
		prob = StatsService.spearmanProbability( rho, dGroup0.size(),
												 "lower", true );
		assertEquals( 0.9719106d, prob, 0.0000001d );
		prob = StatsService.spearmanProbability( rho, dGroup0.size(),
												 "upper", true );
		assertEquals( 0.03036413d, prob, 0.00000001d );
		prob = StatsService.spearmanProbability( rho, dGroup0.size(),
												 "both", true );
		assertEquals( 0.06072825d, prob, 0.0001d );
		
		fGroup0 = [ 1f, 1f, 1f, 1f, 1f, 1f, 24.66666667f,
					1f, 1.444444444f, 34.66666667f, 4f, 5.333333333f, 1f, 1f,
					1f, 12f, 2.8f, 1f, 13.75f, 1f, 1f,
					16f, 4.666666667f, 8.636363636f, 1f, 1f, 1f, 1f ];
		fGroup1 = [ 1.111111111f, 12.22222222f, 0f, 1.111111111f,
					-2.222222222f, 2.222222222f, -2.222222222f, 8.888888889f,
					4.444444444f, 1.111111111f, -7.777777778f, 3.333333333f,
					-1.111111111f, -7.777777778f, -2.222222222f, -2.222222222f,
					-3.333333333f, 0f, 1.111111111f, -13.33333333f,
					5.555555556f, -3.333333333f, -15.55555556f, -8.888888889f,
					2.222222222f, -20f, 7.777777778f, 11.11111111f ];
		corrRslt = StatsService.spearmansRankCorrelation( fGroup0, fGroup1 );
		rho = corrRslt.statistic;
		prob = corrRslt.pValue;
		assertEquals( -0.272925804d, rho, 0.000000001d );
		assertEquals( 0.159965646d, prob, 0.000000001d );
		assertEquals( fGroup0.size(), corrRslt.sampleSize );
		assertEquals( false, corrRslt.noTies );
		prob = StatsService.spearmanProbability( rho, fGroup0.size(),
												 "lower", false );
		assertEquals( 0.079982823d, prob, 0.000000001d );
		prob = StatsService.spearmanProbability( rho, fGroup0.size(),
												 "upper", false );
		assertEquals( 0.920017177d, prob, 0.000000001d );
		prob = StatsService.spearmanProbability( rho, fGroup0.size(),
												 "both", false );
		assertEquals( 0.159965646d, prob, 0.000000001d );
	}

//-----------------------------------------------------------------------------

    void testGroupComparisons( )
    {
        def MEAN = StatsService.MeasureOfCT.MEAN;
        def MEDIAN = StatsService.MeasureOfCT.MEDIAN;
        
        List< Float > fGroup1 = [ 10.0f, 20.0f, 20.0f, 40.0f, 60.0f ];
        List< Float > fGroup2 = [ 30.0f, 30.0f, 40.0f, 100.0f, 250.0f ];
        assertEquals( 60.0f, StatsService.difference( fGroup1, fGroup2,
                                                      MEAN ) );
        assertEquals( -60.0f, StatsService.difference( fGroup1, fGroup2,
                                                       MEAN, false ) );
        assertEquals( 60.0f, StatsService.difference( fGroup2, fGroup1,
                                                      MEAN ) );
        assertEquals( 60.0f, StatsService.difference( fGroup2, fGroup1,
                                                      MEAN, false ) );
        assertEquals( 20.0f, StatsService.difference( fGroup1, fGroup2,
                                                      MEDIAN ) );
        assertEquals( -20.0f, StatsService.difference( fGroup1, fGroup2,
                                                       MEDIAN, false ));
        assertEquals( 20.0f, StatsService.difference( fGroup2, fGroup1,
                                                      MEDIAN, true, true ) );
        assertEquals( 20.0f, StatsService.difference( fGroup2, fGroup1,
                                                      MEDIAN, false ));
        
        assertEquals( 3.0f, StatsService.foldChange( fGroup1, fGroup2, MEAN ) );
        assertEquals( 0.3333f, StatsService.foldChange( fGroup1, fGroup2, MEAN,
                                                        false ), 0.0001f );
        assertEquals( 3.0f, StatsService.foldChange( fGroup2, fGroup1, MEAN ) );
        assertEquals( 3.0f, StatsService.foldChange( fGroup2, fGroup1, MEAN,
                                                     false ) );
        assertEquals( 2.0f, StatsService.foldChange( fGroup1, fGroup2, MEDIAN,
                                                     true, true ) );
        assertEquals( 0.5f, StatsService.foldChange( fGroup1, fGroup2, MEDIAN,
                                                     false ));
        assertEquals( 2.0f, StatsService.foldChange( fGroup2, fGroup1,
                                                     MEDIAN ) );
        assertEquals( 2.0f, StatsService.foldChange( fGroup2, fGroup1,
                                                     MEDIAN, false, true ) );

        float[] fArr1 = fGroup1.toArray();
        float[] fArr2 = fGroup2.toArray();
        assertEquals( 60.0f, StatsService.difference( fArr1, fArr2, MEAN ) );
        assertEquals( -60.0f,
                      StatsService.difference( fArr1, fArr2, MEAN, false ) );
        assertEquals( 60.0f, StatsService.difference( fArr2, fArr1, MEAN ) );
        assertEquals( 60.0f,
                      StatsService.difference( fArr2, fArr1, MEAN, false ) );
        assertEquals( 20.0f,
                      StatsService.difference( fArr1, fArr2, MEDIAN ) );
        assertEquals( -20.0f,
                      StatsService.difference( fArr1, fArr2, MEDIAN, false ));
        assertEquals( 20.0f,
                      StatsService.difference( fArr2, fArr1, MEDIAN, true,
                                               true ) );
        assertEquals( 20.0f,
                      StatsService.difference( fArr2, fArr1, MEDIAN, false ));
        
        assertEquals( 3.0f, StatsService.foldChange( fArr1, fArr2, MEAN ) );
        assertEquals( 0.3333f,
                      StatsService.foldChange( fArr1, fArr2, MEAN, false ),
                      0.0001f );
        assertEquals( 3.0f, StatsService.foldChange( fArr2, fArr1, MEAN ) );
        assertEquals( 3.0f,
                      StatsService.foldChange( fArr2, fArr1, MEAN, false ) );
        assertEquals( 2.0f,
                      StatsService.foldChange( fArr1, fArr2, MEDIAN, true,
                                               true ) );
        assertEquals( 0.5f,
                      StatsService.foldChange( fArr1, fArr2, MEDIAN, false ));
        assertEquals( 2.0f,
                      StatsService.foldChange( fArr2, fArr1, MEDIAN ) );
        assertEquals( 2.0f,
                      StatsService.foldChange( fArr2, fArr1, MEDIAN, false,
                                               true ) );


        List< Double > dGroup1 = [ 10.0d, 20.0d, 20.0d, 40.0d, 60.0d ];
        List< Double > dGroup2 = [ 30.0d, 30.0d, 40.0d, 100.0d, 250.0d ];
        assertEquals( 60.0d, StatsService.difference( dGroup1, dGroup2,
                                                      MEAN ) );
        assertEquals( -60.0d, StatsService.difference( dGroup1, dGroup2,
                                                       MEAN, false ) );
        assertEquals( 60.0d, StatsService.difference( dGroup2, dGroup1,
                                                      MEAN ) );
        assertEquals( 60.0d, StatsService.difference( dGroup2, dGroup1,
                                                      MEAN, false ) );
        assertEquals( 20.0d, StatsService.difference( dGroup1, dGroup2,
                                                      MEDIAN ) );
        assertEquals( -20.0d, StatsService.difference( dGroup1, dGroup2,
                                                       MEDIAN, false ));
        assertEquals( 20.0d, StatsService.difference( dGroup2, dGroup1,
                                                      MEDIAN, true, true ) );
        assertEquals( 20.0d, StatsService.difference( dGroup2, dGroup1,
                                                      MEDIAN, false ));
        
        assertEquals( 3.0d, StatsService.foldChange( dGroup1, dGroup2, MEAN ) );
        assertEquals( 0.3333d, StatsService.foldChange( dGroup1, dGroup2, MEAN,
                                                        false ), 0.0001d );
        assertEquals( 3.0d, StatsService.foldChange( dGroup2, dGroup1, MEAN ) );
        assertEquals( 3.0d, StatsService.foldChange( dGroup2, dGroup1, MEAN,
                                                     false ) );
        assertEquals( 2.0d, StatsService.foldChange( dGroup1, dGroup2, MEDIAN,
                                                     true, true ) );
        assertEquals( 0.5d, StatsService.foldChange( dGroup1, dGroup2, MEDIAN,
                                                     false ));
        assertEquals( 2.0d, StatsService.foldChange( dGroup2, dGroup1,
                                                     MEDIAN ) );
        assertEquals( 2.0d, StatsService.foldChange( dGroup2, dGroup1,
                                                     MEDIAN, false, true ) );

        double[] dArr1 = dGroup1.toArray();
        double[] dArr2 = dGroup2.toArray();
        assertEquals( 60.0d, StatsService.difference( dArr1, dArr2, MEAN ) );
        assertEquals( -60.0d,
                      StatsService.difference( dArr1, dArr2, MEAN, false ) );
        assertEquals( 60.0d, StatsService.difference( dArr2, dArr1, MEAN ) );
        assertEquals( 60.0d,
                      StatsService.difference( dArr2, dArr1, MEAN, false ) );
        assertEquals( 20.0d,
                      StatsService.difference( dArr1, dArr2, MEDIAN ) );
        assertEquals( -20.0d,
                      StatsService.difference( dArr1, dArr2, MEDIAN, false ));
        assertEquals( 20.0d,
                      StatsService.difference( dArr2, dArr1, MEDIAN, true,
                                               true ) );
        assertEquals( 20.0d,
                      StatsService.difference( dArr2, dArr1, MEDIAN, false ));
        
        assertEquals( 3.0d, StatsService.foldChange( dArr1, dArr2, MEAN ) );
        assertEquals( 0.3333d,
                      StatsService.foldChange( dArr1, dArr2, MEAN, false ),
                      0.0001d );
        assertEquals( 3.0d, StatsService.foldChange( dArr2, dArr1, MEAN ) );
        assertEquals( 3.0d,
                      StatsService.foldChange( dArr2, dArr1, MEAN, false ) );
        assertEquals( 2.0d,
                      StatsService.foldChange( dArr1, dArr2, MEDIAN, true,
                                               true ) );
        assertEquals( 0.5d,
                      StatsService.foldChange( dArr1, dArr2, MEDIAN, false ));
        assertEquals( 2.0d,
                      StatsService.foldChange( dArr2, dArr1, MEDIAN ) );
        assertEquals( 2.0d,
                      StatsService.foldChange( dArr2, dArr1, MEDIAN, false,
                                               true ) );
    }

//-----------------------------------------------------------------------------

    void testDistances( )
    {
        List< Double > dVec0 = [ 10.0d, 20.0d, 30.0d ];
        List< Double > dVec1 = [ 22.0d, 16.0d, 33.0d ];
        assertEquals( 13.0d, MathUtil.euclideanDistance( dVec0, dVec1 ) );
        assertEquals( 19.0d, MathUtil.manhattanDistance( dVec0, dVec1 ) );

        double[] dArr0 = dVec0.toArray();
        double[] dArr1 = dVec1.toArray();
        assertEquals( 13.0d, MathUtil.euclideanDistance( dArr0, dArr1 ) );
        assertEquals( 19.0d, MathUtil.manhattanDistance( dArr0, dArr1 ) );
    }

//-----------------------------------------------------------------------------

	void testFDR( )
	{
		List< Double > probsList =
				[ 0.0001d, 0.0459d, 0.0004d, 0.3240d, 0.0019d, 0.4262d,
				  0.0095d, 0.5719d, 0.0201d, 0.6528d, 0.0278d, 0.7590d,
				  0.0298d, 1.000d, 0.0344d ];
		double[] probsArr = probsList.toArray();
		double[] adjProbs = StatsService.fdrAdjust( probsArr );
		assertEquals( 0.0015d, adjProbs[ 0 ], 0.0001d );
		assertEquals( 0.0765d, adjProbs[ 1 ], 0.0001d );
		assertEquals( 0.035625d, adjProbs[ 6 ], 0.0001d );
		assertEquals( 0.0639d, adjProbs[ 10 ], 0.0001d );
		assertEquals( 0.0639d, adjProbs[ 12 ], 0.0001d );

		probsList = [ 0.09d, 0.04d, 0.10d, 0.03d ];
		probsArr = probsList.toArray();
		adjProbs = StatsService.fdrAdjust( probsArr );
		assertEquals( 0.10d, adjProbs[ 0 ], 0.0001d );
		assertEquals( 0.08d, adjProbs[ 1 ], 0.0001d );
		assertEquals( 0.10d, adjProbs[ 2 ], 0.0001d );
		assertEquals( 0.08d, adjProbs[ 3 ], 0.0001d );
	}

//-----------------------------------------------------------------------------

    void testClustering( )
    {
        List< List< Double > > matrix =
                [ [ 1.0d, 2.0d, 3.0d, 4.0d, 5.0d ],
                  [ 11.0d, 20.0d, 29.0d, 41.0d, 50.0d ],
                  [ 4.0d, 5.0d, 3.0d, 2.0d, 1.0d ],
                  [ 39.0d, 49.0d, 30.0d, 22.0d, 15.0d ]
                ];
        ClusterHierarchy hierarchy = HierarchicalClusterer.clusterVectors(
            matrix, HierarchicalClusterer.Metric.EUCLIDEAN, true );
        println( "Cluster hierarcy (Eucl): " + hierarchy );
        ClusterNode tree = hierarchy.getTree( );
        List< Integer > items = new ArrayList< Integer >();
        tree.getItems( items );
        println( "Items: " + items );

        hierarchy = HierarchicalClusterer.clusterVectors(
            matrix, HierarchicalClusterer.Metric.MANHATTAN, true );
        println( "Cluster hierarcy (Manh): " + hierarchy );
        tree = hierarchy.getTree( );
        items.clear();
        tree.getItems( items );
        println( "Items: " + items );

        hierarchy = HierarchicalClusterer.clusterVectors(
            matrix, HierarchicalClusterer.Metric.PEARSON, true );
        println( "Cluster hierarcy (Pear): " + hierarchy )
        tree = hierarchy.getTree( );
        items.clear();
        tree.getItems( items );
        println( "Items: " + items );

        hierarchy = HierarchicalClusterer.clusterVectors(
            matrix, HierarchicalClusterer.Metric.EUCLIDEAN, false );
        println( "Cluster hierarcy (Eucl, cols): " + hierarchy );
        tree = hierarchy.getTree( );
        items.clear();
        tree.getItems( items );
        println( "Items: " + items );

        hierarchy = HierarchicalClusterer.clusterVectors(
            matrix, HierarchicalClusterer.Metric.MANHATTAN, false );
        println( "Cluster hierarcy (Manh, cols): " + hierarchy );
        tree = hierarchy.getTree( );
        items.clear();
        tree.getItems( items );
        println( "Items: " + items );
        assertEquals( [ 3, 4, 2, 0, 1 ], items );
        ClusterNode node = tree; //8
        assertEquals( null, node.getItem() );
        assertEquals( 2, node.getChildren().size() );
        assertEquals( 51 + (1.0d/3.0d), node.getIntraclusterDistance() );
        assertEquals( 2.125d, node.getOffset() );
        assertEquals( 3, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 0 ); //5
        assertEquals( null, node.getItem() );
        assertEquals( 2, node.getChildren().size() );
        assertEquals( 18.0d, node.getIntraclusterDistance() );
        assertEquals( 1.0d, node.getOffset() );
        assertEquals( 1, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 1 ); //7
        assertEquals( null, node.getItem() );
        assertEquals( 2, node.getChildren().size() );
        assertEquals( 30.5d, node.getIntraclusterDistance() );
        assertEquals( 3.25d, node.getOffset() );
        assertEquals( 2, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 0 ).getChildren().get( 0 ); //3
        assertEquals( 3, node.getItem() );
        assertEquals( null, node.getChildren() );
        assertEquals( null, node.getIntraclusterDistance() );
        assertEquals( 0.5d, node.getOffset() );
        assertEquals( 0, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 0 ).getChildren().get( 1 ); //4
        assertEquals( 4, node.getItem() );
        assertEquals( null, node.getChildren() );
        assertEquals( null, node.getIntraclusterDistance() );
        assertEquals( 1.5d, node.getOffset() );
        assertEquals( 0, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 1 ).getChildren().get( 0 ); //2
        assertEquals( 2, node.getItem() );
        assertEquals( null, node.getChildren() );
        assertEquals( null, node.getIntraclusterDistance() );
        assertEquals( 2.5d, node.getOffset() );
        assertEquals( 0, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 1 ).getChildren().get( 1 ); //6
        assertEquals( null, node.getItem() );
        assertEquals( 2, node.getChildren().size() );
        assertEquals( 21.0d, node.getIntraclusterDistance() );
        assertEquals( 4.0d, node.getOffset() );
        assertEquals( 1, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 1 ).getChildren().get( 1 ).getChildren().get( 0 ); //0
        assertEquals( 0, node.getItem() );
        assertEquals( null, node.getChildren() );
        assertEquals( null, node.getIntraclusterDistance() );
        assertEquals( 3.5d, node.getOffset() );
        assertEquals( 0, node.getDistanceFromLeaf() );
        node = tree.getChildren().get( 1 ).getChildren().get( 1 ).getChildren().get( 1 ); //1
        assertEquals( 1, node.getItem() );
        assertEquals( null, node.getChildren() );
        assertEquals( null, node.getIntraclusterDistance() );
        assertEquals( 4.5d, node.getOffset() );
        assertEquals( 0, node.getDistanceFromLeaf() );

        hierarchy = HierarchicalClusterer.clusterVectors(
            matrix, HierarchicalClusterer.Metric.PEARSON, false );
        println( "Cluster hierarcy (Pear, cols): " + hierarchy );
        tree = hierarchy.getTree( );
        items.clear();
        tree.getItems( items );
        println( "Items: " + items );

        int size = 1000;
        matrix = new ArrayList< List< Double > >( size );
        for ( int i = 0; i < size; ++i )
        {
            matrix.add( new ArrayList< Double >( size ) );
            for ( int j = 0; j < size; ++j )
            {
                matrix.get( i ).add( Math.random( ) );
            }
        }
        Date start = new Date();
        hierarchy = HierarchicalClusterer.clusterVectors(
            matrix, HierarchicalClusterer.Metric.PEARSON, true );
        Date end = new Date();
        long milliSecs = end.getTime() - start.getTime();
        println( "Clustering " + size + "x" + size +
                 " took " + milliSecs + "ms" );
        // println( "Cluster hierarcy (Pear): " + hierarchy );
    }

//-----------------------------------------------------------------------------
}                                                           //StatsServiceTests

