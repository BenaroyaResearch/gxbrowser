package org.sagres.sampleSet

import org.sagres.sampleSet.annotation.*
import common.ClinicalDataSource
import common.chipInfo.RawSignalDataType
import common.SecRole
import org.sagres.rankList.RankList
import org.sagres.sampleSet.SampleSetPalx
import common.chipInfo.ChipsLoaded
import common.chipInfo.ChipType
import common.chipInfo.GenomicDataSource


class SampleSet
{

  static hasMany = [sampleSetFiles: SampleSetFile, groupSets:DatasetGroupSet, visibleSecRoles:SecRole, links:SampleSetLinkDetail]
  static hasOne = [sampleSetAnnotation: SampleSetAnnotation, sampleSetSampleInfo: SampleSetSampleInfo,
                   sampleSetPlatformInfo: SampleSetPlatformInfo, sampleSetAdminInfo: SampleSetAdminInfo]

  String name
  String description
  String status
  String studyType
  double fractionComplete = 0.0
  int gxbPublished =System.getenv('publish_default')? Integer.parseInt(System.getenv('publish_default')):0
  int approved = 0
  Integer markedForDelete
  RawSignalDataType rawSignalType
  RawSignalDataType defaultSignalDisplayType
  DatasetGroupSet defaultGroupSet
  ClinicalDataSource clinicalDataSource
  String clinicalDatasourceDataUrl
  RankList defaultRankList
  SampleSetPalx defaultPalx
  SampleSet parentSampleSet
  ChipsLoaded chipsLoaded
  ChipType chipType
  GenomicDataSource genomicDataSource
  FocusedArrayFoldChangeParams defaultFoldChangeParams
  Integer noSamples
  String itnCohortsIdColumn

  static constraints = {
    name(blank: false, unique: true)
    description(blank: false)
    sampleSetAnnotation(nullable: true)
    sampleSetSampleInfo(nullable: true)
    sampleSetPlatformInfo(nullable: true)
    sampleSetAdminInfo(nullable: true)
	fractionComplete(nullable: false)
	approved(nullable: false)
    markedForDelete(nullable: true)
    status(nullable: false)
    studyType(nullable: false)
    rawSignalType(nullable: false)
    defaultSignalDisplayType(nullable: true)
    defaultGroupSet(nullable: true)
    clinicalDataSource(nullable: true)
    clinicalDatasourceDataUrl(nullable: true, blank: true)
    defaultRankList(nullable: true)
    defaultPalx( nullable: true )
    parentSampleSet(nullable: true)
    chipsLoaded(nullable: true)
    chipType(nullable: true)
    genomicDataSource(nullable: true)
	defaultFoldChangeParams(nullable: true)
    noSamples(nullable: true)
    itnCohortsIdColumn(nullable: true, blank: false)
  }

  static mapping = {
    description type:'text'
    sampleSetFiles sort:'filename', order:'asc'
  }

  String toString() {
    return name
  }

}
