package org.sagres.mat

import groovy.sql.Sql

class ModuleAnnotationController {

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def dataSource

  def index = {
    redirect(action: "list", params: params)
  }

  def list = {
    String q = "SELECT DISTINCT generation FROM module_generation ORDER BY generation ASC"
    Sql sql = Sql.newInstance(dataSource)
    List<Integer> generations = []
    sql.eachRow(q) { generations.push(it.generation) }

    List<ModuleAnnotation> annotations = null
    if (generations.size() > 0)
    {
      println params.generation
      int selectedGeneration = params.generation != null ? params.int("generation") : generations[0]
      annotations = ModuleAnnotation.findAllByGeneration(selectedGeneration).sort { it.moduleName }
    }
    return [ annotations:annotations, generations:generations ]
  }

  def create = {
    String q = "SELECT DISTINCT generation FROM module_generation ORDER BY generation ASC"
    Sql sql = Sql.newInstance(dataSource)
    List<Integer> generations = []
    sql.eachRow(q) { generations.push(it.generation) }
    def modules = []
    if (generations.size() > 0)
    {
      ModuleGeneration mg = ModuleGeneration.findByGeneration(generations[0])
      if (mg)
      {
        List<Module> genModules = Module.findAllByModuleGenerationId(mg.id)
        modules = genModules.collect { it.moduleName }
      }
    }
    return [ generations:generations, modules:modules, moduleName:params.moduleName ]
  }

  def save = {
    int generation = params.int("generation")
    String moduleName = params.module
    String annotation = params.name

    if (generation && moduleName && annotation)
    {
      try
      {
        ModuleAnnotation moduleAnnotation = ModuleAnnotation.findByGenerationAndModuleName(generation, moduleName)
        if (moduleAnnotation)
        {
          flash.message = "An annotation for module, ${moduleName}, already exists!"
        }
        else
        {
          ModuleAnnotation a = new ModuleAnnotation(generation:generation, moduleName:moduleName, annotation:annotation,
              abbreviation:params.abbrev, hexColor:params.color)
          a.save(flush:true)

          // update each matching module with module annotation ID
          String updateQ = """UPDATE module, module_generation SET
            module.module_annotation_id=${a.id}
            WHERE module.module_generation_id = module_generation.id
            AND module_generation.generation = ${generation}
            AND module.module_name='${moduleName}'""".toString()
          Sql sql = Sql.newInstance(dataSource)
          sql.executeUpdate(updateQ)
          sql.close()
        }
      }
      catch (Exception e)
      {
        flash.message = "There was an error creating the annotation"
      }
    }
    redirect(action:"create")
  }

  def show = {
    def moduleAnnotationInstance = ModuleAnnotation.get(params.id)
    List<Module> modules = Module.findAllByModuleAnnotationId(moduleAnnotationInstance.id)
    if (!moduleAnnotationInstance) {
      flash.message = "The annotation was not found"
      redirect(action: "list")
    }
    else {
      [ moduleAnnotationInstance:moduleAnnotationInstance, modules:modules ]
    }
  }

  def edit = {
    def moduleAnnotationInstance = ModuleAnnotation.get(params.id)
    if (!moduleAnnotationInstance) {
      flash.message = "The annotation was not found"
      redirect(action: "list")
    }
    else {
      return [ moduleAnnotationInstance:moduleAnnotationInstance ]
    }
  }

  def update = {
    def annotation = ModuleAnnotation.get(params.id)
    if (annotation)
    {
      annotation.annotation = params.name?.trim()
      annotation.hexColor = params.color?.trim()
      annotation.abbreviation = params.abbrev?.trim()
      annotation.save()

      if (params.applyToAll) {
        String updateQ = """UPDATE module_annotation SET
          hex_color='${annotation.hexColor}', abbreviation='${annotation.abbreviation}'
          WHERE annotation='${annotation.annotation}'
          AND generation=${annotation.generation}""".toString()
        Sql sql = Sql.newInstance(dataSource)
        sql.executeUpdate(updateQ)
        sql.close()
      }
    }
    redirect(action:"edit", id:annotation.id)
  }

  def delete = {
    def moduleAnnotationInstance = ModuleAnnotation.findById(params.id)
    if (moduleAnnotationInstance) {
      try {
        // remove reference in modules too
        String updateQ = """UPDATE module SET
          module_annotation_id = 0
          WHERE module_annotation_id=${moduleAnnotationInstance.id}""".toString()
        Sql sql = Sql.newInstance(dataSource)
        sql.executeUpdate(updateQ)
        sql.close()
        moduleAnnotationInstance.delete(flush: true)
        flash.message = "The annotation was successfully deleted"
        redirect(action: "list")
      }
      catch (org.springframework.dao.DataIntegrityViolationException e) {
        flash.message = "There was a problem deleting the annotation"
        redirect(action: "show", id: params.id)
      }
    }
    else {
      flash.message = "The annotation was not found"
      redirect(action: "list")
    }
  }

}
