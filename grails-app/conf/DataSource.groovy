//Rather than modify this file, set environment variables to use different
// hosts, etc. 
// The default is localhost with the standard ports. 
// (Note that this will make deployed (production) versions happy by default.)
// The next simplest case is to set DM3HOST (to srvdm, say), which will keep
// your MySQL and Mongo settings in sync.
// But for VPN work or read-only work you can set the ports, set the hosts
// separately, or use different DB names.

uAppName = appName.toUpperCase()

dm3.envvar.dbHost    = uAppName + "HOST"

dm3.envvar.MySQLHost = uAppName + "MYSQLHOST"
dm3.envvar.MySQLPort = uAppName + "MYSQLPORT"
dm3.envvar.MySQLDB   = uAppName + "MYSQLDB"
dm3.envvar.LogSQL    = uAppName + "LOGSQL"

dm3.envvar.MongoHost = uAppName + "MONGOHOST"
dm3.envvar.MongoPort = uAppName + "MONGOPORT"
dm3.envvar.MongoDB   = uAppName + "MONGODB"

String dbHost    = System.getenv(dm3.envvar.dbHost)    ?: "localhost";
String mysqlHost = System.getenv(dm3.envvar.MySQLHost) ?: dbHost;
String mysqlPort = System.getenv(dm3.envvar.MySQLPort) ?: "3306";
String mysqlDb   = System.getenv(dm3.envvar.MySQLDB)   ?: "dm3";

String mongoHost = System.getenv(dm3.envvar.MongoHost) ?: dbHost;
String mongoPortString = System.getenv(dm3.envvar.MongoPort);
int mongoPort    = mongoPortString ? Integer.parseInt( mongoPortString ) : 27017;
String mongoDb   = System.getenv(dm3.envvar.MongoDB) ?: "dm_dev_mongo";
boolean logMySql = System.getenv(dm3.envvar.LogSQL) ? true : false;

println "CoreDB Host (${dm3.envvar.dbHost}): ${dbHost}"
println "MySQL  Host (${dm3.envvar.MySQLHost}): ${mysqlHost}"
println "MySQL  Port (${dm3.envvar.MySQLPort}): ${mysqlPort}"
println "MySQL  Db   (${dm3.envvar.MySQLDB})  : ${mysqlDb}"
println "Mongo  Host (${dm3.envvar.MongoHost}): ${mongoHost}"
println "Mongo  Port (${dm3.envvar.MongoPort}): ${mongoPort}"
println "Mongo  Db   (${dm3.envvar.MongoDB})  : ${mongoDb}"

println "Final Database Configuration:"
println("MySQL: ${mysqlDb} on ${mysqlHost}:${mysqlPort}");
println("Mongo: ${mongoDb} on ${mongoHost}:${mongoPort}");

dataSource {
  pooled = true
  driverClassName = "com.mysql.jdbc.Driver"
  dbCreate = "update"
  url = "jdbc:mysql://${mysqlHost}:${mysqlPort}/${mysqlDb}?useServerPrepStmts=false&zeroDateTimeBehavior=convertToNull"
  username = ""
  password = ""
  database = "${mysqlDb}"
		properties {
              maxActive = 50
              maxIdle = 10
              minIdle = 1
              initialSize = 1
// validate connections
              validationQuery = "SELECT 1"
              testOnBorrow = true
              testOnReturn = false
              testWhileIdle = false
//  evict connections older than 30 min
              timeBetweenEvictionRunsMillis = 1000 * 60 * 30
              numTestsPerEvictionRun = 3
              minEvictableIdleTimeMillis = 1000 * 60 * 30
            }
        logSql = logMySql;
}

mongodb {
  host = mongoHost
  port = mongoPort
  databasename = mongoDb
}

hibernate {
  cache.use_second_level_cache = false
  cache.use_query_cache = false
  cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}

environments {

    development {
        mongodb {
            if ( dbHost.toLowerCase() == "srvdm" )
            {
                username = ""
                password = ""
            }
        }
    }

    production {
        mongodb {
            username = ""
            password = ""
        }
    }
}

