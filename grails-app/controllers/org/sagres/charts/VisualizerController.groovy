package org.sagres.charts

import org.springframework.web.multipart.commons.CommonsMultipartFile
import com.mongodb.util.JSON
import grails.converters.JSON
import org.sagres.importer.TextTableSeparator

class VisualizerController {

  def visualizerService

  def index = { }

  def list = {
    def plots = visualizerService.savedMatPlots()
    if (plots)
    {
      return [ plots:plots ]
    }
  }

  def matPlot = {
    Map data = [ rows:[:] ]
	
	if (!params.controller) { params.controller = controllerName}
	if (!params.action) { params.action = actionName}

    String defaultPlot = params.fileType in [ "fc", "probelvl" ] ? "group_difference_top_spot" : "individual_all_spot_none_none"

    if (params.plotId)
    {
		params.pvalue = 0.10
      def pgroups = visualizerService.retrieveMatPlot(params.plotId)?.design?.groups
	  def name = visualizerService.retrieveMatPlot(params.plotId)?.name ?: "MAT Plot"
      return [ params: params, plotId:params.plotId, groups:pgroups, defaultPlot:defaultPlot, fileType:params.fileType, title: name]
    }

    // Allow user to upload a difference file or a pos and neg file
    String name = params.chartname ?: "Temp ${new Date().toString()}"
    data.name = name
	
    String fileType = params.fileType ?: "difference"
    data.fileType = fileType

	long sampleSetId = params.sampleSetId ? params.long("sampleSetId") : 0l
	data.sampleSetId = sampleSetId
	
	String modVersionName = params.modVersionName ?: "IlluminaV3"
	data.modVersionName = modVersionName
	
    String delimiter = params.delimiter ?: "csv"
    TextTableSeparator delim = delimiter == "csv" ? TextTableSeparator.CSV : TextTableSeparator.TSV
    CommonsMultipartFile matFile1Tmp = params.matFile1
    CommonsMultipartFile matFile2Tmp = fileType == "posNeg" ? params.matFile2 : null
    File matFile1 = cacheTempFile(matFile1Tmp)
    File matFile2 = cacheTempFile(matFile2Tmp)

    // groups
    def groups = null
    CommonsMultipartFile designFileTmp = params.designFile
    if (!designFileTmp?.fileItem.name.isAllWhitespace())
    {
      File designFile = cacheTempFile(designFileTmp)
      Map design = visualizerService.parseMatDesignFile(designFile, delim)
      data.design = design
      groups = design.groups
      designFile?.delete()
    }
    // module generation for annotations
    if (params.modGen) {
      data.modGen = params.int("modGen")
    }

    Map mat1 = visualizerService.parseMatFiles(matFile1, fileType, true, delim)
    if (fileType in [ "difference", "probelvl", "fc" ])
    {
      data.rows[fileType] = mat1.rows
      data.header = mat1.header
    }
    else
    {
      data.rows.pos = mat1.rows
      data.header = mat1.header
    }
    if (matFile2) {
      Map mat2 = visualizerService.parseMatFiles(matFile2, fileType, true, delim)
      data.rows.neg = mat2.rows
    }

    matFile1?.delete()
    matFile2?.delete()

    String plotId = visualizerService.cacheMatPlot(data)
    return [ plotId:plotId, groups:groups, defaultPlot:defaultPlot, fileType:params.fileType, title: name, params:params]
  }

  private def File cacheTempFile(CommonsMultipartFile f)
  {
    if (f) {
      File tempVersionFile = new File(grailsApplication.config.fileUpload.baseDir.toString(), "temp/${f.originalFilename}".toString())
      if (!tempVersionFile.exists()) {
        tempVersionFile.mkdirs()
      }
      f.transferTo(tempVersionFile)
      return tempVersionFile
    }
  }

  def getMatPlot = {
    String plotId = params.plotId
    Map matPlot = visualizerService.retrieveMatPlot(plotId)
    double floor = params.floor != null ? params.double("floor") : 0d
	double minChange = params.minChange != null ? params.double("minChange") : 0d
	long sampleSetId = matPlot.sampleSetId ?: 0l
	String modVersionName = matPlot.modVersionName ?: "IlluminaV3"
	
    Map settings = [ rows:matPlot.rows, header:matPlot.header, design:matPlot.design, floor:floor, minChange:minChange, fileType:matPlot.fileType, sampleSetId:sampleSetId, modVersionName:modVersionName]

    String[] plot = params.plotName?.split("_")
    boolean isGroup = plot[0] == "group"
    if (isGroup)
    {
      settings.isPie = plot[3] == "piechart"
      settings.mts = params.mts != null ? params.boolean("mts") : true
      settings.top = plot[2] == "top"
    }
    else
    {
      settings.annotatedOnly = plot[1] == "annotated"
      settings.clusterCols = plot[3] == "samples"
      settings.clusterRows = plot[4] == "modules"
      settings.isPie = plot[2] == "piechart"
      settings.showRowSpots = params.showRowSpots != null ? params.boolean("showRowSpots") : true
    }

    Map model = isGroup ? visualizerService.groupModulePlot(settings) : visualizerService.matPlot(settings)
    if (model)
    {
      render model as JSON
    }
    else
    {
      render ""
    }
  }
}
