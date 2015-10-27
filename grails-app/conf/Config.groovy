 import grails.plugins.springsecurity.SecurityConfigType

//Settings that can be controlled with environment variables should be listed
// here (at the top) for visibility.
 
 
 // locations to search for config files that get merged into the main config
 // config files can either be Java properties files or ConfigSlurper scripts
 
 // grails.config.locations = [ "classpath:${appName}-config.properties",
 //                             "classpath:${appName}-config.groovy",
 //                             "file:${userHome}/.grails/${appName}-config.properties",
 //                             "file:${userHome}/.grails/${appName}-config.groovy"]
 
 // if(System.properties["${appName}.config.location"]) {
 //    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
 // }
 
 
 // Lastly, ready in configuration files via classpath current directory, or System.properties()/
 grails.config.locations = ["classpath:${appName}-config.groovy", "file:./${appName}-config.groovy"]
 if (System.properties["${appName}.config.location"]) {
	 grails.config.locations << "file:" + System.properties["${appName}.config.location"]
 }
 
 println "(*) grails.config.locations = ${grails.config.locations}"
 
dm3.status.states = [
	0 : "OK",
	1 : "Error",
	2 : "Warning",
	3 : "Notice"
]

dm3.status.class = [
	0 : "status-ok",
	1 : "status-error",
	2 : "status-warning",
	3 : "status-notice"
]

environments {
	production {
		println "production environment..."
	}
	development {
		println "development environment..."
	}
	test {
		println "test environment..."
	}
}

uAppName = appName.toUpperCase()
dm3.envvar.brand = uAppName + "_BRAND"
dm3.envvar.frontbrand = uAppName + "_FRONT_BRAND"
dm3.envvar.leftbrand = uAppName + "_LEFT_BRAND"
dm3.envvar.rightbrand = uAppName + "_RIGHT_BRAND"
dm3.envvar.favico    = uAppName + "_SITE_FAVICO"
dm3.envvar.landing   = uAppName + "_SITE_LANDING"
dm3.envvar.analyticsAccount = uAppName + "_ANALYTICS_ACCOUNT"
dm3.envvar.analyticsDomain  = uAppName + "_ANALYTICS_DOMAIN"

dm3.envvar.css = uAppName + "_CSS"

dm3.site.branding = System.getenv(dm3.envvar.brand)?:"default_branding.gsp"
println "Branding GSP (${dm3.envvar.brand}): ${dm3.site.branding}"

dm3.front.branding = System.getenv(dm3.envvar.frontbrand)?:"default_branding.gsp"
println "Front Facing Branding GSP (${dm3.envvar.frontbrand}): ${dm3.front.branding}"

dm3.left.branding = System.getenv(dm3.envvar.leftbrand)?:"default_left_branding.gsp"
println "Left Inner Branding GSP (${dm3.envvar.leftbrand}): ${dm3.left.branding}"

dm3.right.branding = System.getenv(dm3.envvar.rightbrand)?:"default_right_branding.gsp"
println "Right Inner Branding GSP (${dm3.envvar.rightbrand}): ${dm3.right.branding}"

dm3.site.favico = System.getenv(dm3.envvar.favico)?:"default_favico.gsp"
println "Favorites Icon GSP (${dm3.envvar.favico}): ${dm3.site.favico}"

dm3.site.landing = System.getenv(dm3.envvar.landing)?:"/landing.gsp"
println "Landing Page (${dm3.envvar.landing}): ${dm3.site.landing}"

//'UA-27217617-1'
dm3.analytics.account = System.getenv(dm3.envvar.analyticsAccount) ?: ""
println "Google Analytics Account (${dm3.envvar.analyticsAccount}): ${dm3.analytics.account}"

dm3.analytics.domain = System.getenv(dm3.envvar.analyticsDomain) ?: ""
println "Google Analytics Domain (${dm3.envvar.analyticsDomain}): ${dm3.analytics.domain}"


dm3.site.css = System.getenv(dm3.envvar.css) ?:"empty.css"
println "Site CSS (${dm3.envvar.css}): ${dm3.site.css}"

dm3.labkey.protocol=System.getenv("DM3_LABKEY_PROTOCOL")?:"https"
println "Labkey Protocol : " + dm3.labkey.protocol
 if (!System.getenv("DM3_LABKEY_PROTOCOL")) {
     System.getenv().each { key, val ->
         println "${key}: ${val}"
     }
 }
dm3.sampleset.editrole=System.getenv("SAMPLE_SET_EDIT_ROLE")?:"ROLE_USER"
println "Role required to edit SampleSet Information (SAMPLE_SET_EDIT_ROLE):" + dm3.sampleset.editrole
dm3.labkey.port=System.getenv("DM3_LABKEY_PORT")?:"443"
dm3.labkey.authentication.host =  System.getenv( "DM3_LABKEY_URL" )?:""
dm3.labkey.authentication.url = null
dm3.authenticate.labkey = System.getenv("DM3_AUTHENTICATE_VIA_LABKEY")? true:false
println "DM3_AUTHENTICATE_VIA_LABKEY: ${dm3.authenticate.labkey}"
if (dm3.labkey.authentication.host != null) {
	dm3.labkey.authentication.url = dm3.labkey.protocol + "://" + dm3.labkey.authentication.host
	matWizardFileTypes =["GEO", "Illumina", "Affymetrix"]
}   else {
	matWizardFileTypes =["GEO", "Illumina", "Affymetrix", "ABI", "Agilent"]

}
println "${dm3.authenticate.labkey?'Using':'Not Using'} labkeyURL (DM3_LABKEY_URL):${dm3.labkey.authentication.url}"
println "MATWizard supported file types: ${matWizardFileTypes}"

//Directory to put log files into
dm3.envvar.logDir  = uAppName + "_LOG_DIR"
String logDir = System.getenv(dm3.envvar.logDir) ?:".";
println "Log Directory (${dm3.envvar.logDir}): ${logDir}"

//30 minutes = 1000 * 30 * 60 = 1800000
//25 minutes = 1000 * 25 * 60 =  1500000

dm3.envvar.tokenCheck     = uAppName + "_TOKEN_CHECK"
dm3.envvar.sessionRefresh = uAppName + "_SESSION_REFRESH"
dm3.session.tokenCheckMS=System.getenv(dm3.envvar.tokenCheck) ?:1800000
dm3.session.sessionRefreshMS=System.getenv(dm3.envvar.sessionRefresh)?:  1500000
println "LabKey Authentication Session token Checks every ms (${dm3.envvar.tokenCheck}): ${dm3.session.tokenCheckMS}"
println "LabKey Authentication Session Refresh every ms (${dm3.envvar.sessionRefresh}): ${dm3.session.sessionRefreshMS}"

//dm3.sessionTimeoutMS = 1800000 //30 minutes


//In addition to DM3_LABKEY_URL there is "DM3_MAIL_IP" - if you want to override the default mail IP
dm3.authenticationTarget = (dm3.authenticate.labkey)?"sampleSet/":"j_spring_security_check"

// Output file locations:
// The server needs to have write access to the subdirectories of these.
// The default for this is generally "/var/local/dm/":
dm3.envvar.basedir = uAppName + "_FILE_BASEDIR"
String fileBaseDir = System.getenv(dm3.envvar.basedir) ?: "/var/local/dm/"
println "Base Directory (${dm3.envvar.basedir}) ${fileBaseDir}"

dm3.envvar.NCBIFilesBase = uAppName + "_NCBIFILES_BASEDIR"
// The default for this is generally "/usr/local/tomcat/webapps/upload_data/ncbiFiles":
ncbiFiles.baseDir = System.getenv(dm3.envvar.NCBIFilesBase) ?: "/usr/local/tomcat/webapps/upload_data/ncbiFiles"
println "NCBI Base Directory (${dm3.envvar.NCBIFilesBase}) ${ncbiFiles.BaseDir}"

// Setting this will cause e-mail to be sent when files are imported.
// Should be sent on production servers (and for testing):
dm3.envvar.importServer    = uAppName + "_IMPORTER_SERVER"
dm3.envvar.importEmailTo   = uAppName + "_IMPORTER_EMAIL_TO"
dm3.envvar.importEmailFrom = uAppName + "_IMPORTER_EMAIL_FROM"
dm3.envvar.defaultGenomicSource = uAppName + "_DEFAULT_GENOMICSOURCE"

importer.serverName = System.getenv(dm3.envvar.importServer);
println "Import Server Name (${dm3.envvar.importServer}) ${importer.serverName}"
// File import notifications go to:
importer.defaultEMailTo = System.getenv(dm3.envvar.importEmailTo) ?:
        "admin@mailhost.domain.org";
println "Import E-Mail To   (${dm3.envvar.importEmailTo}) ${importer.defaultEMailTo}"
// File import notifications come from:
importer.defaultEMailFrom = System.getenv(dm3.envvar.importEmailFrom) ?:
        "Bioinformatics Team <bioinfteam@mailhost.domain.org>";
println "Import E-Mail From (${dm3.envvar.importEmailFrom}) ${importer.defaultEMailFrom}"

importer.defaultGenomicSource = System.getenv(dm3.envvar.defaultGenomicSource) ?: "Other"
println "Default Genomic Source (${dm3.envvar.defaultGenomicSource}): ${importer.defaultGenomicSource}"

genomicSource.tg2Datatypes = ["Benaroya", "Baylor"]

// We want to control the features by Audience, do that here via the environment var <appName>_AUDIENCE
// Valid values should be 'All', 'ITN', 'BRI', 'BIIR', 'HIPC', 'GXB', 'IFIG' but this is not enforced.

dm3.envvar.audience      = uAppName + "_AUDIENCE"
dm3.envvar.areaLogin     = uAppName + "_AREA_LOGIN"
dm3.envvar.menuLogin     = uAppName + "_MENU_LOGIN"
dm3.envvar.email         = uAppName + "_EMAIL_ENABLED"
dm3.envvar.gplus         = uAppName + "_GOOGLEPLUS_ENABLED"
dm3.envvar.wiki          = uAppName + "_MODULEWIKI_ENABLED"
dm3.envvar.matAccess     = uAppName + "_MATACCESS_ENABLED"
dm3.envvar.matGXB        = uAppName + "_MAT_GXB_ENABLED"
dm3.envvar.matTop 		 = uAppName + "_MAT_TOP_ONLY"
dm3.envvar.matPlots		 = uAppName + "_MAT_PLOT_TYPES"
dm3.envvar.tools		 = uAppName + "_TOOLS_ENABLED"

// for Vaccine paper
// DM3_MATACCESS_ENABLED - not currently used.
// DM3_MAT_GXB_ENABLED = false
// DM3_MAT_TOP_ONLY = true
// DM3_MAT_PLOT_TYPES='group'

println "UI Features:"
String targetAudience	= System.getenv(dm3.envvar.audience) ?: (dm3.authenticate.labkey ? "ITN" : "ALL")
println "Target Audience  (${dm3.envvar.audience}): ${targetAudience}"
areaLogin	    = System.getenv(dm3.envvar.areaLogin) ? System.getenv(dm3.envvar.areaLogin).toBoolean() : true
println "Area Login       (${dm3.envvar.areaLogin}): ${areaLogin}"
menuLogin	    = System.getenv(dm3.envvar.menuLogin) ? System.getenv(dm3.envvar.menuLogin).toBoolean() : false
println "Menu Login       (${dm3.envvar.menuLogin}): ${menuLogin}"
send.email.on			= System.getenv(dm3.envvar.email) ? System.getenv( dm3.envvar.email ).toBoolean() : true
println "EMail Enabled    (${dm3.envvar.email}): ${send.email.on}"
tools.available			= System.getenv(dm3.envvar.tools) ? System.getenv( dm3.envvar.tools ) : "ssat,gxb" // ssat, gxb, mat, metacat, project, wizard, rb
println "Tools Available  (${dm3.envvar.tools}): ${tools.available}"
googleplus.on			= System.getenv(dm3.envvar.gplus) ? System.getenv( dm3.envvar.gplus ).toBoolean() : false
println "Google Plus      (${dm3.envvar.gplus}): ${googleplus.on}"
module.wiki.on			= System.getenv(dm3.envvar.wiki) ? System.getenv( dm3.envvar.wiki ).toBoolean() : true
println "Module Wiki      (${dm3.envvar.wiki}): ${module.wiki.on}"
mat.access.on			= System.getenv(dm3.envvar.matAccess) ? System.getenv( dm3.envvar.matAccess ).toBoolean() : true
println "MAT Acccess      (${dm3.envvar.matAccess}): ${mat.access.on}"
mat.access.gxb			= System.getenv(dm3.envvar.matGXB) ? System.getenv( dm3.envvar.matGXB ).toBoolean() : true  // Whether one can go into GXB from MAT spot tooltips
println "MAT GXB Acccess  (${dm3.envvar.matAccess}): ${mat.access.gxb}"
mat.top.only		    = System.getenv(dm3.envvar.matTop) ? System.getenv( dm3.envvar.matTop ).toBoolean() : false  // Whether one can see more than the gen2 top modules 
println "MAT Top Only     (${dm3.envvar.matTop}): ${mat.top.only}"
mat.plot.types		    = System.getenv(dm3.envvar.matPlots) ? System.getenv( dm3.envvar.matPlots ) : "group,individual,gsa" // any plots named: group, individual, gsa
println "MAT Plot Types   (${dm3.envvar.matPlots}): ${mat.plot.types}"

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

dm3.envvar.matWorkDir = uAppName + "_MAT_WORK_DIR"
dm3.envvar.matURL     = uAppName + "_MAT_URL"
dm3.envvar.matRExec   = uAppName + "_MAT_R_EXECUTABLE"

mat.workDir = System.getenv(dm3.envvar.matWorkDir) ?: "/usr/local/apache2/htdocs/MAT/"
println "MAT Working Dir  (${dm3.envvar.matWorkDir}) ${mat.workDir}"

mat.file.url.prepender = System.getenv(dm3.envvar.matURL) ?: "http://localhost/MAT"
println "MAT Base URL     (${dm3.envvar.matURL}) ${mat.file.url.prepender}"

mat.R.executable = System.getenv(dm3.envvar.matRExec) ?: "/usr/local/bin/Rscript"
println "MAT R Executable (${dm3.envvar.matRExec}) ${mat.R.executable}"

mat.analysis.complete.string = "Run complete"

mat.dm3host= "jdbc:mysql://srvdm:3306/dm3?useServerPrepStmts=false"
mat.dm3user= ""
mat.dm3pwd= ""
mat.dm3refreshseconds= "300"

dm3.envvar.eservice      = uAppName + "_EXPRESSION_SERVICE"
mat.dm3expressionfileservice= System.getenv(dm3.envvar.eservice) ?: "http://localhost/${appName}/sampleSet/makeArrayDataDetailTSVFile"
println "ExpressionFileService (${dm3.envvar.eservice}): ${mat.dm3expressionfileservice}"


//5 is the id for ITN
dm3.envvar.loginrequired = uAppName + "_DS_TYPES_REQUIRING_LOGIN"
needs_login= System.getenv(dm3.envvar.loginrequired)
genomic_datasource_names_requiring_login = [:]
dm3.labkeyTabs = ["labkeySubject":"Subject","labkeyClinical":"Clinical","labkeyLabResults":"Lab Results","labkeyAncillaryData":"Ancillary Data","labkeyFlow":"Flow Data"]
if (needs_login != null) {
	needs_login.toString().split(',').each { genomic_datasource_names_requiring_login.put(it, "1")}
}
println "Genomic Datasources requiring login (${dm3.envvar.loginrequired}): ${genomic_datasource_names_requiring_login}"

// Labkey credentials.
dm3.labkeyUserid = ""
dm3.labkeyCredentials = ""

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

dm3.envvar.mail.host = uAppName + "_MAIL_IP"
grails {
   mail {
	 host =  System.getenv(dm3.envvar.mail.host) ?: "192.168.0.1"
	 port = 25
   }
}
println "Mail Host IP Addr (${dm3.envvar.mail.host}): ${grails.mail.host}"

sampleSets.allSamples.groupName = "All Samples"
sampleSets.allSamples.defaultColor = "#009393"
sampleSets.defaultChipType = 17

dm3.feng.permissions = "ROLE_VIEW_FENG"

// sample set annotation defaults
user.default.name = "Me"
hint.default.text = "click here to enter text"
status.default.text = "In Progress"
studyType.default.text = "Clinical"
sampleSet.overviewComponentsSet.default.name = "Default"
abbreviation.text.maxLength = "100"
sampleInfo.spreadsheet.tag = "Sample Set Spreadsheet"
files.directory = "files"
group.default = "All Samples"
group.color.default = "#359636"
mongo.database = "dm_dev_mongo"
mongo.sample.collection = "sample"
mongo.sampleField.collection = "sampleField"
samples.maxNumDisplay = 10
groups.defaultColor = "#359636"

project.statusLookupName = 'Status'
project.speciesLookupName = 'Species'
project.technologyLookupName = 'Genomic Platform'
project.taskLookupName = 'Task'
project.resourceLookupName = 'Resource'

palx.defaultDetectionPVal = 0.01;
palx.defaultFractionPresent = 0.10;

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
			 rollingFile name: "stacktrace", maxFileSize: 1024, file: logDir + "/dm-stacktrace.log"
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate',
           'grails.app'
           'grails.app.controller'
           'grails.app.service'
           'grails.app.domain'
           'grails.app.taglib'

    warn   'org.mortbay.log'
}

if ( fileBaseDir == null )
{
    environments {
        production {
            fileBaseDir = "/var/local/dm/";
        }
        development {
            fileBaseDir = "/var/local/dm/"
        }
        test {
            fileBaseDir = "/usr/local/tomcat/webapps/upload_data/";
        }
    }
    if ( ! fileBaseDir ) //if all else fails
    {
        fileBaseDir = "/var/local/dm/";
    }
}
if ( fileBaseDir[ -1 ] != '/' )
{
    fileBaseDir += '/';
}
fileUpload.baseDir = fileBaseDir + "fileUploads/";
bgSubstractedFiles.baseDir = fileUpload.baseDir + "expressionData/imported"
fileOutput.baseDir = fileBaseDir + "fileOutput/";
fileSampleSetExport.baseDir = fileBaseDir + "exportedSampleSets/";
focusedArrayFoldChange.baseDir = fileBaseDir + "focusedArrayFoldChange/";

if ( ! ncbiFiles.baseDir )
{
    environments {
        production {
            ncbiFiles.baseDir = "/usr/local/tomcat/webapps/upload_data/ncbiFiles"
        }
        development {
            ncbiFiles.baseDir = "/usr/local/tomcat/webapps/upload_data/ncbiFiles"
        }
        test {
            ncbiFiles.baseDir = "/usr/local/tomcat/webapps/upload_data/ncbiFiles"
        }
    }
    if ( ! ncbiFiles.baseDir ) //if all else fails
    {
        ncbiFiles.baseDir = "/usr/local/tomcat/webapps/upload_data/ncbiFiles"
    }
}

environments {
		production {
			grails.serverURL = "http://srvdm:8080/${appName}"
			grails.statslogging = true
			target.audience = targetAudience
			area.login = areaLogin
			menu.login = menuLogin
		}
		development {
			grails.serverURL = "http://localhost:8080/${appName}"
			grails.statslogging = true
			target.audience = targetAudience
			area.login = areaLogin
			menu.login = menuLogin
		}
		test {
			grails.serverURL = "http://localhost:8080/${appName}"
			grails.statslogging = false
			target.audience = 'All'
			area.login = true
			menu.login = false
		}
}

dm3.envvar.serverurl = uAppName + "_SERVER_URL"

if (System.getenv(dm3.envvar.serverurl) != null) {
	grails.serverURL =  System.getenv(dm3.envvar.serverurl)
}

println "Server URL (${dm3.envvar.serverurl}): ${grails.serverURL}"
println "Default Publishing [0:not published, 1: published] (publish_default): ${System.getenv('publish_default')?:0}"

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'common.SecUser'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'common.SecUserSecRole'
grails.plugins.springsecurity.authority.className = 'common.SecRole'
if (dm3.authenticate.labkey)
{
	grails.plugins.springsecurity.providerNames = ['labkeyAuthenticationProvider',
													'daoAuthenticationProvider',
													'anonymousAuthenticationProvider',
													'rememberMeAuthenticationProvider']
}

grails.plugins.springsecurity.rememberMe.tokenValiditySeconds=14400
rememberMe.tokenValiditySeconds=200
grails.plugins.springsecurity.successHandler.targetUrlParameter = "lastVisitedPage"

grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap
grails.plugins.springsecurity.interceptUrlMap = [
  '/secUser/forgotPassword': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/secUser/resetPasswordError': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/secUser/resetPasswordSuccess': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/secUser/resetPasswordSent': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/secUser/sendPasswordReset': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/secUser/updatePassword': ['ROLE_USER'],
  '/secUser/resetPassword': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/secUser/account': ['ROLE_USER'],
  '/secUser/**': ['ROLE_ADMIN'],
  '/secRole/**': ['ROLE_ADMIN'],
  '/annotation/**': ['ROLE_USER'],
  '/sampleSetAdminInfo/getter/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/sampleSetAdminInfo/**': ['ROLE_USER'],
  '/sampleSetSampleInfo/getter/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/sampleSetSampleInfo/**': ['ROLE_USER'],
  '/sampleSetPlatformInfo/getter/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/sampleSetPlatformInfo/**': ['ROLE_USER'],
  '/sampleSetAnnotation/getter/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/sampleSetAnnotation/**': ['ROLE_USER'],
  '/sampleSetTabConfigurator/**': ['ROLE_USER'],
  '/chipData/edit/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/chipData/delete/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/chipData/**': ['ROLE_USER'],
  '/chipType/edit/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/chipType/delete/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/chipType/**': ['ROLE_USER'],
  '/lookupList/**': ['ROLE_USER'],
  '/lookupListDetail/**': ['ROLE_USER'],
  '/miniUrl/create/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/miniUrl/view/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/miniUrl/**': ['ROLE_USER'],
  '/notes/list': ['ROLE_USER'],
  '/notes/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/filesLoaded/**': ['ROLE_USER'],
  '/rankListParams/**': ['ROLE_USER'],
  '/datasetCollection/**': ['ROLE_USER'],
  '/datasetGroupSet/view/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/datasetGroupSet/**': ['ROLE_USER'],
  '/datasetGroupDetail/**': ['ROLE_USER'],
  '/moduleAnnotation/list': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/moduleAnnotation/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/moduleGeneration/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/module/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/version/index': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/version/list': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/version/show/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
  '/version/**': ['ROLE_ADMIN', 'ROLE_POWERUSER'],
  '/annotation/populateSpreadsheetSettings': ['ROLE_ADMIN']
]

salt = "extraSALTYbaconSALT!"

grails.war.resources = { stagingDir ->
	delete(file:"${stagingDir}/WEB-INF/dm3-config.groovy")
}

