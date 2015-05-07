package dm3

import common.LabkeyAppTokenAuthentication
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import common.SecUser
import java.text.SimpleDateFormat



class DM3Filters {

    def springSecurityService
    def labkeyTokenService
    def rememberMeServices
    def mongoDataService

    def filters = {
        all(controller: '*', action: '*') {
            before = {
                if (params.printLogging != null) {
                    session.setAttribute("printLogging", "true")
                }
                if (params.stopLogging != null) {
                    session.removeAttribute("printLogging")
                }
                //println ">>ControllerName : ${controllerName} - action - ${actionName} - target : ${params.loginTarget} "
                request._timeBeforeRequest = System.currentTimeMillis()

				// to print timing information 
				if (params.logTiming != null) {
					println "Timing started"
					session.setAttribute("logTiming", true);
				}
				if (params.stopTiming != null) {
					println "Timing stopped"
					session.removeAttribute("logTiming");
				}

                def loginTarget = params.loginTarget
                if (params.labkeyToken && !session.getAttribute("labkeyToken")) {
                    session.setAttribute("labkeyToken", params.labkeyToken)
                }

                if (session.getAttribute("printLogging")) {
                    println "##############################"
                    params.each { key, value ->
                        println "Params : $key -> $value"
                    }
                    session.attributeNames.toList().each {
                        println "session : $it -> ${session.getAttribute(it)}"
                    }
                    println "${session.getId()}"
                    println "##############################"
                }
                def labkeyToken = session.getAttribute("labkeyToken")


                def returnLink = new ApplicationTagLib().createLink(uri: loginTarget ?: session.forwardURI, absolute: true, params: params)

                if (grailsApplication.config.dm3.authenticate.labkey && springSecurityService.isLoggedIn()) {
                    long sessionRefreshPeriod = grailsApplication.config.dm3.session.sessionRefreshMS
                    long now = new Date().time

                    Long lastSessionRefresh = session.getAttribute("lastSessionRefresh")
                    if (params.printLog) println "sessionRefreshPeriod : $sessionRefreshPeriod - lastSessionRefresh: $lastSessionRefresh - now: $now"

                    long lastTokenCheck = session.getAttribute("lastTokenCheck") ? session.getAttribute("lastTokenCheck") : 0
                    long tokenCheckPeriod = grailsApplication.config.dm3.session.tokenCheckMS
                    if (now - lastTokenCheck > tokenCheckPeriod) {
                        //logout
                        if (!labkeyTokenService.isTokenValid(labkeyToken)  && !controllerName.equals('login') ) {
                            if (controllerName == null) {
                                return true
                            }
                            def forwardURI = request.getAttribute("javax.servlet.forward.request_uri")

                            if (forwardURI == null)   {
                              forwardURI = returnLink
                            }
                            if (session.getAttribute("printLogging")) {
                                println "ControllerName : ${controllerName} - target : ${params.loginTarget} - forwardURI - ${forwardURI} - return link - $returnLink "
                            }
                            session.setAttribute("loginTarget", forwardURI)
                            params.put("loginTarget", forwardURI)
                            params.loginTarget = forwardURI
                            def loginLink = new ApplicationTagLib().createLink(controller: 'login', action: 'labkeyAuth',  absolute: true, params: params)
                            //redirect(controller: 'login', action: 'labkeyAuth', params: params)
                            redirect(url:  loginLink)
                            return false
                        } else {
                            session.setAttribute("lastTokenCheck", now)
                        }
                    }
                    boolean requireRefresh = true
                    try {
                        requireRefresh = (sessionRefreshPeriod > 0 && ((now - lastSessionRefresh) > sessionRefreshPeriod))
                    }   catch (Exception ex) {
                        println ex.toString()
                    }
                    //println "Require SessionRefresh $requireRefresh"
                    if (requireRefresh) {
                        //need to refresh session
                        session.setAttribute("lastSessionRefresh", (new Date()).time)
                        if (returnLink.toString().contains("/dm3/dm3/")) {
                            returnLink = returnLink.toString().replace("/dm3/dm3/", "/dm3/")
                        }
                        if (session.getAttribute("printLogging")) {
                            println "refreshing session - return link ${returnLink}"
                        }
                        def tokenRefreshURL = "${grailsApplication.config.dm3.labkey.authentication.url}/project/home/redirect.view?returnUrl=${returnLink}"
                        redirect(url: tokenRefreshURL)
                        return false
                    } else {
                        if (session.getAttribute("printLogging"))
                        {
                            println "requireRefresh  = false"
                        }
                    }

                }

            }
            after = {
                request._timeAfterRequest = System.currentTimeMillis()
            }
            afterView = {

                if (grailsApplication.config.grails.statslogging) {
                    SecUser user = springSecurityService.currentUser


                    def actionDuration = -1
                    def viewDuration = -1

                    try {
                        actionDuration = request?._timeAfterRequest - request?._timeBeforeRequest
                        viewDuration = System?.currentTimeMillis() - request?._timeAfterRequest
                    } catch (Exception ex) {
                       // println "Exception calculating request time : $ex"
                    }
                    try {

                        def loggingData = [:]
                        params.each { key, value ->
                            if (value instanceof String || value instanceof GString) {
                                loggingData.put("param-" + key.toString().replace(".", "_"), value.toString().replace(".", "_"))
                            }
                        }
                        try {
                            if (user) {loggingData.put("user", user?.username?.toString()) }
                        }  catch (Exception ex) {
                            //No Username available
                        }
                        try {
                            loggingData.put("forwardURI", session?.forwardURI?.toString())
                        } catch (Exception ex) {
                            //Error getting forwardURI
                            println "Exception in logging getting forwardURI : " + ex.toString()
                        }
                        loggingData.put("referrer", request?.getHeader("referer")?.toString())
                        try {
                            loggingData.put("sessionId", request?.getSession()?.getId()?.toString())
                        } catch (Exception ex) {
                            //Exception getting sessionId
                        }
                        loggingData.put("requestURL", request.getRequestURL()?.toString())
                        loggingData.put("remoteAddr", request.getRemoteAddr()?.toString())
                        loggingData.put("actionDuration", actionDuration)
                        loggingData.put("viewDuration", viewDuration)
                        loggingData.put("System-currentTimeMillis", System.currentTimeMillis())
                        Date nDate = new Date();

                        loggingData.put("time", nDate.toLocaleString())
                        mongoDataService.logger(loggingData)
                    } catch (Exception ex) {
                        println "Exception logging request " + ex.toString()
                        ex.printStackTrace()
                    }

                }

            }
        }
    }

}
