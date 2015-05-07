package common

import org.springframework.security.*
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.codehaus.groovy.grails.commons.GrailsApplication
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
import org.apache.commons.httpclient.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.userdetails.UsernameNotFoundException





class LabkeyAuthenticationProviderService implements AuthenticationProvider {

	def userDetailsService
  def sessionFactory
	def grailsApplication

	Authentication authenticate(Authentication customAuth) {
		if (grailsApplication == null) {
			grailsApplication = new DefaultGrailsApplication( );
		}

		//Here do check against labkey
		Authentication auth
		try {
			//https://accesstrial.immunetolerance.org/login/
			def loginURL = grailsApplication.config.dm3.labkey.authentication.url + '/login/'
			int response = authenticateLabkeyUser(customAuth.principal, customAuth.credentials)
			if (response == 401) {
				throw new AuthenticationCredentialsNotFoundException("Unable to validate user name and password")
			}
			def sessionId
			def lku
			try {
				lku = userDetailsService.loadUserByUsername(customAuth.principal)
			}  catch (UsernameNotFoundException nfe) {
				lku = userDetailsService.loadUserByUsername('labkeyUser')
			}

			List labkeySetsViewable = []
			int statusCheck = getLabkeyReportStatus(customAuth.principal, customAuth.credentials)
			if (statusCheck == 200) {
				labkeySetsViewable.add(grailsApplication.config.dm3.feng.permissions)
				println "${customAuth.principal} is able to see Feng Data"
			}  else {
				println "${customAuth.principal} is not able to see Feng Data"

			}
			auth = new LabkeyAppTokenAuthentication(authenticated: true, name: customAuth.principal,principal: lku,
						credentials: customAuth.principal, authorities: lku.authorities,
					  labkeySessionId: sessionId, labkeySetsViewable: labkeySetsViewable)

		} catch (Exception ex) {
			println "Exception authenticating: ${ex.toString()}"
		//	ex.printStackTrace()
		}
		return auth
	}

	boolean supports(Class authentication) {
		return LabkeyAppTokenAuthentication.class.isAssignableFrom(authentication)
	}

	def int authenticateLabkeyUser(def user, def creds) {
		 	HttpHost target = new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, 443, "https")
		  DefaultHttpClient httpClient = new DefaultHttpClient()
		String targetUrl = "https://${grailsApplication.config.dm3.labkey.authentication.host}/login/login.post"
		HttpPost httpPost = new HttpPost(targetUrl)
		List<NameValuePair> nameValuePairs  = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("email", user))
		nameValuePairs.add(new BasicNameValuePair("password", creds))
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs))
		HttpResponse response = httpClient.execute(httpPost)
		return response.getStatusLine().statusCode
	}

	//TODO Genericize this and make it so that it's on a per sampleset level -any itn dataset having a test report
	//Should make it a small/quick report to run since we only care about the access
	//This is purely feng
	def int getLabkeyReportStatus(def user, def creds) {
		 	HttpHost target = new HttpHost(grailsApplication.config.dm3.labkey.authentication.host, 443, "https")
		DefaultHttpClient httpClient = new DefaultHttpClient()
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(target.getHostName(), target.getPort()),
			new UsernamePasswordCredentials(user, creds))
		AuthCache authCache = new BasicAuthCache()
		BasicScheme basicAuth = new BasicScheme()
		authCache.put(target, basicAuth)
		BasicHttpContext localcontext = new BasicHttpContext()
		localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache)
		HttpGet httpget = new HttpGet("/query/Studies/ITN029ST/Study%20Data/exportRowsTsv.view?schemaName=study&query.queryName=Demographics&query.showRows=ALL")
		HttpResponse response = httpClient.execute(target, httpget, localcontext)
		return response.getStatusLine().statusCode
	}


}
