/*
  SoftFile.groovy

  Functions applicable to various Soft file formats
*/

package org.sagres.importer;

import common.chipInfo.ChipType;
import common.chipInfo.GplChipType;


//*****************************************************************************


class SoftFile
{                                                                    //SoftFile
//-----------------------------------------------------------------------------

    static
    List< String > getValues( String key, String section, String fileSpec )
    {
        try
        {
            List< String > values = [];
            def inputFile = new BufferedReader(
                new FileReader( fileSpec ) );
            int state = 0; //1=in series section, 2=past series section
            int lineNum = 0;
			String line = inputFile.readLine( );
            String keyRegExp = /^/ + key + /\s*=/;
            while ( (line != null) && (state < 2) )
            {
                ++lineNum;
                if ( (state == 0) && line.startsWith( section ) )
                {
                    state = 1;
                }
                else if ( state == 1 )
                {
                    if ( line.startsWith( "^" ) &&
                         (line.startsWith( section ) == false) )
                    {
                        state = 2;
                    }
                    else if ( line =~ keyRegExp )
                    {
                        KeyValue keyVal = KeyValue.split( line, "=" );
                        if ( keyVal.value == "" )
                        {
                            String msg = "No value for " + key +
                                    " on line " + lineNum;
                            throw new ImportException( msg );
                        }
                        values.add( keyVal.value );
                    }
                }

				line = inputFile.readLine( );
            }
            return values;
        }
        catch ( FileNotFoundException exc )
        {
            String msg = "Unable to open " + fileSpec;
            throw new ImportException( msg );
        }
        catch ( IOException exc )
        {
            String msg = "I/O error occurred reading " + fileSpec;
            throw new ImportException( msg );
        }
    }

//-----------------------------------------------------------------------------

    static
    String getValue( String key, String section, String fileSpec )
    {
        List< String > values = getValues( key, section, fileSpec );
        if ( values == null )
        {
            return null;
        }
        else if ( values.size() == 0 )
        {
            throw new ImportException( "No value for " + key );
        }
        else if ( values.size() > 1 )
        {
            throw new ImportException( "Multiple values for " + key +
                                       ": " + values.join( ", " ) );
        }
        return values[ 0 ];
    }

//=============================================================================

    static
    ChipType getChipType( String key, String section, String fileSpec )
    {
        Map chips = getChipTypes( key, section, fileSpec );
        if ( chips.unsupportedGplIds.size() > 0 )
        {
            throw new ImportException( "Unsupported GPL platform:" +
                                       chips.unsupportedGplIds[ 0 ] );
        }
        if ( chips.chipTypes.size() == 0 )
        {
            throw new ImportException( "No GPL value found" );
        }
        if ( chips.chipTypes.size() > 1 )
        {
            throw new ImportException( "More than one GPL platform found." +
                                       "Use multi-platform importer" );
        }
        return chips.chipTypes[ 0 ];
    }

//-----------------------------------------------------------------------------

    static
    Map getChipTypes( String key, String section, String fileSpec )
    {
        List< String > gplIds = SoftFile.getValues( key, section, fileSpec );
        List< ChipType > chipTypes = [];
        List< String > unsupportedGplIds = [];
        gplIds.each { id ->
            GplChipType gplChipType = GplChipType.findByGplId( id );
            if ( gplChipType == null )
            {
                unsupportedGplIds.add( id );
            }
            else
            {
                chipTypes.add( gplChipType.chipType );
            }
        }
        chipTypes.sort { it.name() }
        return [
            chipTypes: chipTypes,
            unsupportedGplIds: unsupportedGplIds
        ];
    }

//-----------------------------------------------------------------------------
}                                                                    //SoftFile


//*****************************************************************************
