package org.sagres.circleplot

import au.com.bytecode.opencsv.CSVReader
import org.sagres.charts.ChordData
import org.sagres.charts.ChordColumn
import groovy.sql.Sql
import groovy.sql.GroovyRowResult
import com.mongodb.BasicDBList
import org.sagres.stats.*

class CirclePlotService {

	def mongoDataService
	def dataSource
	static moduleMap = null

	static transactional = true

	def serviceMethod() {

	}



	def getModuleMap() {
		Sql sql = Sql.newInstance(dataSource)
		def nModuleMap = [:]
		try {
			sql.rows("select distinct module_name from module".toString()).each {	GroovyRowResult row ->
			nModuleMap.put(row.get("module_name").toString(), 0)
			}
		} catch (Exception ex) {
			println "Exception - ${ex.toString()}"
			ex.printStackTrace()
		} finally {
			sql.close()
		}
		return nModuleMap
	}

	def getDistanceBetweenAnalyses(List analysisList) {
		def orderedList = analysisList.sort()
		def results = new Float[analysisList.size()][analysisList.size()]
		int count = 0;
		for (int i = 0; i < orderedList.size(); i++) {
			for (int j = i; j < orderedList.size(); j++) {
				if (i==j) {
					results[i][j] = 0
				}  else {
					def analysis1 = analysisList.get(i)
					def analysis2 = analysisList.get(j)
					def distance = getEuclideanDistanceBetweenAnalysis(analysis1, analysis2)
					results[i][j] = distance
					results[j][i] = distance
				}
			}
		}
		return results
	}

	def getEuclideanDistanceBetweenAnalysis(def analysisOne, def analysisTwo) {
		if (moduleMap == null) {
			moduleMap = getModuleMap()
		}
		def analysis1 = loadAnalysisResult(analysisOne)
		def analysis2 = loadAnalysisResult(analysisTwo)
		def list1 = new ArrayList()
		def list2 = new ArrayList()
		if (! (analysis1.size == analysis2.size  && analysis1.size == moduleMap.size)  ) {
			println "ERROR - module list for analysis $analysisOne and $analysisTwo don't have same number of modules"
		}
		def mapKeys = moduleMap.keys
		moduleMap.each { def key, def value ->
			list1.add(analysis1.get(key.toString()))
			list2.add(analysis2.get(key))
		}
		return MathUtil.euclideanDistanceSquared(list1, list2)
	}

	def loadAnalysisResult(def analysisId) {
		if (moduleMap == null) {
			moduleMap = getModuleMap()
		}
		def analysisResult = moduleMap.clone()
				def analysisData = mongoDataService.find("analysis_results",
			["analysisId": analysisId, "numCols": 3, "header.1": "positivePercent", "header.2": "negativePercent"], [], null, -1)

		if (analysisData) {
			def row = analysisData.rows
			row.each {   BasicDBList dbList ->
				dbList.each {
					def module = it.get(0)
					def plus = Double.parseDouble(it.get(1))
					def neg =  Double.parseDouble(   it.get(2)  )
					def moduleScore =plus - neg
				//	println "For ${analysisId} : ${module} = ${moduleScore}"
					if (analysisResult.get(module) != 0) {
						println "Analysis result before being set : ${analysisResult.get(module)}"
					}
					analysisResult.put(module, moduleScore)
				}
			}
		}
		return analysisResult
	}


	def findAnalysisWithResults() {
		def analysisData = mongoDataService.find("analysis_results",
			["numCols": 3, "header.1": "positivePercent", "header.2": "negativePercent"], ["analysisId"], null, -1)
		def analysisList = []
		if (analysisData) {
			analysisData.each {
				analysisList.add(it.analysisId)
			}

		}
		return analysisList
	}



	static int weight = 1

	def convertChordDataToMongoMap(ChordData chordData) {
		def mongoMap = [:]
		def columns = []
		chordData.columns.each { columns.add(it.name)}
		mongoMap.put("date", new Date())

		mongoMap.put("columns", columns)
		mongoMap.put("data", chordData.data)
		return mongoMap
	}

	def loadChordDataFromFile(def filename) {
		ChordData chordData = new ChordData()
		File dataFile = new File(filename).eachCsvLine { tokens ->
			if (chordData.columns.size() == 0) {
				tokens.each {
					ChordColumn cc = new ChordColumn(name: it)
					chordData.columns.add(cc)
				}
				chordData.columns[0].isArc = true
				chordData.columns[1].isLink = true
			} else {
				def values = tokens
				chordData.data.add(values)
				for (int i = 0; i < tokens.size(); i++) {
					chordData.columns[i].addValue(tokens[i])

				}
			}
		}
		return chordData
	}

	def loadChordDataFromMongo(def mongoId) {
		ChordData chordData = new ChordData()
		def mongoData = mongoDataService.find("chord_data", ["_id": mongoId], ["columns", "data"], null, -1)

		mongoData.columns.each {
			it.keySet().each { def keys ->
				ChordColumn cc = new ChordColumn(name: it.get(keys))
				chordData.columns.add(cc)
			}
			chordData.columns[0].isArc = true
			chordData.columns[1].isLink = true
		}
		int numColumns = chordData.columns.size()
		mongoData.data.each {
			it.keySet().each { def key ->
				def row = it.get(key)
				def entries = []
				for (int i = 0; i < numColumns; i++) {
					entries.add(row.get(i))
					chordData.columns[i].addValue(row.get(i))
				}
				chordData.data.add(entries)
			}
		}
		return chordData
	}

	def printMatrix(int[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				print "${matrix[i][j]} "
			}
			println " "
		}
	}

	def generateStringMatrix(int[][] matrix) {
		StringBuilder sMatrix = new StringBuilder()
		for (int i = 0; i < matrix.length; i++) {
			sMatrix.append("[ ")
			for (int j = 0; j < matrix[i].length; j++) {
				sMatrix.append("${matrix[i][j]}")
				if (j < matrix[i].length - 1) {
					sMatrix.append(", ")
				}
			}
			sMatrix.append("]")
			if (i < matrix.length - 1) { sMatrix.append(",")}
		}
		return sMatrix.toString()
	}

	//TODO: Add Filters - No Filters Yet
	def generateChordMatrixFromChordData(ChordData chordData) {
		def arcs = ((ChordColumn) chordData.columns[0]).values
		int numberOfArcs = arcs.size()
		def circleMatrix = new int[numberOfArcs][numberOfArcs]
		for (int i = 0; i < numberOfArcs; i++) {
			String currentArc = ((ChordColumn) chordData.columns[0]).values[i]
			chordData.data.findAll {it[0] == currentArc }.each {	def values ->
				def links = chordData.data.findAll { it[1] == values[1]}
				if (links.size() > 1) {
					links.each {
						int indexOfMatch = arcs.indexOf(it[0])
						if (indexOfMatch > i) {
							//Check filters
							boolean apply = true
							for (int j = 2; j < chordData.columns.size(); j++) {
								ChordColumn cc = chordData.columns.get(j)
								def filterValue = cc.filterValue
								if (cc.filter) {
									if (cc.isIsNumber()) {
										try {

											Long t = Float.parseFloat(values[j])
											Long fv = Float.parseFloat(filterValue)
											if (!(cc.filterType && t < fv)) {
												apply = false
											}
										} catch (Exception ex) {
											apply = false

										}
									} else {
										String t = values[j]
										if (!(cc.filterType && t.equalsIgnoreCase(filterValue))) {
											apply = false
										}
									}
								}
							}
							if (apply) {
								circleMatrix[i][indexOfMatch] += weight
							}
						}
					}
				} else {
					//Check filters
					boolean apply = true
					for (int j = 2; j < chordData.columns.size(); j++) {
						ChordColumn cc = chordData.columns.get(j)
						def filterValue = cc.filterValue

						if (cc.filter) {
							if (cc.isIsNumber()) {
								try {

									Long t = Float.parseFloat(values[j])
									Long fv = Float.parseFloat(filterValue)
									if (!(cc.filterType && t < fv)) {
										apply = false
									}
								} catch (Exception ex) {
									apply = false
								}
							} else {
								String t = values[j]
								if (!(cc.filterType && t.equalsIgnoreCase(filterValue))) {
									apply = false
								}
							}
						}
					}
					if (apply) {
						circleMatrix[i][i] += weight
					}
				}
			}
		}
		return circleMatrix
	}
}
