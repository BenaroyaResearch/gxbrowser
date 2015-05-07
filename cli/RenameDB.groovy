#!/usr/bin/groovy

import groovy.sql.Sql;
import java.sql.SQLException;

//*****************************************************************************

String defaultDbHost = "localhost";
String defaultDbUser = "browser";

def cli = new CliBuilder( usage: "RenameDB.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.h( longOpt: "host",
	   required: (! defaultDbHost),
	   args: 1,
	   argName: "host",
       "Database host [default: ${defaultDbHost}]" );
cli.a( longOpt: "admin",
	   required: true,
	   args: 1,
	   argName: "adminName",
       "Database admin name" );
cli.p( longOpt: "password",
	   required: true,
	   args: 1,
	   argName: "password",
       "Database admin password" );
cli.o( longOpt: "old_db",
	   required: true,
	   args: 1,
	   argName: "OldDbName",
       "Old database name" );
cli.n( longOpt: "new_db",
	   required: true,
	   args: 1,
	   argName: "NewDbName",
       "New database name" );
cli.u( longOpt: "user",
	   required: false,
	   args: 1,
	   argName: "User",
       "Database user name for GRANT (optional)" );
cli.s( longOpt: "userHost",
	   required: false,
	   args: 1,
	   argName: "UserHost",
       "Database user host for GRANT (optional)" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}

String dbHost = clArgs.h ?: defaultDbHost;
String dbAdmin = clArgs.a;
String dbPassword = clArgs.p;
String oldDbName = clArgs.o;
String newDbName = clArgs.n;
String dbUser = clArgs.u ?: defaultDbUser;
String dbUserHost = clArgs.s;

def sql =
       Sql.newInstance( "jdbc:mysql://${dbHost}/${oldDbName}?useServerPrepStmts=false",
                        dbAdmin, dbPassword,
                        "com.mysql.jdbc.Driver");
if ( sql == null )
{
    println( "Unable to get Sql connection with data source ${dataSource}" );
    return;
}

String query =
        "SELECT CONCAT( 'RENAME TABLE `${oldDbName}`.`', table_name," +
        "'` TO `${newDbName}`.`', table_name, '`;' ) AS stmt" +
        "  FROM information_schema.tables WHERE table_schema = '${oldDbName}';";
List tableRenameStmts = sql.rows( query )*.stmt;

String command = "CREATE DATABASE `${newDbName}`;";
println( command );
sql.execute( command );

tableRenameStmts.each { stmt ->
    println( stmt );
    sql.execute( stmt );
}

command = "DROP DATABASE `${oldDbName}`;";
println( command );
sql.execute( command );

if ( dbUser )
{
    command = "GRANT ALL PRIVILEGES ON `${newDbName}`.* TO `${dbUser}`";
    if ( dbUserHost )
        command += "@`${dbUserHost}`";
    command += ";";
    println( command );
    sql.execute( command );
}