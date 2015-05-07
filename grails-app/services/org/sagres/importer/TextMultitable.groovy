/*
  TextMultitable.groovy

  Functions for multiple tables in a text file 
  or for multiple text files with one table each
*/

package org.sagres.importer;


//*****************************************************************************


class TextMultitable
{                                                              //TextMultitable
//-----------------------------------------------------------------------------

    static
    void process( String fileSpec, List< TextTableInfo > tableInfoList,
                  Closure processRow,
                  Closure beforeTables = { }, Closure afterTables = { },
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

        tableInfoList.sort { a, b -> a.firstRow <=> b.firstRow }

        try
        {
            beforeTables( );
            int lineNum = 0;
            
            tableInfoList.each{ tableInfo ->
                while ( lineNum < tableInfo.firstRow )
                {
                    reader.readLine( );
                    ++lineNum;
                }

                beforeTable( tableInfo );

                tableInfo.numRows.times {
                    String row = reader.readLine( );
                    ++lineNum;
                    String[] fields =
                            TextTable.splitRow( row, tableInfo.separator );
                    if ( tableInfo.skipRow( row, fields ) == false )
                    {
                        processRow( tableInfo, fields, lineNum );
                    }
                }

				afterTable( tableInfo );
            }
            afterTables( );
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
    
    static
    void process( List< String > fileSpecs, List< TextTableInfo > tableInfoList,
                  Closure processRow,
                  Closure beforeTables = { }, Closure afterTables = { },
                  Closure beforeTable = { }, Closure afterTable = { } )
    {
		assert( fileSpecs.size() == tableInfoList.size() );

		beforeTables( );

		for ( int i = 0; i < fileSpecs.size(); ++i )
		{
			String fileSpec = fileSpecs[ i ];
			TextTableInfo tableInfo = tableInfoList[ i ];

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
				int lineNum = 0;

				while ( lineNum < tableInfo.firstRow )
				{
					reader.readLine( );
					++lineNum;
				}

				beforeTable( tableInfo );

				tableInfo.numRows.times {
					String row = reader.readLine( );
					++lineNum;
					String[] fields =
							TextTable.splitRow( row, tableInfo.separator );
					if ( tableInfo.skipRow( row, fields ) == false )
					{
						processRow( tableInfo, fields, lineNum );
					}
				}

				afterTable( tableInfo );
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

		afterTables( );
    }
    
//-----------------------------------------------------------------------------
}                                                              //TextMultitable


//*****************************************************************************


abstract
class FileBuilderFromTextMultitable
{                                               //FileBuilderFromTextMultitable
//-----------------------------------------------------------------------------

    FileBuilderFromTextMultitable( String outFileSpec )
    {
        m_outFileSpec = outFileSpec;
    }

//=============================================================================

    void beforeTables( )
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
    void processRow( TextTableInfo tableInfo, String[] fields, int lineNum );
    
//=============================================================================

    void afterTables( )
    {
        m_writer.flush( );
        m_writer.close( );
    }

//=============================================================================

    protected String m_outFileSpec;
    protected PrintWriter m_writer;

//-----------------------------------------------------------------------------
}                                               //FileBuilderFromTextMultitable


//*****************************************************************************
