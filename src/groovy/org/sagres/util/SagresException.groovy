/*
  SagresException.groovy

  Exception class for importing
*/

package org.sagres.util;


//*****************************************************************************


class SagresException
extends Exception
{                                                             //SagresException
//-----------------------------------------------------------------------------

    SagresException( String message )
    {
        super( message );
        this.messages = [ message ];
    }

//.............................................................................

    SagresException( def messages )
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
    
    List< String > messages;

//-----------------------------------------------------------------------------
}                                                             //SagresException


//*****************************************************************************
