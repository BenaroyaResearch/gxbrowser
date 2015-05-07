/*
  FileSysTests.groovy

  Tests of FileSys methods. Test with:
  grails test-app unit: org.Sagres.util.FileSys
  or simply
  grails test-app unit: org.Sagres.util.*
*/

package org.sagres.util;

import grails.test.*


//*****************************************************************************


class FileSysTests
extends GrailsUnitTestCase 
{                                                                //FileSysTests
//-----------------------------------------------------------------------------

    protected void setUp() {
        super.setUp()
    }

//-----------------------------------------------------------------------------

    protected void tearDown() {
        super.tearDown()
    }

//=============================================================================

    void testFileSys( )
    {
        String baseDir = "/tmp/FileSysTests/";
        FileSys.deleteDirectory( baseDir );
        assertEquals( false, new File( baseDir ).exists() );
        String dirName0 = baseDir + "abc/def/";
        FileSys.makeDirIfNeeded( dirName0 );
        assertEquals( true, new File( dirName0 ).exists() );
        FileSys.makeDirIfNeeded( dirName0 );
        assertEquals( true, new File( dirName0 ).exists() );

        String fileSpec0 = dirName0 + "T0";
        makeFile( fileSpec0 );
        assertEquals( true, new File( fileSpec0 ).exists() );
        String fileSpec1 = baseDir + "/abc/345/T1";
        assertEquals( false, new File( fileSpec1 ).exists() );
        FileSys.copyFile( fileSpec0, fileSpec1 );
        assertEquals( true, new File( fileSpec0 ).exists() );
        assertEquals( true, new File( fileSpec1 ).exists() );
        try
        {
            FileSys.copyFile( fileSpec0, fileSpec1 );
            fail( "copyFile did not throw an exception" );
        }
        catch ( Exception exc )
        {
            assertTrue( true ); //exception expected
        }
        assertEquals( true, new File( fileSpec0 ).exists() );
        assertEquals( true, new File( fileSpec1 ).exists() );
        try
        {
            FileSys.copyFile( fileSpec0, fileSpec1, true );
            assertTrue( true ); //no exception expected
        }
        catch ( Exception exc )
        {
            fail( "copyFile threw an exception" );
        }
        assertEquals( true, new File( fileSpec0 ).exists() );
        assertEquals( true, new File( fileSpec1 ).exists() );

        String dirName1 = baseDir + "012";
        FileSys.copyDirectory( baseDir + "abc/", dirName1 );
        assertEquals( true, new File( fileSpec0 ).exists() );
        assertEquals( true, new File( fileSpec1 ).exists() );
        assertEquals( true, new File( baseDir + "012/def/T0" ).exists() );
        assertEquals( true, new File( baseDir + "012/345/T1" ).exists() );
        
        String dirName2 = baseDir + "ghi/jkl/";
        String fileSpec2 = dirName2 + "T2";
        FileSys.moveFile( fileSpec0, fileSpec2 );
        assertEquals( false, new File( fileSpec0 ).exists() );
        assertEquals( true, new File( fileSpec2 ).exists() );
        try
        {
            FileSys.deleteFile( fileSpec0 );
            assertTrue( true ); //no exception thrown
        }
        catch ( Exception exc )
        {
            fail( "deleteFile threw an exception" );
        }
        try
        {
            FileSys.deleteFile( fileSpec0, false );
            fail( "deleteFile did not throw an exception" );
        }
        catch ( Exception exc )
        {
            assertTrue( true ); //exception expected
        }
        assertEquals( false, new File( fileSpec0 ).exists() );
        assertEquals( true, new File( fileSpec2 ).exists() );
        try
        {
            FileSys.deleteFile( fileSpec2, false );
            assertTrue( true ); //no exception thrown
        }
        catch ( Exception exc )
        {
            fail( "deleteFile threw an exception" );
        }
        assertEquals( false, new File( fileSpec0 ).exists() );
        assertEquals( false, new File( fileSpec2 ).exists() );
        makeFile( fileSpec0 );
        assertEquals( true, new File( fileSpec0 ).exists() );
        assertEquals( false, new File( fileSpec2 ).exists() );
        FileSys.linkFile( fileSpec0, fileSpec2 );
        assertEquals( true, new File( fileSpec0 ).exists() );
        assertEquals( true, new File( fileSpec2 ).exists() );
        assertEquals( "This is " + fileSpec0 + ".",
                      new File( fileSpec0 ).getText() );
        assertEquals( "This is " + fileSpec0 + ".",
                      new File( fileSpec0 ).getText() );
        String dirName3 = baseDir + "mno/pqr/";
        String fileSpec3 = dirName2 + "T3";
        String dirName4 = baseDir + "stu/vwx/";
        String fileSpec4 = dirName2 + "T4";
        Closure generate = { String fileSpec ->
            FileSys.makeDirIfNeeded( fileSpec );
            makeFile( fileSpec, "First time" );
        };
        FileSys.generateOrLinkFile( fileSpec4, fileSpec3, generate );
        assertEquals( true, new File( fileSpec3 ).exists() );
        assertEquals( true, new File( fileSpec4 ).exists() );
        assertEquals( "This is " + fileSpec3 + ". First time",
                      new File( fileSpec3 ).getText() );
        assertEquals( "This is " + fileSpec3 + ". First time",
                      new File( fileSpec4 ).getText() );
        String dirName5 = baseDir + "yza/bcd";
        String fileSpec5 = dirName2 + "T5";
        generate = { String fileSpec ->
            FileSys.makeDirIfNeeded( fileSpec );
            makeFile( fileSpec, "Second time" );
        };
        FileSys.generateOrLinkFile( fileSpec5, fileSpec3, generate );
        assertEquals( true, new File( fileSpec3 ).exists() );
        assertEquals( true, new File( fileSpec5 ).exists() );
        //NOTE: Cached version not overwritten, just reused.
        assertEquals( "This is " + fileSpec3 + ". First time",
                      new File( fileSpec3 ).getText() );
        assertEquals( "This is " + fileSpec3 + ". First time",
                      new File( fileSpec4 ).getText() );
        FileSys.deleteDirectory( baseDir );
        assertEquals( false, new File( baseDir ).exists() );
        assertEquals( false, new File( fileSpec0 ).exists() );
        assertEquals( false, new File( fileSpec4 ).exists() );
    }

//-----------------------------------------------------------------------------

    private
    boolean makeFile( String fileSpec, String suffix = "" )
    {
        new File( fileSpec ).write( "This is ${fileSpec}." +
                                    (suffix ? " " + suffix : "") );
    }

//-----------------------------------------------------------------------------
}                                                                //FileSysTests


//*****************************************************************************
