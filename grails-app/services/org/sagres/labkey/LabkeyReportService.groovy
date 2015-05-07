package org.sagres.labkey

import groovy.sql.Sql
import grails.converters.JSON
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.HttpHost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.AuthScope
import org.apache.http.client.*
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.client.protocol.ClientContext
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.HttpResponse
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.sagres.importer.TextTableSeparator
import org.apache.http.entity.StringEntity
import org.sagres.sampleSet.SampleSet
import common.SecRole

class LabkeyReportService {

	static transactional = true
	def springSecurityService
	def grailsApplication
	def dataSource
	def matConfigService
	def samplesFileImportService
	def mongoDataService


	def getAvailableSampleSets() {
		def db = new Sql(dataSource)

		def sampleSets = [:]
		sampleSets.put('-1', 'Select a sample set')
		db.eachRow("select id, name from dm3.sample_set") {
			sampleSets.put(it.id, it.name)
		}
		return sampleSets
	}


	def getLabkeyIdMapping(def id=33l) {
		Map labkeyIdToSampleId = [:]
    SampleSet ss = SampleSet.findById(id)
		if (ss) {
      if (ss.itnCohortsIdColumn) {
        List samples = mongoDataService.getSamples(id)
        samples.each { Map kv ->
          labkeyIdToSampleId.put(kv[ss.itnCohortsIdColumn], kv.id)
        }
      }
		}
		return labkeyIdToSampleId
	}

	//returns status code from request
	def int loadLabkeyReport(long labkeyReportId) {
		LabkeyReport lkr = LabkeyReport.get(labkeyReportId)
        lkr.enabled = true
		def user = grailsApplication.config.dm3.labkeyUserid
		def creds = grailsApplication.config.dm3.labkeyCredentials
		def workDir = matConfigService.MATWorkDirectory + "/temp/"
		File wFile = new File(workDir)
		if (!wFile.exists()) {
			wFile.mkdir()
		}
		File report = new File(workDir + labkeyReportId + ".tsv")
		if (report.exists()) {
			println "Deleting existing report: ${report.canonicalFile}"
			report.delete()
		}
		def rep = new FileOutputStream(report)
		HttpHost target = new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, Integer.valueOf(grailsApplication.config.dm3.labkey.port) , grailsApplication.config.dm3.labkey.protocol )
		DefaultHttpClient httpClient = new DefaultHttpClient()
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
			new UsernamePasswordCredentials(user, creds))
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicAuth = new BasicScheme()
		authCache.put(target, basicAuth)
		BasicHttpContext localcontext = new BasicHttpContext()
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
		HttpGet httpget = new HttpGet(lkr.reportURL)
		HttpResponse response = httpClient.execute(target, httpget, localcontext)
		if (response.getStatusLine().statusCode == 200) {
			HttpEntity entity = response.getEntity()
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))
			String line = ""
			while ((line = br.readLine()) != null) {
				rep << line + "\n"
			}
			rep.close()
            try {

			Date now = new Date();
			lkr.setDateLastLoaded(now)
			if (!lkr.save(flush:  true) ) {
                println "Errors Saving ${lkr}"
                lkr.errors.each {
                    println "$it"
                }
            }

			println "Starting to load report"
                samplesFileImportService.importLabkeyCSVData(lkr.sampleSetId, report, lkr.category, TextTableSeparator.TSV)
            println "lab key report loaded"
            } catch (Exception ex) {
                println "Error trying to update date last loaded $ex.toString()"
            }
		} else {
			println "Error getting report from Labkey : Status Code ${response.getStatusLine().statusCode}"
		}
		return response.getStatusLine().statusCode
	}


	def int getLabkeyReportStatus() {
		 	HttpHost target = new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, Integer.valueOf(grailsApplication.config.dm3.labkey.port) , grailsApplication.config.dm3.labkey.protocol )
		DefaultHttpClient httpClient = new DefaultHttpClient()
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
			new UsernamePasswordCredentials(user, creds))
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicAuth = new BasicScheme()
		authCache.put(target, basicAuth)
		BasicHttpContext localcontext = new BasicHttpContext()
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
		HttpGet httpget = new HttpGet("query/Studies/ITN029ST/Study%20Data/exportRowsTsv.view?schemaName=study&query.queryName=Demographics&query.showRows=ALL")
		HttpResponse response = httpClient.execute(target, httpget, localcontext)
		println "response Code = ${response.getStatusLine().statusCode}"
		return response.getStatusLine().statusCode
	}


	def loadCustomLabkeyReport() {
		def user = grailsApplication.config.dm3.labkeyUserid
		def creds = grailsApplication.config.dm3.labkeyCredentials
		def workDir = matConfigService.MATWorkDirectory + "/temp/"
		def labkeyReportId = "custom"
		File wFile = new File(workDir)
		if (!wFile.exists()) {
			wFile.mkdir()
		}
		File report = new File(workDir + labkeyReportId + ".tsv")
		if (report.exists()) {
			println "Deleting existing report: ${report.canonicalFile}"
			report.delete()
		}
		//	def parentSampleListURL = "query/Studies/ITN029ST/Study%20Data/exportRowsTsv.view?schemaName=study&query.queryName=Flow%20Analysis&query.showAllRows"
		def executeSQL = "/query/Studies/ITN029ST/Study%20Data/executeSql.api"
		String jsonQuery = "{ schemaName: 'study', sql: 'select * from SpecimenSummary' }"
		HttpEntity query = new StringEntity(jsonQuery)
		def rep = new FileOutputStream(report)
		HttpHost target =new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, Integer.valueOf(grailsApplication.config.dm3.labkey.port) , grailsApplication.config.dm3.labkey.protocol )
		DefaultHttpClient httpClient = new DefaultHttpClient()
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
			new UsernamePasswordCredentials(user, creds))
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicAuth = new BasicScheme()
		authCache.put(target, basicAuth)
		BasicHttpContext localcontext = new BasicHttpContext()
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
		HttpGet httpget = new HttpGet(parentSampleListURL)
		HttpResponse response = httpClient.execute(target, httpget, localcontext)
		HttpEntity entity = response.getEntity()
		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))
		String line = ""
		while ((line = br.readLine()) != null) {
			rep << line + "\n"
		}
		rep.close()
		Date now = new Date();
		lkr.setDateLastLoaded(now)
		lkr.save()
		println "Starting to load report"
		samplesFileImportService.importLabkeyCSVData(lkr.sampleSetId, report, lkr.category, TextTableSeparator.TSV)
	}


	 def  loadLabkeyCohorts() {
		 println "getting Cohorts"
		 def cohort = [:]
		def user = grailsApplication.config.dm3.labkeyUserid
		def creds = grailsApplication.config.dm3.labkeyCredentials
 		HttpHost target = new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, Integer.valueOf(grailsApplication.config.dm3.labkey.port) , grailsApplication.config.dm3.labkey.protocol )
		DefaultHttpClient httpClient = new DefaultHttpClient()
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
			new UsernamePasswordCredentials(user, creds))
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicAuth = new BasicScheme()
		authCache.put(target, basicAuth)
		BasicHttpContext localcontext = new BasicHttpContext()
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
		HttpGet httpget = new HttpGet("/query/Studies/ITN029ST/Study%20Data/exportRowsTsv.view?schemaName=study&query.queryName=Participant&query.queryName=Participant&query.showRows=ALL")
		HttpResponse response = httpClient.execute(target, httpget, localcontext)
		if (response.getStatusLine().statusCode == 200) {
			HttpEntity entity = response.getEntity()
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))
			String line = ""
			while ((line = br.readLine()) != null) {
				def vals = line.split("\t")
				if (vals.size() > 1 && !vals[0].equalsIgnoreCase("participantId")) {
					cohort.put(vals[0].trim(), vals[1].trim())
				} else {
					println "validate labkey login - Invalid response: $line"
				}
			}
		} else {
			println "Error getting cohorts from Labkey : Status Code ${response.getStatusLine().statusCode}"
		}
		return cohort
	}

	def loadLabkeyGroups() {
		def groups = []
		def user = grailsApplication.config.dm3.labkeyUserid
		def creds = grailsApplication.config.dm3.labkeyCredentials
 		HttpHost target = new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, Integer.valueOf(grailsApplication.config.dm3.labkey.port) , grailsApplication.config.dm3.labkey.protocol )
		DefaultHttpClient httpClient = new DefaultHttpClient()
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
			new UsernamePasswordCredentials(user, creds))
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicAuth = new BasicScheme()
		authCache.put(target, basicAuth)
		BasicHttpContext localcontext = new BasicHttpContext()
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
		HttpGet httpget = new HttpGet("/participant-group/Studies/ITN029ST/Study%20Data/browseParticipantGroups.api?type=participantGroup&page=1&start=0&limit=100")
		HttpResponse response = httpClient.execute(target, httpget, localcontext)

		if (response.getStatusLine().statusCode == 200) {
			HttpEntity entity = response.getEntity()
			//def jsonGroups = new JsonSlurper.parse(new InputStreamReader(entity.getContent()))
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))
			def groupsJson = JSON.parse(br)
			groups = groupsJson.groups
		} else {
			println "Error getting groups from Labkey : Status Code ${response.getStatusLine().statusCode}"
		}
		return groups
	}

	def loadLabkeyGroupMembers(def groupLabel) {

		 def group = []
		def user = grailsApplication.config.dm3.labkeyUserid
		def creds = grailsApplication.config.dm3.labkeyCredentials
 		HttpHost target =new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, Integer.valueOf(grailsApplication.config.dm3.labkey.port) , grailsApplication.config.dm3.labkey.protocol )
		DefaultHttpClient httpClient = new DefaultHttpClient()
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
			new UsernamePasswordCredentials(user, creds))
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicAuth = new BasicScheme()
		authCache.put(target, basicAuth)
		BasicHttpContext localcontext = new BasicHttpContext()
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
		HttpGet httpget = new HttpGet("/query/Studies/ITN029ST/Study%20Data/exportRowsTsv.view?schemaName=study&query.queryName=ParticipantGroupMap&query.showRows=ALL")
		HttpResponse response = httpClient.execute(target, httpget, localcontext)
		if (response.getStatusLine().statusCode == 200) {
			HttpEntity entity = response.getEntity()
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))
			String line = ""
			while ((line = br.readLine()) != null) {
				def vals = line.split("\t")
				if (vals.size() > 1 && !vals[0].equalsIgnoreCase("participantId") && vals[0].toString().equalsIgnoreCase(groupLabel) ) {
					group.add(vals[1].trim())
				}
			}
		} else {
			println "Error getting cohorts from Labkey : Status Code ${response.getStatusLine().statusCode}"
		}
		return group
	}


    def loadTrialShareProjects() {

        def trialShareProjects = []

        def user = grailsApplication.config.dm3.labkeyUserid
        def creds = grailsApplication.config.dm3.labkeyCredentials
        HttpHost target = new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, Integer.valueOf(grailsApplication.config.dm3.labkey.port) , grailsApplication.config.dm3.labkey.protocol )
//        HttpHost target = new HttpHost("www.itntrialshare.org", 443, "https")
        DefaultHttpClient httpClient = new DefaultHttpClient()
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(user, creds))
        AuthCache authCache = new BasicAuthCache()
        BasicScheme basicAuth = new BasicScheme()
        authCache.put(target, basicAuth)
        BasicHttpContext localcontext = new BasicHttpContext()
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
        def projectQuery = "/query/Studies/exportRowsTsv.view?schemaName=study&query.queryName=StudyProperties&query.queryName=StudyProperties&query.containerFilterName=CurrentAndSubfolders&query.viewName=service_view&query.showRows=ALL&delim=COMMA&quote=DOUBLE"
        //def projectQuery = "/query/Studies/executeQuery.view?query.queryName=StudyProperties&schemaName=study&query.containerFilterName=CurrentAndSubfolders&query.viewName=service_view&delim=COMMA&quote=DOUBLE"
        HttpGet httpget = new HttpGet(projectQuery)
        HttpResponse response = httpClient.execute(target, httpget, localcontext)

        if (response.getStatusLine().statusCode == 200) {
            HttpEntity entity = response.getEntity()
            //def jsonGroups = new JsonSlurper.parse(new InputStreamReader(entity.getContent()))
            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()))
            String line = ""
            while ((line = br.readLine()) != null) {
                def parts = line.split(",")
                if (parts.length == 5) {
                    try {
                        def trialShareId = Integer.valueOf(parts[2])
                        def study = [:]
                        study.trialshareId = parts[2]
                        study.description = parts[0]
                        study.path = parts[1].replace(" ", "%20")
                        trialShareProjects.add(study)
                    } catch (NumberFormatException nfe) {
                        //This is first line of report. Ignore
                    }

                }   else {
                    println "Error reading Project from Labkey : Unable to recognize project ${line}"
                }
            }
        } else {
            println "Error getting projects from Labkey : Status Code ${response.getStatusLine().statusCode}"
        }
        return trialShareProjects

    }


	def loadTrialShareGroups() {
		/**
		def groupInfo = [:]
		def groups = loadLabkeyGroups()
		groups.each {  group ->
			def groupSubjects = loadLabkeyGroup(group)
			groupInfo.put(group, groupSubjects)
		}
		return groupInfo
		 **/
		def groups = [:]
		def groupsl = loadLabkeyGroups()
		groupsl.each { group ->
			groups.put(group.id, group.label)
		}
		return groups
	}


    def loadTrialShareRolesWithDescription() {
        def trialShareRoles = [:]
        def tsProj = loadTrialShareProjects()
        //ENSURE ITN Roles all exist
        tsProj.each {
            def roleName = "ITN_PROJ_" + it.trialshareId
            def tsRole = SecRole.findByAuthority(roleName) ?: new SecRole(authority: roleName).save()
            trialShareRoles.put(java.net.URLDecoder.decode(it.path), tsRole)
        }
        return trialShareRoles
    }






}
