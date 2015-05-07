import grails.converters.JSON

import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import common.SecUserSecRole
import common.SecRole
import common.SecUser
import common.LabkeyAppTokenAuthentication

class LoginController {

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    def grailsApplication
    def rememberMeServices
    def userDetailsService
    def labkeyTokenService

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
     */
    def index = {
        if (springSecurityService.isLoggedIn()) {

            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        }
        else {

            redirect action: 'auth', params: params
        }
    }

    /**
     * Show the login page.
     */
    def auth = {

        def lastVisitedPage = params.lastVisitedPage
        if (lastVisitedPage?.contains("mgmt")) {
            lastVisitedPage = "/"
            params.lastVisitedPage = "/"
        }
        boolean useLabkeyAuthentication = System.getenv("DM3_AUTHENTICATE_VIA_LABKEY") ? true : false
        if (params.message != null) {
            flash.message = params.message
        }

        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            println "User is logged in"
            if (params.loginTarget) {
                redirect uri: params.loginTarget
            } else {
                redirect uri: config.successHandler.defaultTargetUrl
            }
            return
        }
        if (!useLabkeyAuthentication) {

            String view = 'auth'
            String appName = grailsApplication.metadata['app.name']


            String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
            if (grailsApplication.config.dm3.authenticate.labkey) {
                postUrl = "${request.contextPath}/login/authenticateRequest"
            } else {
                if (params.getProperty("loginTarget") != null) {
                    lastVisitedPage = params.loginTarget
                } else if (lastVisitedPage.toString().contains(appName)) {
                    //Stripping out leading dm3 - it's handled differently via spring and the custom labkey filter
                    lastVisitedPage = lastVisitedPage.toString().substring(lastVisitedPage.toString().indexOf(appName) + appName.length())
                }
            }
            render view: view, model: [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter, targetUrlParam: lastVisitedPage]
        } else {
            String view = 'auth'
            def returnLink = createLink(controller: 'login', action: 'labkeyAuth', absolute: true, params: params)

            if (params.loginTarget) {
                lastVisitedPage = params.loginTarget
                session.setAttribute("loginTarget", params.loginTarget)
            } else {
                session.setAttribute("loginTarget", returnLink)
            }
            String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
            def grailsLoginURL = grailsApplication.config.dm3.labkey.protocol + "://" + grailsApplication.config.dm3.labkey.authentication.host + "/login/createToken.view?returnUrl=" + createLink(controller: 'login', action: 'labkeyAuth', absolute: true, params: params)
            println "loginUrl : $grailsLoginURL"


            redirect(url: grailsLoginURL)

            //render view: view, model: [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter, targetUrlParam: lastVisitedPage]
//			redirect
        }

    }

    def labkeyAuth = {
        def labkeyToken = params.labkeyToken ?: session.getAttribute("labkeyToken")

        if (params.labkeyEmail) {
            session.setAttribute("email", params.labkeyEmail)
        }
        def labkeyEmail = params.labkeyEmail ?: session.getAttribute("email")

        println "Checking session loginTarget = ${session.getAttribute('loginTarget')}"
        def tokenValid = labkeyTokenService.isTokenValid(labkeyToken)
        def roles = []
        if (tokenValid) {
            println " Session valid"
            long now = new Date().time
            session.setAttribute("lastTokenCheck", now)
            roles = labkeyTokenService.getLabkeyRoles(labkeyToken)
            def currentUser
            SecUser secUser
            try {
                currentUser = userDetailsService.loadUserByUsername(labkeyEmail)
                secUser = SecUser.findByUsername(labkeyEmail)
            } catch (Exception ex) {
                println "Exception loading user : $currentUser  - ${ex.toString()}"
                secUser = new SecUser(username: labkeyEmail, email: labkeyEmail, password: 'password', enabled: true, accountExpired: false).save(failOnError: true)
                def userRole = SecRole.findByAuthority('ROLE_USER')
                SecUserSecRole.create(secUser, userRole)
                currentUser = userDetailsService.loadUserByUsername(labkeyEmail)
            }
            roles.each {
                if (!secUser.authorities.contains(it)) {
                    //println "User doesn't have role ${it}: adding"
                    SecUserSecRole.create(secUser, it)
                    //} else {
                    //println "User already has role ${it}"
                }
            }
            LabkeyAppTokenAuthentication auth = new LabkeyAppTokenAuthentication(authenticated: true, name: labkeyEmail, principal: currentUser,
                    credentials: labkeyToken, authorities: currentUser.authorities,
                    labkeySessionId: labkeyToken)
            session.setAttribute("labkeyToken", labkeyToken)
            session.setAttribute("lastTokenCheck", (new Date()).time)
            session.setAttribute("lastSessionRefresh", (new Date()).time)
            SecurityContextHolder.getContext().setAuthentication(auth)
            rememberMeServices.onLoginSuccess(request, response, auth)
            println "Login Info: $labkeyEmail  token: $labkeyToken"

            def sessionInfo = session.getAttribute("sessionInfo")
            if (sessionInfo) {
                println "Has SessionInfo : $sessionInfo"
                redirect(controller: sessionInfo.controller, action: sessionInfo.action, params: sessionInfo.params)
            } else {
                println "No Stored Session Info"
                def loginTarget = session.getAttribute('loginTarget')
                if (loginTarget.toString().contains("labkeyAuth")) {
                    loginTarget = null
                }
                def lastVisitedPage = loginTarget ?: params.lastVisitedPage
                def redirectUrl = lastVisitedPage ? lastVisitedPage : "/"
                if (redirectUrl.startsWith("/" + grailsApplication.metadata['app.name'] + "/")) {
                    println "removing leading ${grailsApplication.metadata['app.name'] } from $redirectUrl"
                    redirectUrl = redirectUrl.toString().replaceFirst("/" + grailsApplication.metadata['app.name'], "")
                }
                redirect(url: java.net.URLDecoder.decode(redirectUrl), absolute: true, params: params)
            }


        } else {
            def grailsLoginURL = grailsApplication.config.dm3.labkey.protocol + "://" + grailsApplication.config.dm3.labkey.authentication.host + "/login/createToken.view?returnUrl=" + createLink(controller: 'login', action: 'labkeyAuth', absolute: true, params: [lastVisitedPage: params.lastVisitedPage])

            redirect(url: grailsLoginURL)
        }
    }

    /**
     * The redirect action for Ajax requests.
     */
    def authAjax = {
        response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * Show denied page.
     */
    def denied = {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: 'full', params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full = {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
    }

    /**
     * Callback after a failed login. Redirects to the auth page with a warning message.
     */
    def authfail = {

        def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
        String msg = ''
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
        if (exception) {
            if (exception instanceof AccountExpiredException) {
                msg = g.message(code: "springSecurity.errors.login.expired")
            }
            else if (exception instanceof CredentialsExpiredException) {
                msg = g.message(code: "springSecurity.errors.login.passwordExpired")
            }
            else if (exception instanceof DisabledException) {
                msg = g.message(code: "springSecurity.errors.login.disabled")
            }
            else if (exception instanceof LockedException) {
                msg = g.message(code: "springSecurity.errors.login.locked")
            }
            else {
                msg = g.message(code: "springSecurity.errors.login.fail")
            }
        }

        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
        }
        else {
            flash.message = msg
            println "redirect fail 1"
            redirect action: 'auth', params: params
        }
    }

    /**
     * The Ajax success redirect url.
     */
    def ajaxSuccess = {
        render([success: true, username: springSecurityService.authentication.name] as JSON)
    }

    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied = {
        render([error: 'access denied'] as JSON)
    }


    def authenticateRequest = {
        if (springSecurityService.isLoggedIn()) {
            handleLabkeyUser()
            def lastVisitedPage = params.lastVisitedPage
            def redirectUrl = lastVisitedPage ? lastVisitedPage : "/${grailsApplication.metadata['app.name']}/"
            //redirect to appropriate page
            //			forward (controller: 'geneBrowser', action: 'list', params: params)
            println "redirect authenticateRequest 1"
            redirect(url: java.net.URLDecoder.decode(redirectUrl))
        }
        else {
            println "Authentication failed"
            println "redirect authenticateRequest 2"

            redirect(action: denied, params: params)
        }
    }


    def handleLabkeyUser() {
        try {
            SecUser currentUser = springSecurityService.currentUser
            def labkeyUser = SCH.context?.authentication
            if (labkeyUser != null && labkeyUser instanceof LabkeyAppTokenAuthentication) {
                def perms = ((LabkeyAppTokenAuthentication) labkeyUser).labkeySetsViewable
                if (!currentUser.username.equalsIgnoreCase(labkeyUser.name)) {
                    //Need to
                    //def newSecUser
                    try {
                        currentUser = userDetailsService.loadUserByUsername(labkeyUser.name)
                        println "Loaded user"
                    } catch (Exception ex) {
                        println "Creating new user ${labkeyUser.name} : ${ex.message}"
                        currentUser = new SecUser(username: labkeyUser.name, email: labkeyUser.name, password: 'password', enabled: true, accountExpired: false).save(failOnError: true)
                        def userRole = SecRole.findByAuthority('ROLE_USER')
                        SecUserSecRole.create(currentUser, userRole)
                    }
                    labkeyUser.setPrincipal(currentUser)
                }
                if (perms != null) {
                    perms.each {
                        def userRole = SecRole.findByAuthority(it)
                        if (!currentUser.authorities?.authority.contains(it)) {
                            println "User  does not yet have $it role : adding"
                            SecUserSecRole.create(currentUser, userRole)
                        }
                    }
                }
            }
        } catch (Exception ex) {
            println "Exception handling labkey user : ${ex.toString()}"
            ex.printStackTrace()
        }

    }

}
