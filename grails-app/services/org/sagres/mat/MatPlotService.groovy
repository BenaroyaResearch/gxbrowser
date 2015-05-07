package org.sagres.mat

import cern.colt.function.DoubleDoubleFunction
import cern.colt.function.DoubleFunction
import cern.colt.function.DoubleProcedure
import cern.colt.list.IntArrayList
import cern.colt.matrix.DoubleMatrix1D
import cern.colt.matrix.DoubleMatrix1DProcedure
import cern.colt.matrix.DoubleMatrix2D
import cern.colt.matrix.impl.DenseDoubleMatrix1D
import cern.colt.matrix.impl.SparseDoubleMatrix2D
import cern.jet.math.Functions
import groovy.sql.Sql
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.sagres.FilesLoaded;
import org.sagres.sampleSet.SampleSet
import org.sagres.stats.ClusterHierarchy
import org.sagres.stats.ClusterNode
import org.sagres.stats.HierarchicalClusterer
import org.sagres.stats.StatsService

import java.util.regex.Pattern
import org.sagres.labkey.LabkeyReport

class MatPlotService {

  static transactional = true

  private static final String CSVCOLLECTION = "analysis_results"
  private static final String BLANK_COLOR = "#CCC"
  private static final int SPOT_RADIUS = 17,
                           SPOT_PADDING = 3,
                           SPOT_UNIT = SPOT_RADIUS + SPOT_PADDING,
                           FULL_SPOT_UNIT = SPOT_UNIT * 2,
                           BOX_WIDTH = 40,
                           HALF_BOX_WIDTH = BOX_WIDTH / 2,
                           BOX_PADDING = 3,
                           BORDER_WIDTH = 1,
                           COL_SPACING = 1,
                           BOX_LINE_WIDTH = BOX_WIDTH + COL_SPACING,
                           TOP_PADDING = 30,
                           LEFT_PADDING = 100,
                           MAX_COLS = 20,
                           STARTX = LEFT_PADDING + BORDER_WIDTH,
                           STARTY = TOP_PADDING + BORDER_WIDTH,
                           STARTFAX = 30 + BORDER_WIDTH

  private final int DENDLEFT = 300,  //left margin of heatmap
                    HSCALE = 40,     //width of one heatmap cell (neg. to draw upward)
                    VSCALE = 10      //height of one dendrogram level
  private int dEndBase = 100         //bottom of dendrogram (top of heatmap)

  static final int GSA = 10, PIE = 11, MOD = 12, FA = 13
  static final int UNDER = 0, OVER = 1
  def static final GSA_COLORS = [["#0049f8", "#5970f9", "#939efa", "#cacefc"],  // under
                                 ["#ff3b2d", "#ff6964", "#ff9b98", "#ffcdcc"]]  // over
  //def static final GROUP_COLORS = ["#F3C94A", "#02A748", "#51316D", "#02A7A4"] //"#3288BD","#D53E4F"
  //def static final GROUP_COLORS = ["#F3C94A", "#02A748", "#51316D", "#02A7A4", "#3288BD","#D53E4F"]

  def static final GROUP_COLORS = ["#CC77CC", "#ecbe1b", "#9acd60", "#fe5b85", "#914603", "#838383"]
  def static final D3_COLORS = ["#1f77b4", "#ff7f0e", "#2ca02c",  "#d62728", "#9467bd",
	  							"#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf" ]

  private static final String SEPARATOR = System.getProperty("line.separator")
  private static final String fileSep = System.getProperty("file.separator")

  def matDataService    //injected
  def matConfigService
  def chartingDataService
  def mongoDataService
  def dataSource
  def grailsApplication

  DoubleDoubleFunction diffFunc = new DoubleDoubleFunction() {
    public double apply(double a, double b) {
      return a - b
    }
  }

  DoubleDoubleFunction largerFunc = new DoubleDoubleFunction() {
    public double apply(double a, double b) {
      if (b > a) { return -b }
      return a
    }
  }

  DoubleFunction log2Func = new DoubleFunction() {
    public double apply(double a) {
      if (a) {
        return Math.log(a) / Math.log(2)
      }
      return 0
    }
  }

  DoubleFunction inverseFunc = new DoubleFunction() {
    public double apply(double a) {
      if (a < 1) {
        return -a
      }
      return a - 1
    }
  }

  def Map gsaPlot(Analysis a, String plotname, Integer limit, double fdr) {
    def dsName = a.datasetName.replaceAll(' ', '_')
    def filename = "${dsName}_Gene_Set_Analysis(_.*)+\\.csv".toString()
    groupModulePlot(a, filename, GSA, plotname, limit, fdr)
  }

  def Map modPiePlot(Analysis a, String plotname, int limit, double fdr) {
    def dsName = a.datasetName.replaceAll(' ', '_')
    def filename = "${dsName}_260_Module_Group_Comparison(_.*)+\\.csv".toString()
    groupModulePlot(a, filename, PIE, plotname, limit, fdr)
  }

  def Map modPlot(Analysis a, String plotname, int limit, double fdr) {
    def dsName = a.datasetName.replaceAll(' ', '_')
    def filename = "${dsName}_260_Module_Group_Comparison(_.*)+\\.csv".toString()
    groupModulePlot(a, filename, MOD, plotname, limit, fdr)
  }

  def Map faPlot(Analysis a, String plotname, double maxFoldChange) {
    def dsName = a.datasetName.replaceAll(' ', '_')
    def filename = "${dsName}_Group_Module_Level_FC(_.*)+\\.csv".toString()
    focusedArrayGroupModulePlot(a, filename, plotname, maxFoldChange)
  }

//  def Map faPlot(Analysis a, String plotname) {
//    def dsName = a.datasetName.replaceAll(' ', '_')
//    def filename = "groupModuleLevelFC.csv";
//    focusedArrayGroupModulePlot(a, filename, plotname)
//  }

  private Map focusedArrayGroupModulePlot(Analysis analysis, String filename, String plotname, double maxFoldChange)
  {
    Pattern p = Pattern.compile(filename, Pattern.CASE_INSENSITIVE)
    boolean mongoCsv = mongoDataService.exists(CSVCOLLECTION, [analysisId: analysis.id, filename: p], null)
    if (!mongoCsv) {
      // If it doesn't exist yet in Mongo, store it, then draw
      def csvFile = matDataService.getFile(analysis, filename)
      mongoDataService.storeCsv(csvFile, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
    }
    Map result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], ["rows"])
    List rows = result.rows

    Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))
    List<String> modules = []

    StringBuilder headerSb = new StringBuilder()
    headerSb.append("Module,FC")
    StringBuilder sb = new StringBuilder(1024)

    def boxes = [], spots = [], aBoxes = []
    int col = 0
    int x = STARTFAX, y = STARTY, cx = STARTFAX + BOX_PADDING + SPOT_RADIUS, cy = STARTY + BOX_PADDING + SPOT_RADIUS
    rows.sort { a, b ->
      String[] aM = a[0].substring(1).split("\\.")
      int aM1 = aM[0].toInteger()

      String[] bM = b[0].substring(1).split("\\.")
      int bM1 = bM[0].toInteger()

      if (aM1 > bM1) { return 1 }
      else if (aM1 < bM1) { return -1 }
      else if (aM1 == bM1) {
        int aM2 = aM[1].toInteger()
        int bM2 = bM[1].toInteger()
        if (aM2 > bM2) { return 1 }
        else if (aM2 < bM2) { return -1 }
      }
      return 0
    }.each { List<String> values ->
      sb.append("${values.join(",")}${SEPARATOR}")
      double fc = Math.log(Double.parseDouble(values[1])) / Math.log(2)
      double absFc = Math.abs(fc.trunc(2))
//      double fillOpacity = (Math.abs(fc) / 2).trunc(2)
      double fillOpacity = (Math.abs(fc) / maxFoldChange).trunc(2)

      String module = values[0]?.trim()
      modules.push(module)

      Map moduleInfo = [info: ["Module": module]]

      Map spotInfo = [info: ["Module": module]]
      spotInfo.up = fc > 0f
      spotInfo.value = absFc

      Map annotInfo = [:]
      if (annotations.containsKey(module)) {
        ModuleAnnotation aInfo = annotations.get(module)
        moduleInfo.info["Function"] = aInfo.annotation
        moduleInfo.color = aInfo.hexColor
        spotInfo.info["Function"] = aInfo.annotation
        spotInfo.color = aInfo.hexColor
        if (analysis.modGeneration == "3" && grailsApplication.config.module.wiki.on) {
          annotInfo.annotation = "<a href='http://mat.benaroyaresearch.org/wiki/index.php/Generation_3_Modules_${module}' target='_blank'>${aInfo.annotation}</a>"
        } else {
          annotInfo.annotation = aInfo.annotation
        }
        annotInfo.color = aInfo.hexColor
        annotInfo.abbr = aInfo.abbreviation
      }
      String aColor = annotInfo.color ?: "#FFF"
      Map aBox = [path: "M${x + 0.7 * BOX_WIDTH} ${y}H${x + BOX_WIDTH}V${y + BOX_WIDTH * 0.3}Z", x: x, y: y, width: BOX_WIDTH, height: BOX_WIDTH, attr: [stroke: "none", fill: aColor, "fill-opacity": 0], data: annotInfo]
      if (annotInfo.abbr)
      {
        aBox.text = annotInfo.abbr
        aBox.cx = x + HALF_BOX_WIDTH
        aBox.cy = y + HALF_BOX_WIDTH
      }
      aBoxes.push(aBox)

      String fill = fc > 0f ? "#FF0000" : "#0000FF"
      spots.push([cx: cx, cy: cy, r: SPOT_RADIUS, attr: [stroke: "none", fill: fill, "fill-opacity":fillOpacity], data: spotInfo])
      boxes.push([ x:x, y:y, width: BOX_WIDTH, height: BOX_WIDTH, attr: [stroke: "none", fill: "#FFF"], data: moduleInfo ])

      if (col == 10) {
        col = 0
        x = STARTFAX
        cx = STARTFAX + BOX_PADDING + SPOT_RADIUS
        y += BOX_WIDTH + 1
        cy += BOX_WIDTH + 1
      } else {
        col++
        x += BOX_WIDTH + 1
        cx += BOX_WIDTH + 1
      }
    }

    int maxX = STARTFAX + 11 * (BOX_WIDTH + 1) + BORDER_WIDTH
    int maxY = STARTY + 4 * (BOX_WIDTH + 1) + BORDER_WIDTH

    annotations.keySet().retainAll(modules)
    def annotationKeyLabels = annotationKeyWithLabels(annotations, true)

    def bg = [type: "rect", x: 30, y: TOP_PADDING, width: maxX - STARTFAX, height: maxY - STARTY, stroke: "#111111", "stroke-width": BORDER_WIDTH * 2, fill: "#cccccc"]
    def width = maxX + 30
    def height = maxY + 60

    def model = [width: width, height: height, bg: bg, boxes: boxes, spots: spots, aBoxes: aBoxes, annotations: annotationKeyLabels]

    cachePlot(null, analysis.id, plotname, headerSb.toString(), sb.toString())

    return model
  }

  private def groupModulePlot(Analysis analysis, String filename, int type, String plotName, int limit, double fdr) {
    Map result = null
    List rows = []
    // check for probe level statistics file
    if (type != GSA) {
      result = getProbeLevelRows(analysis)
      if (result)
      {
        rows = filterProbeRows(analysis, result, fdr)
      }
    }
    if (!result)
    {
      Pattern p = Pattern.compile(filename, Pattern.CASE_INSENSITIVE)
      boolean mongoCsv = mongoDataService.exists(CSVCOLLECTION, [analysisId: analysis.id, filename: p], null)
      if (!mongoCsv) {
        // If it doesn't exist yet in Mongo, store it, then draw
        def csvFile = matDataService.getFile(analysis, filename)
        mongoDataService.storeCsv(csvFile, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
      }
      result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], ["rows"])
      rows = result?.rows
    }
	if (result && rows)
	{
    	groupModulePlot(analysis, rows, type, plotName, limit == 62)
	}
	else
	{
		def workDir = matConfigService.getMATWorkDirectory()
		def htmlDir = matConfigService.getMATLink()

		def model = [ "error": "There are no results available for this module analysis."]
		File logFile = new File(workDir + fileSep + analysis.id + fileSep + "analysis.log")
		if (logFile.exists() && logFile.size() > 0) {
			String resultLink = htmlDir + "/" + analysis.id + "/analysis.log"
			model.put("logFile", resultLink)
		}

		return model 
	}
  }

  private Map getProbeLevelRows(Analysis analysis)
  {
    Map result = null
    def dsName = analysis.datasetName.replaceAll(' ', '_')
    def probeLevelFilename = "${dsName}_Probe_Level_Statistics(_.*)+\\.csv".toString()
    Pattern probeP = Pattern.compile(probeLevelFilename, Pattern.CASE_INSENSITIVE)
    boolean mongoProbeCsv = mongoDataService.exists(CSVCOLLECTION, [ analysisId:analysis.id, filename:probeP ], null)
    if (!mongoProbeCsv)
    {
      // If it doesn't exist yet in Mongo, store it, then draw
      def probeCsvFile = matDataService.getFile(analysis, probeLevelFilename)
      if (probeCsvFile) {
        mongoDataService.storeCsv(probeCsvFile, true, CSVCOLLECTION, [ analysisId:analysis.id, sampleSetId:analysis.sampleSetId ])
        result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:probeP ], ["header", "rows"])
      }
    }
    else
    {
      result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:probeP ], ["header", "rows"])
    }
    return result
  }

  private List filterProbeRows(Analysis analysis, Map result, double fdr)
  {
    List rows = []
    int moduleCol = 1, tStatCol = -1, signedCol = 4, diffCol = 6, origPval = 7, adjPval = 8
    result.header.eachWithIndex { String h, int col ->
      if (h.equals("module")) {
        moduleCol = col
      } else if (h.equals("tStatistic")) {
        tStatCol = col
      } else if (h.equals("originalPval")) {
        origPval = col
	  } else if (h.equals("adjustedPval")) {
        adjPval = col
      } else if (h.equals("Diff")) {
	  	diffCol = col
      }
    }
	if (tStatCol > 0) {
		signedCol = tStatCol
	} else {
		signedCol = diffCol
	}
    Map<String,Map<String,Integer>> moduleToProbeCount = [:]
    result.rows.each { List<String> values ->
      String module = values[moduleCol].trim()
      if (module != "NA")
      {
        String upDown = values[signedCol].toDouble() < 0d ? "down" : "up"
        double pvalue = analysis.multipleTestingCorrection == "TRUE" ? values[adjPval].toDouble() : values[origPval].toDouble()
        if (pvalue < fdr)
        {
          if (!moduleToProbeCount.containsKey(module))
          {
            moduleToProbeCount.put(module, [ up:0, down:0 ])
          }
          Map upDownCount = moduleToProbeCount.get(module)
          int newCount = upDownCount.get(upDown) + 1
          upDownCount.put(upDown, newCount)
        }
      }
    }
    long modGen = getModuleGen(analysis)
    Module.findAllByModuleGenerationId(modGen).each {
      String m = it.moduleName
      int totalProbes = it.probeCount
      Map upDownCount = moduleToProbeCount.get(m)
      double percentUp = upDownCount ? upDownCount.up / totalProbes * 100 : 0d
      double percentDown = upDownCount ? upDownCount.down / totalProbes * -100 : 0d
      rows.push([ m, percentUp.toString(), percentDown.toString() ])
    }
    return rows
  }

  private Map groupModulePlot(Analysis analysis, List rows, int type, String plotName, boolean top) {
//    long moduleGenerationId = getModuleGen(analysis)

//    long startTime = System.currentTimeMillis()
    // sort the modules correctly
    Map m1ToMaxM2 = [:]
    List moduleValues = []
    rows.each { List<String> r ->
      String module = r[0]
      String[] modules = module.substring(1).split("\\.")
      int m1 = modules[0].toInteger()
      int m2 = modules[1].toInteger()
	  if (r[1] == "NA") {
		  r[1] = 0.0
	  }
	  if (r[2] == "NA") {
		  if (type == GSA) {
			r[2] = 1.0
		  } else {
			  r[2] = 0.0
		  }
	   }
      if (!(top && m1 > 6)) {
        moduleValues.push([ module:module, m1:m1, m2:m2, v1:r[1].toDouble(), v2:r[2].toDouble() ])
        if (!m1ToMaxM2.containsKey(m1)) {
          m1ToMaxM2.put(m1, m2)
        } else {
          int max = Math.max(m2, m1ToMaxM2.get(m1))
          m1ToMaxM2.put(m1, max)
        }
      }
    }
//    println "mapping @ ${System.currentTimeMillis() - startTime}ms"

    moduleValues.sort { a, b ->
      if (a.m1 > b.m1) { return 1 }
      else if (a.m1 < b.m1) { return -1 }
      else if (a.m1 == b.m1) {
        if (a.m2 > b.m2) { return 1 }
        else if (a.m2 < b.m2) { return -1 }
      }
      return 0
    }
//    println "sorting modules @ ${System.currentTimeMillis() - startTime}ms"

    // get additional information for each module
//    Map annotations = getAnnotations(moduleGenerationId)
    Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))
    Map probeCounts = getProbeCounts(analysis)
    Map modToNumRows = [:]
//    println "retrieving additional info @ ${System.currentTimeMillis() - startTime}ms"

    // Build header
    StringBuilder sb = new StringBuilder();
    StringBuilder headerSb = new StringBuilder()
    headerSb.append("Module,Functional Annotation")
    switch (type) {
      case GSA:
        headerSb.append(",GSA Score,pValue")
        break
//      default:
//        headerSb.append(",% Difference,% Positive,% Negative")
    }

    // go through each row and draw stuff
    List blanks = [], boxes = [], aBoxes = [], spots = [], segments = []
    int lastM1 = -1, lastM2 = 0
    def m1y = 0, cy = STARTY + BOX_PADDING + SPOT_RADIUS, y = STARTY
    def maxX = 0, maxY = 0
    moduleValues.each { Map r ->
      double diff = r.v1 + r.v2
      boolean draw = draw(type, r.v1, r.v2)
      boolean up = up(type, r.v1, r.v2, diff)

      // add additional module info
      Map moduleInfo = [info: ["Module": r.module, "# of probes": probeCounts.get(r.module)]]
      Map spotInfo = [info: ["Module": r.module, "# of probes": probeCounts.get(r.module)]]
      Map annotInfo = [info: ["# of probes": probeCounts.get(r.module)]]
      if (annotations.containsKey(r.module)) {
        ModuleAnnotation aInfo = annotations.get(r.module)
        moduleInfo.info["Function"] = aInfo.annotation
        moduleInfo.color = aInfo.hexColor
        spotInfo.info["Function"] = aInfo.annotation
        spotInfo.color = aInfo.hexColor
//        if (modGen == 2) {
        if (analysis.modGeneration == "2") {
          annotInfo.annotation = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${r.module}' target='_blank'>${aInfo.annotation}</a>"
        } else {
          annotInfo.annotation = aInfo.annotation
        }
        annotInfo.color = aInfo.hexColor
        annotInfo.abbr = aInfo.abbreviation
      }
      switch (type) {
        case GSA:
          spotInfo.up = up
          spotInfo.value = r.v2.trunc(3)
          spotInfo.info["Score"] = r.v1.trunc(2)
          spotInfo.info["P-value"] = r.v2.trunc(3)
          break
        case MOD:
          spotInfo.up = up
          spotInfo.value = "${Math.abs(diff.trunc(2))} %"
          spotInfo.info["% Positive"] = r.v1.trunc(2)
          spotInfo.info["% Negative"] = Math.abs(r.v2.trunc(2))
          break
        case PIE:
          spotInfo.info["% Positive"] = r.v1.trunc(2)
          spotInfo.info["% Negative"] = Math.abs(r.v2.trunc(2))
          break
        default:
          spotInfo.info["Value 1"] = r.v1
          spotInfo.info["Value 2"] = r.v2
      }
      if (analysis.sampleSetId != -1 && grailsApplication.config.mat.access.gxb)
	  {
        if (SampleSet.findByIdAndMarkedForDeleteIsNull(analysis.sampleSetId)) {
          def gxbLink = new ApplicationTagLib().createLink(controller:"geneBrowser", action:"show", id:analysis.sampleSetId, params:[ module:r.module, analysisId:analysis.id ])
          moduleInfo.gxbLink = "<a href='"+gxbLink+"' target='_blank'>View in Gene Expression Browser</a>";
          spotInfo.gxbLink = "<a href='"+gxbLink+"' target='_blank'>View in Gene Expression Browser</a>";
        }
      }
//      if (modGen == 2) {
      if (analysis.modGeneration == "2" && grailsApplication.config.module.wiki.on)
      {
        moduleInfo.moduleWiki = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${r.module}' target='_blank'>[Module Wiki]</a>"
        spotInfo.moduleWiki = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${r.module}' target='_blank'>[Module Wiki]</a>"
      }

      double offset = (((r.m2 - 1) % MAX_COLS) * BOX_LINE_WIDTH)
      def cx = STARTX + BOX_PADDING + SPOT_RADIUS + offset
      def x = STARTX + offset

      // check for skipped modules
      if (lastM1 != -1 && r.m1 != lastM1) {
        modToNumRows.put(lastM1, (int) Math.ceil(lastM2 / MAX_COLS))
        def col = lastM2 % MAX_COLS
        if (col != 0 && col < MAX_COLS) {
          def diffX = LEFT_PADDING + BORDER_WIDTH + col * BOX_LINE_WIDTH
          def diffWidth = (MAX_COLS - col) * BOX_LINE_WIDTH - COL_SPACING // - 1 ?
          def diffY = y + m1y
          def height = BOX_WIDTH
          if (lastM2 > MAX_COLS) {
            diffY++
            height--
          }
          blanks.push([type: "rect", x: diffX, y: diffY, width: diffWidth, height: height, stroke: "none", fill: BLANK_COLOR])
        }
        cy += BOX_LINE_WIDTH + m1y + 1
        y += BOX_LINE_WIDTH + m1y + 1
        m1y = 0
      }
      else {
        def currentM2 = lastM2 + 1
        while (currentM2 < r.m2) {
          def mod = currentM2 % MAX_COLS
          def col = (currentM2 > 0) && mod == 0 ? MAX_COLS - 1 : currentM2 % MAX_COLS - 1
          def diffX = STARTX + col * BOX_LINE_WIDTH
          def tempRy = (int) (currentM2 / MAX_COLS)
          def m1Ry = (mod == 0 ? tempRy - 1 : tempRy) * BOX_LINE_WIDTH
          blanks.push([type: "rect", x: diffX, y: (y + m1Ry), width: BOX_WIDTH, height: BOX_WIDTH, stroke: "none", fill: BLANK_COLOR])
          currentM2++
        }

        def tempCy = (int) (r.m2 / MAX_COLS)
        m1y = ((r.m2 % MAX_COLS) == 0 ? tempCy - 1 : tempCy) * BOX_LINE_WIDTH
      }

      maxX = Math.max(maxX, x)
      maxY = Math.max(maxY, (y + m1y))

      boxes.push([x: x, y: (y + m1y), width: BOX_WIDTH, height: BOX_WIDTH, attr: [stroke: "none", fill: "#FFF"], data: moduleInfo])

      String aColor = annotInfo.color ?: "#FFF"
      Map aBox = [path: "M${x + 0.7 * BOX_WIDTH} ${(y + m1y)}H${x + BOX_WIDTH}V${(y + m1y) + BOX_WIDTH * 0.3}Z", x: x, y: (y + m1y), width: BOX_WIDTH, height: BOX_WIDTH, attr: [stroke: "none", fill: aColor, "fill-opacity": 0], data: annotInfo]
      if (annotInfo.abbr)
      {
        aBox.text = annotInfo.abbr
        aBox.cx = x + HALF_BOX_WIDTH
        aBox.cy = y + m1y+HALF_BOX_WIDTH
      }
      aBoxes.push(aBox)

      if (type == PIE) {
        if (draw) {
          def pos = (r.v1 / 100) * 360f, neg = (r.v2 / 100) * 360f
          if (neg < 0f && neg != -360f) {
            segments.push([segment: [cx, (cy + m1y), SPOT_RADIUS, 270 + neg, 270, -1]])
          }
          if (pos > 0f && pos != 360f) {
            segments.push([segment: [cx, (cy + m1y), SPOT_RADIUS, 270, 270 + pos, 1]])
          }
          def fill = pos == 360f ? "#FF0000" : ((neg == -360f) ? "#0000FF" : "#FFF")
          def fillOpacity = (pos == 360f || neg == -360f) ? 1 : 0
          spots.push([cx: cx, cy: (cy + m1y), r: SPOT_RADIUS, attr: [fill: fill, "fill-opacity": fillOpacity, "stroke-width": 1, stroke: "#999"], data: spotInfo])
        }
        else {
          spots.push([cx: cx, cy: (cy + m1y), r: SPOT_RADIUS, attr: [fill: "#FFF", "stroke-width": 0, stroke: "none"], data: spotInfo])
        }
      }
      else {
        def attr = [stroke: "none", fill: color(type, r.v1, r.v2, diff)]
        if (type == MOD) {
          attr.put("fill-opacity", spotOpacity(diff))
        }
        spots.push([cx: cx, cy: (cy + m1y), r: SPOT_RADIUS, attr: attr, data: spotInfo])
      }

      switch (type) {
//        case PIE:
//          sb.append("${r.module},${moduleInfo.info["Function"] ?: ''},${diff},${r.v1},${r.v2}${SEPARATOR}")
//          break
        case GSA:
          sb.append("${r.module},${moduleInfo.info["Function"] ?: ''},${r.v1},${r.v2}${SEPARATOR}")
          break
//        case MOD:
//          sb.append("${r.module},${moduleInfo.info["Function"] ?: ''},${diff},${r.v1},${r.v2},${SEPARATOR}")
//          break
      }

      lastM1 = r.m1
      lastM2 = r.m2
    }
//    println "iterating @ ${System.currentTimeMillis() - startTime}ms"

    modToNumRows.put(lastM1, (int) Math.ceil(lastM2 / MAX_COLS))
    def col = lastM2 % MAX_COLS
    if (col != 0 && col < MAX_COLS) {
      def diffX = STARTX + col * BOX_LINE_WIDTH
      def diffWidth = (MAX_COLS - col) * BOX_LINE_WIDTH - COL_SPACING // - 1
      def diffY = y + m1y
      def height = BOX_WIDTH
      if (lastM2 > MAX_COLS) {
        diffY++
        height--
      }
      blanks.push([type: "rect", x: diffX, y: diffY, width: diffWidth, height: height, stroke: "none", fill: BLANK_COLOR])
    }

    maxX += BOX_WIDTH + 1
    maxY += BOX_WIDTH + 1

    def xAxis = []
    for (i in 0..(MAX_COLS - 1)) {
      def tx = STARTX + (i * BOX_LINE_WIDTH) + HALF_BOX_WIDTH
      xAxis.push([type: "text", x: tx, y: TOP_PADDING - 12, text: "${i + 1}".toString(), font: "16px Fontin-Sans, Arial"])
      xAxis.push([type: "text", x: tx, y: maxY + 17, text: "${i + 1}".toString(), font: "16px Fontin-Sans, Arial"])
    }
    def yAxis = [], count = 0, labelY = STARTY
    modToNumRows.each { module, rowCount ->
      def labelHeight = rowCount == 1 ? BOX_WIDTH : (rowCount * BOX_WIDTH) + ((rowCount - 1) * COL_SPACING)
      yAxis.push([type: "text", x: 45, y: labelY + (labelHeight / 2), text: "M${module}".toString(), font: "16px Fontin-Sans, Arial", "text-anchor": "end"])
      def ty = labelY
      for (i in 0..(rowCount - 1)) {
        def start = (i * MAX_COLS) + 1, end = Math.min(m1ToMaxM2.get(module), (i + 1) * MAX_COLS)
        yAxis.push([type: "text", x: LEFT_PADDING - 10, y: ty + HALF_BOX_WIDTH, text: "${start}-${end}".toString(), font: "italic 10px Fontin-Sans, Arial", "text-anchor": "end", fill: "#333333"])
        count++
        ty += BOX_WIDTH + 1
      }
      labelY += 2 + labelHeight
    }

    def annotationKeyLabels = annotationKeyWithLabels(annotations, false, top)

    def bg = [type: "rect", x: LEFT_PADDING, y: TOP_PADDING, width: maxX - LEFT_PADDING, height: maxY - TOP_PADDING, stroke: "#111111", "stroke-width": BORDER_WIDTH * 2, fill: "#cccccc"]
    def width = maxX + 30
    def height = maxY + 60

    boolean isPie = type == PIE
    def model = [width: width, height: height, bg: bg, xAxis: xAxis, yAxis: yAxis, aBoxes: aBoxes, annotations: annotationKeyLabels, boxes: boxes, blanks: blanks, spots: spots]
    if (isPie) {
      model.segments = segments
    }

    if (type == GSA)
    {
      runAsync {
        cachePlot(model, analysis.id, plotName, headerSb.toString(), sb.toString())
      }
    }

//    println "finished @ ${System.currentTimeMillis() - startTime}ms"

    return model
  }

  private boolean draw(int type, double v1, double v2) {
    switch (type) {
      case GSA: return (v2 < 0.05)
      case MOD: return (v1 > 0 || v2 < 0)
      default: return true
    }
  }

  private boolean up(int type, double v1, double v2, double diff) {
    switch (type) {
      case GSA: return v1 > 0
      case MOD: return diff > 0
        return true
    }
  }

  private String color(int type, double v1, double v2, double diff) {
    switch (type) {
      case GSA: return gsaColor(v1, v2)
      case MOD:
        if (diff > 0f) { return "#FF0000" }
        else { return "#0000FF" }
    }
  }

  private float spotOpacity(double diff) {
    return (Math.abs(diff) / 100).round(2)
  }

  /**
   * Return the color for the specified GSA score and p-value
   * @param score
   * @param pvalue
   * @return A hex color value
   */
  private String gsaColor(double score, double pvalue) {
    def idx = score < 0 ? UNDER : OVER
    if (pvalue < 0.001) { return GSA_COLORS[idx][0] }
    else if (pvalue < 0.01) { return GSA_COLORS[idx][1] }
    else if (pvalue < 0.03) { return GSA_COLORS[idx][2] }
    else if (pvalue < 0.05) { return GSA_COLORS[idx][3] }
    else { return "#FFF" }
  }

  /**
   * Return the annotations for the specified module generation
   * @param moduleGen
   * @return A map of module -> [annotation, color]
   */
//  private Map getAnnotations(long moduleGen) {
//  private Map getAnnotations(long moduleGen) {
  private Map<String,ModuleAnnotation> getAnnotations(int generation) {
    Map annotations = [:]
    ModuleAnnotation.findAllByGeneration(generation)?.each {
      annotations.put(it.moduleName, it)
    }
    return annotations
//    Map annotations = [:]
//    String query = """SELECT m.module_name 'module', a.annotation 'annotation', a.hex_color 'color', a.abbreviation 'abbr' FROM module_annotation a
//      JOIN module m ON a.id = m.module_annotation_id
//      WHERE a.module_generation_id = ${moduleGen}
//      ORDER BY m.module_name""".toString()
//    Sql sql = Sql.newInstance(dataSource)
//    sql.eachRow(query) {
//      annotations.put(it.module, [annotation: it.annotation, color: it.color, abbr: it.abbr])
//    }
//    sql.close()
//    return annotations
  }

  String exportGroupCsv(Analysis analysis, boolean top, boolean isPie, double fdr)
  {
    List rows = null
    def dsName = analysis.datasetName.replaceAll(' ', '_')
    def probeLevelFilename = "${dsName}_Probe_Level_Statistics(_.*)+\\.csv".toString()
    Pattern p = Pattern.compile(probeLevelFilename, Pattern.CASE_INSENSITIVE)
    Map result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], [ "header","rows"])
    if (result)
    {
      rows = filterProbeRows(analysis, result, fdr)
    }
    else
    {
      def filename = "${dsName}_260_Module_Group_Comparison(_.*)+\\.csv".toString()
      p = Pattern.compile(filename, Pattern.CASE_INSENSITIVE)
      result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], ["rows"])
      rows = result.rows
    }

//    long moduleGenerationId = getModuleGen(analysis)

    // sort the modules correctly
    Map m1ToMaxM2 = [:]
    List moduleValues = []
    rows.each { List<String> r ->
      String module = r[0]
      String[] modules = module.substring(1).split("\\.")
      int m1 = modules[0].toInteger()
      int m2 = modules[1].toInteger()
      if (!(top && m1 > 6)) {
        moduleValues.push([ module:module, m1:m1, m2:m2, v1:r[1].toDouble(), v2:r[2].toDouble() ])
        if (!m1ToMaxM2.containsKey(m1)) {
          m1ToMaxM2.put(m1, m2)
        } else {
          int max = Math.max(m2, m1ToMaxM2.get(m1))
          m1ToMaxM2.put(m1, max)
        }
      }
    }

    moduleValues.sort { a, b ->
      if (a.m1 > b.m1) { return 1 }
      else if (a.m1 < b.m1) { return -1 }
      else if (a.m1 == b.m1) {
        if (a.m2 > b.m2) { return 1 }
        else if (a.m2 < b.m2) { return -1 }
      }
      return 0
    }

    // get additional information for each module
//    Map annotations = getAnnotations(moduleGenerationId)
    Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))
    StringBuilder sb = new StringBuilder()
    StringBuilder headerSb = new StringBuilder()
    headerSb.append("Module,Functional Annotation,% Difference,% Positive,% Negative")

    moduleValues.each { Map r ->
      double diff = r.v1 + r.v2
      sb.append("${r.module},${annotations.get(r.module)?.annotation ?: ''},${diff},${r.v1},${r.v2}${SEPARATOR}")
    }

    return "${headerSb.toString()}${SEPARATOR}${sb.toString()}"
  }

  String exportCsv(Analysis analysis, String plotName, boolean annotatedOnly, boolean isPie,
                   boolean diff, boolean showRowSpots, boolean showControls, double floor, String sampleField,
                   int moduleCount = -1, String rowCluster = null, String colCluster = null)
  {
    Map plotData = getPlotData(analysis, diff, showControls, annotatedOnly, floor, moduleCount)
    def matrix = plotData.matrix
    def modules = plotData.modules
    def samples = plotData.samples
    def annotations = plotData.annotations
    def posMatrix = plotData.posMatrix
    def negMatrix = plotData.negMatrix
    def hasAllColumn = plotData.hasAllColumn
    def diffAllColumn = plotData.diffAllColumn
    def isFocusedArray = plotData.isFocusedArray ?: false

//    Map files = getDifferenceFiles(analysis, true)
//    Map posMongo = files.positive
//    Map negMongo = files.negative
//    Map allMongo = files.allColumn
//    String allType = files.allType
//
//    List<String> samples = posMongo.header
//    samples.remove(0)
//    boolean hasAllColumn = samples.get(0) == "All"

//    def moduleGenerationId = getModuleGen(analysis)
//    def annotations = getAnnotations(moduleGenerationId)
//    Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))

    // Matrices are filtered to modules with annotations if "Annotated" selected
//    Map posResult = loadMatrix(posMongo.rows, annotatedOnly ? annotations : [:], hasAllColumn, moduleCount > 0)
//    Map negResult = loadMatrix(negMongo.rows, annotatedOnly ? annotations : [:], hasAllColumn, moduleCount > 0)

//    DoubleDoubleFunction func = diff ? diffFunc : largerFunc

    // read positive and negative files
//    List<String> modules = posResult.modules
//    Map diffAllColumn = [:]
//    SparseDoubleMatrix2D posMatrix = posResult.matrix
//    SparseDoubleMatrix2D negMatrix = negResult.matrix

    // Filter controls
//    if (!showControls)
//    {
//      def groups = analysisGroups(analysis, true)
//
//      int control = 0
//      def matWizard = MATWizard.findByAnalysisId(analysis.id)
//      if (matWizard) {
//        control = analysis.runDate.before(Date.parse("yyyy-MM-dd", "2012-03-30")) ? 1 : 0
//      }
//
//      // Determine which columns to filter out if show controls is false
//      List<Integer> validColumns = []
//      groups.entrySet().each {
//        if (it.value.groupNum != control)
//        {
//          validColumns.push(samples.indexOf(it.key))
//        }
//      }
//      samples = validColumns.collect { samples[it] }
//
//      posMatrix = posMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
//      negMatrix = negMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
//    }

//    SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(posMatrix.rows(), posMatrix.columns())
//    matrix.assign(posMatrix)
//    matrix.assign(negMatrix, func)

//    boolean addAllColumn = allMongo != null
//    if (allMongo && allType == "diff") {
//      allMongo.rows.each { values ->
//        diffAllColumn.put(values[0], values[1])
//      }
//    }
//    else if (allMongo && allType == "mls") {
//      String deltaType = analysis.deltaType == "fold" ? "_FC" : "_Zscore"
//      int allColumn = allMongo.header.indexOf("all${deltaType.substring(1)}PercentDiff".toString())
//      allMongo.rows.each { values ->
//        diffAllColumn.put(values[0], values[allColumn])
//      }
//    }

    // replace with user selected sample header
    StringBuilder headerSb = new StringBuilder()
    headerSb.append("Module,Functional Annotation")
    if (hasAllColumn) {
      headerSb.append(",All")
    }
    Map sampleIdToLabel = null
    if (sampleField) {
      sampleIdToLabel = [:]
      List result = mongoDataService.getColumnValuesAndSampleId(analysis.sampleSetId, sampleField)
      result.each { Map s ->
        if (s[sampleField] && !((String) s[sampleField]).isAllWhitespace()) {
          sampleIdToLabel.put(s.sampleId.toString(), s[sampleField])
        } else {
          sampleIdToLabel.put(s.sampleId.toString(), s.sampleId)
        }
      }
    }

    def rowClusterResult = null, colClusterResult = null
    if (rowCluster) { rowClusterResult = cluster(matrix, true) }
    if (colCluster) { colClusterResult = cluster(matrix, false) }
    List<Integer> rowClusteredOrder = rowCluster ? rowClusterResult[0] : modules.collect { m -> modules.indexOf(m) }
    List<Integer> colClusteredOrder = colCluster ? colClusterResult[0] : 0..(samples.size() - 1)

    // build the header
    colClusteredOrder.each { col ->
      String sampleId = samples.get(col)
      if (sampleField && sampleIdToLabel.containsKey(sampleId)) {
        headerSb.append(",${sampleIdToLabel.get(sampleId)}")
      } else {
        headerSb.append(",${sampleId}")
      }
    }

    StringBuilder sb = new StringBuilder(4096)
    rowClusteredOrder.each { row ->
      String module = modules[row]
      ModuleAnnotation annotMap = annotations.get(module)
      sb.append("${module},\"${annotMap ? annotMap.annotation : ''}\"")
      if (hasAllColumn) {
        sb.append(",${diffAllColumn.get(module)}")
      }

      colClusteredOrder.each { col ->
        double v = matrix.getQuick(row, col)
        double absV = Math.abs(v)
        double posV = posMatrix ? posMatrix.getQuick(row, col) : -1, negV = negMatrix ? negMatrix.getQuick(row, col) : -1

        // start drawing plot
        if (showRowSpots || absV > floor) {
          if (isPie) {
            sb.append(",\"${negV},${posV}\"")
          }
          else {
            sb.append(",${v}")
          }
        }
        else {
          if (isPie) {
            sb.append(",\"0,0\"")
          }
          else {
            sb.append(",0")
          }
        }
      }
      sb.append(SEPARATOR)
    }

    return "${headerSb.toString()}${SEPARATOR}${sb.toString()}"
  }

  Map getPlotData(Analysis analysis, boolean diff, boolean showControls, boolean annotatedOnly, double floor, int moduleCount = -1)
  {
    SampleSet ss = SampleSet.findById(analysis.sampleSetId)
    Map model = [:]
    if (ss.chipType?.technology?.name == "Focused Array")
    {
      Map files = getFcFile(analysis)
      Map fc = files.fc

      List<String> samples = fc.header
      samples.remove(0)   // ignore the module name column

      Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))
      Map fcLoad = loadMatrix(fc.rows, annotatedOnly ? annotations : [:], [], false, false)
      DoubleMatrix2D fcMatrix = null

      // Filter controls
      if (!showControls)
      {
        def groups = analysisGroups(analysis, true, true)

        // Determine which columns to filter out if show controls is false
        List<Integer> validColumns = []
        groups.entrySet().each {
          String theSample = it.key
          // Keep only cases (i.e. group num = 1)
          if (it.value.groupNum == 1)
          {
            int idx = samples.indexOf(theSample)
            if (idx != -1) {
              validColumns.push(samples.indexOf(theSample))
            }
          }
        }
        samples = validColumns.collect { samples[it] }

        fcMatrix = fcLoad.matrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()]))
      }
      else
      {
        fcMatrix = fcLoad.matrix
      }

      fcMatrix.assign(log2Func) // inverse or log 2

      // Filtering based on floor
      def rowsToKeep = []
      int i = 0, j = 0
      fcMatrix = fcMatrix.viewSelection(new DoubleMatrix1DProcedure() {
        boolean apply(DoubleMatrix1D m) {
          boolean passed = m.aggregate(Functions.max, Functions.abs) > floor
          if (passed) {
            rowsToKeep.push(j)
            i++
          }
          else {
            fcLoad.modules.remove(i)
          }
          j++
          return passed
        }
      }).copy();

      model.samples = samples
      model.modules = fcLoad.modules
      model.matrix = fcMatrix
      model.annotations = annotations
      model.isFocusedArray = true
    }
    else
    {
      Map files = getDifferenceFiles(analysis, true)
      Map posMongo = files.positive
      Map negMongo = files.negative
      Map allMongo = files.allColumn
      String allType = files.allType

      List<String> samples = posMongo.header
      samples.remove(0)     // ignore the module name column
      boolean hasAllColumn = samples.get(0) == "All"
      if (hasAllColumn) {
        samples.remove(0)   // ignore the 'All' column
      }

      Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))

      // Matrices are filtered to modules with annotations if "Annotated" selected
      Map posResult = loadMatrix(posMongo.rows, annotatedOnly ? annotations : [:], [], hasAllColumn, moduleCount > 0)
      Map negResult = loadMatrix(negMongo.rows, annotatedOnly ? annotations : [:], [], hasAllColumn, moduleCount > 0)

	  //DoubleDoubleFunction func = diff ? diffFunc : largerFunc
	  DoubleDoubleFunction func = diffFunc

      // read positive and negative files
      Map diffAllColumn = [:]
      SparseDoubleMatrix2D posMatrix = posResult.matrix
      SparseDoubleMatrix2D negMatrix = negResult.matrix

      // Filter controls
      if (!showControls)
      {
        int control = 0
        def matWizard = MATWizard.findByAnalysisId(analysis.id)
        if (matWizard) {
          control = analysis.runDate.before(Date.parse("yyyy-MM-dd", "2012-03-30")) ? 1 : 0
        }
        def groups = analysisGroups(analysis, true, false)

        // Determine which columns to filter out if show controls is false
        List<Integer> validColumns = []
        groups.entrySet().each {
          if (it.value.groupNum != control)
          {
            validColumns.push(samples.indexOf(it.key))
          }
        }
        samples = validColumns.collect { samples[it] }

        posMatrix = posMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
        negMatrix = negMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
      }

      SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(posMatrix.rows(), posMatrix.columns())
      matrix.assign(posMatrix)
      matrix.assign(negMatrix, func)

      if (allMongo && allType == "diff") {
        allMongo.rows.each { values ->
          diffAllColumn.put(values[0], values[1])
        }
      } else if (allMongo && allType == "mls") {
        String deltaType = analysis.deltaType == "fold" ? "_FC" : "_Zscore"
        int allColumn = allMongo.header.indexOf("all${deltaType.substring(1)}PercentDiff".toString())
        allMongo.rows.each { values ->
          diffAllColumn.put(values[0], values[allColumn])
        }
      }

      model.samples = samples
      model.modules = posResult.modules
      model.posMatrix = posMatrix
      model.negMatrix = negMatrix
      model.matrix = matrix
      model.diffAllColumn = diffAllColumn
      model.annotations = annotations
      model.hasAllColumn = hasAllColumn
    }

    // Filtering based on floor
    def rowsToKeep = []
    int i = 0, j = 0
    model.matrix = model.matrix.viewSelection(new DoubleMatrix1DProcedure() {
      boolean apply(DoubleMatrix1D m) {
        boolean passed = m.aggregate(Functions.max, Functions.abs) > floor
        if (passed) {
          rowsToKeep.push(j)
          i++
        }
        else {
          model.modules.remove(i)
        }
        j++
        return passed
      }
    }).copy();
    int[] rowsToKeepArray = rowsToKeep.toArray(new int[rowsToKeep.size()])
    if (model.posMatrix) {
      model.posMatrix = model.posMatrix.viewSelection(rowsToKeepArray, null).copy();
    }
    if (model.negMatrix) {
      model.negMatrix = model.negMatrix.viewSelection(rowsToKeepArray, null).copy();
    }

    return model
  }

  Map individualModulePlot(Analysis analysis, String plotName, boolean annotatedOnly, boolean isPie,
                           boolean diff, boolean showRowSpots, boolean showControls, double floor, double maxFoldChange,
                           String sampleField, int moduleCount = -1, String rowCluster = null, String colCluster = null) {

    Map plotData = getPlotData(analysis, diff, showControls, annotatedOnly, floor, moduleCount)
    def matrix = plotData.matrix
    def modules = plotData.modules
    def samples = plotData.samples
    def annotations = plotData.annotations
    def posMatrix = plotData.posMatrix
    def negMatrix = plotData.negMatrix
    def isFocusedArray = plotData.isFocusedArray ?: false

    def modToNumProbes = getProbeCounts(analysis)
    def groups = analysisGroups(analysis, true, isFocusedArray)

    // replace with user selected sample header
    Map sampleIdToLabel = null
    if (sampleField) {
      sampleIdToLabel = [:]
      List result = mongoDataService.getColumnValuesAndSampleId(analysis.sampleSetId, sampleField)
      result.each { Map s ->
        if (s[sampleField] && !((String) s[sampleField]).isAllWhitespace()) {
          sampleIdToLabel.put(s.sampleId.toString(), s[sampleField])
        } else {
          sampleIdToLabel.put(s.sampleId.toString(), s.sampleId)
        }
      }
    }

    def rowClusterResult = null, colClusterResult = null
    if (rowCluster) { rowClusterResult = cluster(matrix, true) }
    if (colCluster) { colClusterResult = cluster(matrix, false) }
    List<Integer> rowClusteredOrder = rowCluster ? rowClusterResult[0] : modules.collect { m -> modules.indexOf(m) }
    List<Integer> colClusteredOrder = colCluster ? colClusterResult[0] : 0..(samples.size() - 1)

    def rowDendrogram = rowCluster ? dendrogram(rowClusterResult[1], colClusteredOrder.size()) : null
    def colDendrogram = colCluster ? dendrogram(colClusterResult[1], colClusteredOrder.size()) : null
    def dendrogramHeight = colCluster ? dEndBase - 34 : 0

//    println "done clustering @ ${System.currentTimeMillis() - startTime}ms"

    def r = 17, pad = 3, topPadding = dendrogramHeight + 50, leftPadding = 300, cellWidth = (2 * (pad + r)), spotUnit = pad + r
    def cy = topPadding, annotationY = topPadding
    def circles = [], paths = [], sampleLbls = [], groupsKey = [], moduleLabels = [], annotationKeys = []

    rowClusteredOrder.each { row ->
      String module = modules[row]
      ModuleAnnotation annotMap = annotations.get(module)
      cy += spotUnit

      def textLabel = annotMap ? "${module} ${annotMap.annotation}" : module
      Map moduleData = [info: ["Module": module]]
      if (!isFocusedArray) {
        moduleData.info["# of probes"] = modToNumProbes.get(module)
      }
      if (analysis.sampleSetId != -1 && grailsApplication.config.mat.access.gxb && !isFocusedArray)
      {
        if (SampleSet.findByIdAndMarkedForDeleteIsNull(analysis.sampleSetId)) {
          def gxbLink = new ApplicationTagLib().createLink(controller:"geneBrowser", action:"show", id:analysis.sampleSetId, params:[ module:module, analysisId:analysis.id ])
          moduleData.gxbLink = "<a href='"+gxbLink+"' target='_blank'>View in Gene Expression Browser</a>";
        }
      }
      if (analysis.modGeneration == "2" && grailsApplication.config.module.wiki.on) {
        moduleData.moduleWiki = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>[Module Wiki]</a>"
      } else if (analysis.modGeneration == "3" && grailsApplication.config.module.wiki.on) {
        moduleData.moduleWiki = "<a href='http://mat.benaroyaresearch.org/wiki/index.php/Generation_3_Modules_${module}' target='_blank'>[Module Wiki]</a>"
      }
      moduleLabels.push([x: leftPadding - 40, y: cy, text: textLabel, attr: [font: "16px Fontin-Sans, Arial", "text-anchor": "end"], data: moduleData])

      def cx = leftPadding, addedValue = false
      colClusteredOrder.each { col ->
        cx += spotUnit
        Double v = matrix.getQuick(row, col)
        Double absV = Math.abs(v)
        Double posV = posMatrix?.getQuick(row, col), negV = negMatrix?.getQuick(row, col)

        // populate module info
        String sampleId = samples.get(col).trim()
        Map data = [info: ["Module": module], group: groups.get(sampleId).groupLabel]
        if (annotMap) {
          data.info["Function"] = annotMap.annotation
          data.color = annotMap.hexColor
        }
        if (posV) {
          data.info["% Positive"] = "${posV.round(1)} %"
        }
        if (negV) {
          data.info["% Negative"] = "${Math.abs(negV.round(1))} %"
        }
        data.sampleId = sampleId
        if (sampleField && sampleIdToLabel.containsKey(sampleId)) {
          data.sampleLabel = sampleIdToLabel.get(sampleId)
        }

        // start drawing plot
        if (showRowSpots || absV > floor) {
          addedValue = true
          if (isPie) {
            def pos = (posV / 100) * 360, neg = (-negV / 100) * 360
            if (neg < 0d && neg != -360d) {
              paths.push([segment: [cx, cy, r, 270 + neg, 270, -1]])
            }
            if (pos > 0d && pos != 360d) {
              paths.push([segment: [cx, cy, r, 270, 270 + pos, 1]])
            }
            def fill = pos == 360d ? "#ff0000" : ((neg == -360d) ? "#0000ff" : "#ffffff")
            def fillOpacity = (pos == 360d || neg == -360d) ? 1 : 0
            circles.push([cx: cx, cy: cy, r: r, attr: [fill: fill, "fill-opacity": fillOpacity, stroke: "#666666"], data: data])
          }
          else {
            data.value = isFocusedArray ? "${absV.round(3)}" : "${absV.round(0)} %"
            data.up = up(MOD, posV ?: -1, negV ?: -1, v)
            def spotValue = isFocusedArray ? absV / maxFoldChange : spotOpacity(absV)
            def attr = [stroke: "none", fill: color(MOD, posV ?: -1, negV ?: -1, v), "fill-opacity": spotValue]
            circles.push([cx: cx, cy: cy, r: r, attr: attr, data: data])
          }
        }
        cx += spotUnit
      }
      cy += pad + r
      if (annotMap) {
        annotationKeys.push([type: "rect", x: leftPadding - 30, y: annotationY, width: 20, height: cellWidth, fill: annotMap.hexColor, stroke: "none"])
      }
      annotationY += cellWidth

    }
//    println "done iterating @ ${System.currentTimeMillis() - startTime}ms"

    def x = leftPadding, groupX = leftPadding
    def attr = [font: "16px Fontin-Sans, Arial", "text-anchor": "end"]
    def orderedSamples = []
    colClusteredOrder.each { sIdx ->
      String s = samples[sIdx].trim()
      orderedSamples.push(s)
      x += spotUnit
      String sampleLabel = sampleField ? sampleIdToLabel.get(s) : s
      sampleLbls.push([x: x + 2, y: cy + 30, text: sampleLabel, attr: attr, data: [sampleId: s, cx: groupX]])
      def groupColor = groups.get(s).color //GROUP_COLORS[groups.get(s).groupNum]
      def groupData = [sampleId: s, group: groups.get(s).groupLabel]
      if (sampleLabel) {
        groupData.sampleLabel = sampleLabel
      }
      groupsKey.push([x: groupX, y: topPadding - 16, width: cellWidth - 1, height: 8, attr: [fill: groupColor, stroke: "none"], data: groupData])
      groupsKey.push([x: groupX, y: cy + 10, width: cellWidth - 1, height: 8, attr: [fill: groupColor, stroke: "none"], data: groupData])
      x += spotUnit
      groupX += cellWidth
    }
    def width = samples.size() * (2 * (pad + r)) + 6
    def height = cy - topPadding + 6

    def bg = [type: "rect", x: leftPadding - 3, y: topPadding - 3, width: width, height: height, stroke: "#111111", "stroke-width": 2]
    def chartWidth = leftPadding + width + 50
    def chartHeight = cy + (2 * topPadding)

    def model = [width: chartWidth, height: chartHeight, bg: bg, sampleLbls: sampleLbls, groupsKey: groupsKey, moduleLabels: moduleLabels,
      annotationKeys: annotationKeys, samples: orderedSamples, rowDendrogram: rowDendrogram, spots: circles,
      colDendrogram: colDendrogram, startY: topPadding, endY: cy]
    if (isPie) {
      model.segments = paths
    }
//    println "done @ ${System.currentTimeMillis() - startTime}ms"

    return model
  }

//  private Map getFcFile(Analysis analysis) {
//    def dsName = analysis.datasetName.replaceAll(" ", "_")
//    String fcFilename = "individualModuleLevelFC.csv";
//    Pattern fcPattern = Pattern.compile(fcFilename, Pattern.CASE_INSENSITIVE)
//    boolean fcMongoCsv = mongoDataService.exists(CSVCOLLECTION, [analysisId: analysis.id, filename: fcPattern], null)
//    if (!fcMongoCsv) {
//      // If the files don't exist in Mongo yet, add them
//      def fcCsvFile = matDataService.getFile(analysis, fcFilename)
//      mongoDataService.storeCsv(fcCsvFile, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
//    }
//    Map fcMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: fcPattern], ["header", "rows"])
//    return [ fc:fcMongo ]
//  }
  private Map getFcFile(Analysis analysis) {
    def dsName = analysis.datasetName.replaceAll(" ", "_")
    String fcFilename = "${dsName}_Individual_Module_Level_FC(_.*)+\\.csv".toString()
    Pattern fcPattern = Pattern.compile(fcFilename, Pattern.CASE_INSENSITIVE)
    boolean fcMongoCsv = mongoDataService.exists(CSVCOLLECTION, [analysisId: analysis.id, filename: fcPattern], null)
    if (!fcMongoCsv) {
      // If the files don't exist in Mongo yet, add them
      def fcCsvFile = matDataService.getFile(analysis, fcFilename)
      mongoDataService.storeCsv(fcCsvFile, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
    }
    Map fcMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: fcPattern], ["header", "rows"])
    return [ fc:fcMongo ]
  }

  private Map getDifferenceFiles(Analysis analysis, boolean includeAllColumn = true, boolean includeNegative = true) {
	  
	long start = System.currentTimeMillis()
    def dsName = analysis.datasetName.replaceAll(" ", "_")

    // Check if this is the new version of MAT
    String newVersionFile = "${dsName}_Module_Level_Statistics(_.*)+\\.csv".toString()
    File newAllCsv = matDataService.getFile(analysis, newVersionFile)
    String allType = newAllCsv ? "mls" : "diff"
    Map allColumnMongo = null
    if (includeAllColumn) {
      if (newAllCsv) {
        Pattern allPattern = Pattern.compile(newVersionFile, Pattern.CASE_INSENSITIVE)
		allColumnMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: allPattern], ["header", "rows"])
		if (!allColumnMongo) {
          mongoDataService.storeCsv(newAllCsv, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
		  allColumnMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: allPattern], ["header", "rows"])
        }
      }
	  else
	  {
        // Look in the difference file
        String diffFilename = "${dsName}_Percent_Difference(_.*)+\\.csv".toString()
        Pattern diffPattern = Pattern.compile(diffFilename, Pattern.CASE_INSENSITIVE)
		allColumnMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: diffPattern], ["header", "rows"])
        if (!allColumnMongo)
		{
          File diffFile = matDataService.getFile(analysis, diffFilename)
          mongoDataService.storeCsv(diffFile, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
		  allColumnMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: diffPattern], ["header", "rows"])
        }
      }
    }

    String deltaType = newAllCsv ? (analysis.deltaType == "fold" ? "_FC" : "_Zscore") : ""
    String posFilename = "${dsName}_Positive_Percent${deltaType}(_.*)+\\.csv".toString()
    Pattern posPattern = Pattern.compile(posFilename, Pattern.CASE_INSENSITIVE)
	Map posMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: posPattern], ["header", "rows"])
	if (!posMongo)
	{
		def posCsvFile = matDataService.getFile(analysis, posFilename)
		mongoDataService.storeCsv(posCsvFile, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
		posMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: posPattern], ["header", "rows"])
		
	}
	
	Map negMongo = null;
	if (includeNegative) {
		String negFilename = "${dsName}_Negative_Percent${deltaType}(_.*)+\\.csv".toString()
		Pattern negPattern = Pattern.compile(negFilename, Pattern.CASE_INSENSITIVE)
		negMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: negPattern], ["rows"])
		if (!negMongo)
		{
			def negCsvFile = matDataService.getFile(analysis, negFilename)
			mongoDataService.storeCsv(negCsvFile, true, CSVCOLLECTION, [analysisId: analysis.id, sampleSetId: analysis.sampleSetId])
			negMongo = mongoDataService.findOne(CSVCOLLECTION, [analysisId: analysis.id, filename: negPattern], ["rows"])
		}
	}
	    //long start = System.currentTimeMillis()
    //println "get file = ${System.currentTimeMillis() - start}ms"
//	println "headers: " + posMongo.header
//	println "rows: " + posMongo.rows
    return [positive: posMongo, negative: negMongo, allColumn: allColumnMongo, allType: allType]
  }

  private def cachePlot(Map model, long analysisId, String plotName, String csvHeader, String csv) {
    try {
//      def jsonPlot = model as JSON
      def csvContent = [csvHeader: csvHeader, csv: csv]
      if (model?.samples) {
        csvContent.put("sampleIds", model.samples)
      }
//      mongoDataService.insert("matplots", [analysisId: analysisId, plotName: plotName], [jsonplot: jsonPlot.toString(false)])
      mongoDataService.insert("matfiles", [analysisId: analysisId, plotName: plotName], csvContent)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  private def Map loadMatrix(List data, Map<String,ModuleAnnotation> annotations, List<String> customModules = [], boolean hasAllColumn, boolean top = false) {
    SparseDoubleMatrix2D matrix = null
    List<String> modules = new ArrayList<String>()
    int rows = data.size(), cols = 0, cRow = 0
	// Sort in module order (Module is a number, not a string.)
	//data.sort { Float.parseFloat(it[0].substring(1)) }
	List sdata = data.sort({a, b ->
			String[] aParts = a[0].substring(1).split("\\.")
			int a1 = aParts[0].toInteger()
			int a2 = aParts[1].toInteger()
			String[] bParts = b[0].substring(1).split("\\.")
			int b1 = bParts[0].toInteger()
			int b2 = bParts[1].toInteger()

			if (a1 > b1) { return 1 }
			else if (a1 < b1) { return -1 }
			else if (a1 == b1) {
				if (a2 > b2) { return 1 }
				else if (a2 < b2) { return -1 }
			}	
		return 0
	})

    sdata.eachWithIndex { List<String> values, int row ->
      if (row == 0) {
        cols = values.size() - 1
        // ignore the all column if it's found
        if (hasAllColumn) {
          cols--
        }
        matrix = new SparseDoubleMatrix2D(rows, cols)
      }

      String module = values.remove(0)
      boolean pass = true

	  if (annotations && !annotations?.containsKey(module))
	  {
        pass = false
      }
	  else if (top && module.split("\\.")[0].substring(1).toInteger() > 6)
	  {
		pass = false
	  }
	  else if (customModules && !customModules?.contains(module)) 
	  {
		pass = false
	  }
	  
      if (pass) {
        modules.push(module)
        // ignore the all column if it's found
        if (hasAllColumn) {
          values.remove(0)
        }
		values = values.collect { valStr -> (valStr == 'NA') ? 0 : valStr }
        matrix.viewRow(cRow).assign(values*.toDouble().toArray(new double[values.size()]))
        cRow++
      }
    }

    if (top || annotations || customModules) {
      return [matrix: matrix.viewPart(0, 0, cRow, cols), modules: modules]
    } else {
      return [matrix: matrix, modules: modules]
    }
  }

  private def dendrogram(ClusterNode tree, int numSamples) {
    List lines = [], clickpoints = []
    dEndBase = Math.round(Math.ceil(tree.getDistanceFromLeaf() * VSCALE)) + 20
    drawDendrogramBranch(tree, lines, clickpoints, numSamples)
    return [lines: lines, clickpoints: clickpoints]
  }

  private def void drawDendrogramBranch(ClusterNode node, List lines, List clickpoints, int numSamples) {
    List<ClusterNode> children = node.getChildren();
    if (children == null)
      return;
    assert (children.size() > 0);
    double y = dEndBase - Math.round(node.getDistanceFromLeaf() * VSCALE) - 1
    double x0 = children.get(0).getOffset() * HSCALE + DENDLEFT
    double x1 = children.get(children.size() - 1).getOffset() * HSCALE + DENDLEFT
    def hLine = [type: "hLine", x: Math.round(x0), y: y, width: Math.round(x1 - x0), height: 2, attr: [fill: "#333", stroke: "none", "stroke-width": 0]] //horiz line over children
    double c0 = Math.floor(node.getOffset()), c1 = Math.ceil(node.getOffset())
    if (node.getChildren() != null) {
      List<ClusterNode> cChildren = node.getChildren()
      ClusterNode leftChild = cChildren.get(0)
      while (leftChild.getChildren() != null) {
        leftChild = leftChild.getChildren().get(0)
      }
      ClusterNode rightChild = cChildren.get(cChildren.size() - 1)
      while (rightChild.getChildren() != null) {
        def right = rightChild.getChildren().size() - 1
        rightChild = rightChild.getChildren().get(right)
      }
      c0 = Math.floor(leftChild.getOffset())
      c1 = Math.ceil(rightChild.getOffset())
    }
    c0 = c0 == 0d ? -1 : DENDLEFT + c0 * BOX_WIDTH
    c1 = c1 == numSamples ? -1 : DENDLEFT + c1 * BOX_WIDTH
    hLine.data = [c0: (c0 + 1), c1: c1]
    lines.push(hLine)
    for (ClusterNode child: children) {
      double x = child.getOffset() * HSCALE + DENDLEFT - 1
      double height = dEndBase - child.getDistanceFromLeaf() * VSCALE - y
      lines.push([type: "vLine", x: Math.round(x), y: y, width: 2, height: Math.round(height), attr: [fill: "#333", stroke: "none", "stroke-width": 0]]) //vert line down to child
      //      clickpoints.push([x:(Math.round(x)+1), y:(y+1), r:3, attr:[fill:"#333", stroke:"none", "stroke-width":0], data:[c0:(c0+1), c1:c1]])
      drawDendrogramBranch(child, lines, clickpoints, numSamples) //recurse
    }
  }

  def Map analysisGroups(Analysis a, boolean perSample = true, boolean isFocusedArray = false) {
    //SampleSet ss = SampleSet.findById(a.sampleSetId)
    //boolean isFocusedArray = ss.chipType?.technology?.name == "Focused Array"

    def groups = [:]
    if (!mongoDataService.exists(CSVCOLLECTION, [analysisId: a.id, filename: "designFile.csv"], null)) {
      File designFile = matDataService.getDataFile(a, "designFile.csv")
      mongoDataService.storeCsv(designFile, true, CSVCOLLECTION, [analysisId: a.id, sampleSetId: a.sampleSetId])
    }
    def designFile = mongoDataService.findOne(CSVCOLLECTION, [analysisId: a.id, filename: "designFile.csv"], ["rows"])
    if (designFile) {
      int groupNumCol = isFocusedArray ? 5 : 2
      int groupLabelCol = isFocusedArray ? 2 : 3
      int sampleIdCol = isFocusedArray ? 0 : 1
      designFile.rows.each {
        int groupNum = Integer.parseInt(it[groupNumCol])
        if (groupNum != -1) {
          String groupLabel = isFocusedArray ? getGroupLabel(groupNum) : it[groupLabelCol].encodeAsHumanize()
          if (perSample) {
            groups.put(it[sampleIdCol], [groupNum: groupNum, groupLabel: groupLabel, color: GROUP_COLORS[groupNum]])
          } else {
            if (!groups.containsKey(groupLabel)) {
              groups.put(groupLabel, [ groupNum:groupNum, color:GROUP_COLORS[groupNum] ])
            }
          }
        }
      }
    }
    return groups
  }

  private String getGroupLabel(int groupNum) {
    if (groupNum) {
      return "Control"
    }
    return "Case"
  }

  private def Map getProbeCounts(Analysis a) {
    def modGen = getModuleGen(a)
    def modToProbeCount = [:]
    def query = """SELECT m.module_name 'module', COUNT(DISTINCT md.probe_id) 'probeCount' FROM module_detail AS md
      LEFT JOIN module AS m ON md.module_id = m.id
      WHERE m.module_generation_id = ${modGen}
      GROUP BY md.module_id"""
    Sql sql = Sql.newInstance(dataSource)
    sql.eachRow(query) {
      modToProbeCount.put(it.module, it.probeCount)
    }
    sql.close()
    return modToProbeCount
  }

  private long getModuleGen(Analysis a) {
    def versionName = Version.findById(a.moduleVersion)?.versionName
	//println "moduleVersion: " + a.moduleVersion + " versionName: " + versionName 
    if (!versionName) {
      versionName = a.moduleVersionFile
      versionName = versionName.substring(0, versionName.length() - 4)
      if (versionName == "IlluminaV2") {
        versionName = "IlluminaG2V2"
      }
    }
    return ModuleGeneration.findByVersionName(versionName)?.id
  }

  private def List annotationKeyWithLabels(Map<String,ModuleAnnotation> annotations, boolean focusedArray = false, boolean top = false) {
    int leftPadding = focusedArray ? 50 : 150
    def annotationLabels = [:]
    def aCount = 0
    annotations.each { String module, ModuleAnnotation aMap ->
      String annotation = aMap.annotation
      if (!top || (top && Integer.parseInt(module[1]) <= 6)) {
        if (!annotationLabels.containsKey(annotation)) {
          def attr = [box: [fill: aMap.hexColor, stroke: "#333", "stroke-width":1], text: [fill: "#333", "text-anchor": "start", "font-size": 12]]
          def label = [x: (aCount % 3 * 250 + leftPadding), y: (Math.floor(aCount / 3) * 20), width: 12, height: 12, text: annotation, attr: attr]
          annotationLabels.put(annotation, label)
          aCount++
        }
      }
    }
    return annotationLabels.values().asList()
  }

  private def List cluster(DoubleMatrix2D features, boolean byRow) {
	List<Integer> items = new ArrayList<Integer>()
    ClusterHierarchy hierarchy = HierarchicalClusterer.clusterVectors(features, HierarchicalClusterer.Metric.PEARSON, byRow);
    ClusterNode tree = hierarchy.getTree();
    tree.getItems(items);
	//println "items: " + items
	 
    return [items, hierarchy.getTree()]
  }

  Map correlationPlot(Analysis analysis, String field, double floor, double lFilter, double uFilter, String moduleCount) {
    boolean top = moduleCount == "top"
    def mongoCor = mongoDataService.findOne("correlations", [analysisId:analysis.id, field:field], ["correlation"])
    Map corCoeff = null
    if (mongoCor) {
      corCoeff = mongoCor.correlation
    } else {
      long startTime = System.currentTimeMillis()
      Map matrix = prepareMatrix(analysis)
      corCoeff = calcCorrelation(matrix.matrix, analysis.sampleSetId, matrix.samples, matrix.modules, field, floor)
      println "cc time = ${System.currentTimeMillis() - startTime}ms"
    }

    long moduleGenerationId = getModuleGen(analysis)
    int modGen = ModuleGeneration.findById(moduleGenerationId).generation
    def availableModules = Module.findAllByModuleGenerationId(moduleGenerationId).collect { it.moduleName }
    Map<String,ModuleAnnotation> annotations = getAnnotations(modGen)
    Map probeCounts = getProbeCounts(analysis)

    long startTime = System.currentTimeMillis()
    // sort the modules correctly
    Map m1ToMaxM2 = [:]
    List moduleValues = []
    corCoeff.each { String module, Map corrRslt ->
      double coeff = corrRslt.statistic;
      String[] modules = module.substring(1).split("_")
      int m1 = modules[0].toInteger()
      int m2 = modules[1].toInteger()
      if (!(top && m1 > 6)) {
        moduleValues.push([ module:module, m1:m1, m2:m2, coeff:coeff ])
        if (!m1ToMaxM2.containsKey(m1)) {
          m1ToMaxM2.put(m1, m2)
        } else {
          int max = Math.max(m2, m1ToMaxM2.get(m1))
          m1ToMaxM2.put(m1, max)
        }
      }
      availableModules.remove(module)
    }
    availableModules.each { String module ->
      String[] modules = module.substring(1).split("\\.")
      int m1 = modules[0].toInteger()
      int m2 = modules[1].toInteger()
      if (!(top && m1 > 6)) {
        moduleValues.push([ module:module, m1:m1, m2:m2, coeff:0 ])
      }
    }
//    println "mapping @ ${System.currentTimeMillis() - startTime}ms"

    moduleValues.sort { a, b ->
      if (a.m1 > b.m1) { return 1 }
      else if (a.m1 < b.m1) { return -1 }
      else if (a.m1 == b.m1) {
        if (a.m2 > b.m2) { return 1 }
        else if (a.m2 < b.m2) { return -1 }
      }
      return 0
    }
//    println "sorting modules @ ${System.currentTimeMillis() - startTime}ms"

    // get additional information for each module
    Map modToNumRows = [:]
//    println "retrieving additional info @ ${System.currentTimeMillis() - startTime}ms"

    // go through each row and draw stuff
    List blanks = [], boxes = [], aBoxes = [], spots = []
    int lastM1 = -1, lastM2 = 0
    def m1y = 0, cy = STARTY + BOX_PADDING + SPOT_RADIUS, y = STARTY
    def maxX = 0, maxY = 0
    moduleValues.each { Map r ->
      String module = "M${r.m1}.${r.m2}".toString()
      double coeff = r.coeff
      double absCoeff = Math.abs(coeff).round(2)
      boolean up = r.coeff > 0
      ModuleAnnotation annotInfo = annotations.get(module)

      // add additional module info
      Map spotInfo = [up:up, value:absCoeff, info:[ "Module":module, "# of Probes":probeCounts.get(module) ]]
      if (annotInfo) {
//        if (modGen == 2) {
        if (analysis.modGeneration == "2") {
          spotInfo.info["Function"] = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>${annotInfo.annotation}</a>"
        } else {
          spotInfo.info["Function"] = annotInfo.annotation
        }
        spotInfo.annotation = annotInfo.annotation
        spotInfo.color = annotInfo.hexColor
      }

      double offset = (((r.m2 - 1) % MAX_COLS) * BOX_LINE_WIDTH)
      def cx = STARTX + BOX_PADDING + SPOT_RADIUS + offset
      def x = STARTX + offset

      // check for skipped modules
      if (lastM1 != -1 && r.m1 != lastM1) {
        modToNumRows.put(lastM1, (int) Math.ceil(lastM2 / MAX_COLS))
        def col = lastM2 % MAX_COLS
        if (col != 0 && col < MAX_COLS) {
          def diffX = LEFT_PADDING + BORDER_WIDTH + col * BOX_LINE_WIDTH
          def diffWidth = (MAX_COLS - col) * BOX_LINE_WIDTH - COL_SPACING // - 1 ?
          def diffY = y + m1y
          def height = BOX_WIDTH
          if (lastM2 > MAX_COLS) {
            diffY++
            height--
          }
          blanks.push([type: "rect", x: diffX, y: diffY, width: diffWidth, height: height, stroke: "none", fill: BLANK_COLOR])
        }
        cy += BOX_LINE_WIDTH + m1y + 1
        y += BOX_LINE_WIDTH + m1y + 1
        m1y = 0
      }
      else {
        def currentM2 = lastM2 + 1
        while (currentM2 < r.m2) {
          def mod = currentM2 % MAX_COLS
          def col = (currentM2 > 0) && mod == 0 ? MAX_COLS - 1 : currentM2 % MAX_COLS - 1
          def diffX = STARTX + col * BOX_LINE_WIDTH
          def tempRy = (int) (currentM2 / MAX_COLS)
          def m1Ry = (mod == 0 ? tempRy - 1 : tempRy) * BOX_LINE_WIDTH
          blanks.push([type: "rect", x: diffX, y: (y + m1Ry), width: BOX_WIDTH, height: BOX_WIDTH, stroke: "none", fill: BLANK_COLOR])
          currentM2++
        }

        def tempCy = (int) (r.m2 / MAX_COLS)
        m1y = ((r.m2 % MAX_COLS) == 0 ? tempCy - 1 : tempCy) * BOX_LINE_WIDTH
      }

      maxX = Math.max(maxX, x)
      maxY = Math.max(maxY, (y + m1y))

      if (coeff >= lFilter && coeff < uFilter) {
        String color = coeff > 0 ? "#FF0000" : "#0000FF"
        def attr = [ stroke: "none", fill:color, "fill-opacity":absCoeff ]
        boxes.push([ x:x, y:(y + m1y), width:BOX_WIDTH, height:BOX_WIDTH, attr:attr, data:spotInfo ])
//        spots.push([ cx:cx, cy:(cy + m1y), r:SPOT_RADIUS, attr:attr, data:spotInfo])
      } else {
        boxes.push([ x:x, y:(y + m1y), width:BOX_WIDTH, height:BOX_WIDTH, attr:[ stroke:"none", fill:"#FFF" ], data:spotInfo ])
      }

      if (annotInfo) {
        Map aBoxInfo = [ color:annotInfo.hexColor, annotation:annotInfo.annotation ]
//        if (modGen == 2) {
        if (analysis.modGeneration == "2") {
          aBoxInfo.info = [ "Function":"<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>${annotInfo.annotation}</a>" ]
        } else {
          aBoxInfo.info = [ "Function":annotInfo.annotation ]
        }

        String aColor = annotInfo.hexColor ?: "#FFF"
        Map aBox = [path:"M${x + 0.7 * BOX_WIDTH} ${(y + m1y)}H${x + BOX_WIDTH}V${(y + m1y) + BOX_WIDTH * 0.3}Z", x:x, y:(y + m1y), width:BOX_WIDTH, height:BOX_WIDTH, attr:[ stroke:"none", fill:aColor, "fill-opacity":0 ], data:aBoxInfo ]
        if (annotInfo.abbreviation)
        {
          aBox.text = annotInfo.abbreviation
          aBox.cx = x + HALF_BOX_WIDTH
          aBox.cy = y + m1y+HALF_BOX_WIDTH
        }
        aBoxes.push(aBox)
      }

//      if (absCoeff > filter) {
//      if (coeff >= lFilter && coeff < uFilter) {
//        String color = coeff > 0 ? "#FF0000" : "#0000FF"
//        def attr = [stroke: "none", fill:color]
//        attr.put("fill-opacity", absCoeff)
//        spots.push([ cx:cx, cy:(cy + m1y), r:SPOT_RADIUS, attr:attr, data:spotInfo])
//      }

      lastM1 = r.m1
      lastM2 = r.m2
    }
//    println "iterating @ ${System.currentTimeMillis() - startTime}ms"

    modToNumRows.put(lastM1, (int) Math.ceil(lastM2 / MAX_COLS))
    def col = lastM2 % MAX_COLS
    if (col != 0 && col < MAX_COLS) {
      def diffX = STARTX + col * BOX_LINE_WIDTH
      def diffWidth = (MAX_COLS - col) * BOX_LINE_WIDTH - COL_SPACING // - 1
      def diffY = y + m1y
      def height = BOX_WIDTH
      if (lastM2 > MAX_COLS) {
        diffY++
        height--
      }
      blanks.push([type: "rect", x: diffX, y: diffY, width: diffWidth, height: height, stroke: "none", fill: BLANK_COLOR])
    }

    maxX += BOX_WIDTH + 1
    maxY += BOX_WIDTH + 1

    def xAxis = []
    for (i in 0..(MAX_COLS - 1)) {
      def tx = STARTX + (i * BOX_LINE_WIDTH) + HALF_BOX_WIDTH
      xAxis.push([type: "text", x: tx, y: TOP_PADDING - 12, text: "${i + 1}".toString(), font: "16px Fontin-Sans, Arial"])
      xAxis.push([type: "text", x: tx, y: maxY + 17, text: "${i + 1}".toString(), font: "16px Fontin-Sans, Arial"])
    }
    def yAxis = [], count = 0, labelY = STARTY
    modToNumRows.each { module, rowCount ->
      def labelHeight = rowCount == 1 ? BOX_WIDTH : (rowCount * BOX_WIDTH) + ((rowCount - 1) * COL_SPACING)
      yAxis.push([type: "text", x: 45, y: labelY + (labelHeight / 2), text: "M${module}".toString(), font: "16px Fontin-Sans, Arial", "text-anchor": "end"])
      def ty = labelY
      for (i in 0..(rowCount - 1)) {
        def start = (i * MAX_COLS) + 1, end = Math.min(m1ToMaxM2.get(module), (i + 1) * MAX_COLS)
        yAxis.push([type: "text", x: LEFT_PADDING - 10, y: ty + HALF_BOX_WIDTH, text: "${start}-${end}".toString(), font: "italic 10px Fontin-Sans, Arial", "text-anchor": "end", fill: "#333333"])
        count++
        ty += BOX_WIDTH + 1
      }
      labelY += 2 + labelHeight
    }

    def annotationKeyLabels = annotationKeyWithLabels(annotations, false, false)

    def bg = [ x: LEFT_PADDING, y: TOP_PADDING, width: maxX - LEFT_PADDING, height: maxY - TOP_PADDING, attr:[ stroke: "#111111", "stroke-width": BORDER_WIDTH * 2, fill: "#cccccc" ] ]
    def width = maxX + 30
    def height = maxY + 60

    def model = [ width:width, height:height, bg:bg, xAxis:xAxis, yAxis:yAxis, boxes:boxes, aBoxes:aBoxes, annotations:annotationKeyLabels, blanks:blanks, spots:spots ]

    return model
  }

  String exportCorrelationCsv(Analysis analysis, Map fields, double mFloor, double lFilter, double uFilter,
                              String moduleCount, boolean clusterRow, boolean clusterColumn)
  {
    boolean annotated = moduleCount == "annotated"
    // get the correlations for each field
    List uncachedFields = []
    Set<String> modules = new HashSet<String>()
    Map fieldToCorrelation = [:]
    fields.each { String key, String title ->
      Map mongoCor = mongoDataService.findOne("correlations", [analysisId:analysis.id, field:key], ["correlation"])?.correlation
      if (mongoCor) {
        fieldToCorrelation.put(key, mongoCor)
        modules.addAll(mongoCor.keySet())
      } else {
        uncachedFields.push(key)
      }
    }
    if (!uncachedFields.isEmpty()) {
      Map matrix = prepareMatrix(analysis)
      uncachedFields.each { String field ->
        Map corr = calcCorrelation(matrix.matrix, analysis.sampleSetId, matrix.samples, matrix.modules, field, mFloor)
        fieldToCorrelation.put(field, corr)
        modules.addAll(corr.keySet())
      }
    }

//    long moduleGenerationId = getModuleGen(analysis)
//    Map annotations = getAnnotations(moduleGenerationId)
    Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))
    def sortedModules = modules.collect { String module ->
      String[] parts = module.substring(1).split("_")
      int m1 = parts[0].toInteger()
      int m2 = parts[1].toInteger()
      return [module:module, m1:m1, m2:m2]
    }
    sortedModules.sort { Map a, Map b ->
      if (a.m1 > b.m1) { return 1 }
      else if (a.m1 < b.m1) { return -1 }
      else if (a.m1 == b.m1) {
        if (a.m2 > b.m2) { return 1 }
        else if (a.m2 < b.m2) { return -1 }
      }
      return 0
    }
    if (annotated)
    {
      sortedModules.retainAll { Map mMap ->
        return annotations.containsKey(mMap.module.replaceAll("_","\\."))
      }
    }

    DoubleFunction filterFunc = new DoubleFunction() {
      double apply(double v) {
        if (v != 0 && v >= lFilter && v < uFilter) {
          return 1
        }
        return 0
      }
    }
    SparseDoubleMatrix2D fullMatrix = new SparseDoubleMatrix2D(sortedModules.size(), fields.size())
    List orderedRows = []
    sortedModules.eachWithIndex { Map module, int row ->
      int col = 0
      fieldToCorrelation.each { String field, Map corr ->
        Map corrRslt = corr.get(module.module);
        double corrCoeff = corrRslt?.statistic ?: 0;
        fullMatrix.setQuick(row, col, corrCoeff)
        col++
      }
      double passAgg = fullMatrix.viewRow(row).aggregate(Functions.plus, filterFunc)
      if (passAgg != 0d) {
        orderedRows.push(row)
      }
    }

    if (!orderedRows.isEmpty()) {
      if (orderedRows.size() != sortedModules.size()) {
        int[] rows = new int[orderedRows.size()]
        orderedRows.eachWithIndex { Integer r, int i ->
          rows[i] = r.intValue()
        }
        fullMatrix = fullMatrix.viewSelection(rows, null).copy()
      }

      def rowClusterResult = null, colClusterResult = null
      if (clusterRow) { rowClusterResult = cluster(fullMatrix, true) }
      if (clusterColumn) { colClusterResult = cluster(fullMatrix, false) }
      List<Integer> rowClusteredOrder = clusterRow ? rowClusterResult[0] : 0..(orderedRows.size()-1)
      List<Integer> colClusteredOrder = clusterColumn ? colClusterResult[0] : 0..(fields.size()-1)

      int[] colSelection = colClusteredOrder.toArray(new int[colClusteredOrder.size()])

      StringBuilder headerSb = new StringBuilder()
      headerSb.append("Module,Functional Annotation")
      fieldToCorrelation.each { String field, Map corr ->
        String colLabel = fields.get(field)
        headerSb.append(",${colLabel}")
      }

      StringBuilder sb = new StringBuilder(4096)
      rowClusteredOrder.each { int row ->
        int origRow = orderedRows.get(row)
        String module = sortedModules.get(origRow).module.replaceAll("_","\\.")
        sb.append(module)
        if (annotations.containsKey(module)) {
          sb.append(",${annotations.get(module).annotation}")
        } else {
          sb.append(",")
        }

        DoubleMatrix1D currentRow = fullMatrix.viewRow(row).viewSelection(colSelection)
        int numCols = currentRow.size() - 1
        for (col in 0..numCols) {
          double v = currentRow.getQuick(col)
          if (v != 0d && v >= lFilter && v < uFilter)
          {
            sb.append(",${v}")
          }
        }
        sb.append(SEPARATOR)
      }
      return "${headerSb.toString()}${SEPARATOR}${sb.toString()}"
    }
    return null
  }

  Map moduleAnalysesPlot(ModuleGeneration generation, boolean annotatedOnly, boolean isPie, boolean showRowSpots,
                         double floor, boolean rowCluster, boolean colCluster)
  {
//    Map annotations = getAnnotations(generation.id)
    Map<String,ModuleAnnotation> annotations = getAnnotations(generation.generation)

    SparseDoubleMatrix2D posMatrix = null, negMatrix = null, diffMatrix = null
    List<String> columnNames = []
    if (annotatedOnly) {
      rowNames.retainAll(annotations.keySet())
    }
    rowNames.sort { a, b ->
      String[] aParts = a.substring(1).split("\\.")
      int a1 = aParts[0].toInteger()
      int a2 = aParts[1].toInteger()
      String[] bParts = b.substring(1).split("\\.")
      int b1 = bParts[0].toInteger()
      int b2 = bParts[1].toInteger()

      if (a1 > b1) { return 1 }
      else if (a1 < b1) { return -1 }
      else if (a1 == b1) {
        if (a2 > b2) { return 1 }
        else if (a2 < b2) { return -1 }
      }
      return 0
    }
    int numRows = rowNames.size(), numCols = 0

    List analyses = []
    Sql sql = Sql.newInstance(dataSource)
    String query = """SELECT a.id 'id', dataset_name 'name', sample_set_id 'ssid'
      FROM analysis a
      JOIN analysis_summary s ON a.id = s.analysis_id
      WHERE a.mod_generation = ${generation.generation}
      AND a.sample_set_id IS NOT NULL
      AND a.flag_delete IS NULL
      AND s.analysis_complete_time IS NOT NULL""".toString()
    sql.eachRow(query) {
      analyses.push([ id:it.id, name:it.name, sampleSetId:it.ssid ])
    }
    sql.close()
    analyses.each { Map analysis ->
      def dsName = analysis.name.replaceAll(" ", "_")
      def filename = "${dsName}_260_Module_Group_Comparison(_.*)+\\.csv".toString()
      Pattern p = Pattern.compile(filename, Pattern.CASE_INSENSITIVE)
      boolean mongoCsv = mongoDataService.exists(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], null)
      if (!mongoCsv) {
        // If it doesn't exist yet in Mongo, store it, then draw
        def a = Analysis.findById(analysis.id)
        if (a) {
          def csvFile = matDataService.getFile(a, filename)
          mongoDataService.storeCsv(csvFile, true, CSVCOLLECTION, [ analysisId:analysis.id, sampleSetId:analysis.sampleSetId ])
        }
      }
      Map result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], ["rows"])
      if (result?.rows) {
        List rows = result.rows
        columnNames.push("${analysis.id}")
        if (numCols == 0) {
          posMatrix = new SparseDoubleMatrix2D(numRows, analyses.size())
          negMatrix = new SparseDoubleMatrix2D(numRows, analyses.size())
          diffMatrix = new SparseDoubleMatrix2D(numRows, analyses.size())
        }
        rows.eachWithIndex { List values, int r ->
          int mRow = rowNames.indexOf(values[0])
          if (mRow != -1) {
            posMatrix.setQuick(mRow, numCols, Double.parseDouble(values[1]))
            negMatrix.setQuick(mRow, numCols, Double.parseDouble(values[2]))
          }
        }
        numCols++
      }
    }

    DoubleDoubleFunction func = new DoubleDoubleFunction() {
      public double apply(double a, double b) {
        return a + b
      }
    }

    // derive the diff matrix
    diffMatrix = new SparseDoubleMatrix2D(numRows, numCols)
    diffMatrix.assign(posMatrix.viewPart(0, 0, numRows, numCols))
    diffMatrix.assign(negMatrix.viewPart(0, 0, numRows, numCols), func)
    if (floor > 0d)
    {
      List<String> modulesToRemove = []
      IntArrayList rowsToKeep = new IntArrayList()
      for (i in 0..(numRows-1)) {
        if (diffMatrix.viewRow(i).aggregate(Functions.max, Functions.abs) >= floor)
        {
          rowsToKeep.add(i)
        }
        else
        {
          modulesToRemove.push(rowNames.get(i))
        }
      }
      if (rowsToKeep.size() < numRows) {
        rowsToKeep.trimToSize()
        int[] filteredRows = rowsToKeep.elements()
        diffMatrix = diffMatrix.viewSelection(filteredRows, null).copy()
        posMatrix = posMatrix.viewSelection(filteredRows, null).copy()
        negMatrix = negMatrix.viewSelection(filteredRows, null).copy()
        rowNames.removeAll(modulesToRemove)
      }
    }
    numRows = rowNames.size()

    def rowClusterResult = null, colClusterResult = null
    if (rowCluster) { rowClusterResult = cluster(diffMatrix, true) }
    if (colCluster) { colClusterResult = cluster(diffMatrix, false) }
    List<Integer> rowClusteredOrder = rowCluster ? rowClusterResult[0] : 0..(rowNames.size()-1)
    List<Integer> colClusteredOrder = colCluster ? colClusterResult[0] : 0..(columnNames.size() - 1)

    def rowDendrogram = rowCluster ? dendrogram(rowClusterResult[1], colClusteredOrder.size()) : null
    def colDendrogram = colCluster ? dendrogram(colClusterResult[1], colClusteredOrder.size()) : null
    def dendrogramHeight = colCluster ? dEndBase - 46 : 0

    def r = 17, pad = 3, topPadding = dendrogramHeight + 50, leftPadding = 300, cellWidth = (2 * (pad + r)), spotUnit = pad + r
    def cy = topPadding, annotationY = topPadding
    def circles = [], paths = [], sampleLbls = [], moduleLabels = [], annotationKeys = []

    rowClusteredOrder.each { row ->
      String module = rowNames[row]
      ModuleAnnotation annotMap = annotations.containsKey(module) ? annotations.get(module) : null
      cy += spotUnit

      def textLabel = annotMap ? "${module} ${annotMap.annotation}" : module
      Map moduleData = [ info:[ "Module":module ] ]

      if (generation.generation == 2 && annotMap && grailsApplication.config.module.wiki.on) {
        moduleData.moduleWiki = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>[Module Wiki]</a>"
      }
      moduleLabels.push([ x:leftPadding - 40, y:cy, text:textLabel, attr:[ font:"16px Fontin-Sans, Arial", "text-anchor":"end" ], data:moduleData ])

      def cx = leftPadding, addedValue = false
      colClusteredOrder.each { col ->
        cx += spotUnit
        double v = diffMatrix.getQuick(row, col)
        double absV = Math.abs(v)
        double posV = posMatrix.getQuick(row, col), negV = negMatrix.getQuick(row, col)

        // populate module info
        Map analysis = analyses.get(col)
        Map data = [ info:[ "Module": module ] ]
        if (annotMap) {
          data.info["Function"] = annotMap.annotation
          data.color = annotMap.hexColor
        }
        data.info["% Positive"] = "${posV.round(1)}"
        data.info["% Negative"] = "${Math.abs(negV.round(1))}"
        data.analysisId = analysis.id

        // start drawing plot
        if (showRowSpots || absV > floor) {
          addedValue = true
          if (isPie) {
            def pos = (posV / 100) * 360, neg = (-negV / 100) * 360
            if (neg < 0d && neg != -360d) {
              paths.push([segment: [cx, cy, r, 270 + neg, 270, -1]])
            }
            if (pos > 0d && pos != 360d) {
              paths.push([segment: [cx, cy, r, 270, 270 + pos, 1]])
            }
            def fill = pos == 360d ? "#ff0000" : ((neg == -360d) ? "#0000ff" : "#ffffff")
            def fillOpacity = (pos == 360d || neg == -360d) ? 1 : 0
            circles.push([cx: cx, cy: cy, r: r, attr: [fill: fill, "fill-opacity": fillOpacity, stroke: "#666666"], data: data])
          }
          else
          {
            data.value = "${absV.round(0)} %"
            data.up = up(MOD, posV, negV, v)
            def attr = [stroke: "none", fill: color(MOD, posV, negV, v), "fill-opacity": spotOpacity(absV)]
            circles.push([cx: cx, cy: cy, r: r, attr: attr, data: data])
          }
        }
        cx += spotUnit
      }

      cy += pad + r
      if (annotMap) {
        annotationKeys.push([type: "rect", x: leftPadding - 30, y: annotationY, width: 20, height: cellWidth, fill: annotMap.hexColor, stroke: "none"])
      }
      annotationY += cellWidth
    }

    def x = leftPadding
    def orderedSamples = []
    colClusteredOrder.each { sIdx ->
      String s = columnNames[sIdx]
      orderedSamples.push(s)
      x += spotUnit
      sampleLbls.push([ x:(x + 2), y:(cy + 10), text:s, attr:[ font:"16px Fontin-Sans, Arial", "text-anchor":"end" ], data:[ sampleId:s ]])
      x += spotUnit
    }
    def width = columnNames.size() * (2 * (pad + r)) + 6
    def height = cy - topPadding + 6

    def bg = [ type:"rect", x:(leftPadding - 3), y:(topPadding - 3), width:width, height:height, stroke:"#111111", "stroke-width":2 ]
    def chartWidth = leftPadding + width + 50
    def chartHeight = cy + (2 * topPadding)

    def model = [width: chartWidth, height: chartHeight, bg: bg, sampleLbls: sampleLbls, moduleLabels: moduleLabels,
      annotationKeys: annotationKeys, samples: orderedSamples, rowDendrogram:rowDendrogram, spots: circles,
      colDendrogram:colDendrogram, startY: topPadding, endY: cy]
    if (isPie) {
      model.segments = paths
    }

    return model
  }

  Map getSamplesCount(MetaCat metaCat) {
	  List posheader = []
	  List <Analysis> analyses = []
	  
	  metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id }).each {	analyses.push(Analysis.get(it.id)) }
	  
	  analyses.each { analysis ->
		  Map files = getDifferenceFiles(analysis, false, false)
		  files.positive.header.remove(0)
		  posheader = posheader + files.positive.header
	  }

	  	  //List<String> samples = posMongo.header
	  List<String> samples = posheader
	  //samples.remove(0)     // ignore the module name column
	  boolean hasAllColumn = samples.get(0) == "All"
	  if (hasAllColumn) {
		  samples.remove(0)   // ignore the 'All' column
	  }
	  
	  // Determine which columns to filter out if show controls is false
	  int control = 0
	  int count = 0;
	  analyses.each { analysis ->
		  def matWizard = MATWizard.findByAnalysisId(analysis.id)
		  if (matWizard) {
			  control = analysis.runDate.before(Date.parse("yyyy-MM-dd", "2012-03-30")) ? 1 : 0
		  }
		  def groups = analysisGroups(analysis, true, false)
		  groups.entrySet().each {
			  if (it.value.groupNum != control)
			  {
					  count++
			  }
		  }
	}
  	return [totalSamples: samples.size(), totalCases: count]
  }
  
  def zeroFill(List row, List namedModules) {
	  int col = row.get(0).size() - 1 // first element of a row is the module name
	  List rowModules = row.collect { it.first() }
	  if (rowModules.size() != namedModules.size()) {
		  //println "rows to fill: " + (namedModules - rowModules).size()
		  (namedModules - rowModules).each {
			  List zeros = [it] + [0] * col
			  row = row << zeros
		  }
	  } 
	  return row
  }
  
  // not all rows have the same number of modules. zero fill those that don't have data?
  def mergeRows(a, b)
  {
	  Map tmp = b.collectEntries { [it.first(), it.tail()] }
	  return a.collect { it + tmp[it.first()] }
  }
  
  // There may be multiple samples by the same id in the sample array.
  // Look for the sample id, and the appropriate analysis in the parallel array called asl, and return that index.
  int multiIndexOf(def samp, def asl, String sStr, long aId) {
	  int result = -1
	  def pIndices = samp.findIndexValues({ it as String == sStr })
	  pIndices.each { idx ->
		if (asl[idx.toInteger()] == aId) {
			result = idx
			return result
		}
	  }
	  if (result == -1) {
//		  result = pIndices.get(0)
		  println "warning: no index for '" + sStr + "' and " + aId + " from: " + pIndices
	  }

	  return result
  }
  
  Map getPlotsData(List <Analysis> analyses, int module_generation, boolean isDiff, boolean showControls, boolean annotatedOnly, List<String> customModules, double floor, int moduleCount = -1)
  {
	  
	long startTime = System.currentTimeMillis()
	  
	Map model = [:]

	Map diffAllColumn = [:]

	List posheader = []
	List posrows = []
	List negrows = []
	List aslist = []
	List allheader = []
	List allrows  = []
	Map caseCount = [:]
	Map rowCount = [:]

	Map<String,ModuleAnnotation> annotations = getAnnotations(module_generation)
	//Map<String,ModuleAnnotation> allAnnotations = getAnnotations(module_generation)

	// Need to find the maximum number of modules across the analyses in the set...
	analyses.each { analysis ->
		Map files = getDifferenceFiles(analysis, false, false)
		rowCount.put(analysis.id, files.positive.rows.size())
	}
	
	def amax = rowCount.max { it.value }.key
	def avalue = rowCount.max { it.value }.value
	//println "maxid: " + amax + " of: " + avalue

	// ... then use that to get the module generation for the moduleName list.	
	def generation_id = getModuleGen(Analysis.get(amax))
	// def namedModules = Module.findAllByModuleGenerationId(moduleGenerationId).collect { it.moduleName }
	List namedModules = Module.findAllByModuleGenerationId(generation_id).moduleName.toList()
	
	//println "named modules: " + namedModules.size()
	boolean first = true
	//analyses.sort({ Analysis a, Analysis b -> rowCount.get(a.id) <=> rowCount.get(b.id) }).each { analysis ->
	analyses.each { analysis ->
	  String allType = ""
	  //long loopTime = System.currentTimeMillis()
	  Map files = getDifferenceFiles(analysis, true, true)
	  //println "loop files @ ${System.currentTimeMillis() - loopTime}ms"
	  //println "analysis: " + analysis.displayName

	  if (first) {
		posrows = zeroFill(files.positive.rows, namedModules)
		negrows = zeroFill(files.negative.rows, namedModules)
		first = false
	  } else {
		posrows = mergeRows(posrows, zeroFill(files.positive.rows, namedModules))
		negrows = mergeRows(negrows, zeroFill(files.negative.rows, namedModules))
	  }		  

	  files.positive.header.remove(0)
	  posheader = posheader + files.positive.header
	  
	  // NOTES: we also need to keep track of the analysis these headers/samples came from
	  // do this in a parallel array called aslist, operate on it the same as samples()
	  aslist = aslist + ([analysis.id as Long] * files.positive.header.size())
	  
	  //println "header: " + posheader
	  
	  allheader = files.allColumn.header
	  allrows =   files.allColumn.rows
	  allType =   files.allType
	  
	  if (allrows && allType == "diff") {
		  allrows.each { values ->
			diffAllColumn.put(values[0], values[1])
		  }
	  } else if (allrows && allType == "mls") {
		  String deltaType = analysis.deltaType == "fold" ? "_FC" : "_Zscore"
		  int allColumn = allheader.indexOf("all${deltaType.substring(1)}PercentDiff".toString())
		  allrows.each { values ->
			diffAllColumn.put(values[0], values[allColumn])
		  }
	  }
	  //println "loop end @ ${System.currentTimeMillis() - loopTime}ms"
	}
	println "done getFiles @ ${System.currentTimeMillis() - startTime}ms"
	
	//Map posMongo = files.positive  // header and rows
	//Map negMongo = files.negative
	//Map allMongo = files.allColumn
	//String allType = files.allType

	//List<String> samples = posMongo.header
	List<String> samples = posheader
	//samples.remove(0)     // ignore the module name column
	boolean hasAllColumn = samples.get(0) == "All"
	if (hasAllColumn) {
		samples.remove(0)   // ignore the 'All' column
		aslist.remove(0)
	}
	
	//Map<String,ModuleAnnotation> annotations = getAnnotations(module_generation) 
	Map posResult = loadMatrix(posrows, annotatedOnly ? annotations : [:], customModules, hasAllColumn, moduleCount > 0)
	Map negResult = loadMatrix(negrows, annotatedOnly ? annotations : [:], customModules, hasAllColumn, moduleCount > 0)
	
	// Why "larger" instead of always diff?
	// Checked with Kelly: she says it's from old experimentation.
	//DoubleDoubleFunction func = isDiff ? diffFunc : largerFunc
	DoubleDoubleFunction func = diffFunc

	SparseDoubleMatrix2D posMatrix = posResult.matrix
	SparseDoubleMatrix2D negMatrix = negResult.matrix

	println "done loadMatrix @ ${System.currentTimeMillis() - startTime}ms"
	
	// Determine which columns to filter out if show controls is false
	if (!showControls)
	{
		int offset = 0
		int control = 0

		List<Integer> validColumns = []
		
		analyses.each { analysis ->
			def matWizard = MATWizard.findByAnalysisId(analysis.id)
			if (matWizard) {
				control = analysis.runDate.before(Date.parse("yyyy-MM-dd", "2012-03-30")) ? 1 : 0
			}
			//println "one MATWizard @ ${System.currentTimeMillis() - startTime}ms"
			def groups = analysisGroups(analysis, true, false)
			//println "one analysisGroups @ ${System.currentTimeMillis() - startTime}ms"

			int count = 0;
			groups.entrySet().each {
				def myKey = it.key
				if (it.value.groupNum != control)
				{
					//def oldIdx = samples.indexOf(it.key)
					def idx = multiIndexOf(samples, aslist, myKey, analysis.id)
					//print "oldIdx: " + oldIdx + " newIdx: " + idx
					validColumns.push(idx)
					count++
				}
			}
			caseCount.put(analysis.id, count)
		}
		
		samples = validColumns.collect { samples[it] }
		aslist  = validColumns.collect { aslist[it] }
	
		posMatrix = posMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
		negMatrix = negMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
	}

	println "done showControls @ ${System.currentTimeMillis() - startTime}ms"
	
	SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(posMatrix.rows(), posMatrix.columns())
	matrix.assign(posMatrix)
	matrix.assign(negMatrix, func)

	model.samples = samples
	model.analyses = aslist
	model.modules = posResult.modules
	model.posMatrix = posMatrix
	model.negMatrix = negMatrix
	model.matrix = matrix
	model.diffAllColumn = diffAllColumn
	model.annotations = annotations
	//model.allAnnotations = allAnnotations
	model.hasAllColumn = hasAllColumn
	model.caseCount = caseCount
	model.rowCount  = rowCount
	
	// Filtering based on floor
	// works on larger matrix, don't need to modifiy
	def rowsToKeep = []
	int i = 0, j = 0
	model.matrix = model.matrix.viewSelection(new DoubleMatrix1DProcedure() {
	  boolean apply(DoubleMatrix1D m) {
		boolean passed = m.aggregate(Functions.max, Functions.abs) > floor
		if (passed) {
		  rowsToKeep.push(j)
		  i++
		}
		else {
		  model.modules.remove(i)
		}
		j++
		return passed
	  }
	}).copy();
	int[] rowsToKeepArray = rowsToKeep.toArray(new int[rowsToKeep.size()])
	if (model.posMatrix) {
	  model.posMatrix = model.posMatrix.viewSelection(rowsToKeepArray, null).copy();
	}
	if (model.negMatrix) {
	  model.negMatrix = model.negMatrix.viewSelection(rowsToKeepArray, null).copy();
	}

	println "done floorFilter @ ${System.currentTimeMillis() - startTime}ms"
	
	return model
  }

  def individualMetaCatScatter(MetaCat metaCat, String plotName, boolean annotatedOnly, boolean isSigned = false,
			boolean showZeroValues = false, boolean showRowSpots, boolean showControls, double floor, double maxFoldChange,
			String sampleField, int moduleCount = -1, List<String> customModules = [], String rowSort = "none", boolean colSort = false, int colLevel = 0) {
	
	 long startTime = System.currentTimeMillis()
	
	 Map model = [:]
	 Map moduleAnnotation = [:]
		List<Integer> analysisIndex = []
		List<Integer> analysisColors = []
		// replace with user selected sample header
		Map sampleIdToLabel = [:]
		//Map sampleIdToAnalysis = [:]
		//Map sampleIdToSampleSet = [:]
			
		
	  List <Analysis> analyses = metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id })
	  //List <Analysis> analyses = []
	  //metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id }).each {	analyses.push(Analysis.get(it.id)) }
  
	  //int aCount = analyses.size()
	  Map aLegend = [:]
	  Map sortFeatures = [:]
	  List results = []
	  List clinical = []
	  
	  Map plotData = getPlotsData(analyses, metaCat.generation, true, showControls, annotatedOnly, customModules, floor, moduleCount)
	  
	  println "done fetch @ ${System.currentTimeMillis() - startTime}ms"
	  
	  def matrix = plotData.matrix
	  def aslist = plotData.analyses
	  def modules = plotData.modules
	  def samples = plotData.samples
	  def annotations = plotData.annotations
	  //def allAnnotations = plotData.allAnnotations
	  def caseCount = plotData.caseCount

	  analyses.eachWithIndex { analysis, idx ->
		  // analysis based colors are handled entirely within d3 on this plot, just keep the indices straight.
		  //def currentColor = D3_COLORS[idx % 10]
		  // analysis.id comes in from the database as a BigInt, and is transformed into a Long, we really need it to be an int in this array.
		  analysisIndex.push(analysis.id)

//		  println "a: " + analyses.indexOf(analysis) + " i: " + idx
//		  def analLink = new ApplicationTagLib().createLink(controller:"analysis", action:"show", id:analysis.id)
//		  analLink = "<a href='" + analLink + "' target='_blank'>" + analysis.displayName + "</a><br/>" + caseCount.get(analysis.id) + " case samples"
//		  aLegend.put(analLink, idx)

		  def infoLink = new ApplicationTagLib().createLink(controller:"sampleSet", action:"getInfo", id:analysis.sampleSetId)
		  infoLink = "<a href='#' class='clickTip' rel='" + infoLink + "'><img class='studyinfo' src='../../images/skin/information.png' width='16' height='16'/></a>"
		  def analLink = new ApplicationTagLib().createLink(controller:"analysis", action:"show", id:analysis.id)
		  analLink = "<a href='" + analLink + "' target='_blank'>" + analysis.displayName + "</a><br/>" + caseCount.get(analysis.id) + " case samples"
		  aLegend.put(analysis.displayName, [alink: analLink, ilink: infoLink, color: idx])
  
		  if (rowSort.startsWith("values.")) {
		  	// overlayOptions already scans for whether this should be shown via overlay_visible:show
			  Map features = chartingDataService.overlayOptions(analysis.sampleSetId)
			  //println "numerical features: " + features?.numerical
			  sortFeatures += features?.numerical.collectEntries { [it.key, it.displayName] }
		  }

		  if (sampleField) {
			  List result = mongoDataService.getColumnValuesAndSampleId(analysis.sampleSetId, sampleField)
			  result.each { Map s ->
				  if (s[sampleField] && !((String) s[sampleField]).isAllWhitespace()) {
					  sampleIdToLabel.put(s.sampleId.toString(), s[sampleField])
				  } else {
					  sampleIdToLabel.put(s.sampleId.toString(), s[sampleId])
				  }
				  //sampleIdToAnalysis.put(s.sampleId.toString(), analysis.id)
				  //sampleIdToSampleSet.put(s.sampleId.toString(), analysis.sampleSetId)
			  }
		  }
	  }
	  println "done setup @ ${System.currentTimeMillis() - startTime}ms"
	  //println "rowSort: " + rowSort
	  
	  List<Integer> rows = modules.collect { m -> modules.indexOf(m) }
	  int nModules = rows.size()
	  def sortMap = [:] // keep them outside incase we want to use later.
	  
	  def columns = 0..(samples.size() - 1) // columns index
	  if (rowSort == "signed" || rowSort == "variance") {
		  Map squaredError = [:]
		  columns.each { col ->
			  double stderr = 0;
			  rows.each { row ->
				  int value = matrix.getQuick(row, col).round(0)
				  if (isSigned) {
					  stderr += (value * value) * (value < 0 ? -1 : 1)
				  }
				  else
				  {
					  stderr += (value * value)
				  }
			  }
			  stderr = stderr / nModules
			  squaredError.put(col, stderr.round(0))
		  }
		  
		  columns = squaredError.sort({ a, b -> b.value <=> a.value }).keySet()
	  }
	  else if (rowSort.startsWith("values.")) // assume its a feature such as Age
	  {
		  def cList = []
		  analyses.eachWithIndex { analysis, idx ->
			  def result = mongoDataService.getColumnValuesAndSampleId(analysis.sampleSetId, rowSort)
			  result.each {
				  String s =  it.sampleId.toString()
				  // Integer.MIN_VALUE == N/A
				  def sIdx = multiIndexOf(samples, aslist, s, analysis.id)
				  if (sIdx >= 0) {
//				  if (samples.contains(s)) {
					  int v = it[rowSort] ? it[rowSort].toFloat() : Integer.MIN_VALUE
					  sortMap.put(sIdx, v)
				  }
			  }
		  }
		columns = sortMap.sort({ a, b -> a.value <=> b.value }).keySet()
		columns.eachWithIndex { col, idx ->
			// don't use N/A in the line.
			if (sortMap[col] != Integer.MIN_VALUE) {
				def s = samples.get(col)
				def a = aslist.get(col)
				Map out = [:]
				out.put('sample', sampleIdToLabel[s])
				out.put('sIdx', idx+1)
				out.put('sVal', sortMap[col])
				out.put('aIdx', analysisIndex.indexOf(a))
				cList.push(out)
			}
		}
		clinical.push(cList)
	  }

	  //println "done sorting @ ${System.currentTimeMillis() - startTime}ms"
	  
	  int count = 0;
	  rows.each { row ->
		  def mList = []
		  def mod = modules[row]
		  def modsafe = modules[row].replaceAll(/\./, "_")
		  def modColor = annotations[mod]?.hexColor ?: "#999"
		  //println "mod: " + mod + " color: " + modColor
		  columns.eachWithIndex { col, idx ->
			  Map out = [:]
			  def s = samples.get(col)
			  def a = aslist.get(col)
			  int value = matrix.getQuick(row, col).round(0)
			  if (value != 0 || showZeroValues) {
				  out.put('module', modsafe)
				  out.put('mName', mod)
				  out.put('mColor', modColor)
				  out.put('sample', sampleIdToLabel[s])
				  out.put('sIdx', idx+1)
				  out.put('mResp', value)
				  out.put('aIdx', analysisIndex.indexOf(a))
//				  println "sample1: " + s + " aid: " + sampleIdToAnalysis[s] + " aidx: " + analysisIndex.indexOf(sampleIdToAnalysis[s])
//				  println "sample2: " + s + " aid: " + a + " aidx: " + analysisIndex.indexOf(a)
//			      out.put('analysisName', analyses.get(analysisIndex.indexOf(sampleIdToAnalysis[samples.get(col)])).displayName)
//				  println "c: " + count + " r: " + row + " c: " + col + " value: " + value.toString()
//				  println " this is the map: " + out
				  mList.push(out)
			  }
			  count++
		  }
		  results.push(mList);
	  }

	  //println "done iterating @ ${System.currentTimeMillis() - startTime}ms"

	  //allAnnotations.each { key, value ->
	  annotations.each { key, value ->
		  moduleAnnotation.put(key, value.annotation)
	  }
  
	  moduleAnnotation =  moduleAnnotation.groupBy{ it.value }.collectEntries{ k, v -> [k, v.keySet()] }
  
	  model = [results: results, clinical: clinical, sortName: rowSort, sortFeatures: sortFeatures, noSamples: samples.size(), aLegend: aLegend, moduleAnnotation: moduleAnnotation]
	  
	  println "done individualMetaCatScatter #${metaCat.id} @ ${System.currentTimeMillis() - startTime}ms"
	  
	  return model 
  }

    Map individualMetaCatPlot(MetaCat metaCat, String plotName, boolean annotatedOnly, boolean isPie,
			boolean showRowSpots, boolean showControls, double floor, double maxFoldChange,
			String sampleField, int moduleCount = -1, List<String> customModules = [], boolean rowCluster = false, boolean colCluster = false, int colLevel = 0) {

	long startTime = System.currentTimeMillis()
	int maxDepth = 0
	int currentDepth = 0
	
	println "isPie:" + isPie
	//println "isDiff:" + diff
	// Push them onto the list in increasting id order.
	//List <Analysis> analyses = []
	//metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id }).each {	analyses.push(Analysis.get(it.id)) }
	List <Analysis> analyses = metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id })

	int aCount = analyses.size()
	
	Map plotData = getPlotsData(analyses, metaCat.generation, !isPie, showControls, annotatedOnly, customModules, floor, moduleCount)
	def matrix = plotData.matrix
	def modules = plotData.modules
	def aslist = plotData.analyses
	def samples = plotData.samples
	def annotations = plotData.annotations
	//def allAnnotations = plotData.allAnnotations
	def posMatrix = plotData.posMatrix
	def negMatrix = plotData.negMatrix
	def caseCount = plotData.caseCount
	//def rowCount = plotData.rowCount
	def isFocusedArray = plotData.isFocusedArray ?: false

	//def groups = analysisGroups(analysis, true, isFocusedArray)
	Map groups = [:]
	Map aLegend = [:]
	Map sLegend = [:]
	Map moduleAnnotation = [:]
	List<Integer> analysisIndex = []
	List<Integer> analysisColors = []
	// replace with user selected sample header
	Map sampleIdToLabel = [:]
	//Map sampleIdToAnalysis = [:]
	//Map sampleIdToSampleSet = [:]
	
	// resort by id
	//analyses.sort { Analysis a, Analysis b -> a.id <=> b.id }
	//Map modToNumProbes = getProbeCounts(analyses.get(0))

	analyses.eachWithIndex { analysis, idx ->
		def currentColor = D3_COLORS[idx % 10]
		analysisIndex.push(analysis.id)
		analysisColors.push(currentColor)
		
		def infoLink = new ApplicationTagLib().createLink(controller:"sampleSet", action:"getInfo", id:analysis.sampleSetId)
		infoLink = "<a href='#' class='clickTip' rel='" + infoLink + "'><img class='studyinfo' src='../../images/skin/information.png' width='16' height='16'/></a>";
		def analLink = new ApplicationTagLib().createLink(controller:"analysis", action:"show", id:analysis.id)
		analLink = "<a href='" + analLink + "' target='_blank'>" + analysis.displayName + "</a><br/>" + caseCount.get(analysis.id) + " case samples"
		aLegend.put(analysis.displayName, [alink: analLink, ilink: infoLink, color: currentColor])
		
		// this is also really expensive.
//		SampleSet set = SampleSet.findById(analysis.sampleSetId)
//		def setLink = new ApplicationTagLib().createLink(controller:"geneBrowser", action:"show", id:analysis.sampleSetId)
//		setLink = "<a href='" + setLink + "' target='_blank'>" + set.name + " (" + set.noSamples + " samples) </a>";
//		sLegend.put(setLink, currentColor)

		//println "sampleField: " + sampleField		
		if (sampleField) {
			List result = mongoDataService.getColumnValuesAndSampleId(analysis.sampleSetId, sampleField)
			result.each { Map s ->
				if (s[sampleField] && !((String) s[sampleField]).isAllWhitespace()) {
					sampleIdToLabel.put(s.sampleId.toString(), s[sampleField])
				} else {
					sampleIdToLabel.put(s.sampleId.toString(), s[sampleId])
				}
				//sampleIdToAnalysis.put(s.sampleId.toString(), analysis.id as int)
				//sampleIdToSampleSet.put(s.sampleId.toString(), analysis.sampleSetId)
				groups.put(s.sampleId.toString() + "," + analysis.id, [colors: [currentColor], percents: [100], stroke: '#000', count: 1, groupLabel: analysis.displayName])
			} 
		}
	}
	
	println "done setup @ ${System.currentTimeMillis() - startTime}ms"
	
	def rowClusterResult = null, colClusterResult = null
	if (rowCluster) { rowClusterResult = cluster(matrix, true) }
	if (colCluster) { colClusterResult = cluster(matrix, false) }
	List<Integer> rowClusteredOrder = rowCluster ? rowClusterResult[0] : modules.collect { m -> modules.indexOf(m) }
	List<Integer> colClusteredOrder = colCluster ? colClusterResult[0] : 0..(samples.size() - 1)

	if (colCluster) { 
	
		ClusterNode colRoot = colClusterResult[1]
		maxDepth = colRoot.maxDepth()
		currentDepth = maxDepth
		
		if (colLevel > 0) {

			List<Integer> validColumns = []
			List<Integer> invalidColumns = []
			List<Integer> validItems = []
			List<Integer> invalidItems = []
	
			List<ClusterNode> nodesAtDepth = new ArrayList<ClusterNode>()
			colRoot.getNodesFromLeaf(nodesAtDepth, colLevel);

			int count = 0;
			
			nodesAtDepth.each { node ->
				List<Integer> items = new ArrayList<Integer>()
				node.getItems(items)
				items.sort()
				def keySample = samples.get(items.get(0))
				def keyAnalysis = aslist.get(items.get(0))
				def keyNodeIdx = items.get(0)
				def countNodes = items.size()
				//println "items: " + items
				// 1) combine information from each group of items into a new element including new pos new neg
				// 2) Write the combined element into a previously used index. (matrix, posMatrix, negMatrix)
				// 3) Update the samples array with new information (e.g. names: cluster1, cluster2); assign new cluster color (black?)
				// 4) Create some structure to keep track of meta information about the cell (which samples it came from)
				// 5) savedCellList = All Valid cells - collasped cells
				// 6) new matrix = maxtrix.viewSelection(null, savedCellList).copy()
				// 7) recluster.
				//println "before matrix: " + matrix
				rowClusteredOrder.each { row ->
					Double v = 0, posV = 0, negV = 0
					items.each { col -> // was 'i'
						//int col = colClusteredOrder.indexOf(i)
						//println "cluster: " + (count+1) + " sample: " + col + " name: " + sampleIdToLabel.get(samples.get(i).toString())
						v    += matrix.getQuick(row, col)
						posV += posMatrix?.getQuick(row, col)
						negV += negMatrix?.getQuick(row, col)
					}
					//println "cluster: " + (count+1) + " keyNodeIdx: " + keyNodeIdx + " v: " + (v/countNodes) + " posV: " + (posV/countNodes) + " negV: " + (negV/countNodes)
					matrix.setQuick(row, keyNodeIdx, v/countNodes)
					posMatrix?.setQuick(row, keyNodeIdx, posV/countNodes)
					negMatrix?.setQuick(row, keyNodeIdx, negV/countNodes)
	
				}
				//println "reset matrix: " + matrix
				// preprare to remove all but the first item, which was just overwritten above
				invalidItems = invalidItems + items.tail()
				
				List analysesCount = [0] * aCount  // Initialize sample counts per analysis
				//List analysesCount = [] // Initialize sample counts per analysis
				//for (i in 0 .. aCount) { analysesCount[i] = 0 }
				
				List collectedSamples = [""] * aCount
				//List collectedSamples = []
				//for (i in 0 .. aCount) { collectedSamples[i] = "" }
				
				items.each {
					//int aIndex = analysisIndex.indexOf(sampleIdToAnalysis.get(samples.get(it)))
					int aIndex = analysisIndex.indexOf(aslist.get(it))
					//println "aIndex: " + aIndex + " aslist: " + aslist.get(it)
					analysesCount[aIndex] += 1
					collectedSamples[aIndex] += sampleIdToLabel.get(samples.get(it)) + " "
				}

				String name = "Cluster" + (count + 1)
				sampleIdToLabel.put(keySample, name)

				//println "Cluster: name: " + name + " item: " + items.get(0) + " sample: " + samples.get(items.get(0))
				//println "Remnant items: " + items.tail() 				
				// Summarize counts as percent
				// If a result is 0% don't include it, piechart() can't handle e.g. [66, 33, 0]
				def resultsMap = [:]
				def summarizedAnalyses = ""
				boolean f = true
				analysesCount.eachWithIndex { it, index ->
					if (it != 0) {
						if (!f) { summarizedAnalyses += "<br/>" }
						resultsMap.put(index, it/countNodes * 100)
						summarizedAnalyses += it + ": " + analyses.get(index).displayName + "<br/>" + collectedSamples[index]
						f = false
					}
				}

				// further, they must jointly be in decreasing order otherwise piechart sorts percents, but not the colors.
				def analysesPercents = []
				def analysesColors = []
				resultsMap.sort({ a, b -> b.value <=> a.value}).each { k, v ->
					analysesPercents.push(v)
					analysesColors.push(analysisColors[k])
				}
				//println "keySample: " + keySample + " keyAnalysis: " + keyAnalysis
				//println "resultsMap: " + resultsMap
				//println "count: " + count + " 2perecent: " + analysesPercents
				//println "count: " + count + " 2colors: " + analysesColors
				groups.put(keySample + "," + keyAnalysis, [colors: analysesColors, percents: analysesPercents, stroke: '#000', count: countNodes, groupLabel: summarizedAnalyses])
				count++
			}
	
			validItems = colClusteredOrder.clone()
//			println "validItems: " + validItems
//			println "invalidItems: " + invalidItems
			
			validItems -= invalidItems
//			println "newItems: " + validItems
//			validItems.each { 
//				validColumns.push(colClusteredOrder.indexOf(it))
//			}

			validColumns = validItems.clone()
						
			samples = validItems.collect { samples[it] }
			aslist  = validItems.collect { aslist[it] }

			matrix    = matrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
			posMatrix = posMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
			negMatrix = negMatrix.viewSelection(null, validColumns.toArray(new int[validColumns.size()])).copy()
	
			//println "new matrix: " + matrix
			// cluster columns again on new collapsed results. 
			colClusterResult = cluster(matrix, false)
			colClusteredOrder = colClusterResult[0]
			
			ClusterNode secondRoot = colClusterResult[1]
			currentDepth = secondRoot.maxDepth();
	
		} 
	}

	def rowDendrogram = rowCluster ? dendrogram(rowClusterResult[1], colClusteredOrder.size()) : null
	def colDendrogram = colCluster ? dendrogram(colClusterResult[1], colClusteredOrder.size()) : null
	def dendrogramHeight = colCluster ? (dEndBase - (colLevel > 0 ? 4 : 30)) : 0

	//println "done clustering @ ${System.currentTimeMillis() - startTime}ms"

	def r = 17, pad = 3, topPadding = dendrogramHeight + 50, leftPadding = 300, cellWidth = (2 * (pad + r)), spotUnit = pad + r
	def cy = topPadding, annotationY = topPadding
	def circles = [], paths = [], sampleLbls = [], groupsKey = [], moduleLabels = [], annotationKeys = []

	rowClusteredOrder.each { row ->
		String module = modules[row]
		ModuleAnnotation annotMap = annotations.get(module)
		cy += spotUnit

		def textLabel = annotMap ? "${module} ${annotMap.annotation}" : module
		Map moduleData = [info: ["Module": module]]
//		if (!isFocusedArray) {
//			moduleData.info["# of probes"] = modToNumProbes.get(module)
//		}
//		if (analysis.sampleSetId != -1 && grailsApplication.config.mat.access.gxb && !isFocusedArray)
//		{
//			if (SampleSet.findByIdAndMarkedForDeleteIsNull(analysis.sampleSetId)) {
//				def gxbLink = new ApplicationTagLib().createLink(controller:"geneBrowser", action:"show", id:analysis.sampleSetId, params:[ module:module, analysisId:analysis.id ])
//				moduleData.gxbLink = "<a href='"+gxbLink+"' target='_blank'>View in Gene Expression Browser</a>";
//			}
//		}
		if (metaCat.generation == 2 && grailsApplication.config.module.wiki.on) {
			moduleData.moduleWiki = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>[Module Wiki]</a>"
		} else if (metaCat.generation == 3 && grailsApplication.config.module.wiki.on) {
			moduleData.moduleWiki = "<a href='http://mat.benaroyaresearch.org/wiki/index.php/Generation_3_Modules_${module}' target='_blank'>[Module Wiki]</a>"
		}
		moduleLabels.push([x: leftPadding - 40, y: cy, text: textLabel, attr: [font: "16px Fontin-Sans, Arial", "text-anchor": "end"], data: moduleData])

		def cx = leftPadding, addedValue = false
		colClusteredOrder.each { col ->
			cx += spotUnit
			Double v = matrix.getQuick(row, col)
			Double absV = Math.abs(v)
			Double posV = posMatrix?.getQuick(row, col), negV = negMatrix?.getQuick(row, col)

			// populate module info
			String sampleId = samples.get(col) //.trim()
			Map data = [info: ["Module": module]] // , group: groups.get(sampleId).groupLabel]
			if (annotMap) {
				data.info["Function"] = annotMap.annotation
				data.color = annotMap.hexColor
			}
			if (posV) {
				data.info["% Positive"] = "${posV.round(1)} %"
			}
			if (negV) {
				data.info["% Negative"] = "${Math.abs(negV.round(1))} %"
			}
			data.sampleId = sampleId
			
			if (sampleField && sampleIdToLabel.containsKey(sampleId)) {
				data.sampleLabel = sampleIdToLabel.get(sampleId)
				data.info["Label"] = sampleIdToLabel.get(sampleId)
			} else {
				data.info["SampleId"] = sampleId
			}
//			if (sampleIdToAnalysis.containsKey(sampleId)) {
//				data.info["Analysis"] = sampleIdToAnalysis.get(sampleId)
//			}
//			if (sampleIdToSampleSet.containsKey(sampleId)) {
//				data.info["SampleSet"] = sampleIdToSampleSet.get(sampleId)
//			}
			// start drawing plot
			if (showRowSpots || absV > floor) {
				addedValue = true
				if (isPie) {
					def pos = (posV / 100) * 360, neg = (-negV / 100) * 360
					if (neg < 0d && neg != -360d) {
						paths.push([segment: [ cx, cy, r, 270 + neg, 270, -1]])
					}
					if (pos > 0d && pos != 360d) {
						paths.push([segment: [cx, cy, r, 270, 270 + pos, 1]])
					}
					def fill = pos == 360d ? "#ff0000" : ((neg == -360d) ? "#0000ff" : "#ffffff")
					def fillOpacity = (pos == 360d || neg == -360d) ? 1 : 0
					circles.push([cx: cx, cy: cy, r: r, attr: [fill: fill, "fill-opacity": fillOpacity, stroke: "#666666"], data: data])
				}
				else {
					data.value = isFocusedArray ? "${absV.round(3)}" : "${absV.round(0)} %"
					data.up = up(MOD, posV ?: -1, negV ?: -1, v)
					def spotValue = isFocusedArray ? absV / maxFoldChange : spotOpacity(absV)
					def attr = [stroke: "none", fill: color(MOD, posV ?: -1, negV ?: -1, v), "fill-opacity": spotValue]
					circles.push([cx: cx, cy: cy, r: r, attr: attr, data: data])
				}
			}
			cx += spotUnit
		}
		cy += pad + r
		if (annotMap) {
			annotationKeys.push([type: "rect", x: leftPadding - 30, y: annotationY, width: 20, height: cellWidth, fill: annotMap.hexColor, stroke: "none"])
		}
		annotationY += cellWidth

	}
	
	//println "done iterating @ ${System.currentTimeMillis() - startTime}ms"

	//allAnnotations.each { key, value ->
	annotations.each { key, value ->
		moduleAnnotation.put(key, value.annotation)
	}

	moduleAnnotation =  moduleAnnotation.groupBy{ it.value }.collectEntries{ k, v -> [k, v.keySet()] }
	
	//def annotationKeyLabels = annotationKeyWithLabels(annotations, false, true) // false = not focused array, true = top modules only.
	
	def x = leftPadding, groupX = leftPadding + (colLevel > 0 ? 20 : 0) // + 20 for ci
	def barHeight = 8
	def attr = [font: "16px Fontin-Sans, Arial", "text-anchor": "end"]
	def orderedSamples = []
	colClusteredOrder.each { sIdx ->
		String s = samples[sIdx]
		String sdx = s + "," + aslist[sIdx]
		//println "sdx:" + sdx
		orderedSamples.push(sdx)
		x += spotUnit
		String sampleLabel = sampleIdToLabel.get(s)
		//def groupColor = groups.get(s).colors //GROUP_COLORS[groups.get(s).groupNum]
		def groupColors = groups.get(sdx).colors
		def groupColor = groupColors.get(0)
		def groupPerc = groups.get(sdx).percents
		def stroke    = groups.get(sdx).stroke
		def count     = groups.get(sdx).count
		def groupData = [sampleId: s, group: groups.get(sdx).groupLabel]
		if (sampleLabel) {
			groupData.sampleLabel = sampleLabel
		}
		if (colLevel > 0) {
			sampleLbls.push([x: x + 2, y: cy + 30 + r, text: sampleLabel, attr: attr, data: [sampleId: s, cx: groupX]])
			groupsKey.push([x: groupX, y: topPadding - 24, r: r, percents: groupPerc, colors: groupColors, stroke: stroke, count: count, data: groupData])
			groupsKey.push([x: groupX, y: cy + 24,         r: r, percents: groupPerc, colors: groupColors, stroke: stroke, data: groupData])
		
		}
		else
		{
			sampleLbls.push([x: x + 2, y: cy + 10 + barHeight + 4, text: sampleLabel, attr: attr, data: [sampleId: s, cx: groupX]])
			groupsKey.push([x: groupX, y: topPadding - 16, width: cellWidth - 1, height: barHeight, attr: [fill: groupColor, stroke: "none"], data: groupData])
			groupsKey.push([x: groupX, y: cy + 10, width: cellWidth - 1, height: barHeight, attr: [fill: groupColor, stroke: "none"], data: groupData])
		}
		x += spotUnit
		groupX += cellWidth
	}
	def width = samples.size() * (2 * (pad + r)) + 6
	def height = cy - topPadding + 6

	def bg = [type: "rect", x: leftPadding - 3, y: topPadding - 3, width: width, height: height, stroke: "#111111", "stroke-width": 2]
	def chartWidth = leftPadding + width + 50
	def chartHeight = cy + (2 * topPadding)
	//sLegend: sLegend,
	def model = [width: chartWidth, height: chartHeight, bg: bg, sampleLbls: sampleLbls, groupsKey: groupsKey, aLegend: aLegend,  moduleLabels: moduleLabels,
		annotationKeys: annotationKeys, moduleAnnotation: moduleAnnotation, noSamples: orderedSamples.size(), rowDendrogram: rowDendrogram, spots: circles, maxDepth: maxDepth, currentDepth: currentDepth,
		colDendrogram: colDendrogram, startY: topPadding, endY: cy, startX: leftPadding]
	if (isPie) {
		model.segments = paths
	}
	
	println "done individualMetaCatPlot #${metaCat.id} @ ${System.currentTimeMillis() - startTime}ms"

	return model
  }

  Map groupMetaCatPlot(MetaCat metaCat, boolean annotatedOnly, boolean isPie, boolean showRowSpots,
			double floor, int moduleCount, boolean rowCluster, boolean colCluster)
	{

		long startTime = System.currentTimeMillis()
		int maxDepth = 0
		Map aLegend = [:]
		Map groups = [:]

		//List<Analysis> analyses = []
		//metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id }).each { analyses.push(Analysis.get(it.id)) }
		List <Analysis> analyses = metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id })
	
		//Map modToNumProbes = getProbeCounts(analyses.get(0))
		//List<Integer> analysisIndex = []
		//List<Integer> analysisColors  = []
		
		SparseDoubleMatrix2D posMatrix = null, negMatrix = null, diffMatrix = null
		List<String> columnNames = []
	
		analyses.each { analysis ->

			def currentColor = D3_COLORS[analyses.indexOf(analysis) % 10]
			//analysisIndex.push(analysis.id)
			//analysisColors.push(currentColor)

			def infoLink = new ApplicationTagLib().createLink(controller:"sampleSet", action:"getInfo", id:analysis.sampleSetId)
			infoLink = "<a href='#' class='clickTip' rel='" + infoLink + "'><img class='studyinfo' src='../../images/skin/information.png' width='16' height='16'/></a>";
			def analLink = new ApplicationTagLib().createLink(controller:"analysis", action:"show", id:analysis.id)
			analLink = "<a href='" + analLink + "' target='_blank'>" + analysis.displayName + "</a><br/>" + SampleSet.get(analysis.sampleSetId).noSamples + " total samples";
			aLegend.put(analysis.displayName, [alink: analLink, ilink: infoLink, color: currentColor])
			groups.put(analysis.displayName, [color: currentColor, colors: [currentColor], percents: [100], stroke: '#fff', count: 1, groupLabel: analysis.displayName])
		}
		
		Map<String,ModuleAnnotation> annotations = getAnnotations(metaCat.generation)
		int generation_id = getModuleGen(analyses.get(0))
		List<String> irowNames = Module.findAllByModuleGenerationId(generation_id).moduleName.toList()
		
		if (annotatedOnly) {
			irowNames.retainAll(annotations.keySet())
		}
		List<String>rowNames = []
		boolean top = moduleCount == 62

		if (moduleCount > 0) { 
			irowNames.each { module ->
				String[] modules = module.substring(1).split("\\.")
				int m1 = modules[0].toInteger()
				int m2 = modules[1].toInteger()
				if (!top || (top && m1 <= 6)) {
					rowNames.push(module)
				}
			}
		} else {
			rowNames = irowNames
		}
  
		rowNames.sort { a, b ->
			String[] aParts = a.substring(1).split("\\.")
			int a1 = aParts[0].toInteger()
			int a2 = aParts[1].toInteger()
			String[] bParts = b.substring(1).split("\\.")
			int b1 = bParts[0].toInteger()
			int b2 = bParts[1].toInteger()

			if (a1 > b1) { return 1 }
			else if (a1 < b1) { return -1 }
			else if (a1 == b1) {
				if (a2 > b2) { return 1 }
				else if (a2 < b2) { return -1 }
			}
			return 0
		}
		int numRows = rowNames.size(), numCols = 0
		
		analyses.each { analysis ->
			//println "analysis:" + analysis
			def dsName = analysis.datasetName.replaceAll(" ", "_")
			def filename = "${dsName}_260_Module_Group_Comparison(_.*)+\\.csv".toString()
			Pattern p = Pattern.compile(filename, Pattern.CASE_INSENSITIVE)
			//boolean mongoCsv = mongoDataService.exists(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], null)
			Map result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], ["rows"])
			
			if (!result) {
				def csvFile = matDataService.getFile(analysis, filename)
				mongoDataService.storeCsv(csvFile, true, CSVCOLLECTION, [ analysisId:analysis.id, sampleSetId:analysis.sampleSetId ])
				result = mongoDataService.findOne(CSVCOLLECTION, [ analysisId:analysis.id, filename:p ], ["rows"])
			}

			if (result?.rows) {
				List rows = result.rows
				columnNames.push(analysis.displayName)
				if (numCols == 0) {
					posMatrix = new SparseDoubleMatrix2D(numRows, analyses.size())
					negMatrix = new SparseDoubleMatrix2D(numRows, analyses.size())
					diffMatrix = new SparseDoubleMatrix2D(numRows, analyses.size())
				}
				rows.eachWithIndex { List values, int r ->
					int mRow = rowNames.indexOf(values[0])
					if (mRow != -1) {
						posMatrix.setQuick(mRow, numCols, Double.parseDouble(values[1]))
						negMatrix.setQuick(mRow, numCols, Double.parseDouble(values[2]))
					}
				}
				numCols++
			}
		}

		DoubleDoubleFunction func = new DoubleDoubleFunction() {
			public double apply(double a, double b) {
				return a + b
			}
		}

		// derive the diff matrix
		diffMatrix = new SparseDoubleMatrix2D(numRows, numCols)
		diffMatrix.assign(posMatrix.viewPart(0, 0, numRows, numCols))
		diffMatrix.assign(negMatrix.viewPart(0, 0, numRows, numCols), func)
		
		// Remove modules that don't meet the floor requirement
		if (floor > 0d)
		{
			List<String> modulesToRemove = []
			IntArrayList rowsToKeep = new IntArrayList()
			for (i in 0..(numRows-1)) {
				if (diffMatrix.viewRow(i).aggregate(Functions.max, Functions.abs) >= floor)
				{
					rowsToKeep.add(i)
				}
				else
				{
					modulesToRemove.push(rowNames.get(i))
				}
			}
			if (rowsToKeep.size() < numRows) {
				rowsToKeep.trimToSize()
				int[] filteredRows = rowsToKeep.elements()
				diffMatrix = diffMatrix.viewSelection(filteredRows, null).copy()
				posMatrix = posMatrix.viewSelection(filteredRows, null).copy()
				negMatrix = negMatrix.viewSelection(filteredRows, null).copy()
				rowNames.removeAll(modulesToRemove)
			}
		}
		numRows = rowNames.size()

		println "done setup @ ${System.currentTimeMillis() - startTime}ms"
		
		def rowClusterResult = null, colClusterResult = null
		if (rowCluster) { rowClusterResult = cluster(diffMatrix, true) }
		if (colCluster) { colClusterResult = cluster(diffMatrix, false) }
		List<Integer> rowClusteredOrder = rowCluster ? rowClusterResult[0] : 0..(rowNames.size()-1)
		List<Integer> colClusteredOrder = colCluster ? colClusterResult[0] : 0..(columnNames.size() - 1)
	
		def rowDendrogram = rowCluster ? dendrogram(rowClusterResult[1], colClusteredOrder.size()) : null
		def colDendrogram = colCluster ? dendrogram(colClusterResult[1], colClusteredOrder.size()) : null
		def dendrogramHeight = colCluster ? dEndBase - 30 : 0

//		println "done clustering @ ${System.currentTimeMillis() - startTime}ms"
		
		def r = 17, pad = 3, topPadding = dendrogramHeight + 50, leftPadding = 300, cellWidth = (2 * (pad + r)), spotUnit = pad + r
		def cy = topPadding, annotationY = topPadding
		def circles = [], paths = [], sampleLbls = [], groupsKey = [], moduleLabels = [], annotationKeys = []

		rowClusteredOrder.each { row ->
			String module = rowNames[row]
			ModuleAnnotation annotMap = annotations.containsKey(module) ? annotations.get(module) : null
			cy += spotUnit

			def textLabel = annotMap ? "${module} ${annotMap.annotation}" : module
			Map moduleData = [ info:[ "Module":module ] ]

			//moduleData.info["# of probes"] = modToNumProbes.get(module)
			
			if (metaCat.generation == 2 && annotMap && grailsApplication.config.module.wiki.on) {
				moduleData.moduleWiki = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>[Module Wiki]</a>"
			} else if (metaCat.generation == 3 && grailsApplication.config.module.wiki.on) {
				moduleData.moduleWiki = "<a href='http://mat.benaroyaresearch.org/wiki/index.php/Generation_3_Modules_${module}' target='_blank'>[Module Wiki]</a>"
			}
			moduleLabels.push([ x:leftPadding - 40, y:cy, text:textLabel, attr:[ font:"16px Fontin-Sans, Arial", "text-anchor":"end" ], data:moduleData ])

			def cx = leftPadding, addedValue = false
			colClusteredOrder.each { col ->
				cx += spotUnit
				double v = diffMatrix.getQuick(row, col)
				double absV = Math.abs(v)
				double posV = posMatrix.getQuick(row, col), negV = negMatrix.getQuick(row, col)

				// populate module info
				Analysis analysis = analyses.get(col)
				Map data = [ info:[ "Module": module ] ]
				if (annotMap) {
					data.info["Function"] = annotMap.annotation
					data.color = annotMap.hexColor
				}
				data.info["% Positive"] = "${posV.round(1)}"
				data.info["% Negative"] = "${Math.abs(negV.round(1))}"
				//data.info["Analysis"] = analysis.id
				//data.info["SampleSet"] = analysis.sampleSetId
				//data.analysisId = analysis.id

				// this is incredibly expensive.
//				if (SampleSet.findByIdAndMarkedForDeleteIsNull(analysis.sampleSetId)) {
					def gxbLink = new ApplicationTagLib().createLink(controller:"geneBrowser", action:"show", id:analysis.sampleSetId, params:[ module:module, analysisId:analysis.id ])
					data.gxbLink = "<a href='"+gxbLink+"' target='_blank'>View in Gene Expression Browser</a>";
//				}

				if (metaCat.generation == 2 && annotMap && grailsApplication.config.module.wiki.on) {
					data.moduleWiki = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>[Module Wiki]</a>"
				} else if (metaCat.generation == 3 && grailsApplication.config.module.wiki.on) {
					data.moduleWiki = "<a href='http://mat.benaroyaresearch.org/wiki/index.php/Generation_3_Modules_${module}' target='_blank'>[Module Wiki]</a>"
				}

				// start drawing plot
				if (showRowSpots || absV > floor) {
					addedValue = true
					if (isPie) {
						def pos = (posV / 100) * 360f, neg = (negV / 100) * 360f
						if (neg < 0d && neg != -360f) {
							paths.push([segment: [cx, cy, r, 270 + neg, 270, -1]])
						}
						if (pos > 0d && pos != 360f) {
							paths.push([segment: [cx, cy, r, 270, 270 + pos, 1]])
						}
						def fill = pos == 360f ? "#ff0000" : ((neg == -360f) ? "#0000ff" : "#ffffff")
						def fillOpacity = (pos == 360f || neg == -360f) ? 1 : 0
						circles.push([cx: cx, cy: cy, r: r, attr: [fill: fill, "fill-opacity": fillOpacity, stroke: "#666666"], data: data])
					}
					else
					{
						data.value = "${absV.round(0)} %"
						data.up = up(MOD, posV, negV, v)
						def attr = [stroke: "none", fill: color(MOD, posV, negV, v), "fill-opacity": spotOpacity(absV)]
						circles.push([cx: cx, cy: cy, r: r, attr: attr, data: data])
					}
				}
				cx += spotUnit
			}

			cy += pad + r
			if (annotMap) {
				annotationKeys.push([type: "rect", x: leftPadding - 30, y: annotationY, width: 20, height: cellWidth, fill: annotMap.hexColor, stroke: "none"])
			}
			annotationY += cellWidth
		}

//		println "done iterating @ ${System.currentTimeMillis() - startTime}ms"
		
		def x = leftPadding, groupX = leftPadding, barHeight = 8
		def orderedSamples = []
		colClusteredOrder.each { sIdx ->
			String s = columnNames[sIdx]
			orderedSamples.push(s)
			def groupColor = groups.get(s).color
			def groupData = [sampleId: s, group: groups.get(s).groupLabel]
			
			x += spotUnit
			sampleLbls.push([ x:(x + 2), y:(cy + 10 + barHeight + 4), text:s, attr:[ font:"16px Fontin-Sans, Arial", "text-anchor":"end" ], data:[ sampleId:s ]])
			groupsKey.push([x: groupX, y: topPadding - 16, width: cellWidth - 1, height: barHeight, attr: [fill: groupColor, stroke: "none"], data: groupData])
			groupsKey.push([x: groupX, y: cy + 10, width: cellWidth - 1, height: barHeight, attr: [fill: groupColor, stroke: "none"], data: groupData])
  			groupX += cellWidth
			x += spotUnit
		}
		
		def width = columnNames.size() * (2 * (pad + r)) + 6
		def height = cy - topPadding + 6

		def bg = [ type:"rect", x:(leftPadding - 3), y:(topPadding - 3), width:width, height:height, stroke:"#111111", "stroke-width":2 ]
		def chartWidth = leftPadding + width + 50
		def chartHeight = cy + (2 * topPadding) + 250 // TODO: calculate properly

		def model = [width: chartWidth, height: chartHeight, bg: bg, sampleLbls: sampleLbls, groupsKey: groupsKey, aLegend: aLegend, moduleLabels: moduleLabels,
			annotationKeys: annotationKeys, noSamples: orderedSamples.size(), rowDendrogram:rowDendrogram, spots: circles, maxDepth: maxDepth,
			colDendrogram:colDendrogram, startY: topPadding, endY: cy]
		if (isPie) {
			model.segments = paths
		}

		println "done moduleMetaCatPlot #${metaCat.id} @ ${System.currentTimeMillis() - startTime}ms"
		
		return model
}

  
	Map multiCorrelationPlots(Analysis analysis, Map fields, double mFloor,
							  double lFilter, double uFilter, double lPvFilter, double uPvFilter,
							  String moduleCount, boolean clusterRow, boolean clusterColumn, boolean fdrAdjust = false )
  {
    boolean annotated = moduleCount == "annotated"
    // get the correlations for each field
    List uncachedFields = []
    Set<String> modules = new HashSet<String>()
    Map fieldToCorrelation = [:]
    fields.each { String key, String title ->
      Map mongoCor = mongoDataService.findOne("correlations", [analysisId:analysis.id, field:key], ["correlation"])?.correlation
      if (mongoCor) {
        fieldToCorrelation.put(key, mongoCor)
        modules.addAll(mongoCor.keySet())
      } else {
        uncachedFields.push(key)
      }
    }
    if (!uncachedFields.isEmpty()) {
      Map matrix = prepareMatrix(analysis)
      uncachedFields.each { String field ->
        Map corr = calcCorrelation(matrix.matrix, analysis.sampleSetId, matrix.samples, matrix.modules, field, mFloor)
        fieldToCorrelation.put(field, corr)
        modules.addAll(corr.keySet())
      }
    }
	if ( fdrAdjust )
	{
		fdrAdjustMultiCorrelationPvalues( fieldToCorrelation );
	}

//    long moduleGenerationId = getModuleGen(analysis)
//    Map annotations = getAnnotations(moduleGenerationId)
    Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))
    Map probeCounts = getProbeCounts(analysis)
    def sortedModules = modules.collect { String module ->
      String[] parts = module.substring(1).split("_")
      int m1 = parts[0].toInteger()
      int m2 = parts[1].toInteger()
      return [module:module, m1:m1, m2:m2]
    }
    sortedModules.sort { Map a, Map b ->
      if (a.m1 > b.m1) { return 1 }
      else if (a.m1 < b.m1) { return -1 }
      else if (a.m1 == b.m1) {
        if (a.m2 > b.m2) { return 1 }
        else if (a.m2 < b.m2) { return -1 }
      }
      return 0
    }
    if (annotated)
    {
      sortedModules.retainAll { Map mMap ->
        return annotations.containsKey(mMap.module.replaceAll("_","\\."))
      }
    }

//    DoubleFunction filterFunc = new DoubleFunction() {
//      double apply(double v) {
//        if (v != 0 && v >= lFilter && v < uFilter) {
//          return 1
//        }
//        return 0
//      }
//    }
	
//	DoubleProcedure pValueProc = new DoubleProcedure() {
//      boolean apply(double v) {
//		  if ( (uPvFilter == 0.0) || //if upper bnd 0, don't filter any out
//			   (v >= lPvFilter && v < uPvFilter) ) {
//          return true
//        }
//        return false
//      }
//    }
	
	SparseDoubleMatrix2D fullMatrix = new SparseDoubleMatrix2D(sortedModules.size(), fields.size())
	SparseDoubleMatrix2D bFullMatrix = new SparseDoubleMatrix2D(sortedModules.size(), fields.size())
	SparseDoubleMatrix2D pValueMatrix = new SparseDoubleMatrix2D(sortedModules.size(), fields.size())
	SparseDoubleMatrix2D bPvalueMatrix = new SparseDoubleMatrix2D(sortedModules.size(), fields.size())
    List orderedRows = []
    sortedModules.eachWithIndex { Map module, int row ->
      int col = 0
      fieldToCorrelation.each { String field, Map corr ->
        Map corrRslt = corr.get(module.module);
		double rho = corrRslt?.statistic ?: 0.0
        fullMatrix.setQuick(row, col, rho)
		bFullMatrix.setQuick(row, col, (rho != 0 && (rho >= lFilter && rho < uFilter)) ? 1 : 0)
        double pValue = (corrRslt?.pValue != null) ? corrRslt?.pValue : 1.0;
        pValueMatrix.setQuick( row, col, pValue );
		bPvalueMatrix.setQuick(row, col, (uPvFilter == 0.0 || (pValue >= lPvFilter && pValue < uPvFilter)) ? 1 : 0)
        col++
      }

//	  double passAgg = fullMatrix.viewRow(row).aggregate(Functions.plus, filterFunc)
//	  int numPvalsInRange = pValueMatrix.viewRow( row ).viewSelection( pValueProc ).size()

	  // try: public double aggregate(DoubleMatrix2D other, DoubleDoubleFunction aggr, DoubleDoubleFunction f)?
	  double passRow = bFullMatrix.viewRow(row).aggregate(bPvalueMatrix.viewRow(row), Functions.plus, Functions.mult)

//	  println "module: " + module.module + " rho filter: " + passAgg + " pvalue filter: " + numPvalsInRange + " passRow: " + passRow
//	  if ((passAgg != 0d) && (numPvalsInRange > 0)) {
      if (passRow > 0d) {
        orderedRows.push(row)
      }
    }

    if (!orderedRows.isEmpty()) {
      if (orderedRows.size() != sortedModules.size()) {
        int[] rows = new int[orderedRows.size()]
        orderedRows.eachWithIndex { Integer r, int i ->
          rows[i] = r.intValue()
        }
        fullMatrix = fullMatrix.viewSelection(rows, null).copy()
        pValueMatrix = pValueMatrix.viewSelection(rows, null).copy()
      }

      def rowClusterResult = null, colClusterResult = null
      if (clusterRow) { rowClusterResult = cluster(fullMatrix, true) }
      if (clusterColumn) { colClusterResult = cluster(fullMatrix, false) }
      List<Integer> rowClusteredOrder = clusterRow ? rowClusterResult[0] : 0..(orderedRows.size()-1)
      List<Integer> colClusteredOrder = clusterColumn ? colClusterResult[0] : 0..(fields.size()-1)

      int[] colSelection = colClusteredOrder.toArray(new int[colClusteredOrder.size()])

      def colDendrogram = clusterColumn ? dendrogram(colClusterResult[1], colClusteredOrder.size()) : null
      def dendrogramHeight = clusterColumn ? dEndBase - ITOP_PADDING : 0

      // draw axes
      def columnY = clusterColumn ? -10 : ITOP_PADDING - SPOT_UNIT - 10
      def colHeaders = [], colLabels = [:]
      int oldCol = 0
      fieldToCorrelation.each { String field, Map corr ->
        int col = colClusteredOrder.indexOf(new Integer(oldCol))
        String[] fKeys = field.split("_",3)
        colLabels.put(col, [ label:fields.get(field), field:"${fKeys[0]}_${fKeys[2]}", datatype:fKeys[1] ])
        def cx = ILEFT_PADDING + BORDER_WIDTH + SPOT_UNIT + (col * FULL_SPOT_UNIT)
        String colLabel = fields.get(field)
        colHeaders.push([ type:"text", x:cx, y:columnY, text:colLabel, "text-anchor":"start", font:"12px Helvetica", transform:"R-45" ])
        oldCol++
      }

      // draw spots
      def width = 0, height = 0, r = 0
      def spots = [], spotTexts = [], rowHeaders = []
      rowClusteredOrder.each { int row ->
        int origRow = orderedRows.get(row)
        String module = sortedModules.get(origRow).module.replaceAll("_","\\.")
        String moduleLabel = annotations.containsKey(module) ? "${module} ${annotations.get(module).annotation}" : module
        DoubleMatrix1D currentRow = fullMatrix.viewRow(row).viewSelection(colSelection)
        DoubleMatrix1D pValueRow = pValueMatrix.viewRow(row).viewSelection(colSelection)

        // add additional module info
        ModuleAnnotation annotInfo = annotations.get(module)
        Map spotInfo = [info:[ "Module":module, "# of Probes":probeCounts.get(module) ]]
        if (annotInfo) {
//          if (modGen == 2) {
          if (analysis.modGeneration == "2") {
            spotInfo.info["Function"] = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${module}' target='_blank'>${annotInfo.annotation}</a>"
          } else {
            spotInfo.info["Function"] = annotInfo.annotation
          }
          spotInfo.color = annotInfo.hexColor
        }
        if (analysis.sampleSetId != -1 && grailsApplication.config.mat.access.gxb)
		{
          if (SampleSet.findByIdAndMarkedForDeleteIsNull(analysis.sampleSetId)) {
            def gxbLink = new ApplicationTagLib().createLink(controller:"geneBrowser", action:"show", id:analysis.sampleSetId, params:[ module:module, analysisId:analysis.id ])
            spotInfo.gxbLink = gxbLink
          }
        }

        Map rowModel = fingerprintRow(currentRow, pValueRow, moduleLabel, annotations.get(module)?.hexColor, spotInfo, r, lFilter, uFilter, lPvFilter, uPvFilter, colLabels, dendrogramHeight)
        spots.addAll(rowModel.spots)
        spotTexts.addAll(rowModel.spotText)
        rowHeaders.add(rowModel.rowHeader)
        rowHeaders.add(rowModel.rowAnnot)

        width = Math.max(width, rowModel.width)
        height = Math.max(height, rowModel.height)
        r++
      }

      Map bg = [ x:ILEFT_PADDING, y:(dendrogramHeight + ITOP_PADDING), width:(width - ILEFT_PADDING), height:(height - dendrogramHeight - ITOP_PADDING), attr:[ fill:"#FFF", stroke:"#333", "stroke-width":2 ] ]
      return [width:width, height:height, bg:bg, yAxis:rowHeaders, xAxis:colHeaders, spots:spots, spotTexts:spotTexts, colDendrogram:colDendrogram]
    }
    return null
  }

  private Map fingerprintRow(DoubleMatrix1D matrix, DoubleMatrix1D pValues, String rowLabel, String rowColor, Map rowData, int row, double lFilter, double uFilter, double lPvFilter, double uPvFilter, Map colLabels, double topOffset) {
    List spots = [], spotText = []

    int x = ILEFT_PADDING + BORDER_WIDTH, y = topOffset + ITOP_PADDING + BORDER_WIDTH + (row * FULL_SPOT_UNIT)
    int cx = ILEFT_PADDING + BORDER_WIDTH, cy = topOffset + ITOP_PADDING + BORDER_WIDTH + (row * FULL_SPOT_UNIT) + SPOT_UNIT
    int numCols = matrix.size() - 1
    for (col in 0..numCols) {
      cx += SPOT_UNIT

      double v = matrix.getQuick(col)
      double absV = Math.abs(v)
	  double pValue = pValues.getQuick( col );

      Map thisRowData = new LinkedHashMap()
      rowData.each { rk, rv ->
        if (rk == "info") {
          thisRowData.put("info", rv.clone())
        } else {
          thisRowData.put(rk,rv)
        }
      }
      if ( v != 0d && v >= lFilter && v < uFilter
		   && ((uPvFilter == 0.0) || (pValue >= lPvFilter && pValue < uPvFilter)) )
      {
        boolean up = v > 0
        String color = up ? "#FF0000" : "#0000FF"

        thisRowData.up = up
        thisRowData.value = absV.round(2)
		thisRowData.pValue = pValue;
        thisRowData.info["Variable"] = colLabels.get(col).label

        String field = colLabels.get(col).field
        String overlay = colLabels.get(col).datatype == "string" ? "categorical_overlay:${field}=0:0" : "numericalOverlay=${field}"
		if (rowData.gxbLink && overlay) {
			def gxbLinkWithOverlay = "${rowData.gxbLink}&${overlay}&initOverlays=true"
			thisRowData.gxbLink = "<a href='${gxbLinkWithOverlay}' target='_blank'>View in Gene Expression Browser</a>";
		}

        spots.push([ x:x, y:y, width:BOX_WIDTH, height:BOX_WIDTH, attr:[ fill:color, "fill-opacity":absV.round(2), stroke:"none", "stroke-width":0 ], data:thisRowData ])
        spotText.push([ cx:cx, cy:cy, text:v.round(2), attr:[ fill:"#000", font:"11px Helvetica" ], data:thisRowData ])
      }

      x += BOX_WIDTH
      cx += SPOT_UNIT
    }

    Map rowHeader = [ type:"text", x:(ILEFT_PADDING - 35), y:cy, text:rowLabel, "text-anchor":"end", font:"16px Helvetica" ]
    Map rowAnnot = [ type:"rect", x:(ILEFT_PADDING - 25), y:(topOffset + ITOP_PADDING + BORDER_WIDTH + (row * FULL_SPOT_UNIT)), width:20, height:FULL_SPOT_UNIT, fill:rowColor, stroke:"none", "stroke-width":0 ]

    return [ width:(x + BORDER_WIDTH), height:(y + BOX_WIDTH), spots:spots, spotText:spotText, rowHeader:rowHeader, rowAnnot:rowAnnot ]
//    return [ width:(cx + BORDER_WIDTH), height:(cy + SPOT_UNIT), spots:spots, rowHeader:rowHeader, rowAnnot:rowAnnot ]
  }

	private static
	void fdrAdjustMultiCorrelationPvalues( Map fieldToCorrelation )
	{
		List< Double > pValues = [];
		fieldToCorrelation.each { String field, Map moduleCorrs ->
			moduleCorrs.each { String module, Map corrRslt ->
				pValues.add( corrRslt.pValue );
			}
		}

		double[] pValArr = pValues.toArray();
		double[] adjPvalues = StatsService.fdrAdjust( pValArr );

		int i = 0;
		fieldToCorrelation.each { String field, Map moduleCorrs ->
			moduleCorrs.each { String module, Map corrRslt ->
				corrRslt.pValue = adjPvalues[ i++ ];
			}
		}
	}

  private static final int ITOP_PADDING = 50, ILEFT_PADDING = 300

  Map correlationHistogram(Analysis analysis, String field, double floor, boolean fdrAdjust = false ) {
    def mongoCor = mongoDataService.findOne("correlations", [analysisId:analysis.id, field:field], ["correlation"])
    Map corCoeff = null
    if (mongoCor) {
      corCoeff = mongoCor.correlation
    } else {
      long startTime = System.currentTimeMillis()
      Map matrix = prepareMatrix(analysis)
      corCoeff = calcCorrelation(matrix.matrix, analysis.sampleSetId, matrix.samples, matrix.modules, field, floor)
      println "cc time = ${System.currentTimeMillis() - startTime}ms"
    }
	if ( fdrAdjust )
	{
		fdrAdjustModuleCorrelationPvalues( corCoeff );
	}

//    def moduleGenerationId = getModuleGen(analysis)
//    def annotations = getAnnotations(moduleGenerationId)
    Map<String,ModuleAnnotation> annotations = getAnnotations(Integer.parseInt(analysis.modGeneration))

    // draw barchart
    int[] buckets = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    def modBuckets = [[], [], [], [], [], [], [], [], [], []]
    int i = 0
    corCoeff.each { String mod, Map corrRslt ->
      double cc = corrRslt.statistic;
      if (cc < -0.8) {
        i = 0
      } else if (cc < -0.6) {
        i = 1
      } else if (cc < -0.4) {
        i = 2
      } else if (cc < -0.2) {
        i = 3
      } else if (cc < 0.0) {
        i = 4
      } else if (cc < 0.2) {
        i = 5
      } else if (cc < 0.4) {
        i = 6
      } else if (cc < 0.6) {
        i = 7
      } else if (cc < 0.8) {
        i = 8
      } else if (cc < 1.0) {
        i = 9
      }
      buckets[i]++
      Map modInfo = [module:mod.replaceAll("_","\\."), score:cc.round(2)]
      if (annotations.containsKey(modInfo.module)) {
        modInfo.annotation = annotations.get(modInfo.module).annotation
      }
      modBuckets[i].push(modInfo)
    }

    def bars = []
    for (j in 0..(buckets.length - 1)) {
      int count = buckets[j]
      double lRange = -1.0 + (0.2 * j)
      double uRange = -1.0 + (0.2 * (j + 1))
      bars.push([ count:count, data:[count:count, lRange:lRange, uRange:uRange, modules:modBuckets[j]] ])
    }
//    println "bucketing time = ${System.currentTimeMillis() - start} ms"
    Map correlations = [:]
    corCoeff.each { String m, Map corrRslt ->
      double score = corrRslt.statistic;
	  double pValue = corrRslt.pValue;
      String module = m.replaceAll("_","\\.")
      String annotation = annotations.get(module)?.annotation ?: ""
      correlations.put(module, [annotation:annotation, score:score, pValue: pValue])
    }

    return [bars: bars, max: corCoeff.size(), correlations:correlations]
  }

  private static
	void fdrAdjustModuleCorrelationPvalues( Map moduleCorrs )
	{
		List< Double > pValues = [];
		moduleCorrs.each { String module, Map corrRslt ->
			pValues.add( corrRslt.pValue );
		}

		double[] pValArr = pValues.toArray();
		double[] adjPvalues = StatsService.fdrAdjust( pValArr );

		int i = 0;
		moduleCorrs.each { String module, Map corrRslt ->
			corrRslt.pValue = adjPvalues[ i++ ];
		}
	}

  Map prepareMatrix(Analysis analysis) {
//    long startTime = System.currentTimeMillis()
    Map files = getDifferenceFiles(analysis, false, true)
    Map posMongo = files.positive
    Map negMongo = files.negative
//    println "files retrieved @ ${System.currentTimeMillis() - startTime}ms"

    List<String> samples = posMongo.header
    samples.remove(0)
    boolean hasAllColumn = samples.get(0) == "All"

    DoubleDoubleFunction func = new DoubleDoubleFunction() {
      public double apply(double a, double b) {
        return a - b
      }
    }

    // read positive and negative files
    def posLoadResult = loadMatrix(posMongo.rows, null, hasAllColumn)
    def negLoadResult = loadMatrix(negMongo.rows, null, hasAllColumn)
    List<String> modules = posLoadResult.modules
    SparseDoubleMatrix2D posMatrix = posLoadResult.matrix
    SparseDoubleMatrix2D negMatrix = negLoadResult.matrix
    SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(posMatrix.rows(), posMatrix.columns())
    matrix.assign(posMatrix)
    matrix.assign(negMatrix, func)
//    println "files loaded @ ${System.currentTimeMillis() - startTime}ms"
    //    println "matrices loaded = ${System.currentTimeMillis() - start} ms"
    return [matrix: matrix, samples: samples, modules: modules]
  }

  Map calcCorrelation(DoubleMatrix2D matrix, long sampleSetId, List<String> samples, List<String> modules, String field, double floor) {
    DenseDoubleMatrix1D values = new DenseDoubleMatrix1D(samples.size())
    String[] keys = field.split("_", 3)
    List<Integer> validSamples = new ArrayList<Integer>()
    if (keys[0].startsWith("labkey"))
    {
      String[] pids = new String[samples.size()]
      def sampleToIds = [:]

      LabkeyReport theReport = LabkeyReport.findBySampleSetIdAndCategory(sampleSetId, keys[0].trim())
      if (theReport?.sampleSetColumn && theReport?.reportColumn)
      {
        String sampleColumn = "values.${theReport.sampleSetColumn}".toString()
        List pidResults = mongoDataService.find("sample", [ sampleSetId:sampleSetId ], [ "sampleId", sampleColumn ], null, -1)
        pidResults.each { Map r ->
          int idx = samples.indexOf(r.sampleId.toString())
          if (idx != -1) {
            sampleToIds.put(r.sampleId, r.values[theReport.sampleSetColumn])
            pids[idx] = r.values[theReport.sampleSetColumn]
          }
        }
        List orderPids = pids.toList()
        if (!(keys[0] in [ "labkeyLabResults", "labkeyFlow" ])) {
          Map fInfo = mongoDataService.findOne("sampleSet", [sampleSetId: sampleSetId], ["${keys[0]}.header.${keys[2]}".toString()])
          String fType = fInfo.get(keys[0]).header.get(keys[2]).datatype
          Map query = [:]
          query.put(theReport.reportColumn, ['$in':pids])
          query.put(keys[2], ['$ne': ""])
          def strToInt = [:]
          double vInt = 1d
          List lkResults = mongoDataService.find(keys[0], query, [theReport.reportColumn, keys[2]], null, -1)
          lkResults.each { Map lkr ->
            def v = lkr[keys[2]]
            int idx = orderPids.indexOf(lkr[theReport.reportColumn])
            if (idx != -1) {
              validSamples.push(idx)
              if (fType == "string") {
                if (!strToInt.containsKey(v)) {
                  strToInt.put(v, vInt)
                  vInt++
                }
                values.setQuick(idx, strToInt.get(v))
              } else {
                if (v instanceof Number) {
                  values.setQuick(idx, v)
                } else {
                  values.setQuick(idx, Double.parseDouble(v))
                }
              }
            }
          }
        } else {
          String fKey = keys[2]
          String testName = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSetId ], [ "${keys[0]}.tests.${fKey}".toString() ] )?.get(keys[0])?.tests?.get(fKey)?.displayName
          String testNameCol = keys[0] == "labkeyLabResults" ? "lbtest" : "flow_population"
          String testValueCol = keys[0] == "labkeyLabResults" ? "lborres" : "flow_value"
          sampleToIds.each { long sid, Object theSampleId ->
            Map query = [:]
            query.put(theReport.reportColumn, theSampleId)
            query.put(testNameCol, testName)
            Map v = mongoDataService.findOne(keys[0], query, [ theReport.reportColumn, testValueCol ])
            if (v?.get(testValueCol) && v.get(testValueCol) != "") {
              int idx = orderPids.indexOf(v[theReport.reportColumn])
              if (idx != -1) {
                validSamples.push(idx)
                if (v.get(testValueCol) instanceof Number) {
                  values.setQuick(idx, v.get(testValueCol))
                } else {
                  values.setQuick(idx, Double.parseDouble(v.get(testValueCol)))
                }
              }
            }
          }
        }
      }
    }
    else
    {
      String fKey = keys[2]
      String fHeaderKey = fKey.substring(7)

      Map sampleSetInfo = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSetId ], [ "spreadsheet" ])
      Map fieldInfo = sampleSetInfo?.spreadsheet?.header?.get(fHeaderKey)
      String datatype = fieldInfo ? fieldInfo.datatype : "string"
      int numUnique = fieldInfo ? fieldInfo.numUnique : -1

      List uniqueValues = mongoDataService.getColumnValues(sampleSetId, fKey)
      if (uniqueValues.contains(""))
      {
        uniqueValues.remove("")
        numUnique = uniqueValues.size()
      }
      Map valueToCode = [:]
      if (datatype == "string")
      {
        if (numUnique == 2 && uniqueValues.containsAll(["N","Y"]))
        {
          valueToCode = [ "Y":1, "N":-1 ]
        }
        else
        {
          uniqueValues.eachWithIndex { v, i ->
            valueToCode.put(v, (i+1))
          }
        }
      }

      List sResults = mongoDataService.getColumnValuesAndSampleId(sampleSetId, keys[2].toString())
      if (datatype == "number")
      {
        sResults.each {
          try {
            double v = Double.parseDouble(it[fKey])
            int idx = samples.indexOf(Long.toString(it.sampleId))
            if (idx != -1) {
              validSamples.push(idx)
              values.setQuick(idx, v)
            }
          } catch (Exception e) {
          }
        }
      }
      else
      {
        sResults.each {
          int idx = samples.indexOf(Long.toString(it.sampleId))
          if (idx != -1) {
            validSamples.push(idx)
            def val = valueToCode.containsKey(it[fKey]) ? valueToCode.get(it[fKey]) : 0
            values.setQuick(idx, val)
          }
        }
      }
      Collections.sort(validSamples)
    }

    if (values.cardinality() > 0)
    {
      int[] validSamplesArray = validSamples.toArray(new int[validSamples.size()])
      def corCoeff = [:]
      modules.eachWithIndex { m, i ->
        String mod = m.replaceAll("\\.","_")
        DoubleMatrix1D row = matrix.viewRow(i)
        DoubleMatrix1D validRow = row.viewSelection(validSamplesArray)
        boolean passed = validRow.aggregate(Functions.max, Functions.identity) > floor || validRow.aggregate(Functions.min, Functions.identity) < -floor
        if (passed) {
          corCoeff.put(mod, StatsService.spearmansRankCorrelation(values.viewSelection(validSamplesArray).toArray(), validRow.toArray()))
        }
      }
      return corCoeff
    }

    return null
  }

  Map corScatterPlot(Analysis analysis, String module, String field)
  {
    Map files = getDifferenceFiles(analysis, false, true)
    Map posMongo = files.positive
    Map negMongo = files.negative

    List<String> samples = posMongo.header
    samples.remove(0)
    boolean hasAllColumn = samples.get(0) == "All"

    DoubleDoubleFunction func = new DoubleDoubleFunction() {
      public double apply(double a, double b) {
        return a - b
      }
    }

    // read positive and negative files to get % diff
    List diffRow = []
    posMongo.rows.each { List pRow ->
      if (pRow[0] == module) {
        int len = pRow.size()-1
        if (hasAllColumn) {
          diffRow = pRow[2..len]
        } else {
          diffRow = pRow[1..len]
        }
      }
    }
    negMongo.rows.each { List nRow ->
      if (nRow[0] == module) {
        int len = nRow.size()-1
        List tempNRow = null
        if (hasAllColumn) {
          tempNRow = nRow[2..len]
        } else {
          tempNRow = nRow[1..len]
        }
        tempNRow.eachWithIndex { nr, i ->
          diffRow[i] += nr
        }
      }
    }


  }

  Map moduleLineChart(Analysis analysis, String module) {
    long moduleGenerationId = getModuleGen(analysis)

    String query = """SELECT md.probe_id 'probe' FROM module m
      JOIN module_detail md ON m.id = md.module_id
      WHERE m.module_generation_id = ${moduleGenerationId}
      AND m.module_name = '${module.trim()}'""".toString()
    Sql sql = Sql.newInstance(dataSource)
    sql.eachRow(query) {
      it.probe
    }
    sql.close()

    String sampleIdQuery = """SELECT dd.sample_id FROM dataset_group dg
      JOIN dataset_group_detail dd ON dg.id = dd.group_id
      WHERE dg.group_set_id = 10"""

    Map model = [:]
    return model
  }

}
