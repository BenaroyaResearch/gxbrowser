package org.sagres.mat

import groovy.sql.Sql
import common.chipInfo.ChipType

import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.sagres.importer.TextTable
import groovy.sql.GroovyRowResult
import grails.converters.JSON

class ModuleGenerationController {

  static allowedMethods = [save: "POST", update: "POST"]

  def dataSource //injected

  def index = {
    redirect(action: "list", params: params)
  }

  def upload = {
    String q = "SELECT DISTINCT generation FROM module_generation ORDER BY generation ASC"
    Sql sql = Sql.newInstance(dataSource)
    List<Integer> generations = []
    sql.eachRow(q) { generations.push(it.generation) }
    return [ generations:generations ]
  }

  def list = {
    String q = """SELECT mg.id 'id', mg.version_name 'name', mg.generation 'gen',
      ct.name 'chiptype', COUNT(DISTINCT m.id) 'modules', COUNT(DISTINCT md.id) 'probes'
      FROM module_generation mg
      JOIN chip_type ct ON ct.id = mg.chip_type_id
      JOIN module m ON m.module_generation_id = mg.id
      JOIN module_detail md ON md.module_id = m.id
      GROUP BY mg.id """.toString()
    Sql sql = Sql.newInstance(dataSource)
    List<GroovyRowResult> result = sql.rows(q)
    return [ moduleGenerations:result ]
  }

  def create = {
    def moduleGenerationInstance = new ModuleGeneration()
    moduleGenerationInstance.properties = params

    def chipTypes = getChipTypes()

    return [moduleGenerationInstance:moduleGenerationInstance, chipTypes:chipTypes]
  }

  def cancel = {
    redirect(action:"list")
  }

  def save = {
    CommonsMultipartFile versionFile = params.versionFile
    CommonsMultipartFile functionFile = params.functionFile

    def vFile = cacheTempFile(versionFile)
    def fFile = cacheTempFile(functionFile)

    def moduleGenerationInstance = new ModuleGeneration(params)
    moduleGenerationInstance.save(flush:true)

    if (versionFile) {
      // import modules & probeIds & annotations
      boolean success = importModules(moduleGenerationInstance, moduleGenerationInstance.chipTypeId, vFile, fFile)
      if (success) {
        // only proceed with saving module generation if successful import occurs
        flash.message = "New module generation ${moduleGenerationInstance.version} was successfully created"
        redirect(action: "list")
      }
    }
    else
    {
      render(view: "create", model: [moduleGenerationInstance: moduleGenerationInstance])
    }
  }

  private def File cacheTempFile(CommonsMultipartFile f)
  {
    if (f.originalFilename) {
      File tempVersionFile = new File(grailsApplication.config.mat.workDir, "temp/${f.originalFilename}")
      if (!tempVersionFile.exists()) {
        tempVersionFile.mkdirs()
      }
	  f.transferTo(tempVersionFile)
      return tempVersionFile
    }
  }

  private def importModules(ModuleGeneration gen, long chipTypeId, File moduleFile, File functionFile) {
    def chipType = ChipType.get(chipTypeId)
    def probeTable = chipType.probeListTable
    def probeColumn = chipType.probeListColumn
    def symbolColumn = chipType.symbolColumn

    // retrieve annotations if they already exist
    int generation = gen.generation
    Map<String,ModuleAnnotation> gAnnotations = [:]
    ModuleAnnotation.findAllByGeneration(generation)?.each {
      gAnnotations.put(it.moduleName, it)
    }

    functionFile?.eachLine { text, line ->
      List<String> values = TextTable.splitCsvRow(text)
      if (line > 1) {
        def theModule = values[0].trim()
        def annotation = values[1].trim()
        if (!gAnnotations.containsKey(theModule))
        {
          def a = new ModuleAnnotation(generation:generation, moduleName:theModule, annotation:annotation, hexColor:"#FF0000")
          a.save()
          gAnnotations.put(theModule, a)
        }
      }
    }
    functionFile?.delete()

    // add modules
    def probesToSymbols = [:]
    Sql sql = Sql.newInstance(dataSource)
    def query = "SELECT ${probeColumn} 'probe', ${symbolColumn} 'symbol' FROM ${probeTable} WHERE ${symbolColumn} IS NOT NULL AND ${symbolColumn} != '---' AND ${symbolColumn} != 'null'".toString()
    sql.eachRow(query) { row ->
      if (!row.symbol.isAllWhitespace()) {
        probesToSymbols.put(row.probe?.trim().toUpperCase(), row.symbol?.trim())
      }
    }
    sql.close()

    def modulesFound = [:]
    Map<String,List<String>> moduleToProbes = [:]
    int mIdx = 0, pIdx = 1
    moduleFile.eachLine { text, line ->
      List<String> values = TextTable.splitCsvRow(text)
      if (line == 1) {
        if (values[1].equalsIgnoreCase("module")) {
          mIdx = 1
          pIdx = 0
        }
      } else {
        def module = values[mIdx]
        if (!modulesFound.containsKey(module)) {
          // add to module table
          def m = new Module(moduleGenerationId:gen.id, moduleName:module)
          if (gAnnotations.containsKey(module))
          {
            m.moduleAnnotationId = gAnnotations.get(module).id
          }
          m.save(flush:true)
          modulesFound.put(module, m.id)
          moduleToProbes.put(module, [])
        }
        long mId = modulesFound.get(module)
        String probeId = values[pIdx].stripIndent().trim().toUpperCase()
        if (probeId.startsWith("ILMN_")) {
          probeId = probeId.substring(5)
        }
        def d = new ModuleDetail(moduleId:mId, probeId:probeId)
        if (probesToSymbols.containsKey(probeId))
        {
          d.geneSymbol = probesToSymbols.get(probeId)
        }
        d.save()
        moduleToProbes.get(module).push(probeId)
      }
    }
    moduleToProbes.each { String module, List<String> probes ->
      int count = probes.unique().size()
      def m = Module.findById(modulesFound.get(module))
	  if ( m )
	  {
		  m.probeCount = count
		  m.save()
	  }
    }
    moduleFile.delete()

    return true
  }

  def getChipTypes() {
		def chipTypes = []
		Sql db = Sql.newInstance(dataSource)
		String sqlS = "select ct.id, cd.manufacturer, cd.model, cd.name " +
			"from chip_type ct, chip_data cd " +
			"where ct.probe_list_table != '' and ct.chip_data_id = cd.id and model is not null and ct.active >= 0 " +
      "order by cd.manufacturer, cd.model"
		db.eachRow(sqlS) {
      def name = "${it.manufacturer} ${it.model} ${it.name}"
      chipTypes.push([id:it.id, name:name])
		}
		db.close()
		return chipTypes
	}

  def show = {
    if (ModuleGeneration.exists(params.id))
    {
      ModuleGeneration moduleGeneration = ModuleGeneration.findById(params.id)
      ChipType ct = ChipType.findById(moduleGeneration.chipTypeId)

      String q = """SELECT m.id 'id', m.module_name 'name', COUNT(md.id) 'probes',
        ma.id 'annotation', ma.annotation 'annotation_name', ma.hex_color 'color', ma.abbreviation 'abbrev'
        FROM module m
        JOIN module_detail md ON md.module_id = m.id
        LEFT JOIN module_annotation ma ON ma.id = m.module_annotation_id
        WHERE m.module_generation_id = ${moduleGeneration.id}
        GROUP BY m.id""".toString()
      Sql sql = Sql.newInstance(dataSource)
      List<GroovyRowResult> result = sql.rows(q)
      return [ moduleGeneration:moduleGeneration, chipType:ct, modules:result ]
    }
    else
    {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'moduleGeneration.label', default: 'ModuleGeneration'), params.id])}"
      redirect(action: "list")
    }
  }

  def edit = {
    def moduleGeneration = ModuleGeneration.get(params.id)
    if (!moduleGeneration) {
      flash.message = "Module generation was not found"
      redirect(action: "list")
    }
    else {
      return [ moduleGeneration:moduleGeneration ]
    }
  }

  def update = {
    def moduleGeneration = ModuleGeneration.get(params.id)
    if (moduleGeneration) {
      moduleGeneration.versionName = params.versionName
      moduleGeneration.generation = params.int("generation")
      moduleGeneration.save(flush:true)
    }
    else
    {
      flash.message = "Unable to find this module generation"
    }
    redirect(action: "list")
  }

  def delete = {
    def moduleGeneration = ModuleGeneration.get(params.id)
    if (moduleGeneration) {
      try {
        // delete modules and probes
        String deleteModulesQ = """DELETE m, md FROM module m, module_detail md
          WHERE m.id = md.module_id AND m.module_generation_id = ${moduleGeneration.id}""".toString()
        // delete module annotations
        Sql sql = Sql.newInstance(dataSource)
        sql.execute(deleteModulesQ)
        sql.close()
        // finally delete the module generation
        moduleGeneration.delete(flush:true)
        flash.message = "The module generation <b>${moduleGeneration.versionName}</b> has been deleted"
      }
      catch (Exception e)
      {
        flash.message = "There was a problem deleting the module generation, <b>${moduleGeneration.versionName}</b>."
      }
    }
    redirect(action: "list")
  }

  def updateAnnotations = {
    int generation = params.int("generation")
    Map<String,Map> annotations = [:]

    CommonsMultipartFile annotationFile = params.annotationFile
    def aFile = cacheTempFile(annotationFile)
    aFile.eachLine { text, line ->
      List<String> values = TextTable.splitCsvRow(text)
      String abbrev = values.size() == 4 ? values[3].trim() : null
      // this file should contain: module,annotation,color,abbreviation(optional)
      annotations.put(values[0].trim(),[ generation:generation, moduleName:values[0].trim(),
          annotation:values[1].trim(), hexColor:values[2].trim() ?: "#FF0000", abbreviation:abbrev ])
    }
    aFile.delete()

    annotations.each { String module, Map info ->
      ModuleAnnotation annot = ModuleAnnotation.findByGenerationAndModuleName(generation, module)
      if (annot)
      {
        annot.annotation = info.annotation
        annot.hexColor = info.hexColor
        annot.abbreviation = info.abbreviation
        annot.save()
      }
      else
      {
        ModuleAnnotation newAnnot = new ModuleAnnotation(info)
        newAnnot.save(flush:true)

        // update each matching module with module annotation ID
        String q = """SELECT mg.id 'gId', m.id 'mId' FROM module_generation mg
            JOIN module m ON m.module_generation_id = mg.id
            WHERE mg.generation = ${generation}
            AND m.module_name='${module}'""".toString()
        Sql sql = Sql.newInstance(dataSource)
        sql.eachRow(q) {
          Module mod = Module.findById(it.mId)
          mod.moduleAnnotationId = newAnnot.id
          mod.save(flush:true)
        }
      }
    }
    redirect(action:"upload")
  }

  def getModulesForGeneration = {
    int generation = params.int("id")
    ModuleGeneration mg = ModuleGeneration.findByGeneration(generation)
    def modules = []
    if (mg)
    {
      List<Module> genModules = Module.findAllByModuleGenerationId(mg.id)
      modules = genModules.collect { it.moduleName }
    }
    def result = [ modules:modules ]
    render result as JSON
  }
}
