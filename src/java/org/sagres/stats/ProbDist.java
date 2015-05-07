/*
  ProbDist.java
  Copyright © 2010 David M. Anderson

  Density and distribution functions for common probability distributions.
  NOTES:
  1. The _PDF functions compute the probability density function.
  2. The _DF functions compute the (cumulative) distribution function,
     i.e. Prob( X <= x ).
  3. Distributions:
      Uniform: any number in the range as likely as any other.
          This comes in discrete (integers 0 through limit-1)
          and continuous (interval on real line) forms.
      Triangle: pdf is triangular, with a peak at mode.
      Geometric: X = number of independent Bernoulli trials, each with
          given probability, needed for success.
      Binomial: X = number of successes in given number of Bernoulli trials,
          each with given probability.
      Hypergeometric: Given a total population size, the size of a
          subset, and a sample size (without replacement), X = number of
          samples from the subset.
      Poisson: X = number of occurences in interval t of a Poisson process:
          independent increments; probability of occurence in interval h is
          lambda * h + o(h); probability of more than one is o(h).
          The mean is lambda * t.
      Exponential: X = waiting time for first occurence of Poisson process.
      Gamma: X = waiting time for n occurences of Poisson process.
      Normal (a.k.a. Gaussian): Limiting distribution of sum (or average) of
          independent random variables with a finite variance.
          Symmetric around mean.
      LogNormal: X = exp(Y), where Y is normally distributed.
          If momentsOfLog is true, the given mean and standard
          deviation are of Y, otherwise they are of X itself. Typically,
          this is the limiting distribution of a product of independent
          random variables.
      Chi-square: X = sum from i = 1 to (degreesOfFreedom) of Y[i]^2
          where Y[i] is N(0,1).
      Student's t: X = Y1 / sqrt(Y2/r), where Y1 is N(0,1) and Y2 is
          Chi-square. Symmetric around 0.
      Snedecor's F: X = (Y1/d1) / (Y2/d2), where Y1 and Y2 are Chi-square
          with degrees of freedom d1 and d2.
      Cauchy: (a) X = a * tan(Y), where Y is uniform between -pi/2 and pi/2.
          (b) X = Y1 / Y2, where Y1 and Y2 are independent N(0,1) (a=1).
      Beta: (a) X = Y1 / (Y1 + Y2), where Y1 and Y2 have Gamma distributions
          with n = 1 and lambda parameters a and b.
          (b) X = 1 / (1 + (a/b)X), where X has an F distribution.
      Kolmogorov Smirnov: distribution of the statistic from the K-S test.
*/


package org.sagres.stats;


//*****************************************************************************

public
class ProbDist
{                                                                    //ProbDist
//-----------------------------------------------------------------------------

    public static    
    double 
    uniform_PDF( int x, int limit )
    {
        if ( limit <= 0 )
            throw new IllegalArgumentException( "uniform_PDF: limit <= 0" );
        if ( (x < 0) || (x >= limit) )
            return 0.;
        return 1. / limit;
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    uniform_DF( int x, int limit )
    {
        if ( limit <= 0 )
            throw new IllegalArgumentException( "uniform_DF: limit <= 0" );
        if ( x < 0 )
            return 0.;
        if ( x >= limit - 1 )
            return 1.;
        return (x + 1.) / limit;
    }

//=============================================================================

    public static    
    double 
    uniform_PDF( double x, double minimum, double maximum )
    {
        if ( minimum >= maximum )
            throw new IllegalArgumentException( "uniform_PDF: "
                                                + "minimum >= maximum" );
        if ( (x < minimum) || (x >= maximum) )
            return 0.;
        return 1. / (maximum - minimum);
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    uniform_DF( double x, double minimum, double maximum )
    {
        if ( minimum >= maximum )
            throw new IllegalArgumentException( "uniform_DF: "
                                                + "minimum >= maximum" );
        if ( x <= minimum )
            return 0.;
        if ( x >= maximum )
            return 1.;
        return (x - minimum) / (maximum - minimum);
    }

//=============================================================================

    public static    
    double 
    triangle_PDF( double x, double minimum, double mode, double maximum )
    {
        if ( minimum >= maximum )
            throw new IllegalArgumentException( "triangle_PDF: "
                                                + "minimum >= maximum" );
        if ( mode < minimum )
            throw new IllegalArgumentException( "triangle_PDF: "
                                                + "mode < minimum" );
        if ( mode > maximum )
            throw new IllegalArgumentException( "triangle_PDF: "
                                                + "mode > maximum" );
        double peak = 2. / (maximum - minimum);
        if ( x == mode )
            return peak;
        if ( (x <= minimum) || (x >= maximum) )
            return 0.;
        if ( x <= mode )
            return peak * (x - minimum) / (mode - minimum);
        else
            return peak * (maximum - x) / (maximum - mode);
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    triangle_DF( double x, double minimum, double mode, double maximum )
    {
        if ( minimum >= maximum )
            throw new IllegalArgumentException( "triangle_DF: "
                                                + "minimum >= maximum" );
        if ( mode < minimum )
            throw new IllegalArgumentException( "triangle_DF: "
                                                + "mode < minimum" );
        if ( mode > maximum )
            throw new IllegalArgumentException( "triangle_DF: "
                                                + "mode > maximum" );
        if ( x <= minimum )
            return 0.;
        if ( x >= maximum )
            return 1.;
        double peak = 2. / (maximum - minimum);
        if ( x <= mode )
            return 0.5 * peak * (x - minimum) * (x - minimum)
                    / (mode - minimum);
        else
            return 1.
                    -  0.5 * peak * (maximum - x) * (maximum - x) / (maximum - mode);
    }

//=============================================================================

    public static    
    double 
    geometric_PDF( int x, double probability )
    {
        if ( probability < 0. )
            throw new IllegalArgumentException( "geometric_PDF: "
                                                + "probability < 0" );
        if ( probability > 1. )
            throw new IllegalArgumentException( "geometric_PDF: "
                                                + "probability > 1" );
        if ( x <= 0 )
            return 0.;
        if ( probability == 0. )
            return 0.;
        if ( probability == 1. )
            return ((x == 1)  ?  1.  :  0.);
        return probability * Math.pow( (1. - probability), (x - 1.) );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    geometric_DF( int x, double probability )
    {
        if ( probability < 0. )
            throw new IllegalArgumentException( "geometric_DF: "
                                                + "probability < 0" );
        if ( probability > 1. )
            throw new IllegalArgumentException( "geometric_DF: "
                                                + "probability > 1" );
        if ( x <= 0 )
            return 0.;
        double failProb = 1. - probability;
        double prob = probability;
        double cumProb = probability;
        for ( int i = 2; i <= x; ++i )
        {
            prob *= failProb;
            cumProb += prob;
        }
        return cumProb;
    }

//=============================================================================

    public static    
    double 
    binomial_PDF( int x, double probability, int trials )
    {
        if ( probability < 0. )
            throw new IllegalArgumentException( "binomial_PDF: "
                                                + "probability < 0" );
        if ( probability > 1. )
            throw new IllegalArgumentException( "binomial_PDF: "
                                                + "probability > 1" );
        if ( trials <= 0 )
            throw new IllegalArgumentException( "binomial_PDF: trials <= 0" );
        if ( (x < 0) || (x > trials) )
            return 0.;
        return SpecFunc.binomialCoefficient( trials, x )
                * Math.pow( probability, x )
                * Math.pow( (1. - probability), (trials - x) );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    binomial_DF( int x, double probability, int trials )
    {
        if ( probability < 0. )
            throw new IllegalArgumentException( "binomial_DF: "
                                                + "probability < 0" );
        if ( probability > 1. )
            throw new IllegalArgumentException( "binomial_DF: "
                                                + "probability > 1" );
        if ( trials <= 0 )
            throw new IllegalArgumentException( "binomial_DF: trials <= 0" );
        if ( x < 0 )
            return 0.;
        if ( x >= trials )
            return 1.;
        if ( probability == 0. )
            return 1.;
        if ( probability == 1. )
            return ((x < trials)  ?  0.  :  1.);
        if ( trials <= 15 )
        {
            double prob = Math.pow( (1. - probability), trials );
            double cumProb = prob;
            double odds = probability / (1. - probability);
            for ( int i = 1; i <= x; ++i )
            {
                prob *= odds * (trials - i + 1) / i;
                cumProb += prob;
            }
            return cumProb;
        }
        else
        {
            return 1. - SpecFunc.incompleteBeta( (x + 1), (trials - x),
                                                 probability );
        }
    }

//=============================================================================

    public static    
    double 
    hypergeometric_PDF( int x, int populationSize, int subsetSize,
                        int sampleSize )
    {
        if ( subsetSize < 0 )
            throw new IllegalArgumentException( "hypergeometric_PDF: "
                                                + "subsetSize < 0" );
        if ( subsetSize > populationSize )
            throw new IllegalArgumentException( "hypergeometric_PDF: "
                                                + "subsetSize > populationSize" );
        if ( sampleSize < 0 )
            throw new IllegalArgumentException( "hypergeometric_PDF: "
                                                + "sampleSize < 0" );
        if ( sampleSize > populationSize )
            throw new IllegalArgumentException( "hypergeometric_PDF: "
                                                + "sampleSize > populationSize" );
        int minX = Math.max( 0, subsetSize + sampleSize - populationSize );
        int maxX = Math.min( subsetSize, sampleSize );
        if ( x < minX )
            return 0.;
        if ( x > maxX )
            return 0.;
        return SpecFunc.binomialCoefficient( subsetSize, x )
                * SpecFunc.binomialCoefficient( (populationSize - subsetSize),
                                       (sampleSize - x) )
                / SpecFunc.binomialCoefficient( populationSize, sampleSize );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    hypergeometric_DF( int x, int populationSize, int subsetSize,
                       int sampleSize )

    {
        if ( subsetSize < 0 )
            throw new IllegalArgumentException( "hypergeometric_DF: "
                                                + "subsetSize < 0" );
        if ( subsetSize > populationSize )
            throw new IllegalArgumentException( "hypergeometric_DF: "
                                                + "subsetSize > populationSize" );
        if ( sampleSize < 0 )
            throw new IllegalArgumentException( "hypergeometric_DF: "
                                                + "sampleSize < 0" );
        if ( sampleSize > populationSize )
            throw new IllegalArgumentException( "hypergeometric_DF: "
                                                + "sampleSize > populationSize" );
        int minX = Math.max( 0, subsetSize + sampleSize - populationSize );
        int maxX = Math.min( subsetSize, sampleSize );
        if ( x < minX )
            return 0.;
        if ( x >= maxX )
            return 1.;
        double prob = 0.;
        if ( minX == 0 )
            prob = Math.exp(
                SpecFunc.logFactorial( populationSize - subsetSize )
                + SpecFunc.logFactorial( populationSize - sampleSize )
                - SpecFunc.logFactorial( populationSize )
                - SpecFunc.logFactorial( populationSize - subsetSize
                                         - sampleSize ) );
        else
            prob = Math.exp( SpecFunc.logFactorial( subsetSize )
                             + SpecFunc.logFactorial( sampleSize )
                             - SpecFunc.logFactorial( populationSize )
                             - SpecFunc.logFactorial( minX ) );
        double cumProb = prob;
        for ( int i = minX + 1; i <= x; ++i )
        {
            prob *= (subsetSize - i + 1.) * (sampleSize - i + 1.)
                    / (i * (populationSize - subsetSize - sampleSize + i));
            cumProb += prob;
        }
        return cumProb;
    }

//=============================================================================

    public static    
    double 
    poisson_PDF( int x, double mean )
    {
        if ( mean <= 0. )
            throw new IllegalArgumentException( "poisson_PDF: mean <= 0" );
        if ( x < 0 )
            return 0.;
        return Math.exp( - mean  +  x * Math.log( mean )
                         -  SpecFunc.logFactorial( x ) );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    poisson_DF( int x, double mean )
    {
        if ( mean <= 0. )
            throw new IllegalArgumentException( "poisson_DF: mean <= 0" );
        if ( x < 0 )
            return 0.;
        return 1. - SpecFunc.incompleteGamma( (x + 1.), mean );
    }

//=============================================================================

    public static    
    double 
    exponential_PDF( double x, double lambda )
    {
        if ( lambda <= 0. )
            throw new IllegalArgumentException( "exponential_PDF: "
                                                + "lambda <= 0" );
        if ( x < 0. )
            return 0.;
        return lambda * Math.exp( - lambda * x );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    exponential_DF( double x, double lambda )
    {
        if ( lambda <= 0. )
            throw new IllegalArgumentException( "exponential_DF: lambda <= 0" );
        if ( x < 0. )
            return 0.;
        return 1. - Math.exp( - lambda * x );
    }

//=============================================================================

    public static    
    double 
    gamma_PDF( double x, int n, double lambda )
    {
        if ( n <= 0 )
            throw new IllegalArgumentException( "gamma_PDF: n <= 0" );
        if ( lambda <= 0. )
            throw new IllegalArgumentException( "gamma_PDF: lambda <= 0" );
        if ( x <= 0. )
            return 0.;
        return Math.exp( (n - 1.) * Math.log( x )  -  x * lambda
                         +  n * Math.log( lambda )  -  SpecFunc.logGamma( n ) );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    gamma_DF( double x, int n, double lambda )
    {
        if ( n <= 0 )
            throw new IllegalArgumentException( "gamma_DF: n <= 0" );
        if ( lambda <= 0. )
            throw new IllegalArgumentException( "gamma_DF: lambda <= 0" );
        if ( x <= 0. )
            return 0.;
        return SpecFunc.incompleteGamma( n, x * lambda );
    }

//=============================================================================

    public static    
    double 
    normal_PDF( double x, double mean, double standardDeviation )
    {
        if ( standardDeviation <= 0. )
            throw new IllegalArgumentException( "normal_PDF: "
                                                + "standardDeviation <= 0" );
        final double sqrt2pi = Math.sqrt( 2 * Math.PI );
        return Math.exp( - (x - mean)*(x - mean)
                         / (2 * standardDeviation * standardDeviation) )
                / (standardDeviation * sqrt2pi);
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    normal_DF( double x, double mean, double standardDeviation )
    {
        if ( standardDeviation <= 0. )
            throw new IllegalArgumentException( "normal_DF: "
                                                + "standardDeviation <= 0" );
        final double sqrt2 = Math.sqrt( 2. );
        return 0.5 * (1. + SpecFunc.erf( (x - mean)
                                         / (sqrt2 * standardDeviation) ));
    }

//=============================================================================

    public static    
    double 
    logNormal_PDF( double x, double mean, double standardDeviation,
                   boolean momentsOfLog )
    {
        if ( standardDeviation <= 0. )
            throw new IllegalArgumentException( "logNormal_PDF: "
                                                + "standardDeviation <= 0" );
        if ( x <= 0. )
            return 0.;
        if ( ! momentsOfLog )
        {
            if ( mean <= 0. )
                throw new IllegalArgumentException( "logNormal_PDF: "
                                                    + "mean <= 0" );
            double meanSqr = mean * mean;
            double variance = standardDeviation * standardDeviation;
            double m = 2. * Math.log( mean )
                    -  0.5 * Math.log( meanSqr + variance );
            double v = Math.log( 1.  +  variance / meanSqr );
            double s = Math.sqrt( v );
            return normal_PDF( Math.log( x ), m, s );
        }
        return normal_PDF( Math.log( x ), mean, standardDeviation );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    logNormal_DF( double x, double mean, double standardDeviation,
                  boolean momentsOfLog )
    {
        if ( standardDeviation <= 0. )
            throw new IllegalArgumentException( "logNormal_DF: "
                                                + "standardDeviation <= 0" );
        if ( x <= 0. )
            return 0.;

        if ( ! momentsOfLog )
        {
            if ( mean <= 0. )
                throw new IllegalArgumentException( "logNormal_PDF: "
                                                    + "mean <= 0" );
            double meanSqr = mean * mean;
            double variance = standardDeviation * standardDeviation;
            double m = 2. * Math.log( mean )
                    -  0.5 * Math.log( meanSqr + variance );
            double v = Math.log( 1.  +  variance / meanSqr );
            double s = Math.sqrt( v );
            return normal_DF( Math.log( x ), m, s );
        }
        return normal_DF( Math.log( x ), mean, standardDeviation );
    }

//=============================================================================

    public static    
    double 
    chiSquare_PDF( double x, int degreesOfFreedom )
    {
        if ( degreesOfFreedom <= 0 )
            throw new IllegalArgumentException( "chiSquare_PDF: "
                                                + "degreesOfFreedom <= 0" );
        if ( x <= 0. )
            return 0.;
        double dof2 = degreesOfFreedom / 2.;
        final double logHalf = Math.log( 0.5 );
        return Math.exp( dof2 * logHalf  +  (dof2 - 1.) * Math.log( x )
                         -  x / 2.  -  SpecFunc.logGamma( dof2 ) );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    chiSquare_DF( double x, int degreesOfFreedom )
    {
        if ( degreesOfFreedom <= 0 )
            throw new IllegalArgumentException( "chiSquare_DF: "
                                                + "degreesOfFreedom <= 0" );
        if ( x <= 0. )
            return 0.;
        return SpecFunc.incompleteGamma( (degreesOfFreedom / 2.), (x / 2.) );
    }

//=============================================================================

    public static    
    double 
    t_PDF( double x, int degreesOfFreedom )
    {
        if ( degreesOfFreedom <= 0 )
            throw new IllegalArgumentException( "t_PDF: "
                                                + "degreesOfFreedom <= 0" );
        double d = degreesOfFreedom;
        final double logSqrtPi = Math.log( Math.PI ) / 2.;
        return Math.exp( SpecFunc.logGamma( (d + 1.) / 2. )
                         -  SpecFunc.logGamma( d / 2. )
                         -  Math.log( d ) / 2.  -  logSqrtPi
                         -  ((d + 1.) / 2.) * Math.log( 1. +  x * x / d ) );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    t_DF( double x, int degreesOfFreedom )
    {
        if ( degreesOfFreedom <= 0 )
            throw new IllegalArgumentException( "t_DF: "
                                                + "degreesOfFreedom <= 0" );
        double d = degreesOfFreedom;
        if ( x > 0. )
            return 1.  -  0.5 * SpecFunc.incompleteBeta( (d / 2.), 0.5,
                                                         (d / (d + x * x)) );
        else if ( x < 0. )
            return 0.5 * SpecFunc.incompleteBeta( (d / 2.), 0.5,
                                                  (d / (d + x * x)) );
        else
            return 0.5;
    }

//=============================================================================

    public static    
    double 
    F_PDF( double x, int dof1, int dof2 )
    {
        if ( dof1 <= 0 )
            throw new IllegalArgumentException( "F_PDF: dof1 <= 0" );
        if ( dof2 <= 0 )
            throw new IllegalArgumentException( "F_PDF: dof2 <= 0" );
        if ( x <= 0. )
            return 0.;
        double a = (dof1 * x) / (dof1 * x  +  dof2);
        return Math.pow( a, dof1 / 2. ) * Math.pow( (1. - a), dof2 / 2. )
                / (x * SpecFunc.beta( dof1 / 2., dof2 / 2. ));
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    F_DF( double x, int dof1, int dof2 )
    {
        if ( dof1 <= 0 )
            throw new IllegalArgumentException( "F_DF: dof1 <= 0" );
        if ( dof2 <= 0 )
            throw new IllegalArgumentException( "F_DF: dof2 <= 0" );
        if ( x <= 0. )
            return 0.;
        double a = (dof1 * x) / (dof1 * x  +  dof2);
        return SpecFunc.incompleteBeta( dof1 / 2., dof2 / 2., a );
    }

//=============================================================================

    public static    
    double 
    cauchy_PDF( double x, double a )
    {
        if ( a <= 0. )
            throw new IllegalArgumentException( "cauchy_PDF: a <= 0" );
        return a / (Math.PI * (a * a  +  x * x));
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    cauchy_DF( double x, double a )
    {
        if ( a <= 0. )
            throw new IllegalArgumentException( "cauchy_DF: a <= 0" );
        return 0.5  +  Math.atan( x / a ) / Math.PI;
    }

//=============================================================================

    public static    
    double 
    beta_PDF( double x, double a, double b )
    {
        if ( a <= 0. )
            throw new IllegalArgumentException( "beta_PDF: a <= 0" );
        if ( b <= 0. )
            throw new IllegalArgumentException( "beta_PDF: b <= 0" );
        if ( x <= 0. )
            return 0.;
        if ( x >= 1. )
            return 0.;
        return Math.exp( (a - 1.) * Math.log( x )
                         +  (b - 1.) * Math.log( 1. - x )
                         +  SpecFunc.logGamma( a + b )
                         -  SpecFunc.logGamma( a )
                         -  SpecFunc.logGamma( b ) );
    }

//-----------------------------------------------------------------------------

    public static    
    double 
    beta_DF( double x, double a, double b )
    {
        if ( a <= 0. )
            throw new IllegalArgumentException( "beta_DF: a <= 0" );
        if ( b <= 0. )
            throw new IllegalArgumentException( "beta_DF: b <= 0" );
        if ( x <= 0. )
            return 0.;
        if ( x >= 1. )
            return 1.;
        return SpecFunc.incompleteBeta( a, b, x );
    }

//=============================================================================

    public static    
    double 
    kolmogorovSmirnov_DF( double x )
    {
        //Press, et al., "Numerical Recipes in C++", 2nd ed., p. 631.
        final double epsilon1 = 1.e-6;
        final double epsilon2 = 1.e-16;
        double x2 = -2. * x * x;
        double factor = 2.;
        double sum = 0.;
        double absPrevTerm = 0.;
        for ( int i = 1; i < 1000; ++i )
        {
            double term = factor * Math.exp( x2 * i * i );
            sum += term;
            double absTerm = Math.abs( term );
            if ( (absTerm < epsilon1 * absPrevTerm)
                 || (absTerm < epsilon2 * sum) )
                return 1. - sum;
            factor = - factor;
            absPrevTerm = absTerm;
        }
        //failed to converge
        return 1.;
    }

//-----------------------------------------------------------------------------
}                                                                    //ProbDist


//*****************************************************************************
