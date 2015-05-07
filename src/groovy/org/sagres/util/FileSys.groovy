/*
  FileSys.groovy

  Routines for file handling.
*/

package org.sagres.util;


//*****************************************************************************


class FileSys
{                                                                     //FileSys
//-----------------------------------------------------------------------------

    static
    void makeDirIfNeeded( String fileSpec )
    {
        File path;
        if ( fileSpec.endsWith( "/" ) )
        {
            path = new File( fileSpec );
        }
        else
        {
            path = (new File( fileSpec )).getParentFile();
        }
        if ( path.exists() )
            return;
        if ( path.mkdirs() == false )
        {
            throw new SagresException( "Unable to mkdir '${path}'" );
        }
    }

//=============================================================================

    static
    void copyFile( String srcSpec, String destSpec,
                   boolean overwriteExisting = false )
    {
        File srcFile = new File( srcSpec );
        if ( srcFile.exists() == false )
        {
            throw new SagresException( "${srcSpec} does not exist" );
        }
        File destFile = new File( destSpec );
        if ( destFile.exists() )
        {
            if ( overwriteExisting )
            {
                if ( destFile.delete( ) == false )
                {
                    throw new SagresException(
                        "Unable to delete '${destSpec}'" );
                }
            }
            else
            {
                throw new SagresException(
                    "${destSpec} exists. Must not overwrite it" );
            }
        }

        makeDirIfNeeded( destSpec );
        destFile << srcFile.asWritable();
    }

//-----------------------------------------------------------------------------

    static
    void copyDirectory( String srcSpec, String destSpec,
                        boolean overwriteExisting = false )
    {
        if ( srcSpec[ -1 ] != '/' )
            srcSpec += '/';
        if ( destSpec[ -1 ] != '/' )
            destSpec += '/';
        File srcFile = new File( srcSpec );
        if ( srcFile.exists() == false )
        {
            throw new SagresException( "${srcSpec} does not exist" );
        }
        String[] names = srcFile.list();
        for ( String name : names )
        {
            String srcSpec1 = srcSpec + name;
            String destSpec1 = destSpec + name;
            if ( new File( srcSpec1 ).isDirectory() )
            {   //recurse
                copyDirectory( srcSpec1, destSpec1, overwriteExisting );
            }
            else
            {
                copyFile( srcSpec1, destSpec1, overwriteExisting );
            }
        }
    }

//=============================================================================

    //Moves a file, creating directories for the destination file if needed.

    static
    void moveFile( String origSpec, String newSpec,
                   boolean okIfNonexistent = false )
    {
        File origFile = new File( origSpec );
        File newFile = new File( newSpec );
        makeDirIfNeeded( newSpec );
        if ( origFile.exists() == false )
        {
            if ( okIfNonexistent )
            {
                return;
            }
            else
            {
                throw new SagresException( "${origSpec} does not exist" );
            }
        }
        if ( origFile.renameTo( newFile ) == false )
        {
            throw new SagresException(
                "Unable to move '${origSpec}' to '${newSpec}'" );
        }
    }

//=============================================================================

    //Deletes a file.
    // By default reports success if the file already doesn't exist.

    static
	void deleteFile( String fileSpec, boolean okIfNonexistent = true )
	{
		File file = new File( fileSpec );
        if ( file.exists() == false )
        {
            if ( okIfNonexistent )
            {
                return;
            }
            else
            {
                throw new SagresException( "${fileSpec} does not exist" );
            }
        }
		if ( file.delete( ) == false )
        {
            throw new SagresException( "Unable to delete '${fileSpec}'" );
        }
	}

//-----------------------------------------------------------------------------

    static
    void deleteDirectory( String dirSpec )
    {
        File dir = new File( dirSpec );
        if ( dir.isDirectory() == false )
        {
            throw new SagresException( "${dirSpec} is not a directory" );
        }
        File[] dirContents = dir.listFiles();
        for ( File file : dirContents )
        {
            if ( file.isDirectory() )
            {
                deleteDirectory( file.toString() ); //recurse
            }
            else
            {
                file.delete( );
            }
        }
        dir.delete( );
    }

//-----------------------------------------------------------------------------

    static
    void deleteFileOrDirectory( String fileSpec )
    {
		File file = new File( fileSpec );
        if ( file.exists() == false )
        {
            return;
        }
        if ( file.isDirectory() )
        {
            deleteDirectory( fileSpec );
        }
        else
        {
            deleteFile( fileSpec );
        }
    }

//=============================================================================

    static
    void linkFile( String srcSpec, String destSpec )
    {
        makeDirIfNeeded( destSpec );
        deleteFile( destSpec );
        String command = "ln -s ${srcSpec} ${destSpec}";
        if ( OS.execSysCommand( command ) == false )
        {
            String msg = "Unable to link '${srcSpec}' as ${destSpec}";
            throw new SagresException( msg );
        }
    }

//-----------------------------------------------------------------------------

    static
    void generateOrLinkFile( String destSpec,
                             String cachedSpec,
                             Closure generateFile )
    {
        File cachedFile = new File( cachedSpec );
        if ( cachedFile.exists() == false )
        {
            generateFile( cachedSpec );
        }
        linkFile( cachedSpec, destSpec );
    }

//-----------------------------------------------------------------------------
}                                                                     //FileSys


//*****************************************************************************
