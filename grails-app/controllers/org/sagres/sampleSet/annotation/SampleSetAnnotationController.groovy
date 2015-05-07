package org.sagres.sampleSet.annotation

class SampleSetAnnotationController {

  static scaffold = SampleSetAnnotation

  def converterService

  def setter = {
    if (params.value == "") {
      render "<span class='hint'>${grailsApplication.config.hint.default.text}</span>"
    }
    else {
      def value = params.value
      if (params.clean)
      {
        value = converterService.docToHTML(params.value)
      }
      def sampleSetAnnotationInstance = SampleSetAnnotation.get(params.id)
      sampleSetAnnotationInstance.setProperty(params.property, value)
      sampleSetAnnotationInstance.save()
      if (params.paragraph) {
        value = value.encodeAsParagraph()
      }
      render value
    }
  }

  def getter = {
    def sampleSetAnnotation = SampleSetAnnotation.get(params.id)
    def value = sampleSetAnnotation.getPersistentValue(params.property)
    if (!value) {
      render ''
    }
    else {
      render value
    }
  }

}
