package org.sagres.sampleSet

class SampleSetUtilTagLib {

  def iconizePlatform = { attrs ->
    String platform = attrs.platform
    if (platform)
    {
      def pType = platform.split("\\s+")[0].toLowerCase()
      out << "<span title='${platform}' class='ui-icon-${pType}'></span>"
    }
  }

}
