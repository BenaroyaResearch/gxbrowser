DM3 Virtual Machine Setup
---

1) Environment variables are used to control the configuration of the web application (dm3). This file is located at:

	/usr/local/tomcat/bin/dm3.envvar.sh
	
Three features will need to be configured, the internal hostname, the full length url:

	export DM3_IMPORTER_SERVER=<short_host_name> #thatvm
	export DM3_MAT_URL=http://<short_host_name>/MAT
	export DM3_SERVER_URL=<full_URL_address>  # e.g. "https://www.thatvm.org/dm3/landing.gsp"

... and the e-mail configuration:

	export DM3_IMPORTER_EMAIL_FROM=“<from_addr>”
	export DM3_IMPORTER_EMAIL_TO=“<to_addr for errors, etc>”
	export DM3_MAIL_IP=<mail_server_ipaddr>
	
2) If for some reason you change the database passwords, those live in:

	/usr/local/tomcat/lib/dm3-config.govvy
	
The dataSource username and password are for MySQL/MariaDB.  while the mongodb username and password are for mongo.

DM3 App. Maintenance
---

If you recieve a new dm3.war file, we recommend installing it by the following:

	% cd /usr/local/tomcat/webapps
	% cp dm3.war /var/tmp #backup, until upgrade succeeds
	% sudo rm -fr dm3.war dm3 #remove the app and the dir.
	% sudo cp ~/dm3-<version>.war dm3.war
	
Tomcat should re-deploy the webapp, but if it doesn't restart tomcat with:

	% sudo service tomcat stop
	% ps -ef | grep tomcat #confirm tomcat has stopped
	% sudo service tomcat start











