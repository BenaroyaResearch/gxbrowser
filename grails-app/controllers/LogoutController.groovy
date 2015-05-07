import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.core.context.SecurityContextHolder


class LogoutController {

	def rememberMeServices
	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// Put any pre-logout code here
    def lastVisitedPage = params.lastVisitedPage
    def logoutUrl = SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
    def redirectUrl = lastVisitedPage ? lastVisitedPage : "/"

		boolean useLabkeyAuthentication = System.getenv("DM3_AUTHENTICATE_VIA_LABKEY") ? true : false
		if (!useLabkeyAuthentication ) {
			SecurityContextHolder.clearContext();
			//redirect uri: "${logoutUrl}?spring-security-redirect=${redirectUrl}"// '/j_spring_security_logout'
			redirect url: redirectUrl

		} else {
			SecurityContextHolder.clearContext();
			rememberMeServices.loginFail(request, response)
			def grailsLogoutURL = grailsApplication.config.dm3.labkey.protocol   + "://"  + grailsApplication.config.dm3.labkey.authentication.host   + "/login/invalidateToken.view?returnUrl=" + resource(dir: "", file: "/", absolute: true)  + "&labkeyToken=" + session.getAttribute("labkeyToken")
			println "loginUrl : $grailsLogoutURL"
			redirect(url :grailsLogoutURL)

		}
//		redirect uri: "${logoutUrl}?spring-security-redirect=${redirectUrl}"// '/j_spring_security_logout'
	}
}
