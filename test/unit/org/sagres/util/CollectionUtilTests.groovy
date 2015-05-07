/*
  CollectionUtilTests.groovy

  Tests of CollectionUtil methods. Test with:
  grails test-app unit: us.EpsilonDelta.util.CollectionUtil
  or simply
  grails test-app unit: us.EpsilonDelta.util.*
*/

package org.sagres.util;

import grails.test.*


//*****************************************************************************


class CollectionUtilTests
extends GrailsUnitTestCase 
{                                                              //CollectionUtil
//-----------------------------------------------------------------------------


    protected void setUp() {
        super.setUp()
    }

//-----------------------------------------------------------------------------

    protected void tearDown() {
        super.tearDown()
    }

//=============================================================================

    void testFlattenMap( )
    {
        Map origMap =
                [
                    i1: 1,
                    s1: "s1",
                    m1:
                    [
                        i11: 11,
                        s11: "s11"
                    ],
                    m2:
                    [
                        i21: 21,
                        s21: "s21",
                        m21:
                        [
                            i211: 211,
                            s211: "s211"
                        ]
                    ],
                    m3:
                    [
                        i31: 31,
                        s31: "s31",
                        m31:
                        [
                            i311: 311,
                            s311: "s311",
                            m311:
                            [
                                i3111: 3111,
                                s3111: "s3111"
                            ]
                        ]
                    ],
                    i2: 2,
                    s2: "s2"
                ];
        println( "origMap = " + origMap );
        assertEquals( true, origMap.containsKey( "i1" ) );
        assertEquals( "s1", origMap.s1 );
        assertEquals( "s2", origMap[ "s2" ] );
        assertEquals( true, origMap.containsKey( "m1" ) );
        assertEquals( false, origMap.containsKey( "m1.i11" ) );
        //But can do this:
        assertEquals( "s11", origMap.m1.s11 );
        assertEquals( false, origMap.containsKey( "m3.m31.m311.i3111" ) );
        //But can do this:
        assertEquals( 3111, origMap.m3.m31.m311.i3111 );
        assertEquals( true, origMap.containsKey( "m3" ) );
        assertEquals( true, origMap.m3.containsKey( "m31" ) );
        assertEquals( true, origMap.m3.m31.m311.containsKey( "i3111" ) );
        assertEquals( 3111, origMap[ "m3" ][ "m31" ][ "m311" ][ "i3111" ] );

        Map flattened = CollectionUtil.flattenMap( origMap );
        println( "flattened = " + flattened );
        assertEquals( true, flattened.containsKey( "i1" ) );
        assertEquals( "s1", flattened.s1 );
        assertEquals( "s2", flattened[ "s2" ] );
        assertEquals( false, flattened.containsKey( "m1" ) );
        assertEquals( true, flattened.containsKey( "m1.i11" ) );
        //But can't do this:
        //assertEquals( "s11", flattened.m1.s11 );
        assertEquals( "s11", flattened[ "m1.s11" ] );
        assertEquals( true, flattened.containsKey( "m3.m31.m311.i3111" ) );
        //So can do this
        assertEquals( 3111, flattened[ "m3.m31.m311.i3111" ] );
        //But can't do this:
        // assertEquals( 3111, flattened.m3.m31.m311.i3111 );
        assertEquals( false, flattened.containsKey( "m3" ) );
        //So can't do these:
        // assertEquals( false, flattened.m3.containsKey( "m31" ) );
        // assertEquals( false, flattened.m3.m31.m311.containsKey( "i3111" ) );
        // assertEquals( 3111, flattened[ "m3" ][ "m31" ][ "m311" ][ "i3111" ] );

        flattened = CollectionUtil.flattenMap( origMap, 1 );
        println( "flattened = " + flattened );
        assertEquals( true, flattened.containsKey( "i1" ) );
        assertEquals( "s1", flattened.s1 );
        assertEquals( "s2", flattened[ "s2" ] );
        assertEquals( false, flattened.containsKey( "m1" ) );
        assertEquals( true, flattened.containsKey( "m1.i11" ) );
        //But can't do this:
        //assertEquals( "s11", flattened.m1.s11 );
        assertEquals( "s11", flattened[ "m1.s11" ] );
        assertEquals( false, flattened.containsKey( "m3.m31.m311.i3111" ) );
        //Can't do this
        // assertEquals( 3111, flattened.m3.m31.m311.i3111 );
        assertEquals( false, flattened.containsKey( "m3" ) );
        //So can't do these:
        // assertEquals( false, flattened.m3.containsKey( "m31" ) );
        // assertEquals( false, flattened.m3.m31.m311.containsKey( "i3111" ) );
        // assertEquals( 3111, flattened[ "m3" ][ "m31" ][ "m311" ][ "i3111" ] );
        assertEquals( true, flattened.containsKey( "m3.m31" ) );
        //So can do this:
        assertEquals( true, flattened[ "m3.m31" ].containsKey( "m311" ) );
        assertEquals( 3111, flattened[ "m3.m31" ][ "m311" ][ "i3111" ] );

        flattened = CollectionUtil.flattenMap( origMap, 0 );
        println( "flattened = " + flattened );
        assertEquals( origMap, flattened );
        assertEquals( true, flattened.containsKey( "i1" ) );
        assertEquals( "s1", flattened.s1 );
        assertEquals( "s2", flattened[ "s2" ] );
        assertEquals( true, flattened.containsKey( "m1" ) );
        assertEquals( false, flattened.containsKey( "m1.i11" ) );
        //So can't do this:
        // assertEquals( "s11", flattened[ "m1.s11" ] );
        //But can do this:
        assertEquals( "s11", flattened.m1.s11 );
        assertEquals( false, flattened.containsKey( "m3.m31.m311.i3111" ) );
        //But can do this:
        assertEquals( 3111, flattened.m3.m31.m311.i3111 );
        assertEquals( true, flattened.containsKey( "m3" ) );
        assertEquals( true, flattened.m3.containsKey( "m31" ) );
        assertEquals( true, flattened.m3.m31.m311.containsKey( "i3111" ) );
        assertEquals( 3111, flattened[ "m3" ][ "m31" ][ "m311" ][ "i3111" ] );
    }

//-----------------------------------------------------------------------------
}                                                              //CollectionUtil


//*****************************************************************************
