package org.sagres.sampleSet.annotation

import java.text.SimpleDateFormat

class SampleSetAdminInfoController {

  def setter = {
    def value = params.value
    if (params.property == 'samplesSent' || params.property == 'resultsNeeded')
    {
      value = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(value)
    }
    def sampleSetAdminInfoInstance = SampleSetAdminInfo.get(params.id)
    sampleSetAdminInfoInstance.setProperty(params.property, value)
    sampleSetAdminInfoInstance.save()
  }

}
