/*
  TextTable.groovy

  Structs and functions for a table in a text file
*/

package org.sagres.importer;


//*****************************************************************************


enum TextTableSeparator
{
    TAB,    //simply tabs ('\t') between fields
    SPACE,  //simply spaces (' ') between fields
    CSV,    //comma-separated-value format, with optional quotation marks, etc.
    TSV     //tab-separated-value format, with optional quotation marks, etc.
};

//=============================================================================

class TextTableInfo
{                                                               //TextTableInfo
//-----------------------------------------------------------------------------

    TextTableSeparator separator;
    int firstRow; //0-based
    int numRows;
    int numColumns;
    Closure skipRow = { row, fields -> return false; }
    
//-----------------------------------------------------------------------------
}                                                               //TextTableInfo


//*****************************************************************************


class TextTable
{                                                                   //TextTable
//-----------------------------------------------------------------------------

    static
    String[] splitRow( String row, TextTableSeparator separator )
    {
        switch ( separator )
        {
        case TextTableSeparator.TAB:
            return row.split( '\t', -1 );
        case TextTableSeparator.SPACE:
            return row.split( ' ', -1 );
        case TextTableSeparator.CSV:
            return splitCsvRow( row, ',' );
        case TextTableSeparator.TSV:
            return splitCsvRow( row, '\t' );
        }
    }

//-----------------------------------------------------------------------------

	static 
    String[] splitCsvRow( String row, String delimiter = ',' )
	{
		List< String > parts = [];
		String curPart = "";
		boolean inQuotes = false;
		int i = 0;
		while ( i < row.size() )
		{
			if ( row[ i ] == '"' )
			{
				if ( inQuotes && (i < row.size() - 1) && (row[ i + 1 ] == '"') )
				{
					//doubled quotes: keep the second
					++i;
					curPart += row[ i ];
					++i;
				}
				else
				{
					inQuotes = ! inQuotes;
                    curPart += '"';
					++i;
				}
			}
			else if ( (row[ i ] == delimiter) && (inQuotes == false) )
			{
				parts.add( curPart );
				curPart = "";
				++i;
			}
			else
			{
				curPart += row[ i ];
				++i;
			}
		}
		parts.add( curPart );
        for ( i = 0; i < parts.size(); ++i )
        {
            String part = parts[ i ].trim();
            if ( (part.size() > 0) && (part[ 0 ] == '"') )
                part = part.substring( 1 );
            if ( (part.size() > 0) && (part[ -1 ] == '"') )
                part = part.substring( 0, part.size() - 1 );
            parts[ i ] = part;
        }
		return parts.toArray();
	}

//=============================================================================

    static
    String joinRow( List< String > parts, TextTableSeparator separator,
                    boolean quoteAll = false )
    {
        switch ( separator )
        {
        case TextTableSeparator.TAB:
            return parts.join( '\t' );
        case TextTableSeparator.SPACE:
            return parts.join( ' ' );
        case TextTableSeparator.CSV:
            return joinCsvRow( parts, ',', quoteAll );
        case TextTableSeparator.TSV:
            return joinCsvRow( parts, '\t', quoteAll );
        }
    }

//-----------------------------------------------------------------------------

    static
    String joinCsvRow( List parts, String delimiter,
                       boolean quoteAll = true, String nullRep = '' )
    {
        List< String > quotedParts = parts.collect { part ->
			if ( part == null )
			{
				part = nullRep;
			}
			else if ( part instanceof String )
			{
				if ( quoteAll || part.contains( delimiter ) )
				{
					part = part.toString().replaceAll( '"', '""' );
					part = '"' + part + '"';
				}
            }
			else
			{
				part = part.toString();
			}
            return part;
        }
        return quotedParts.join( delimiter );
    }
    
//=============================================================================

    static
    void process( String fileSpec, TextTableInfo tableInfo,
                  Closure processRow,
                  Closure beforeTable = { }, Closure afterTable = { } )
    {
        BufferedReader reader;
        try
        {
            reader = new BufferedReader( new FileReader( fileSpec ) );
        }
        catch ( FileNotFoundException exc )
        {
            throw new ImportException( "Unable to open " + fileSpec +
                                       "\n" + exc.message );
        }

        try
        {
            tableInfo.firstRow.times { reader.readLine( ) };
            int lineNum = tableInfo.firstRow;

            beforeTable( );

            tableInfo.numRows.times {
                String row = reader.readLine( );
                ++lineNum;
                String[] fields = splitRow( row, tableInfo.separator );
                if ( tableInfo.skipRow( row, fields ) == false )
                {
                    processRow( fields, lineNum );
                }
            }

            afterTable( );
        }
        catch ( IOException exc )
        {
            throw new ImportException( "I/O error reading " + fileSpec +
                                       "\n" + exc.message );
        }
        finally
        {
            reader.close( );
        }
    }
    
//-----------------------------------------------------------------------------
}                                                                   //TextTable


//*****************************************************************************


abstract
class FileBuilderFromTextTable
{                                                    //FileBuilderFromTextTable
//-----------------------------------------------------------------------------

    FileBuilderFromTextTable( String outFileSpec )
    {
        m_outFileSpec = outFileSpec;
    }

//=============================================================================

    void beforeTable( )
    {
        try
        {
            m_writer = new PrintWriter( m_outFileSpec );
        }
        catch ( FileNotFoundException exc )
        {
            throw new ImportException( "Unable to create " + m_outFileSpec +
                                       "\n" + exc.message );
        }
    }

//=============================================================================

    abstract
    void processRow( String[] fields, int lineNum );
    
//=============================================================================

    void afterTable( )
    {
        m_writer.flush( );
        m_writer.close( );
    }

//=============================================================================

    protected String m_outFileSpec;
    protected PrintWriter m_writer;

//-----------------------------------------------------------------------------
}                                                    //FileBuilderFromTextTable


//*****************************************************************************
