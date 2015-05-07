package org.sagres.sampleSet

import org.sagres.sampleSet.component.LookupList
import groovy.sql.Sql

class PlatformService {

  def dataSource

  def List getPlatformOptions(String platform)
  {
    def platformOptions = []
    if (platform)
    {
      if (platform == "Microarray")
      {
        Sql sql = Sql.newInstance(dataSource)
        def dataQuery = "SELECT manufacturer, model, chip_version " +
          "FROM chip_data"
        sql.rows(dataQuery).each { chip ->
          if (chip.model)
          {
            def platformOption = "${chip.manufacturer} ${chip.model}"
            platformOption += chip.chip_version ? " v${chip.chip_version}" : ""
            platformOptions.push(platformOption.trim())
          }
        }
        sql.close()
      }
      else
      {
        platformOptions = LookupList.findByName(platform)?.lookupDetails?.collect { it.name }
      }
    }
    return platformOptions
  }

  def List getLibraryPrepOptions(String platform)
  {
	def libraryPrepOptions = []
	if (platform == "RNAseq" || platform == "RNA-seq")
	{
		libraryPrepOptions = LookupList.findByName('Library Preparation').lookupDetails?.collect { it.name }
	}
	return libraryPrepOptions
  }
}