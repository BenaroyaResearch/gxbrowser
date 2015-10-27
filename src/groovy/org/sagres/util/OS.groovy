/*
  OS.groovy

  Methods related to the operating system.
*/

package org.sagres.util;


//*****************************************************************************


class OS
{                                                                          //OS
//-----------------------------------------------------------------------------

    static
    boolean execSysCommand( String command,
                            List arguments = [],
                            Map results = null,
                            int verbosity = 1,
                            String workingDir = null)
    {
		def out = new ByteArrayOutputStream()
		def err = new ByteArrayOutputStream()
        boolean success = false;
        try
        {
            //It seems best to use sh to parse the command & args, rather than
            // Java, because, among other things, the latter doesn't handle
            // wildcards properly.
            command += " " + arguments.join( " " );
            if ( verbosity > 1 )
            {
                println( "Executing: " + command );
            }
            File dir = workingDir ? new File( workingDir ) : null;
            List commandList = [ "sh", "-c", command ];
            Process process = commandList.execute( null, dir );
			process.consumeProcessOutput(out, err)
            int returnValue = process.waitFor( );
            success = (returnValue == 0);
			String output = out.toString()
			String error = err.toString()
            if ( success )
            {
                if (verbosity > 2)
                {
                    println( output );
                }
            }
            else
            {
                if ( verbosity > 0 )
                {
                    if ( verbosity == 1 )
                    {
                        println( "Error executing: " + command );
                    }
                    println( error );
                }
                if ( verbosity > 1 )
                {
                    println( output );
                }
            }
            if ( results != null )
            {
                results.returnValue = returnValue;
                results.output = output;
                results.error = error;
            }
        }
        catch( IOException exc )
        {
            success = false;
            if ( verbosity > 0 )
            {
                println( "IO Exception: " + exc.message );
            }
            if ( results )
            {
                results.error = exc.message;
            }
        }
        return success;
    }

//-----------------------------------------------------------------------------
}                                                                          //OS


//*****************************************************************************
