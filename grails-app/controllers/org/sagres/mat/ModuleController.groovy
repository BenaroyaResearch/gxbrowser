package org.sagres.mat

import common.chipInfo.ChipType
import groovy.sql.Sql

class ModuleController {

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def dataSource

  def index = {
    redirect(controller:"moduleGeneration", action:"list")
  }

  def show = {
    Module module = Module.findById(params.id)
    if (module)
    {
      ModuleGeneration generation = ModuleGeneration.findById(module.moduleGenerationId)
      ChipType ct = ChipType.findById(generation.chipTypeId)
      ModuleAnnotation annotation = ModuleAnnotation.findById(module.moduleAnnotationId)
      List<ModuleDetail> probes = ModuleDetail.findAllByModuleId(module.id)
      return [ generation:generation, chip:ct, module:module, annotation:annotation, probes:probes ]
    }
    else
    {
      flash.message = "Module not found"
      redirect(controller:"moduleGeneration", action:"list")
    }
  }

  def editAnnotation = {
    def module = Module.get(params.id)
    if (module)
    {
      ModuleAnnotation a = ModuleAnnotation.findById(module.moduleAnnotationId)
      return [ module:module, annotation:a ]
    }
  }

  def updateModuleAnnotation = {
    def module = Module.get(params.id)
    if (module)
    {
      ModuleAnnotation a = ModuleAnnotation.findById(module.moduleAnnotationId)
      if (a)
      {
        a.annotation = params.name?.trim()
        a.abbreviation = params.abbrev?.trim()
        a.hexColor = params.color?.trim()
        a.save()
      }
    }
    redirect(action:"editAnnotation", id:module.id)
  }

  def setAnnotation = {
    def module = Module.get(params.id)
    if (module)
    {
      ModuleAnnotation annotation = ModuleAnnotation.findById(module.moduleAnnotationId)
      int generation = ModuleGeneration.findById(module.moduleGenerationId).generation
      if (params.annotation)
      {
        ModuleAnnotation templateAnnotation = ModuleAnnotation.findByGenerationAndAnnotation(generation, params.annotation.trim())
        if (templateAnnotation)
        {
          // use this as a template
          if (annotation?.annotation != templateAnnotation.annotation) {
            if (annotation) {
              annotation.delete()
            }
            ModuleAnnotation newAnnotation = new ModuleAnnotation(templateAnnotation.properties)
            newAnnotation.save(flush:true)
            String updateQ = """UPDATE module, module_generation SET
            module.module_annotation_id=${newAnnotation.id}
            WHERE module.module_generation_id=module_generation.id
            AND module_generation.generation=${generation}
            AND module.module_name='${module.moduleName}'""".toString()
            Sql sql = Sql.newInstance(dataSource)
            sql.executeUpdate(updateQ)
            sql.close()
            redirect(controller:"moduleGeneration", action:"show", id:module.moduleGenerationId)
          }
        }
      }
      List<String> annotations = ModuleAnnotation.findAllByGeneration(generation)?.collect { it.annotation }.unique().sort()
      return [ module:module, moduleAnnotation:annotation?.annotation, annotations:annotations ]
    }
  }

  def deleteModuleAnnotation = {
    def module = Module.get(params.id)
    if (module)
    {
      ModuleAnnotation a = ModuleAnnotation.findById(module.moduleAnnotationId)
      if (a)
      {
        // remove reference in modules too
        String updateQ = """UPDATE module SET
          module_annotation_id = 0
          WHERE module_annotation_id=${a.id}""".toString()
        Sql sql = Sql.newInstance(dataSource)
        sql.executeUpdate(updateQ)
        sql.close()
        a.delete()
      }
    }
    redirect(action:"show", id:module.id)
  }

}
