Revision 2225: Database authentication 5/1/13 3:00 PM
---------------------------------------

As of revision 2225, database authentication credentials are not in dataSource.groovy,
but should be placed in ${appName}-config.groovy, e.g. dm3-config.groovy, and placed in
the directory .../tomcat/lib, which is on the application $(CLASSPATH)

The format of the variables is as follows:

dataSource.username="foo"	// for MySQL
dataSource.password="bar"
mongodb.username="foo"		// for Mongodb
mongodb.password="bar"
dm3.labkeyUserid = "foo"	// for ITN/LabKey
dm3.labkeyCredentials = "bar"
