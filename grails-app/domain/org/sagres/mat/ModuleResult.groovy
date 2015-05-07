package org.sagres.mat

class ModuleResult {

	static def columnLookup =  [
		'Median_Score':	'medianScore',
		'Module_Mean':	'moduleMean',
		'Module_Median':	'moduleMedian',
		'Module_Median_Absolute_Deviation':	'moduleMedianAbsoluteDeviation',
		'Module_Standard_Deviation':	'moduleStandardDeviation',
		'Negative_Percent':	'negativePercent',
		'Percent_Difference':	'percentDifference',
		'Positive_Percent':	'positivePercent',
		'Valence':	'valence'
	]

	static constraints = {
		module(nullable: false)
		sampleId(nullable: false)
	}

	long analysisId
	String module
	String sampleId
	Double medianScore
	Double moduleMean
	Double moduleMedian
	Double moduleMedianAbsoluteDeviation
	Double moduleStandardDeviation
	Double negativePercent
	Double percentDifference
	Double positivePercent
	Double valence

}
