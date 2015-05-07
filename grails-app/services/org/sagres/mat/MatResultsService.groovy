package org.sagres.mat

import static groovy.io.FileType.FILES

class MatResultsService {

	def matConfigService

	def fileSep = System.getProperty("file.separator")

	def springSecurityService

	static transactional = true

	def serviceMethod() {
	}

	def processAnalysisResults(long analysisId) {
		/**
		println " processing analysis results"
		def moduleResultMap = [:]
		def analysis = Analysis.findById(analysisId)
		File resDir = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Results${fileSep}${analysis.datasetName.replaceAll(" ", "_")}")
		ModuleResult moduleResult = null
		def p = ~/.*\.csv/
		resDir.eachFileMatch FILES, p, { File outFile ->
			def sampleIds = null
			String column = null
			ModuleResult.columnLookup.each { key, value ->
				if (outFile.name.contains(key)) {
					column = value
				}
			}
			if (column == null) { return;}
			outFile.eachLine { def line ->
				if (sampleIds == null) {
					sampleIds = line.split(',')
					sampleIds.eachWithIndex {it, index ->	sampleIds[index] = it.minus('"').minus('"')}
				} else {
					def sampleId = null
					def module = null
					line.split(",").eachWithIndex { it, index ->
						if (index == 0) {
							module = it.minus('"').minus('"')
						} else {
							sampleId = sampleIds[index]
							def key = "${analysisId},${sampleId},${module}"
							moduleResult = moduleResultMap.get(key)
							//@TODO: Fix update - change to be an update - have an open sql connection and do an update yourself. create new instance if update fails.
							moduleResult = moduleResult ?: ModuleResult.find("from ModuleResult as m where m.analysisId=? and m.sampleId=? and  m.module=?", [analysisId, sampleId, module])
							moduleResult = moduleResult ?: new ModuleResult(sampleId: sampleId, analysisId: analysisId, module: module)
							moduleResultMap.put(key, moduleResult)
							try {
								moduleResult["${column}"] = Double.valueOf(it)
								moduleResult.save()
							} catch (NumberFormatException nfe) {
								println "NumberFormatException ${key}:${column}:${it} "
							}
						}
					}
				}
			}
		}

		println " Number of Results generated: ${moduleResultMap.size()}"
		 **/
	}


}
