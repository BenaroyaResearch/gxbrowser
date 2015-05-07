package org.sagres.sampleSet.annotation

import grails.converters.JSON
import org.sagres.sampleSet.component.LookupList

class SampleSetPlatformInfoController {

  def dataSource
  def platformService

  def getPlatformOptions = {
    def platformOptions = platformService.getPlatformOptions(params.platform)
    render platformOptions as JSON
  }
  
  def getLibraryPrepOptions = {
	  def libraryPrepOptions = platformService.getLibraryPrepOptions(params.platform)
	  render libraryPrepOptions as JSON
  }
  
  def setPlatform = {
    // set platform only if it exists in the Lookup List
    if (params.value && LookupList.findByName("Genomic Platform").lookupDetails.name.contains(params.value))
    {
      def platformInfo = SampleSetPlatformInfo.get(params.id)
      platformInfo.platform = params.value
      platformInfo.platformOption = null
      platformInfo.save()
    }
  }

  def setter = {
    def platformInfo = SampleSetPlatformInfo.get(params.id)
    if (platformInfo && params.property)
    {
      platformInfo.setProperty(params.property, params.value)
    }
    render params.value
  }
}
