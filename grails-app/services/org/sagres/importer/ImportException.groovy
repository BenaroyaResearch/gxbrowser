/*
  ImportException.groovy

  Exception class for importing
*/

package org.sagres.importer;


//*****************************************************************************


class ImportException
extends Exception
{                                                             //ImportException
//-----------------------------------------------------------------------------

    ImportException( String message )
    {
        super( message );
        this.messages = [ message ];
    }

//.............................................................................

    ImportException( def messages )
    {
        super( messages?.size() > 0  ?  messages[ 0 ]  :  "" );
        this.messages = [];
        for ( String message : messages )
        {
            this.messages.add( message );
        }
    }

//=============================================================================

    String getMessage( int maxLength = 0, int maxLines = 0 )
    {
        return Importer.combineMessages( messages, maxLength, maxLines );
    }

//=============================================================================
    
    List< String > messages;

//-----------------------------------------------------------------------------
}                                                             //ImportException


//*****************************************************************************
