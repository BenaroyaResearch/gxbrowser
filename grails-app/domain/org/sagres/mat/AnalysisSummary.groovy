package org.sagres.mat

class AnalysisSummary {

	static constraints = {
		analysisCompleteTime(blank: true, nullable:true)
		analysisStartTime(blank: true, nullable:true)
		resultsLoadedField(blank: true, nullable:true)

	}

		static transients = ['resultsLoaded']

	long analysisId
	Integer resultsLoadedField   //1 true, 0 false
	Date analysisStartTime
	Date analysisCompleteTime

	boolean getResultsLoaded() {
		return resultsLoadedField == 1
	}

	boolean setResultsLoaded(boolean resultsLoaded) {
		resultsLoadedField = (resultsLoaded)?1:0
	}


}
