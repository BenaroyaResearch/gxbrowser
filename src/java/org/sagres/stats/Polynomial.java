/*
  Polynomial.java
  Copyright © 2010 David M. Anderson
*/


package org.sagres.stats;

import java.util.List;
import java.util.ArrayList;


//*****************************************************************************


/**
   A polynomial, represented by its coefficients,
   so p(x) = c[0] + c[1]*x + c[2]*x^2 +...+ c[n]*x^n, where n is the degree
   of the polynomial.
   [Ideally this would be generic, so that it could be over the fields of
   integers, reals, complex numbers, quaternions, etc., but this is not easy to
   accomplish in Java (as opposed to C++).]
*/

public
class Polynomial
{                                                                  //Polynomial
//-----------------------------------------------------------------------------

    /**
       Constructor from list
       @param coeffs    list of coefficients
    */
    public
    Polynomial( List< Double > coeffs )
    {
        m_coeffs.ensureCapacity( coeffs.size() );
        for ( Double c : coeffs )
            m_coeffs.add( c );
        if ( m_coeffs.size() == 0 )
            m_coeffs.add( 0. );
        trim( );
    }

//.............................................................................

    /**
       Constructor from varargs
       @param coeffs    coefficients, beginning with constant term
    */
    public
    Polynomial( Double ... coeffs )
    {
        m_coeffs.ensureCapacity( coeffs.length );
        for ( Double c : coeffs )
            m_coeffs.add( c );
        if ( m_coeffs.size() == 0 )
            m_coeffs.add( 0. );
        trim( );
    }

//-----------------------------------------------------------------------------

    private
    void
    trim( )
    {
        int i = m_coeffs.size();
        while ( i > 0 )
            if ( m_coeffs.get( --i ) != 0. )
                break;
        for ( int j = m_coeffs.size() - 1; j > i; --j )
            m_coeffs.remove( j );
        m_coeffs.trimToSize( );
    }

//=============================================================================

    /**
       Degree of the polynomial.
       @return  power associated with the highest-order term
    */
    public
    int
    degree( )
    {
        return m_coeffs.size() - 1;
    }

//=============================================================================

    /**
       Gets specified coefficient.
       @param power index of coefficient (0 <= power)
       @return coefficient of specified power of x.
               If power > degree, returns 0.
    */
    public
    Double
    coefficient( int power )
    {
        if ( power >= m_coeffs.size() )
            return 0.;
        return m_coeffs.get( power );
    }

//=============================================================================

    /**
       Evaluate the polynomial as a function.
       @param arg   argument
       @return value of the polynomial at x=arg
    */
    public
    Double
    evaluate( Double arg )
    {
        int i = degree();
        Double value = m_coeffs.get( i );
        while ( i > 0 )
            value  = value * arg  +  m_coeffs.get( --i );
        return value;
    }

//-----------------------------------------------------------------------------

    /**
       Evaluate the polynomial and its derivative.
       @param arg   argument
       @return {@link #ValueAndDerivative valueAndDerivative}
               value of the polynomial and its derivative at x=arg
    */
    public
    ValueAndDerivative
    evaluateAndDerive( Double arg )
    {
        int i = degree();
        Double value = m_coeffs.get( i );
        Double deriv = 0.;
        while ( i > 0 )
        {
            deriv = deriv * arg  +  value;
            value  = value * arg  +  m_coeffs.get( --i );
        }
        return new ValueAndDerivative( value, deriv );
    }

//=============================================================================

    @Override
    public
    boolean
    equals( Object otherObject )
    {
        if ( otherObject == this )
            return true;
        if ( otherObject == null )
            return false;
        if ( ! (otherObject instanceof Polynomial) )
            return false;
        Polynomial other = (Polynomial)otherObject;
        return other.m_coeffs.equals( m_coeffs );
    }

//-----------------------------------------------------------------------------

    @Override
    public
    int
    hashCode( )
    {
        int hash = 0;
        for ( int i = 0; i < m_coeffs.size(); ++i )
            hash += (i + 1) * m_coeffs.get( i ).hashCode();
        return hash;
    }

//=============================================================================

    /**
       Add algebraically.
       @param rhs   right-hand-side addend polynomial
       @return this + rhs
    */
    public
    Polynomial
    add( final Polynomial rhs )
    {
        int newDegree = Math.max( degree(), rhs.degree() );
        List< Double > newCoeffs
                = new ArrayList< Double >( newDegree + 1 );
        for ( int i = 0; i <= newDegree; ++i )
            newCoeffs.set( i, (m_coeffs.get( i ) + rhs.m_coeffs.get( i )) );
        return new Polynomial( newCoeffs );
    }

//-----------------------------------------------------------------------------

    /**
       Subtract algebraically.
       @param rhs   right-hand-side subtrahend polynomial
       @return this - rhs
    */
    public
    Polynomial
    subtract( final Polynomial rhs )
    {
        int newDegree = Math.max( degree(), rhs.degree() );
        List< Double > newCoeffs
                = new ArrayList< Double >( newDegree + 1 );
        for ( int i = 0; i <= newDegree; ++i )
            newCoeffs.set( i, (m_coeffs.get( i ) - rhs.m_coeffs.get( i )) );
        return new Polynomial( newCoeffs );
    }

//-----------------------------------------------------------------------------

    /**
       Multiply by a scalar.
       @param rhs   right-hand-side multiplier scalar
       @return this * rhs
    */
    public
    Polynomial
    multiply( Double rhs )
    {
        List< Double > newCoeffs = new ArrayList< Double >( m_coeffs.size() );
        for ( int i = 0; i < newCoeffs.size(); ++i )
            newCoeffs.set( i, (m_coeffs.get( i ) * rhs) );
        return new Polynomial( newCoeffs );
    }

//.............................................................................

    /**
       Multiply by another polynomial.
       @param rhs   right-hand-size multiplier polynomial
       @return this * rhs
    */
    public
    Polynomial
    multiply( final Polynomial rhs )
    {
        int lhsSize = this.m_coeffs.size();
        int rhsSize = rhs.m_coeffs.size();
        List< Double > newCoeffs
                = new ArrayList< Double >( lhsSize + rhsSize - 1 );
        for ( int i = 0; i < newCoeffs.size(); ++i )
            newCoeffs.set( i, 0. );
        for ( int i = 0; i < lhsSize; ++i )
            for ( int j = 0; j < rhsSize; ++j )
            {
                newCoeffs.set( i + j,
                               (newCoeffs.get( i + j )
                                +  m_coeffs.get( i ) * rhs.m_coeffs.get( j )) );
            }
        return new Polynomial( newCoeffs );
    }
    
//-----------------------------------------------------------------------------

    /**
       Divide by a scalar.
       @param rhs   right-hand-side divisor scalar
       @return this / rhs
    */
    public
    Polynomial
    divide( Double rhs )
    {
        if ( rhs == 0. )
            throw new IllegalArgumentException( "Divisor is 0" );
        List< Double > newCoeffs = new ArrayList< Double >( m_coeffs.size() );
        for ( int i = 0; i < newCoeffs.size(); ++i )
            newCoeffs.set( i, (m_coeffs.get( i ) / rhs) );
        return new Polynomial( newCoeffs );
    }

//=============================================================================


    /**
       Holds the value of the polynomial and its derivative as computed by
       Polynomial.evalAndDerive.
    */
    public
    class ValueAndDerivative
    {                                                      //ValueAndDerivative
    //-------------------------------------------------------------------------

        ValueAndDerivative( Double value, Double derivative )
        {
            this.value = value;
            this.derivative = derivative;
        }

    //=========================================================================

        public final Double value;
        public final Double derivative;
        
    //-------------------------------------------------------------------------
    }                                                      //ValueAndDerivative
        
//=============================================================================

    private final ArrayList< Double > m_coeffs = new ArrayList< Double >( );
    
//-----------------------------------------------------------------------------
}                                                                  //Polynomial


//*****************************************************************************
