/*
  AnnotationImporter.groovy

  Routines for importing a chip (or other platform) annotation file
*/

package org.sagres.importer;

import groovy.sql.Sql;
import java.sql.SQLException;


//*****************************************************************************


class AnnotationColumnInfo
{                                                        //AnnotationColumnInfo
//-----------------------------------------------------------------------------

    String name;
    int maxLength;

//-----------------------------------------------------------------------------
}                                                        //AnnotationColumnInfo


//*****************************************************************************


class AnnotationImporter
{                                                          //AnnotationImporter
//-----------------------------------------------------------------------------

    static 
    void createAnnotationDbTable( String tableName,
                                  List< AnnotationColumnInfo > columns,
                                  Sql sql )
    {
        String command;
        try
        {
            command = "DROP TABLE IF EXISTS " + tableName;
            sql.execute( command );

            command = "CREATE TABLE " + tableName +
                    " ( " +
                    "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT";
            columns.each { col ->
                String colType = getColType( col.maxLength );
                command += ", `" + col.name + "` " + colType;
            }
            command += " )";
            sql.execute( command );
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message
            throw new ImportException( msg );
        }
    }

//.............................................................................

    private
    static
    String getColType( int maxLength )
    {
        if ( maxLength < 250 )
        {
            return "VARCHAR( 255 )";
        }
        else if ( maxLength < 5000 )
        {
            int colsize = 500 * (((maxLength + 600) / 500) as int);
            return "VARCHAR( $colsize )";
        }
        else if ( maxLength < 65000 )
        {
            return "TEXT";
        }
        else //<16 MB, I hope!
        {
            return "MEDIUMTEXT";
        }
    }

//-----------------------------------------------------------------------------
}                                                          //AnnotationImporter


//*****************************************************************************
