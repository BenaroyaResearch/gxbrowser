package org.sagres.sampleSet

import org.sagres.sampleSet.component.OverviewComponent
import org.sagres.sampleSet.component.SampleSetOverviewComponentDefaultSet
import org.sagres.sampleSet.component.SampleSetOverviewComponentDefault
import org.sagres.sampleSet.SampleSet
import org.sagres.sampleSet.component.SampleSetOverviewComponent

class SampleSetAdminController {

	def grailsApplication
	
	def index = {
		redirect(action: "showAnnotationComponents")
	}

  def showAnnotationComponents = {
    def overviewComponents = OverviewComponent.list()
    def defaultSampleSetComponentSet = SampleSetOverviewComponentDefaultSet.list()
    def defaultComponents = [:]
    defaultSampleSetComponentSet.each {
      defaultComponents.put(it.id, SampleSetOverviewComponentDefault.findAllByDefaultSetId(it.id))
    }

    return [overviewComponents: overviewComponents, defaultSets: defaultComponents]
  }

  def createOverviewComponent = {
    if (params.name && params.annotationName && params.password == "friend")
    {
      def componentType = params.componentType ?: "textarea"
      def tooltip = params.tooltip ?: params.name
      new OverviewComponent(name: params.name, tooltip: tooltip, componentType: componentType, annotationName: params.annotationName).save()
    }
    redirect(action: "showAnnotationComponents")
  }

  def addComponentToDefaultSet = {
    if (params.addPassword == "friend")
    {
      def defaultSet = SampleSetOverviewComponentDefaultSet.get(params.id)
      def component = OverviewComponent.get(params.component)
      if (SampleSetOverviewComponentDefault.countByDefaultSetIdAndComponentId(defaultSet.id, component.id) == 0)
      {
        def numDefaultComponents = SampleSetOverviewComponentDefault.countByDefaultSetId(defaultSet.id)
        new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: component.id, displayOrder: numDefaultComponents+1).save()
      }
    }
    redirect(action: "showAnnotationComponents")
  }

  def updateSampleSets = {
    if (params.updatePassword == "friend")
    {
      SampleSet.list().each {
        def sampleSetId = it.id
        def existingComponents = SampleSetOverviewComponent.findAllBySampleSetId(sampleSetId).collect { it.componentId }

        // copy over default components for this sample set
        def ssDefComponentSet = SampleSetOverviewComponentDefaultSet.findByName(grailsApplication.config.sampleSet.overviewComponentsSet.default.name)
        if (ssDefComponentSet) {
          def defComponents = SampleSetOverviewComponentDefault.findAllByDefaultSetId(ssDefComponentSet.id)
          defComponents.each {
            if (!existingComponents.contains(it.componentId))
            {
              new SampleSetOverviewComponent(sampleSetId: sampleSetId, componentId: it.componentId, displayOrder: it.displayOrder).save()
            }
          }
        }
      }
    }
    redirect(action: "showAnnotationComponents")
  }

}
