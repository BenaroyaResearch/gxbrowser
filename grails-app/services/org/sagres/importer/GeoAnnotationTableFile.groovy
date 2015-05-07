/*
  GeoAnnotationTableFile.groovy

  Routines for importing Soft GEO platform (GPL) annotation files.
*/

package org.sagres.importer;

import common.chipInfo.ChipType;
import common.chipInfo.GplChipType;


//*****************************************************************************


class GeoAnnotationTableFile
{                                                      //GeoAnnotationTableFile
//-----------------------------------------------------------------------------

    static
    ChipType getChipType( String fileSpec )
    {
        FileSpecParts specParts = Importer.parseFileSpec( fileSpec );
        String[] nameParts = specParts.name.split( '-' );
        String gplId = nameParts[ 0 ];
        GplChipType gplChipType = GplChipType.findByGplId( gplId );
        if ( gplChipType == null )
        {
            throw new ImportException( "Unlisted platform: " + gplId );
        }
        return gplChipType.chipType;
    }

//-----------------------------------------------------------------------------
}                                                      //GeoAnnotationTableFile


//*****************************************************************************
