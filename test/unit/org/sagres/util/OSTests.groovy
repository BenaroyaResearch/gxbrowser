/*
  OSTests.groovy

  Tests of OS methods. Test with:
  grails test-app unit: org.sagres.util.OS
  or simply
  grails test-app unit: org.sagres.util.*

  The println output and also some output from execSysCommand will appear in
  target/test-reports/TEST-unit-unit-org.sagres.util.OSTests.xml .
  The tests will fail if this is run with root privileges. But you wouldn't do
  that, would you?
*/

package org.sagres.util;

import grails.test.*


//*****************************************************************************


class OSTests
extends GrailsUnitTestCase 
{                                                                     //OSTests
//-----------------------------------------------------------------------------

    protected void setUp() {
        super.setUp()
    }

//-----------------------------------------------------------------------------

    protected void tearDown() {
        super.tearDown()
    }

//=============================================================================

    void testOS( )
    {
        String command = "echo Hello";
        int verbosity = 0;
        Map results = [:];
        println( "command='${command}'  verbosity=${verbosity}" );
        boolean rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "Hello\n", results.output );
        assertEquals( "", results.error );
        verbosity = 1;
        println( "command='${command}'  verbosity=${verbosity}" );
        rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "Hello\n", results.output );
        assertEquals( "", results.error );
        verbosity = 2;
        println( "command='${command}'  verbosity=${verbosity}" );
        rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "Hello\n", results.output );
        assertEquals( "", results.error );
        verbosity = 3;
        println( "command='${command}'  verbosity=${verbosity}" );
        rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "Hello\n", results.output );
        assertEquals( "", results.error );

        command = "pwd";
        verbosity = 0;
        String workingDir = "/tmp";
        println( "command='${command}'  verbosity=${verbosity}" +
                 "  workingDir=${workingDir}" );
        rslt = OS.execSysCommand( command, verbosity, results, workingDir );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "/tmp\n", results.output );
        assertEquals( "", results.error );
        verbosity = 1;
        println( "command='${command}'  verbosity=${verbosity}" +
                 "  workingDir=${workingDir}" );
        rslt = OS.execSysCommand( command, verbosity, results, workingDir );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "/tmp\n", results.output );
        assertEquals( "", results.error );
        verbosity = 2;
        println( "command='${command}'  verbosity=${verbosity}" +
                 "  workingDir=${workingDir}" );
        rslt = OS.execSysCommand( command, verbosity, results, workingDir );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "/tmp\n", results.output );
        assertEquals( "", results.error );
        verbosity = 3;
        println( "command='${command}'  verbosity=${verbosity}" +
                 "  workingDir=${workingDir}" );
        rslt = OS.execSysCommand( command, verbosity, results, workingDir );
        assertEquals( true, rslt );
        assertEquals( 0, results.returnValue );
        assertEquals( "/tmp\n", results.output );
        assertEquals( "", results.error );
        
        command = "mkdir /root/foo";
        verbosity = 0;
        println( "command='${command}'  verbosity=${verbosity}" );
        rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( false, rslt );
        assertEquals( 1, results.returnValue );
        assertEquals( "", results.output );
        assertEquals( "mkdir: cannot create directory `/root/foo': Permission denied\n", results.error );
        verbosity = 1;
        println( "command='${command}'  verbosity=${verbosity}" );
        rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( false, rslt );
        assertEquals( 1, results.returnValue );
        assertEquals( "", results.output );
        assertEquals( "mkdir: cannot create directory `/root/foo': Permission denied\n", results.error );
        verbosity = 2;
        println( "command='${command}'  verbosity=${verbosity}" );
        rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( false, rslt );
        assertEquals( 1, results.returnValue );
        assertEquals( "", results.output );
        assertEquals( "mkdir: cannot create directory `/root/foo': Permission denied\n", results.error );
        verbosity = 3;
        println( "command='${command}'  verbosity=${verbosity}" );
        rslt = OS.execSysCommand( command, verbosity, results );
        assertEquals( false, rslt );
        assertEquals( 1, results.returnValue );
        assertEquals( "", results.output );
        assertEquals( "mkdir: cannot create directory `/root/foo': Permission denied\n", results.error );
    }

//-----------------------------------------------------------------------------
}                                                                     //OSTests


//*****************************************************************************
