## GXBrowser

Benaroya Research Institute Gene Expression Browser (GXB) for integrating Microarray, RNA-seq data, expression data with demographic and clinical information.

### Minimum Required Software

1. Apache Tomcat 6.0.24
2. Java 1.7.0_03
3. Apache http server 2.2
4. R 2.15.1 (R 3.1 suggested)
5. Mysql 5.1.41 (MySQL 5.5 suggested)
6. Mongo 2.02 (Mongo 2.4 suggested)
7. Grails 2.1.0

#### Software Installation Instructions

1. Start with a clean Ubuntu Installation (or VM)
2. Verify Java 7 installation (apt-get install openjdk-7-jre-headless; java -version)
3. Verify Apache2 installation (apt-get install apache2)
4. Enable apache/tomcat tunneling (apt-get install libapache2-mod-jk)
5. Install Tomcat (apt-get install tomcat6)
6. Install R (see http://cran.r-project.org/bin/linux/ubuntu/README; apt-get install r-base)
7. Grant permissions to write to R library (chmod -R a+rwx /usr/lib/R)
8. Update Tomcat startup params (/etc/default/tomcat6)
9. Setup mod-jk to allow /dm3 to pass through from apache (/etc/apache2/mods-available/jk.conf)
		
		jkMount /dm3/ GXB
		jkMount /dm3/* GXB
		jkMount /dm3* GXB
		
10. Install mongo (http://docs.mongodb.org/manual/tutorial/install-mongodb-on-debian-or-ubuntu-linux/)
11. Create mongo user, password and datbase for application
12. Install MySQL (apt-get install mysql-server)
13. Create mysql user, password, and database (recommend 'dm3') for application, grant db permissions for that user to dm3
14. Create 'ben\_tg2' database (must be named 'ben\_tg2') for application, grant db permissions for same user to ben\_tg2
15. Load starter databases (see http://gxb.benaroyaresearch.org/downloads/dm3_core.sql and .../ben_tg2_core.sql)
16. Install Grails 2.1.0 (https://grails.org/download.html)



#### Filesystem Setup

1. Create MAT,ranklist, and scripts directories under apache htdocs:

		mkdir {Apache HTDocs} (often /var/www)
		mkdir {Apache HTDocs}/MAT
		mkdir {Apache HTDocs}/ranklist
		mkdir {Apache HTDocs}/scripts/ModuleAnalysis

2. Copy R scripts from repository into scripts/ModuleAnalysis directory.
3. Create file storage directory:

		mkdir /var/local/dm		

#### Building the Application

The approximate steps for building the application are:

		mkdir build-tmp
		cd build-tmp
		export JAVA_HOME={your Java 7 home}
		git {clone|fetch|fork} {repository} dm
		cd dm
		git rev-list HEAD --count | sed 's/$/-stable-gxb1-release/' >> grails-app/views/builddate.gsp
		export GRAILS_HOME={/opt/local/}grails-2.1.0
		export PATH=$GRAILS_HOME/bin:$PATH
		grails war
		
Copy target/dm3-1.70.war as dm3.war to the webapps directory of tomcat.

#### Environment

The following should be placed in either setenv.sh for tomcat (catalina.sh), or read in by setenv.sh from some other file.  The goal is to have them in the runtime environment of the tomcat process.

		export DM3_IMPORTER_SERVER={hostname}
		export DM3_LOG_DIR=/usr/local/tomcat/logs
		export DM3_SERVER_URL="http://{hostname}/dm3"
		export DM3_EXPRESSION_SERVICE=http://localhost:8080/dm3/sampleSet/makeArrayDataDetailTSVFile
		export DM3_MAT_R_EXECUTABLE=/usr/bin/Rscript
		export DM3_FILE_BASEDIR=/var/local/dm/
		export DM3_MAT_WORK_DIR=/var/www/MAT
		export DM3MYSQLDB={MySQL database name}
		export DM3MONGODB={Mongo database name}


#### Passwords File

Create a file named dm3-config.groovy containing the usernames and passwords for database access.  Example:

		println "credentials loaded from classpath"
		dataSource.username={MySQL username}
		dataSource.password={MySQL password}
		mongodb.username={Mongo username}
		mongodb.password={Mongo password}
		
Place this file on the classpath for tomcat and the web applications, we use {Tomcat6 HOME}/lib.

#### Checklist:

1. All services installed, and configured appropriately.
2. MySQL and Mongo databases created, usernames and passwords setup.
3. setenv.sh or indirect file of environment data created and placed where tomcat catalina.sh can find it.
4. dm3-config.groovy containing database credentials created and placed in $CLASSPATH.
5. dm3 application built from source
6. dm3.war placed in {Tomcat Home}/webapps

Finally, start apache, then start tomcat.  Give tomcat some time to start, then use your favorite browser to go to http://{hostname}/dm3.  If no joy, peruse catalina.log in the Tomcat log directory for clues to the startup issues.

##### Version 1.2

