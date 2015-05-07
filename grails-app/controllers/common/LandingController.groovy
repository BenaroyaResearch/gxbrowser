package common

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class LandingController {

	def grailsApplication
	
    def index() {
		def landing = "/landing.gsp" 
		if (grailsApplication.config.dm3.site.landing) {
			landing = grailsApplication.config.dm3.site.landing
		}
		// println "landing: " + landing
		redirect(uri: landing)
	}
}
