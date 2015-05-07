/*
  Importer.groovy

  Utility classes and functions for importing files into database.
 */

package org.sagres.importer;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import groovy.sql.Sql;
import java.sql.SQLException;
//import org.codehaus.groovy.grails.commons.GrailsApplication;
import groovy.util.ConfigObject;
import grails.plugin.mail.MailService;
import org.sagres.FileLoadStatus;
import org.sagres.FilesLoaded;
import common.chipInfo.ChipType;
import org.sagres.util.FileSys;


//*****************************************************************************


class Importer
{                                                                    //Importer
//-----------------------------------------------------------------------------

    static
    Number parseNumber( String field )
    {
        DecimalFormat format = new DecimalFormat();
        ParsePosition pos = new ParsePosition( 0 );
        field = field.toUpperCase().replace( "E+", "E" ).replace( ",", "." );
        Number number = format.parse( field, pos );
        if ( (number == null) || (pos.getIndex() < field.size()) )
            return null;
        return number;
    }

//-----------------------------------------------------------------------------

    static
    Integer parseInteger( String field )
    {
        Number number = parseNumber( field );
        return number?.intValue();
    }

//-----------------------------------------------------------------------------

    static
    Float parseFloat( String field )
    {
        Number number = parseNumber( field );
        return number?.floatValue();
    }

//-----------------------------------------------------------------------------

    static
    Double parseDouble( String field )
    {
        Number number = parseNumber( field );
        return number?.doubleValue();
    }

//=============================================================================

    static
    String combineMessages( List< String > messages,
                            int maxLength = 0, int maxLines = 0 )
    {
        List< String > messageLines = [];
        if ( (maxLines == 0) || (messages.size() <= maxLines) )
        {
            messageLines.addAll( messages );
        }
        else if ( maxLines > 1 )
        {
            for ( int i = 0; i < maxLines - 1; ++i )
            {
                messageLines.add( messages[ i ] );
            }
            messageLines.add( "..." );
        }
        else
        {
            messageLines.add( messages[ 0 ] + "..." );
        }
        String message = messageLines.join( "\n" );
        if ( (maxLength > 0) && (message.size() > maxLength) )
        {
            message = message.substring( 0, maxLength - 4 );
            message += "...";
        }
        return message;
    }

//=============================================================================

    //Moves a file, creating directories for the destination file if needed.

    static
    void moveFile( String origSpec, String newSpec,
                      boolean okIfNonexistent = false )
    {
        File origFile = new File( origSpec );
        File newFile = new File( newSpec );
        File newParentPath = newFile.getParentFile();
        if ( (newParentPath.exists() == false) &&
             (newParentPath.mkdirs( ) == false) )
        {
            throw new ImportException( "Unable to mkdir '${newParentPath}'" );
        }
        if ( origFile.exists() == false )
        {
            if ( okIfNonexistent )
            {
                return;
            }
            else
            {
                throw new ImportException( "${origSpec} does not exist" );
            }
        }
        if ( origFile.renameTo( newFile ) == false )
        {
            throw new ImportException(
                "Unable to move '${origSpec}' to '${newSpec}'" );
        }
    }

//-----------------------------------------------------------------------------

    //Deletes a file.
    // By default reports success if the file already doesn't exist.

    static
	void deleteFile( String fileSpec, boolean okIfNonexistent = true )
	{
		def file = new File( fileSpec );
        if ( file.exists() == false )
        {
            if ( okIfNonexistent )
            {
                return;
            }
            else
            {
                throw new ImportException( "${fileSpec} does not exist" );
            }
        }
		if ( file.delete( ) == false )
        {
            throw new ImportException( "Unable to delete '${fileSpec}'" );
        }
	}

//=============================================================================

    static
    String abbreviateProbeId( String probeId )
    {
        if ( probeId.startsWith( "ILMN_" ) )
        {
            probeId = probeId.substring( "ILMN_".size() );
        }
        return probeId;
    }

//-----------------------------------------------------------------------------

    static
    String expandProbeId( String probeId, ChipType chipType )
    {
        if ( (chipType.chipData.manufacturer == "Illumina") &&
             (probeId.startsWith( "ILMN_" ) == false) )
        {
            probeId = "ILMN_" + probeId;
        }
        return probeId;
    }

//=============================================================================

    static
    FilesLoaded createFilesLoadedRecord( String fileName )
    {
        FilesLoaded logRecord =
                new FilesLoaded( filename: fileName,
                                 loadStatus: ImportStatus.STARTING.record,
                                 dateStarted: new Date() );
        assert( logRecord );
        if ( logRecord.save( flush: true ) == null )
        {
            List< String > errors = [ "Error saving FilesLoaded record:" ];
            logRecord.errors.each { err ->
                errors.add( err );
            }
            throw new ImportException( errors );
        }
        return logRecord;
    }

//=============================================================================

    static
    void updateLogRecord( def logRecord, ImportStatus status,
                          Map fields = [:] )
    {
        assert( logRecord );
        if ( status )
            logRecord.loadStatus = status.record;
        fields.each { key, value ->
            logRecord[ key ] = value;
        }
        if ( logRecord.save( flush: true ) == null )
        {
            List< String > errors = [ "Error saving log record:" ];
            logRecord.errors.each { err ->
                errors.add( err );
            }
            throw new ImportException( errors );
        }
    }

//=============================================================================

    static
    void reportErrors( List< String > messages, def logRecord,
                       String errorLogSpec, boolean asError = true, Map fields = [:] )
    {
        final int maxTableNote = 1000;
        try
        {
            println( combineMessages( messages, 1000, 10 ) );

            if ( logRecord )
            {
                logRecord.loadStatus =
                        asError  ?  ImportStatus.ERROR.record  :
                        ImportStatus.WARNING.record;
				fields.each { key, value ->
					logRecord[ key ] = value;
				}
                logRecord.notes = combineMessages( messages, 1000 );
                logRecord.save( flush: true );
            }

            if ( errorLogSpec )
            {
                PrintWriter writer;
                try
                {
                    FileSys.makeDirIfNeeded( errorLogSpec );
                    writer = new PrintWriter( errorLogSpec );
                    for ( String msg : messages )
                    {
                        writer.print( msg + "\n" );
                    }
                    writer.flush( );
                }
                catch ( FileNotFoundException exc )
                {
                    println( "Unable to create " + errorLogSpec );
                }
                catch ( IOException exc )
                {
                    println( "Error writing to " + errorLogSpec );
                }
                finally
                {
                    writer?.close( );
                }
            }
        }
        catch ( Exception exc )
        {
            println( "Import Error: " + exc.message );
        }
    }

//=============================================================================

    static
    void sendEMail( MailService mailService, ConfigObject config,
                    List< String > fileSpecs,
                    ImportStatus status, String message,
                    String recipient = null )
    {
        if ( (config == null) || (mailService == null) )
            return;
        if ( config.importer.serverName == null )
            return;
        String ccRecipient;
        if ( recipient )
        {
            ccRecipient = config.importer.defaultEMailTo;
        }
        else
        {
            recipient = config.importer.defaultEMailTo;
        }
        String sender = config.importer.defaultEMailFrom;
        List< String > fileNames = [];
        fileSpecs.each { spec ->
            FileSpecParts parts = parseFileSpec( spec );
            fileNames.add( parts.name );
        }
        String fileString = fileNames.join( " & " );
        String theSubject = "Import of " + fileString +
                " to " + config.importer.serverName + ": " + status;
        String theMessage = theSubject + "\n" + message;

        println( "Importer.sendMail: to=${recipient} from=${sender} subject=${theSubject}" );
        mailService.sendMail {
            to( recipient );
            if ( ccRecipient )
                cc( ccRecipient );
            from( sender );
            subject( theSubject );
            body( theMessage );
        }
    }

//=============================================================================

    /*
      Given a file specification with the form
      {baseDir/}{midDir/}{subDir/}{name}{.ext}
      returns those parts.
      Missing parts are set to empty strings.
      Internal and trailing slashes, as well as leading dots, are retained
    */

    static
    FileSpecParts parseFileSpec( String fileSpec, String midDirName = null )
    {
        FileSpecParts specParts = new FileSpecParts();
        String[] parts = fileSpec.split( "/" );
        if ( parts.size() >= 1 )
        {
            specParts.path = parts[ 0..-2 ].join( "/" );
            String filename = parts[ -1 ];
            String[] nameParts = filename.split( /\./ );
            specParts.name = (nameParts.size() < 2)  ?  filename  :
                    nameParts[ 0..-2 ].join( "." );
            specParts.ext = (nameParts.size() > 1) ? "." + nameParts[ -1 ] : "";
        }
        else
        {
            specParts.path = specParts.name = specParts.ext = "";
        }
        int midIndex = -1;
        if ( midDirName )
        {
            String mid = midDirName.toLowerCase();
            for ( int i = 0; i < parts.size() - 1; ++i )
            {
                if ( parts[ i ].toLowerCase() == mid )
                {
                    midIndex = i;
                    break;
                }
            }
        }
        if ( midIndex >= 0 )
        {
            specParts.baseDir = (midIndex > 0) ?
                    parts[ 0..(midIndex - 1) ].join( "/" ) + "/"  :  "";
            specParts.midDir = (midIndex >= 0) ? parts[ midIndex ] + "/" : "";
            specParts.subDir = (midIndex < parts.size() - 2) ?
                    parts[ (midIndex + 1)..-2 ].join( "/" ) + "/"  :  "";
        }
        else
        {
            specParts.baseDir = specParts.path + "/";
            specParts.midDir = "";
            specParts.subDir = "";
        }
        return specParts;
    }

//-----------------------------------------------------------------------------

    static
    AuxFileSpecs buildAuxFileSpecs( FileSpecParts specParts )
    {
        AuxFileSpecs auxSpecs = new AuxFileSpecs();
        String inPath = specParts.baseDir + specParts.midDir + specParts.subDir;
        String outPath = specParts.baseDir + "imported/" + specParts.subDir;
        String errorPath = specParts.baseDir + "errors/";
        String fileName = specParts.name + specParts.ext;
        String bulkName = specParts.name + "_bulkLoad.txt";
        String errorName = specParts.name + "_errors.txt";
        auxSpecs.inputSpec = inPath + fileName;
        auxSpecs.bulkLoadSpec = inPath + bulkName;
        auxSpecs.archiveSpec = outPath + fileName;
        auxSpecs.bulkLoadArchiveSpec = outPath + bulkName;
        auxSpecs.errorSpec = errorPath + fileName;
        auxSpecs.errorLogSpec = errorPath + errorName;
		auxSpecs.bulkLoadErrorSpec = errorPath + bulkName;
        return auxSpecs;
    }

//=============================================================================

	static
	String quoteForSql( String str )
	{
		return "'" + str.replaceAll( "'", "''" ) + "'";
	}

//=============================================================================

    static
    void writeBulkLoadLine( PrintWriter writer, List data )
    {
        for ( int i = 0; i < data.size(); ++i )
        {
            if ( i > 0 )
                writer.print( "\t" );
            def datum = data[ i ];
            writer.print( (datum != null) ? datum : "\\N" );
        }
        writer.print( "\n" );
    }

//-----------------------------------------------------------------------------

    static
    void loadDataToDb( String bulkLoadSpec, String tableName,
                       List< String > fields, Sql sql )
    {
        String command;

        try
        {

            command =
                    "LOAD DATA LOCAL INFILE '" + bulkLoadSpec + "'" +
                    " INTO TABLE " + tableName + " ( " + 
                    (fields.collect { "`" + it + "`" }).join( ", " ) +
                    " )";
		    println "started SQL loadding \"${command}\" ..."
			long startTime = System.currentTimeMillis()
            sql.execute( command );
			long endTime = System.currentTimeMillis()
			println "finished loading in ${(endTime - startTime)/1000.0}s"
			
            if ( sql.updateCount <= 0 )
            {
                String msg = "Unable to load " + bulkLoadSpec +
                        " into " + tableName;
                throw new ImportException( msg );
            }
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new  ImportException( msg );
        }
    }

//-----------------------------------------------------------------------------

    static
    void deleteAllFromDb( String tableName, Sql sql )
    {
        String command;
        try
        {
            command = "DELETE FROM " + tableName;
            sql.execute( command );
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new  ImportException( msg );
        }
    }

//=============================================================================

    static
    void cleanup( boolean succeeded, AuxFileSpecs auxSpecs,
				  List< FileSpecParts > specPartsList = [] )
    {
        if ( auxSpecs == null )
            return;
		List< AuxFileSpecs > extraAuxSpecs = [];
		for ( FileSpecParts specParts : specPartsList )
		{
			extraAuxSpecs.add( buildAuxFileSpecs( specParts ) );
		}
        if ( succeeded )
        {
			moveFile( auxSpecs.inputSpec, auxSpecs.archiveSpec );
			moveFile( auxSpecs.bulkLoadSpec, auxSpecs.bulkLoadArchiveSpec,
                      true );
			for ( AuxFileSpecs specs : extraAuxSpecs )
			{
				moveFile( specs.inputSpec, specs.archiveSpec );
			}
        }
        else
        {
			moveFile( auxSpecs.inputSpec, auxSpecs.errorSpec );
			moveFile( auxSpecs.bulkLoadSpec, auxSpecs.bulkLoadErrorSpec, true );
			for ( AuxFileSpecs specs : extraAuxSpecs )
			{
				moveFile( specs.inputSpec, specs.errorSpec );
			}
        }
    }

//-----------------------------------------------------------------------------
}                                                                    //Importer


//*****************************************************************************


enum ImportStatus 
{                                                                //ImportStatus
//-----------------------------------------------------------------------------

    STARTING( 1 ), IMPORTING( 2 ), COMPLETE( 3 ), ERROR( 4 ), WARNING( 5 );

//=============================================================================

    ImportStatus( int id )
    {
        record = FileLoadStatus.get( id );
    }

//=============================================================================

    FileLoadStatus record;

//-----------------------------------------------------------------------------
}                                                                //ImportStatus


//*****************************************************************************


class FileSpecParts
{                                                               //FileSpecParts
//-----------------------------------------------------------------------------

    String path;
    String baseDir;
    String midDir;
    String subDir;
    String name;
    String ext;

//-----------------------------------------------------------------------------
}                                                               //FileSpecParts


//*****************************************************************************


class AuxFileSpecs
{                                                                //AuxFileSpecs
//-----------------------------------------------------------------------------

    String inputSpec;
    String bulkLoadSpec;
    String archiveSpec;
    String bulkLoadArchiveSpec;
    String errorSpec;
    String errorLogSpec;
    String bulkLoadErrorSpec;

//-----------------------------------------------------------------------------
}                                                                //AuxFileSpecs


//*****************************************************************************


class KeyValue
{                                                                    //KeyValue
//-----------------------------------------------------------------------------

    String key = "";
    String value = "";

//=============================================================================

    static
    KeyValue split( String string, String separator )
    {
        KeyValue keyVal = new KeyValue();
        List< String > rawParts = string.split( separator, -1 );
        if ( rawParts.size() > 0 )
        {
            keyVal.key = rawParts[ 0 ].trim();
        }
        if ( rawParts.size() > 1 )
        {
            keyVal.value = rawParts[ 1..-1 ].join( separator ).trim();
        }
        return keyVal;
    }

//-----------------------------------------------------------------------------
}                                                                    //KeyValue


//*****************************************************************************
