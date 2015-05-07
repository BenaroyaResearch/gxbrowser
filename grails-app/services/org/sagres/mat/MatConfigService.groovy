package org.sagres.mat

class MatConfigService {

 	static transactional = true


	def grailsApplication

	def serviceMethod() {
	}

	def getMATDM3Host() {
		return System.getenv("DM3Host") ?:grailsApplication.config.mat.dm3host;
	}
	def getMATDM3User() {
		return System.getenv("DM3User") ?:grailsApplication.config.mat.dm3user;
	}
	def getMATDM3Password() {
		return System.getenv("DM3Password") ?:grailsApplication.config.mat.dm3pwd;
	}



	def getDM3RefreshSeconds() {
		def refTimeS = System.getenv("DM3RefreshTime") ?:grailsApplication.config.mat.dm3refreshseconds;
		return Long.parseLong(refTimeS)
	}

	def getMATWorkDirectory() {
		return System.getenv("MAT") ?: grailsApplication.config.mat.workDir;
	}

	def getMATLink() {
		return System.getenv("MATLink") ?: grailsApplication.config.mat.file.url.prepender;
	}

	def getRScriptLocation() {
		return System.getenv("Rlocation") ?: grailsApplication.config.mat.R.executable;
	}

	def getAnalysisCompleteString() {
		return grailsApplication.config.mat.analysis.complete.string;
	}
}
