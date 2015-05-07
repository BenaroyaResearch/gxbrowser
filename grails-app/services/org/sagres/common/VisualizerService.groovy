package org.sagres.common

import cern.colt.function.DoubleDoubleFunction
import cern.colt.matrix.DoubleMatrix1D
import cern.colt.matrix.DoubleMatrix1DProcedure
import cern.colt.matrix.DoubleMatrix2D
import cern.colt.matrix.impl.SparseDoubleMatrix2D
import cern.jet.math.Functions
import groovy.sql.Sql
import org.sagres.mat.ModuleGeneration
import org.sagres.stats.ClusterHierarchy
import org.sagres.stats.ClusterNode
import org.sagres.stats.HierarchicalClusterer
import org.sagres.importer.TextTable
import org.sagres.importer.TextTableSeparator
import org.bson.types.ObjectId
import org.sagres.mat.Module
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

class VisualizerService {

  def grailsApplication
  def dataSource
  def mongoDataService

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
                           STARTY = TOP_PADDING + BORDER_WIDTH

  private final int DENDLEFT = 300,  //left margin of heatmap
                    HSCALE = 40,     //width of one heatmap cell (neg. to draw upward)
                    VSCALE = 10      //height of one dendrogram level
  private int dEndBase = 100         //bottom of dendrogram (top of heatmap)

  static final int GSA = 10, PIE = 11, MOD = 12
  static final int UNDER = 0, OVER = 1
  def static final GSA_COLORS = [["#0049f8", "#5970f9", "#939efa", "#cacefc"],  // under
                                 ["#ff3b2d", "#ff6964", "#ff9b98", "#ffcdcc"]]  // over
  def static final GROUP_COLORS = ["#F3C94A", "#02A748", "#51316D", "#02A7A4"] //"#3288BD","#D53E4F"

  Map matPlot(Map params)
  {
    Map rows = params.rows
    if (rows)
    {
      boolean isPie = params.isPie != null ? params.isPie : false
      boolean annotatedOnly = params.annotatedOnly != null ? params.annotatedOnly : false
      boolean showRowSpots = params.showRowSpots != null ? params.showRowSpots : true
      boolean clusterRows = params.clusterRows != null ? params.clusterRows : true
      boolean clusterCols = params.clusterCols != null ? params.clusterCols : true
	  double floor = params.floor != null ? params.floor : 0
      boolean posNeg = rows.pos && rows.neg

      int numCols = -1
      List<List<String>> posRows = null, negRows = null, diffRows = null
      if (posNeg)
      {
        posRows = rows.pos
        negRows = rows.neg
        numCols = posRows[1].size() - 1
      }
      else if (rows.difference)
      {
        diffRows = rows.difference
        numCols = diffRows[1].size() - 1
      }

      // can't draw a pie chart if there are no pos and neg rows
      if (isPie && !posNeg) {
        return null
      }

      int modGen = params.modGen ?: 2
      Map annotations = getAnnotations(modGen)
      Map groups = params.design?.groups
      Map sampleToGroup = params.design?.sampleToGroup
      List<String> header = params.header ?: (1..numCols).collect { "Column ${it}".toString() }

      // Matrices are filtered to modules with annotations if "Annotated" selected
      List<String> modules = null
      SparseDoubleMatrix2D posMatrix = null, negMatrix = null, matrix = null
      if (posNeg)
      {
        Map posResult = loadMatrix(posRows, (annotatedOnly ? annotations : [:]))
        Map negResult = loadMatrix(negRows, (annotatedOnly ? annotations : [:]))

        DoubleDoubleFunction func = new DoubleDoubleFunction() {
          public double apply(double a, double b) {
            return a - b
          }
        }

        // read positive and negative files
        modules = posResult.modules
        posMatrix = posResult.matrix
        negMatrix = negResult.matrix
        matrix = new SparseDoubleMatrix2D(posMatrix.rows(), posMatrix.columns())
        matrix.assign(posMatrix)
        matrix.assign(negMatrix, func)
      }
      else
      {
        Map diffResult = loadMatrix(diffRows, (annotatedOnly ? annotations : [:]))
        modules = diffResult.modules
        matrix = diffResult.matrix
      }

      // Filter
      def rowsToKeep = []
      int i = 0, j = 0
      matrix = matrix.viewSelection(new DoubleMatrix1DProcedure() {
        boolean apply(DoubleMatrix1D m) {
          boolean passed = m.aggregate(Functions.max, Functions.abs) > floor
          if (passed) {
            rowsToKeep.push(j)
            i++
          }
          else {
            modules.remove(i)
          }
          j++
          return passed
        }
      }).copy();
      int[] rowsToKeepArray = rowsToKeep.toArray(new int[rowsToKeep.size()])
      if (posNeg)
      {
        posMatrix = posMatrix.viewSelection(rowsToKeepArray, null).copy();
        negMatrix = negMatrix.viewSelection(rowsToKeepArray, null).copy();
      }

      def rowClusterResult = null, colClusterResult = null
      if (clusterRows) { rowClusterResult = cluster(matrix, true) }
      if (clusterCols) { colClusterResult = cluster(matrix, false) }
      List<Integer> rowClusteredOrder = clusterRows ? rowClusterResult[0] : 0..(modules.size()-1)
      List<Integer> colClusteredOrder = clusterCols ? colClusterResult[0] : 0..(numCols-1)

      def rowDendrogram = clusterRows ? dendrogram(rowClusterResult[1], colClusteredOrder.size()) : null
      def colDendrogram = clusterCols ? dendrogram(colClusterResult[1], colClusteredOrder.size()) : null
      def dendrogramHeight = clusterCols ? dEndBase : 20

      def groupHeight = groups ? 16 : 4
      def r = 17, pad = 3, topPadding = dendrogramHeight + groupHeight, leftPadding = 300, cellWidth = (2 * (pad + r)), spotUnit = pad + r
      def cy = topPadding, annotationY = topPadding
      def circles = [], paths = [], sampleLbls = [], groupsKey = [], moduleLabels = [], annotationKeys = []

      rowClusteredOrder.each { row ->
        String module = modules[row]
        Map annotMap = annotations.get(module)
        cy += spotUnit

        def textLabel = annotMap ? "${module} ${annotMap.name}" : module
        Map moduleData = [ info:[ "Module":module ] ]
        moduleLabels.push([ x:leftPadding - 40, y:cy, text:textLabel, attr:[ font:"16px Fontin-Sans, Arial", "text-anchor":"end" ], data:moduleData ])

        def cx = leftPadding, addedValue = false
        colClusteredOrder.each { col ->
          cx += spotUnit
          double v = matrix.getQuick(row, col)
          double absV = Math.abs(v)
          double posV = 0.0, negV = 0.0
          if (posNeg)
          {
            posV = posMatrix.getQuick(row, col)
            negV = negMatrix.getQuick(row, col)
          }

          // populate module info
          String sampleId = header.get(col).trim()
          Map data = [ info:["Module": module] ]
          if (sampleToGroup)
          {
            data.group = sampleToGroup[sampleId]
          }
          if (annotMap)
          {
            data.info["Function"] = annotMap.name
            data.color = annotMap.color
          }
          if (posNeg)
          {
            data.info["% Positive"] = "${posV.round(1)} %"
            data.info["% Negative"] = "${Math.abs(negV.round(1))} %"
          }
          data.sampleId = sampleId

          // start drawing plot
          if (showRowSpots || absV > floor)
          {
            addedValue = true
            if (isPie)
            {
              def pos = (posV / 100) * 360, neg = (-negV / 100) * 360
              if (neg < 0d && neg != -360d) {
                paths.push([segment: [cx, cy, r, 270 + neg, 270, -1]])
              }
              if (pos > 0d && pos != 360d) {
                paths.push([segment: [cx, cy, r, 270, 270 + pos, 1]])
              }
              def fill = pos == 360d ? "#ff0000" : ((neg == -360d) ? "#0000ff" : "#ffffff")
              def fillOpacity = (pos == 360d || neg == -360d) ? 1 : 0
              circles.push([ cx:cx, cy:cy, r:r, attr:[ fill:fill, "fill-opacity":fillOpacity, stroke:"#666666" ], data:data ])
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
        if (annotMap)
        {
          annotationKeys.push([ type:"rect", x:(leftPadding - 30), y:annotationY, width:20, height:cellWidth, fill:annotMap.color, stroke:"none" ])
        }
        annotationY += cellWidth

      }

      def x = leftPadding, groupX = leftPadding, groupY = groups ? 30 : 10
      def attr = [ font:"16px Fontin-Sans, Arial", "text-anchor":"end" ]
      def orderedSamples = []
      colClusteredOrder.each { sIdx ->
        String s = header[sIdx].trim()
        orderedSamples.push(s)
        x += spotUnit
        Map sData = [ sampleId:s ]
        if (sampleToGroup)
        {
          sData.cx = groupX
          def sampleGroup = sampleToGroup[s]
          def groupColor = groups[sampleGroup].color
          def groupData = [ sampleId:s, group:sampleGroup ]
          groupsKey.push([ x:groupX, y:(topPadding - 16), width:(cellWidth - 1), height:8, attr:[ fill:groupColor, stroke:"none" ], data:groupData ])
          groupsKey.push([ x:groupX, y:(cy + 10), width:(cellWidth - 1), height:8, attr:[ fill:groupColor, stroke:"none" ], data:groupData ])
        }
        sampleLbls.push([ x:(x + 2), y:(cy + groupY), text:s, attr:attr, data:sData ])
        x += spotUnit
        groupX += cellWidth
      }
      def width = header.size() * (2 * (pad + r)) + 6
      def height = cy - topPadding + 6

      def bg = [ type:"rect", x:(leftPadding - 3), y:(topPadding - 3), width:width, height:height, stroke:"#111111", "stroke-width":2 ]
      def chartWidth = leftPadding + width + 50
      def chartHeight = cy + (2 * topPadding)

      def model = [ width:chartWidth, height:chartHeight, bg:bg, sampleLbls:sampleLbls, groupsKey:groupsKey, moduleLabels:moduleLabels,
        annotationKeys:annotationKeys, samples:orderedSamples, rowDendrogram:rowDendrogram, spots:circles,
        colDendrogram:colDendrogram, startY:topPadding, endY:cy ]
      if (isPie)
      {
        model.segments = paths
      }

      return model
    }
  }

  Map groupModulePlot(Map params) {

    boolean top = params.top != null ? params.top : true
    boolean isPie = params.isPie != null ? params.isPie : false
    int modGen = params.modGen ?: 2
	long sampleSetId = params.sampleSetId ?: 0
	String modVersionName = params.modVersionName ?: "IlluminaV3"

    int type = isPie ? PIE : MOD

    List rows = filterProbeRows(params)
    if (rows)
    {
      // sort the modules correctly
      Map m1ToMaxM2 = [:]
      List moduleValues = []
      rows.each { List<String> r ->
        String module = r[0]
        String[] modules = module.substring(1).split("\\.")
        int m1 = modules[0].toInteger()
        int m2 = modules[1].toInteger()
        if (!(top && m1 > 6)) {
          moduleValues.push([ module:module, m1:m1, m2:m2, v1:r[1], v2:r[2], numProbes:r[3] ])
          if (!m1ToMaxM2.containsKey(m1)) {
            m1ToMaxM2.put(m1, m2)
          } else {
            int max = Math.max(m2, m1ToMaxM2.get(m1))
            m1ToMaxM2.put(m1, max)
          }
        }
      }
  //    println "mapping @ ${System.currentTimeMillis() - startTime} ms"

      moduleValues.sort { a, b ->
        if (a.m1 > b.m1) { return 1 }
        else if (a.m1 < b.m1) { return -1 }
        else if (a.m1 == b.m1) {
          if (a.m2 > b.m2) { return 1 }
          else if (a.m2 < b.m2) { return -1 }
        }
        return 0
      }
  //    println "sorting modules @ ${System.currentTimeMillis() - startTime} ms"

      // get additional information for each module
      Map annotations = getAnnotations(modGen)
      Map modToNumRows = [:]
  //    println "retrieving additional info @ ${System.currentTimeMillis() - startTime} ms"

      // go through each row and draw stuff
      List blanks = [], boxes = [], aBoxes = [], spots = [], segments = []
      int lastM1 = -1, lastM2 = 0
      def m1y = 0, cy = STARTY + BOX_PADDING + SPOT_RADIUS, y = STARTY
      def maxX = 0, maxY = 0
      moduleValues.each { Map r ->
        double diff = r.v1 + r.v2
        boolean draw = r.v1 > 0d || r.v2 < 0d
        boolean up = up(MOD, r.v1, r.v2, diff)

        // add additional module info
        Map moduleInfo = [info: ["Module": r.module, "# of probes": r.numProbes]]
        Map spotInfo = [info: ["Module": r.module, "# of probes": r.numProbes]]
        Map annotInfo = [info: ["# of probes": r.numProbes]]
        if (annotations.containsKey(r.module)) {
          Map aInfo = annotations.get(r.module)
          moduleInfo.info["Function"] = aInfo.name
          moduleInfo.color = aInfo.color
          spotInfo.info["Function"] = aInfo.name
          spotInfo.color = aInfo.color
          if (modGen == 2) {
            annotInfo.annotation = "<a href='http://www.biir.net/public_wikis/module_annotation/V2_Trial_8_Modules_${r.module}' target='_blank'>${aInfo.name}</a>"
          } else {
            annotInfo.annotation = aInfo.name
          }
          annotInfo.color = aInfo.color
          annotInfo.abbr = aInfo.abbrev
        }
        switch (type) {
          case PIE:
            spotInfo.info["% Positive"] = r.v1.trunc(2)
            spotInfo.info["% Negative"] = Math.abs(r.v2.trunc(2))
            break
          default:
            spotInfo.up = up
            spotInfo.value = "${Math.abs(diff.trunc(2))} %"
            spotInfo.info["% Positive"] = r.v1.trunc(2)
            spotInfo.info["% Negative"] = Math.abs(r.v2.trunc(2))
            break
        }
		if (sampleSetId != 0) {
			def gxbLink = new ApplicationTagLib().createLink(controller:"geneBrowser", action:"show", id:sampleSetId, params:[ module:r.module, modVersionName: modVersionName])
			  moduleInfo.gxbLink = "<a href='"+gxbLink+"' target='_blank'>View in Gene Expression Browser</a>";
			  spotInfo.gxbLink = "<a href='"+gxbLink+"' target='_blank'>View in Gene Expression Browser</a>";
		}
        if (modGen == 2 && grailsApplication.config.module.wiki.on)
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
            spots.push([cx: cx, cy: (cy + m1y), r: SPOT_RADIUS, attr: [fill: "#FFF", "stroke-width": 1, stroke: "#999"], data: spotInfo])
          }
        }
        else {
          def attr = [stroke: "none", fill: color(type, r.v1, r.v2, diff)]
          if (type == MOD) {
            attr.put("fill-opacity", spotOpacity(diff))
          }
          spots.push([cx: cx, cy: (cy + m1y), r: SPOT_RADIUS, attr: attr, data: spotInfo])
        }

        lastM1 = r.m1
        lastM2 = r.m2
      }
  //    println "iterating @ ${System.currentTimeMillis() - startTime} ms"

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

      def annotationKeyLabels = annotationKeyWithLabels(annotations, top)

      def bg = [type: "rect", x: LEFT_PADDING, y: TOP_PADDING, width: maxX - LEFT_PADDING, height: maxY - TOP_PADDING, stroke: "#111111", "stroke-width": BORDER_WIDTH * 2, fill: "#cccccc"]
      def width = maxX + 30
      def height = maxY + 60

      def model = [width: width, height: height, bg: bg, xAxis: xAxis, yAxis: yAxis, aBoxes: aBoxes, annotations: annotationKeyLabels, boxes: boxes, blanks: blanks, spots: spots]
      if (isPie) {
        model.segments = segments
      }

      return model
    }
  }

  private List filterProbeRows(Map params)
  {
    boolean isFc = params.fileType == "fc"
    List<List<String>> rows = isFc ? params.rows.fc : params.rows.probelvl
    double cutoff = params.floor ?: 0.05 // params.floor is really the p-value slider cutoff
	double minChange  = (params.minChange ?: 0.10) * 100 // percent
    boolean mts = params.mts?.asBoolean()
    long modGenId = params.modGenId ?: ModuleGeneration.findByGeneration(2).id
	List results = []

    int moduleCol = 1, upDownCol = 2, origPval = 3, adjPval = 4
    Map<String,Map<String,Integer>> moduleToProbeCount = [:]
    rows.each { List<String> values ->
      String module = values[moduleCol].trim()
      double upDownVal = values[upDownCol].toDouble()
      if (module != "NA") // && (!isFc || upDownVal < -1 || upDownVal > 1))
      {
		String upDown = upDownVal < 0d ? "down" : "up"
		//String upDown = isFc ? (upDownVal > 1 ? "up" : "down") : (upDownVal < 0d ? "down" : "up")
        double pvalue = mts ? values[adjPval].toDouble() : values[origPval].toDouble()
        if (pvalue < cutoff)
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

	Module.findAllByModuleGenerationId(modGenId).each {
      String m = it.moduleName
      int totalProbes = it.probeCount
      Map upDownCount = moduleToProbeCount.get(m)
      double percentUp = upDownCount ? upDownCount.up / totalProbes * 100 : 0d
      double percentDown = upDownCount ? upDownCount.down / totalProbes * -100 : 0d
	  if (!isFc || (percentUp - percentDown) > minChange) { // push all if not Fc, percent down are negative
		results.push([ m, percentUp, percentDown, totalProbes ])
	  } else {
	  	results.push([ m, 0d, 0d, totalProbes ])
	  }
    }

    return results
  }

  private def List annotationKeyWithLabels(Map annotations, boolean top) {
    def annotationLabels = [:]
    def aCount = 0
    annotations.each { String module, Map aMap ->
      String annotation = aMap.name
      if (!top || (top && Integer.parseInt(module[1]) <= 6)) {
        if (!annotationLabels.containsKey(annotation)) {
          def attr = [box: [fill: aMap.color, stroke: "#333", "stroke-width":1], text: [fill: "#333", "text-anchor": "start", "font-size": 12]]
          def label = [x: (aCount % 3 * 250 + 150), y: (Math.floor(aCount / 3) * 20), width: 12, height: 12, text: annotation, attr: attr]
          annotationLabels.put(annotation, label)
          aCount++
        }
      }
    }
    return annotationLabels.values().asList()
  }

  private Map loadMatrix(List data, Map annotations)
  {
    SparseDoubleMatrix2D matrix = null
    List<String> modules = new ArrayList<String>()
    int rows = data.size(), cols = 0, cRow = 0
    data.eachWithIndex { List<String> values, int row ->
      if (row == 0) {
        cols = values.size() - 1
        matrix = new SparseDoubleMatrix2D(rows, cols)
      }

      String module = values.remove(0)
      if (!annotations || annotations?.containsKey(module)) {
        modules.push(module)
        matrix.viewRow(cRow).assign(values*.toDouble().toArray(new double[values.size()]))
        cRow++
      }
    }

    if (annotations) {
      return [ matrix:matrix.viewPart(0, 0, cRow, cols), modules:modules ]
    } else {
      return [ matrix:matrix, modules:modules]
    }
  }

  private List cluster(DoubleMatrix2D features, boolean byRow) {
    List<Integer> items = new ArrayList<Integer>()
    ClusterHierarchy hierarchy = HierarchicalClusterer.clusterVectors(features, HierarchicalClusterer.Metric.PEARSON, byRow);
    ClusterNode tree = hierarchy.getTree();
    tree.getItems(items);
    return [items, hierarchy.getTree()]
  }

  private Map dendrogram(ClusterNode tree, int numSamples) {
    List lines = [], clickpoints = []
    dEndBase = Math.round(Math.ceil(tree.getDistanceFromLeaf() * VSCALE)) + 20
    drawDendrogramBranch(tree, lines, clickpoints, numSamples)
    return [lines: lines, clickpoints: clickpoints]
  }

  private void drawDendrogramBranch(ClusterNode node, List lines, List clickpoints, int numSamples) {
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
      drawDendrogramBranch(child, lines, clickpoints, numSamples) //recurse
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
    return Math.abs(diff) / 100
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

  private Map getAnnotations(int modGen)
  {
    Map annotations = [:]

    long modGenId = ModuleGeneration.findByGeneration(modGen).id
    String query = """SELECT m.module_name 'module', a.annotation 'fullname', a.abbreviation 'shortName', a.hex_color 'color'
      FROM module m
      JOIN module_annotation a ON m.module_annotation_id = a.id
      WHERE m.module_generation_id = ${modGenId}"""
    Sql sql = Sql.newInstance(dataSource)
    sql.eachRow(query) {
      annotations.put(it.module, [ name:it.fullname, abbrev:it.shortName, color:it.color ])
    }
    sql.close()

    return annotations
  }

  Map parseMatFiles(File f, String fileType, boolean hasHeader = true, TextTableSeparator delimiter = TextTableSeparator.CSV)
  {
    List header = null, rows = []
    f.eachLine { text, i ->
      String[] values = TextTable.splitRow(text, delimiter)
      if (hasHeader && i == 1)
      {
        // MAT files should have module as first column
        header = values.toList()
        header.remove(0)
      }
      else
      {
        rows.push(values.toList())
      }
    }
    return [ header:header, rows:rows ]
  }

  Map parseMatDesignFile(File f, TextTableSeparator delimiter = TextTableSeparator.CSV)
  {
    Map groups = [:], sampleToGroup = [:]
    int sampleIdx = 1, groupIdx = 2, groupLblIdx = 3
    f.eachLine { text, i ->
      String[] values = TextTable.splitRow(text, delimiter)
      if (i == 1) {
        values.eachWithIndex { header, col ->
          switch (header) {
            case header.equalsIgnoreCase("sample_id"): sampleIdx = col; break;
            case header.equalsIgnoreCase("group"): groupIdx = col; break;
            case header.equalsIgnoreCase("group_label"): groupLblIdx = col; break;
          }
        }
      } else {
        String sampleLabel = values[sampleIdx]
        String groupLabel = values[groupLblIdx].encodeAsHumanize()
        if (!groups.containsKey(groupLabel)) {
          int groupNum = Integer.parseInt(values[groupIdx])
          groups.put(groupLabel, [ groupNum:groupNum, color:GROUP_COLORS[groupNum] ])
        }
        sampleToGroup.put(sampleLabel, groupLabel)
      }
    }
    return [ groups:groups, sampleToGroup:sampleToGroup ]
  }

  def cacheMatPlot(Map params)
  {
    def id = mongoDataService.insert("visualCache", null, params)
    return id.toString()
  }

  Map retrieveMatPlot(String plotId)
  {
    def id = new ObjectId(plotId)
    def result = mongoDataService.findOne("visualCache", [ _id:id ], null)
    return result
  }

  List savedMatPlots()
  {
    def plots = mongoDataService.find("visualCache", null, [ "_id", "name", "fileType" ], [ "name":1 ], -1)
    return plots
  }

}
