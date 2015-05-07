/*
  SpecFunc.java
  Copyright © 2010 David M. Anderson

  Some special mathematical functions:
  factorial: n! = 1*2*3*...*n.
  logFactorial: (natural) log of n!
  binomialCoefficient: n! / k!(n-k)!, 0 <= k < n.
  gamma: the integral over 0 <= t < infinity of t^(a-1) * exp(-t),
      a continuous function with the property that
      gamma(n+1) = n * gamma(n), and so gamma(n) = (n-1)! for integral n.
  logGamma: log of gamma(a). Use of this helps avoid overflow.
  incompleteGamma: integral over 0 <= t <= x of t^(a-1) * exp(-t), divided by
      gamma(a).
  beta: the integral over 0 <= t <= 1 of t^(a-1) * (1-t)^(b-1).
      beta(a,b) = gamma(a) * gamma(b) / gamma(a+b).
  incompleteBeta: integral over 0 <= t <= x of t^(a-1) * (1-t)^(b-1),
      divided by beta(a,b).
  erf: 2 / sqrt(pi) * the integral over 0 <= t <= x of exp( -x^2 ).
      The error function. erf(x) = +/- gamma( 0.5, x^2 ).
*/

package org.sagres.stats;


//*****************************************************************************


public
class SpecFunc
{                                                                    //SpecFunc
//-----------------------------------------------------------------------------

    public static    
    long
    factorial( int n )
    {
        if ( n <= 1 )
            return 1;
        else if ( n < 33 )
        {
            if ( m_factCache[ n ] != 0 )
                return m_factCache[ n ];
            else
                return (m_factCache[ n ] = n * factorial( n - 1 ));
        }
        else
            return n * factorial( n - 1 );
    }

//=============================================================================
    
    public static    
    double 
    logFactorial( int n )
    {
        if ( n <= 1 )
            return 0.;
        if ( n < 100 )
        {
            if ( m_logFactCache[ n ] != 0. )
                return m_logFactCache[ n ];
            else
                return (m_logFactCache[ n ] = logGamma( n + 1. ));
        }
        return logGamma( n + 1.);
    }

//=============================================================================

    public static    
    double 
    binomialCoefficient( int n, int k )
    {
        return Math.floor( Math.exp( logFactorial( n )
                                     - logFactorial( k )
                                     - logFactorial( n - k ) )
                      + 0.5 );
    }

//=============================================================================
    
    public static    
    double
    gamma( double a )
    {
        return Math.exp( logGamma( a ) );
    }

//=============================================================================

    public static    
    double 
    logGamma( double a )
    {
        //Approximation due to Lanczos, as given in Press, et al.,
        // "Numerical Recipes in C++", 2nd ed., p. 218.
        if ( a <= 0. )
            throw new IllegalArgumentException( "logGamma: a <= 0" );
        final double logSqrt2pi = Math.log( 2. * Math.PI ) / 2.;
        final double c0 = 1.000000000190015;
        final double [] cs
                = { 76.18009172947146, -86.50532032941677,
                    24.01409824083091, -1.231739572450155,
                    0.1208650973866179e-2, -0.5395239384953e-5 };
        double b = a + 4.5;
        double s = c0;
        double y = a;
        for ( int i = 0; i < 6; ++i )
        {
            s += cs[i] / y;
            y += 1.;
        }
        return  Math.log( b ) * (a - 0.5)  -  b  +  logSqrt2pi
                +  Math.log( s );
    }

//=============================================================================

    public static    
    double 
    incompleteGamma( double a, double x )
    {
        if ( a <= 0. )
            throw new IllegalArgumentException( "incompleteGamma: a <= 0" );
        if ( x < 0. )
            throw new IllegalArgumentException( "logGamma: x < 0" );
        final double epsilon = 2.5E-16;
        if ( x < a + 1. )
        {
            //series approximation
            double term = 1. / a;
            double sum = term;
            double aa = a;
            for ( int i = 0; i < 1000; ++i )
            {
                aa += 1.;
                term *= x / aa;
                sum += term;
                if ( Math.abs( term )
                     < Math.abs( sum ) * epsilon )
                    return sum * Math.exp( -x  +  a * Math.log( x )
                                           -  logGamma( a ) );
            }
            throw new IllegalStateException( "incompleteGamma series "
                                             + "failed to converge." );
        }
        else //x >= a + 1.
        {
            //continued fraction approximation
            final double minDouble = 2.5E-308;
            final double tiny = minDouble / epsilon;
            double b = x - a + 1.;
            double c = 1. / tiny  +  b;
            double d = b;
            if ( Math.abs( d ) < tiny )
                d = tiny;
            d = 1. / d;
            double f = d;
            for ( int i = 1; i < 1000; ++i )
            {
                double ai = -i * (i - a);
                b += 2.;
                c = ai / c  +  b;
                if ( Math.abs( c ) < tiny )
                    c = tiny;
                d = ai * d  +  b;
                if ( Math.abs( d ) < tiny )
                    d = tiny;
                d = 1. / d;
                double delta = c * d;
                f *= delta;
                if ( Math.abs( delta - 1. ) < epsilon )
                    return 1.  -  f * Math.exp( -x  +  a * Math.log( x )
                                                -  logGamma( a ) );
            }
            throw new IllegalStateException( "incompleteGamma continued "
                             + "fraction failed to converge." );
        }
    }

//=============================================================================

    public static    
    double 
    beta( double a, double b )
    {
        if ( a <= 0. )
            throw new IllegalArgumentException( "beta: a <= 0" );
        if ( b <= 0. )
            throw new IllegalArgumentException( "beta: b <= 0" );
        return Math.exp( logGamma( a ) + logGamma( b ) - logGamma( a + b ) );
    }

//=============================================================================

    public static    
    double 
    incompleteBeta( double a, double b, double x )
    {
        if ( a <= 0. )
            throw new IllegalArgumentException( "incompleteBeta: a <= 0" );
        if ( b <= 0. )
            throw new IllegalArgumentException( "incompleteBeta: b <= 0" );
        if ( x <= 0. )
            return 0.;
        if ( x >= 1. )
            return 1.;
        if ( x > (a + 1.) / (a + b + 2.) )
            return 1. - incompleteBeta( b, a, 1. - x );

        final double epsilon = 2.5E-16;
        final double minDouble = 2.5E-308;
        final double tiny = minDouble / epsilon;
        //continued fraction approximation
        final double ap1 = a + 1.;
        final double am1 = a - 1.;
        final double apb = a + b;
        double c = 1.;
        double d = 1. - apb * x / ap1;
        if ( Math.abs( d ) < tiny )
            d = tiny;
        d = 1. / d;
        double f = d;
        for ( int i = 1; i < 5000; ++i )
        {
            double i2 = 2. * i;
            //even step
            double aa = i * (b - i) * x / ((a + i2) * (am1 + i2));
            c = aa / c  +  1.;
            if ( Math.abs( c ) < tiny )
                c = tiny;
            d = aa * d  +  1.;
            if ( Math.abs( d ) < tiny )
                d = tiny;
            d = 1. / d;
            f *= c * d;
            //odd step
            aa = - (a + i) * (apb + i) * x / ((a + i2) * (ap1 + i2));
            c = aa / c  +  1.;
            if ( Math.abs( c ) < tiny )
                c = tiny;
            d = aa * d  +  1.;
            if ( Math.abs( d ) < tiny )
                d = tiny;
            d = 1. / d;
            double delta = c * d;
            f *= delta;
            if ( Math.abs( delta - 1. ) < epsilon )
                return f / a * Math.exp( a * Math.log( x )
                                         +  b * Math.log( 1. - x )
                                         +  logGamma( a + b )  -  logGamma( a )
                                         -  logGamma( b ) );
        }
        throw new IllegalStateException( "incompleteBeta continued fraction "
                                         + "failed to converge." );
    }

//=============================================================================

    public static    
    double 
    erf( double x )
    {
        //See Press, et al., "Numerical Recipes in C++", 2nd ed., p. 226.
        final Polynomial poly =
                new Polynomial( -1.26551223, 1.00002368, 0.37409196,
								0.09678418, -0.18628806, 0.27886807,
								-1.13520398, 1.48851587, -0.82215223,
								0.17087277 );
        double y = Math.abs( x );
        double z = 1. / (1. + 0.5 * y);
        double e = z * Math.exp( -y * y  +  poly.evaluate( z ) );
        if ( x >= 0. )
            return 1. - e;
        else
            return e - 1.;
    }

//=============================================================================

    static long [] m_factCache = new long[ 33 ]; //for factorial
    static double [] m_logFactCache = new double[ 100 ]; //for logFactorial

//-----------------------------------------------------------------------------
}                                                                    //SpecFunc


//*****************************************************************************
