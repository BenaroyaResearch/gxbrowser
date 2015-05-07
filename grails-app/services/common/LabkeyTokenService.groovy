package common

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
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
import org.apache.http.HttpResponse
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicResponseHandler

class LabkeyTokenService {

	static transactional = true
	def grailsApplication
    def labkeyReportService
    def springSecurityService

	def serviceMethod() {

	}


	boolean isTokenValid(String labkeyToken)  {
		def permissionCheckURL = grailsApplication.config.dm3.labkey.protocol   + "://"  + grailsApplication.config.dm3.labkey.authentication.host + "/login/verifyToken.view?labkeyToken=" + labkeyToken
        println "tokenCheck URL : ${permissionCheckURL}"
		HttpClient httpClient = new DefaultHttpClient()
		SecRole fengRole = SecRole.findByAuthority(grailsApplication.config.dm3.feng.permissions)
		def validToken = false
		try {
			HttpGet httpGet = new HttpGet(permissionCheckURL)
			ResponseHandler<String> responseHandler = new BasicResponseHandler()
			String responseBody=httpClient.execute(httpGet, responseHandler)
			def sCheck = responseBody.minus("<").minus("TokenAuthentication").minus("/>").trim().split()

            println "Resonse to tokenCheck : " + responseBody
			sCheck.findAll {it.contains("success") }.each{
				if (it.toString().contains("true")) {
					 validToken = true
					println "Trialshare token reported valid :" + labkeyToken
					//valid
					//Extend session
					extendLabkeySession(labkeyToken)
				}  else {
                    println "Trialshare token reported invalid :" + labkeyToken
					//INVALID - TIMED OUT
				}
			}
		}  finally {
			httpClient.getConnectionManager().shutdown()
		}
		return validToken   //assume all true - but still need to check roles
	}

		boolean extendLabkeySession(String labkeyToken)  {
			//TODO to finish implementing
			def permissionCheckURL = grailsApplication.config.dm3.labkey.protocol   + "://"  + grailsApplication.config.dm3.labkey.authentication.host + "/login/verifyToken.view?labkeyToken=" + labkeyToken

	}


	def getLabkeyRoles(String labkeyToken, String studyPath = "/Studies/ITN029ST/Study%20Data", String studyName = "feng", String roleName = grailsApplication.config.dm3.feng.permissions) {


		def roles = []

        //Get All Projects
        def tsProjects = labkeyReportService.loadTrialShareProjects()
        SecUser user = springSecurityService.currentUser
        tsProjects.each {
            def rName = "ITN_PROJ_" + it.trialshareId

            if (checkLabkeyRole(labkeyToken,it.path )) {
                SecRole rRole =  SecRole.findByAuthority(rName) ?: new SecRole(authority: rName).save()
                roles.add(rRole)
            } else {
                if (user?.authorities?.authority?.contains(rName)) {
                    SecRole rRole =  SecRole.findByAuthority(rName)
                    SecUserSecRole.remove(user, rRole, true)
                    println "deleting role $rName for ${user.username}"
                }
            }
        }
		return roles

	}

    boolean checkLabkeyRole(String labkeyToken, String studyPath = "/Studies/ITN029ST/Study%20Data") {

        def permissionCheckURL =  grailsApplication.config.dm3.labkey.protocol   + "://" + grailsApplication.config.dm3.labkey.authentication.host + "/login" + studyPath + "/verifyToken.view?labkeyToken=" + labkeyToken
        HttpClient httpClient = new DefaultHttpClient()
        boolean hasRole = false;

        try {
            HttpGet httpGet = new HttpGet(permissionCheckURL)
            ResponseHandler<String> responseHandler = new BasicResponseHandler()
            String responseBody = httpClient.execute(httpGet, responseHandler)
            def sCheck = responseBody.minus("<").minus("TokenAuthentication").minus("/>").trim().split()
            def email = null
            /**
                sCheck.findAll{it.contains("email")}  {
                    email = it.split("=")[1].substring(1,it.toString().length() -2)
                    println "email address = ${email}"
                }
 **/
            sCheck.findAll {it.contains("permissions") }.each {
                def perm = it.toString().substring(it.toString().length() - 3)
                if (perm.length() > 3) {
                    println "Unexpected permission value to $studyPath : ${sCheck}"
                    //but probably valid
                } else {
                    if (perm.equals('"0"')) {
                        println "No permission to $studyPath : $sCheck"
                    } else {
                      //  println "access to $studyPath: $sCheck"
                        hasRole = true
                    }
                }
            }
        } finally {
            httpClient.getConnectionManager().shutdown()
        }
        return hasRole
    }
}


