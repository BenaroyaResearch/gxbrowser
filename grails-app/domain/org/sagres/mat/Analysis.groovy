package org.sagres.mat

class Analysis {

	static constraints = {
		datasetName(blank: false, matches: "[a-zA-Z0-9_ ]+")
		dataSetGroups(nullable:true)
		platformType(nullable: false, inList: ["Microarray", "RNA-seq", "Focusedarray"]) // These need to match platform names created in bootstrap.
		user(blank: true, nullable:true)
		runDate()
		inputType(blank: false, inList: ["BeadStudio", "Custom"])
		signalPattern(blank: false)
		expressionDataFile(nullable: false)
		designDataFile(nullable: false)
		moduleFunctionFile(nullable: false, blank: false)
		moduleVersionFile(nullable: false, blank: false)
		chipId(nullable: true)
		variables()
		colorGroups()
		orderModules()
		plotWidth(nullable: true, blank: true)
		plotHeight(nullable: true, blank: true)
		modGeneration(blank: false, inList: ["2", "3"])

		deltaType(blank: false, inList: ["zscore", "fold"])
		minExpression(blank: false, min: 0d)
		topModule(blank: false, min: 0)
		deltaCut(blank: false, min: 0d)
		foldCut(blank: false, min: 0d)
		zscoreCut(blank: false, min: 0d)
		PALX(nullable: true, blank: true, min: 0d, max: 1d)
		chisqPvalue(blank: false, min: 0d, max: 1d)
		minDiff(blank: false, min: 0d)
		rangeCut(nullable: true, blank: true, min: 0d, max: 1d)

		minPercent(blank: false, min: 0d, max: 100d)
		preprocess(blank: false, inList: ["TRUE", "FALSE"])
		performNormalize(blank: false, inList: ["TRUE", "FALSE"])
		performLog2(blank: false, inList: ["TRUE", "FALSE"])
		performFloor(blank: false, inList: ["TRUE", "FALSE"])
		performPALO(blank: false, inList: ["TRUE", "FALSE"])
		multipleTestingCorrection(blank: false, inList: ["TRUE", "FALSE"])

		zMeasure(blank: false, inList: ["mean", "median"])
		flagDelete( nullable:true )
		matPublished( nullable: false)
		metacatPublished(nullaable: false)
		
    displayName(nullable:true, blank:false)
	}


	String datasetName
	Long sampleSetId = -1
	Integer dataSetGroups = -1
	String platformType = "Microarray"
	Date runDate
	String user
	String inputType ="Custom"
	String signalPattern = "AVG_Signal"
	String expressionDataFile = "Not Known"
	String designDataFile = "Not Known"
	String moduleFunctionFile = "Not Known"
	String moduleVersionFile  = "Not Known"
	long moduleVersion
	Long chipId = -1
	String variables = "Not Known"
	String colorGroups = "group_label"
	String orderModules = "Function"
	double plotWidth
	double plotHeight
	String deltaType = "fold"
	double minExpression = 10.0
	int topModule = 26
	double deltaCut = 100.0
	double foldCut = 2.0
	double zscoreCut = 2.0
	double chisqPvalue = 0.05
	double minDiff = 0.0
	double minPercent = 0.0
	Double fdr = 0.05
	Double rangeCut = 0.0
	Double PALX = 0.1
	String modGeneration = "2"
	String performNormalize = "TRUE"
	String performFloor = "TRUE"
	String performLog2 = "TRUE"
	String performPALO = "TRUE"
	String preprocess = "TRUE"
	String zMeasure = "mean"
	String multipleTestingCorrection = "TRUE"
	String flagDelete

	int matPublished = System.getenv('publish_default')?Integer.parseInt(System.getenv('publish_default')): 0
	int metacatPublished = 0;
	
	String displayName


	def Map returnScriptParameters() {
		def parameterMap = [
							'evaluation_method': deltaType,
							'fold_change_cutoff': foldCut,
							'difference_cutoff': deltaCut,
							'z-score_cutoff': zscoreCut,
							'range_cutoff': rangeCut,
							'chi-squared_p-value': chisqPvalue,
							'minimum_difference': minDiff,
							'minimum_percent': minPercent,
//	
							"false_discovery_rate" : fdr, 
							'multiple_testing_correction': multipleTestingCorrection,
//							
							'preprocess_data': preprocess,
							'quantile_normalize': performNormalize,
							'log2_transform': performLog2,
							'floor_data': performFloor,
							'minimum_expression_floor': minExpression,
							'apply_PALO_filter': performPALO,
							'present_in_sample_fraction': PALX,
							'central_tendencey_metric': zMeasure,
//							'top_module': topModule,
							'module_generation': modGeneration,
							'platform_type' : platformType,
//							'input': inputType,
//							'signal_pattern' : signalPattern,
							'module_plotting_order': orderModules,
							'sample_plotting_order': '\"group, sample_id\"',
							'color_groups': "\"${colorGroups}\"",
//							'plot_width_inches': (plotWidth == 0.0) ? " " : plotWidth,
//							'plot_height_inches': (plotHeight == 0.0) ? " " : plotHeight,
//
//							'expression_data_file': expressionDataFile,
//							'design_data_file': designDataFile,
//							'signal_pattern_file': signalPattern,
//							'module_function_file': moduleFunctionFile,
//							'module_version_file': moduleVersionFile


		]
		return parameterMap
	}


	def Map returnScriptParametersForR() {
		def parameterMap = ['input': inputType, 'signal_pattern': signalPattern, 'expression': expressionDataFile,
			'design': designDataFile, 'module_function': moduleFunctionFile, 'module_version': moduleVersionFile,'platform_type' : platformType,
			'order_modules': orderModules, 'order': '\"\"group, sample_id\"\"', 'color_groups': "\"\"\"${colorGroups}\"\"\"",
			'plot_width_inches': (plotWidth == 0.0) ? " " : plotWidth, 'plot_height_inches': (plotHeight == 0.0) ? " " : plotHeight,
			'delta_type': deltaType, 'min_expr': minExpression, 'top_module': topModule, 'delta_cut': deltaCut, 'fold_cut': foldCut,
			'zscore_cut': zscoreCut, 'chisq_pvalue': chisqPvalue, 'min_diff': minDiff, 'min_percent': minPercent,
			'performNormalize': performNormalize, 'performFloor': performFloor, 'performLog2': performLog2, 'performPALO': performPALO,
			'PALX': PALX, 'modGeneration': modGeneration, 'zmeasure': zMeasure, 'preproc': preprocess, "FDR" : fdr, 'MTC': multipleTestingCorrection,
			'range_cut': rangeCut
		]
		return parameterMap
	}

}
