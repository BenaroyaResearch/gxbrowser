import org.sagres.sampleSet.component.FileTag
import org.sagres.sampleSet.component.LookupListDetail
import org.sagres.sampleSet.component.LookupList
import org.sagres.sampleSet.component.SampleSetOverviewComponentDefaultSet
import org.sagres.sampleSet.component.SampleSetOverviewComponentDefault
import org.sagres.sampleSet.component.OverviewComponent
import org.sagres.sampleSet.component.LookupListType
import org.sagres.sampleSet.annotation.SampleSetFile
import org.sagres.project.Deliverable
import org.sagres.mat.MATWizardFileFormat
import common.chipInfo.RawSignalDataType
import org.sagres.sampleSet.SampleSet
import org.sagres.geneList.GeneListCategory
import common.chipInfo.ChipsLoaded
import common.ClinicalDataSource
import common.SecRole
import common.chipInfo.GenomicDataSource
import groovy.sql.Sql
import groovy.sql.GroovyRowResult
import common.chipInfo.Species
import common.SecUser
import common.SecUserSecRole
//import common.CustomTimeoutSessionListener

import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.sagres.util.mongo.MongoConnector

class BootStrap {

  def dataSource
  def mailService
  def grailsApplication
  
  boolean useLabkeyAuthentication = System.getenv( "DM3_AUTHENTICATE_VIA_LABKEY" ) ? true:false


  def init = { servletContext ->

//	  mailService.sendMail {
//	  	from "SPresnell@benaroyaresearch.org"
//		  to "SPresnell@benaroyaresearch.org"
//     subject "DM3 startup"
//	    body "DM3 has started up"
//	  }
	  
//	  	servletContext.addListener(CustomTimeoutSessionListener)
		for (sc in grailsApplication.serviceClasses) {
			//println "Adding getGrailsApplication Method to ${sc.fullName}"
			//sc.clazz.metaClass.getGrailsApplication = { -> grailsApplication   }
			//sc.clazz.metaClass.static.getGrailsApplication = { ->  grailsApplication}
		}
		if (MATWizardFileFormat.count() == 0) {
			//inList: ["Affy Zip", "Illumina Background Subtracted", "GEO .soft format", "text (.csv or .tsv)"]
			new MATWizardFileFormat(fileFormat: "affy_zip", displayName: "Affy Zip").save()
			new MATWizardFileFormat(fileFormat: "illumina_background_subtracted", displayName: "Illumina Background Subtracted").save()
			new MATWizardFileFormat(fileFormat: "geo", displayName: "GEO .soft format").save()
			new MATWizardFileFormat(fileFormat: "text", displayName: "text (.csv or .tsv)").save()
		}

	//	if(useLabkeyAuthentication) {
		// println "Regstering Filter"
	//		SpringSecurityUtils.clientRegisterFilter('labkeyAuthenticationFilter', SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 10)
	//	}



    if (OverviewComponent.count() == 0)
    {
      // Components
      OverviewComponent purpose = new OverviewComponent(name: "Purpose", tooltip: "Purpose", componentType: "textarea", annotationName: "purpose")
      purpose.save()
      OverviewComponent hypothesis = new OverviewComponent(name: "Hypothesis", tooltip: "Hypothesis", componentType: "textarea", annotationName: "hypothesis")
      hypothesis.save()
      OverviewComponent controls = new OverviewComponent(name: "Controls", tooltip: "Controls", componentType: "textarea", annotationName: "controls")
      controls.save()
      OverviewComponent methods = new OverviewComponent(name: "Methods", tooltip: "Methods", componentType: "textarea", annotationName: "methods")
      methods.save()
      OverviewComponent samplingMethodInclusion = new OverviewComponent(name: "Sampling method (inclusion criteria)", tooltip: "Sampling method (inclusion criteria)", componentType: "textarea", annotationName: "samplingMethodInclusionCriteria")
      samplingMethodInclusion.save()
      OverviewComponent samplingMethodExclusion = new OverviewComponent(name: "Sampling method (exclusion criteria)", tooltip: "Sampling method (exclusion criteria)", componentType: "textarea", annotationName: "samplingMethodExclusionCriteria")
      samplingMethodExclusion.save()
      OverviewComponent controlGroups = new OverviewComponent(name: "Control Groups", tooltip: "Control Groups", componentType: "textarea", annotationName: "controlGroups")
      controlGroups.save()
      OverviewComponent methodMatching = new OverviewComponent(name: "Method of matching", tooltip: "Method of matching", componentType: "textarea", annotationName: "methodOfMatching")
      methodMatching.save()
      OverviewComponent experimentalDesign = new OverviewComponent(name: "Experimental Design", tooltip: "Experimental Design", componentType: "textarea", annotationName: "experimentalDesign")
      experimentalDesign.save()
      OverviewComponent experimentalDesignLongitudinal = new OverviewComponent(name: "Sample Set Longitudinal", tooltip: "Longitudinal", componentType: "checkbox", annotationName: "experimentalDesignIsLongitudinal")
      experimentalDesignLongitudinal.save()
      OverviewComponent possibleSourcesVariation = new OverviewComponent(name: "Possible sources of variation", tooltip: "Possible sources of variation", componentType: "textarea", annotationName: "possibleSourcesOfVariation")
      possibleSourcesVariation.save()
      OverviewComponent experimentalVariables = new OverviewComponent(name: "Experimental Variables", tooltip: "Experimental variables", componentType: "textarea", annotationName: "experimentalVariables")
      experimentalVariables.save()
      OverviewComponent additionalInfo = new OverviewComponent(name: "Additional Information", tooltip: "Additional information", componentType: "textarea", annotationName: "additionalInfo")
      additionalInfo.save()

      if (SampleSetOverviewComponentDefaultSet.count() == 0)
      {
        // Create default sets
        SampleSetOverviewComponentDefaultSet defaultSet = new SampleSetOverviewComponentDefaultSet(name: "Default", description: "Default set")
        defaultSet.save()
        SampleSetOverviewComponentDefault defaultPurpose = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: purpose.id, displayOrder: 1)
        defaultPurpose.save()
        SampleSetOverviewComponentDefault defaultHypothesis = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: hypothesis.id, displayOrder: 2)
        defaultHypothesis.save()
        SampleSetOverviewComponentDefault defaultExperimentalDesign = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: experimentalDesign.id, displayOrder: 3)
        defaultExperimentalDesign.save()
        SampleSetOverviewComponentDefault defaultExperimentalVariables = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: experimentalVariables.id, displayOrder: 4)
        defaultExperimentalVariables.save()
        SampleSetOverviewComponentDefault defaultControls = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: controls.id, displayOrder: 5)
        defaultControls.save()
        SampleSetOverviewComponentDefault defaultMethods = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: methods.id, displayOrder: 6)
        defaultMethods.save()
        SampleSetOverviewComponentDefault defaultAdditionalInfo = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: additionalInfo.id, displayOrder: 7)
        defaultAdditionalInfo.save()
//        SampleSetOverviewComponentDefault defaultSamplingInclusion = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: samplingMethodInclusion.id, displayOrder: 4)
        //        defaultSamplingInclusion.save()
        //        SampleSetOverviewComponentDefault defaultSamplingExclusion = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: samplingMethodExclusion.id, displayOrder: 5)
        //        defaultSamplingExclusion.save()
        //        SampleSetOverviewComponentDefault defaultLongitudinal = new SampleSetOverviewComponentDefault(defaultSetId: defaultSet.id, componentId: experimentalDesignLongitudinal.id, displayOrder: 7)
        //        defaultLongitudinal.save()
      }
    }

	if (GeneListCategory.count() == 0) {
		GeneListCategory pathways = new GeneListCategory(name: "Pathways", description: "Molecular Pathways")
		pathways.save()
		GeneListCategory diseases = new GeneListCategory(name: "Diseases", description: "Clinical Diseases")
		diseases.save()
		GeneListCategory modules = new GeneListCategory(name: "Modules", description: "From the Chaussabel Lab" )
		modules.save()
		GeneListCategory userdef = new GeneListCategory(name: "User Defined", description: "User Defined")
		userdef.save()
		GeneListCategory other = new GeneListCategory(name: "Other", description: "Other Gene Lists")
		other.save()
	}
	
	// TODO: LookupListType doesn't seem to be used anymore?  Scott Presnell May 30, 2013
    if (LookupListType.count() == 0)
    {
      LookupListType sampleInfoType = new LookupListType(name: "Sample Info")
      sampleInfoType.save()
      LookupListType platformInfoType = new LookupListType(name: "Platform Info")
      platformInfoType.save()
    }

    if (LookupList.count() == 0)
    {
	  // TODO: Species in LookupListDetail doesn't seem to be used anymore?  Scott Presnell May 30, 2013
      LookupListDetail hs = new LookupListDetail(name: "Homo sapiens")
      hs.save()
      LookupListDetail mm = new LookupListDetail(name: "Mus musculus")
      mm.save()

      LookupListDetail wholeBlood = new LookupListDetail(name: "Whole Blood")
      wholeBlood.save()
      LookupListDetail pbmc = new LookupListDetail(name: "PBMC")
      pbmc.save()
      LookupListDetail cultured = new LookupListDetail(name: "Cultured/Sorted Cells")
      cultured.save()

	  // TODO: Species in LookupList doesn't seem to be used anymore?  Scott Presnell May 30, 2013
      LookupList species = new LookupList(name: "Species", description: "Species", type: "Sample Info")
      species.addToLookupDetails(hs)
      species.addToLookupDetails(mm)
      species.save()

      LookupList sampleSource = new LookupList(name: "Sample Source", description: "Sample Source", type: "Sample Info")
      sampleSource.addToLookupDetails(wholeBlood);
      sampleSource.addToLookupDetails(pbmc)
      sampleSource.addToLookupDetails(cultured)
      sampleSource.save()

      LookupListDetail wbtempus = new LookupListDetail(name: "Whole Blood in Tempus Tubes")
      wbtempus.save()
      LookupList sampleType = new LookupList(name: "Sample Type Submitted", description: "Sample Type Submitted", type: "Sample Info")
      sampleType.addToLookupDetails(wbtempus)
      sampleType.save()

      LookupListDetail rlt = new LookupListDetail(name: "RLT")
      rlt.save()
      LookupList sampleStorageSolution = new LookupList(name: "Sample Storage Solution", description: "Sample Storage Solution", type: "Sample Info")
      sampleStorageSolution.addToLookupDetails(rlt)
      sampleStorageSolution.save()

      LookupList microarray = new LookupList(name: "Microarray", type: "Platform Info")
      microarray.addToLookupDetails(new LookupListDetail(name: "HT-12 v4"))
      microarray.addToLookupDetails(new LookupListDetail(name: "WG6 v2"))
      microarray.save()

      LookupList genomicPlatform = new LookupList(name: "Genomic Platform", type: "Platform Info")
      genomicPlatform.addToLookupDetails(new LookupListDetail(name: "Microarray"))
      genomicPlatform.addToLookupDetails(new LookupListDetail(name: "RNA-seq"))
      genomicPlatform.save()

      LookupList rnaseq = new LookupList(name: "RNA-seq", type: "Platform Info")
      rnaseq.addToLookupDetails(new LookupListDetail(name: "Multiplex 1 sample per lane"))
      rnaseq.addToLookupDetails(new LookupListDetail(name: "Multiplex 2 samples per lane"))
      rnaseq.addToLookupDetails(new LookupListDetail(name: "Multiplex 3 samples per lane"))
      rnaseq.addToLookupDetails(new LookupListDetail(name: "Multiplex 4 samples per lane"))
      rnaseq.addToLookupDetails(new LookupListDetail(name: "Multiplex 5 samples per lane"))
      rnaseq.addToLookupDetails(new LookupListDetail(name: "Multiplex more than 5 samples per lane"))
      rnaseq.save()

      LookupList studyType = new LookupList(name: "Study Type")
      studyType.addToLookupDetails(new LookupListDetail(name: "Clinical"))
      studyType.addToLookupDetails(new LookupListDetail(name: "Experimental"))
      studyType.save()

      LookupList status = new LookupList(name: "Status")
      status.addToLookupDetails(new LookupListDetail(name: "Data Generation In Progress"))
      status.addToLookupDetails(new LookupListDetail(name: "Analysis In Progress"))
      status.addToLookupDetails(new LookupListDetail(name: "Complete"))
      status.save()
    }

    if (!LookupList.findByName("Disease"))
    {
      new LookupList(name: "Disease").save()
    }

	// For Projects
	if (!LookupList.findByName("Task"))
	{
		def pt = new LookupList(name: "Task", type: "Project Info", description:"Project Tasks")
		pt.addToLookupDetails(new LookupListDetail(name: "Sample Preparation"))
		pt.addToLookupDetails(new LookupListDetail(name: "Sample Processing"))
		pt.addToLookupDetails(new LookupListDetail(name: "Data Analysis"))
		pt.addToLookupDetails(new LookupListDetail(name: "Data Publishing"))
		pt.addToLookupDetails(new LookupListDetail(name: "Figure Development"))
		pt.addToLookupDetails(new LookupListDetail(name: "Other"))
		pt.save()
		
	}
	
	if (!LookupList.findByName("Resource"))
	{
		new LookupList(name: "Resource", type: "Project Info", description:"Task Resources").save()
	}
	
	if (Deliverable.count() == 0)
	{
		new Deliverable(name: "Expression Data").save()
		new Deliverable(name: "DEG List").save()
		new Deliverable(name: "GXB Sample Set").save()
		new Deliverable(name: "Pathway Analysis").save()
		new Deliverable(name: "Module Analysis").save()
		new Deliverable(name: "WGCNA").save()
		new Deliverable(name: "Data Reduction").save()
		new Deliverable(name: "Classifiers").save()
		new Deliverable(name: "Browser Tracks").save()
		new Deliverable(name: "Other").save()
	}
	
    if (!FileTag.findByTag("Sample Set Spreadsheet"))
    {
      FileTag spreadsheetTag = new FileTag(tag: "Sample Set Spreadsheet")
      spreadsheetTag.save()
    }

    if (!FileTag.findByTag("Chip Files"))
    {
      FileTag dataFilesTag = new FileTag(tag: "Chip Files")
      dataFilesTag.save(flush:true)

	  def samples = SampleSet.findAll()
	  if (samples)
	  {
		  samples.each { ss ->
			  def chip = ChipsLoaded.findBySampleSet(ss)
			  if (chip)
			  {
				  new SampleSetFile(sampleSet: ss, description: "Expression Data File", filename: chip.filename, extension: "txt", tag: dataFilesTag).save()
			  }
		  }
      }
    }

    if (RawSignalDataType.count() == 0)
    {
      new RawSignalDataType(name:"raw_signal", displayName:"Raw Signal").save()
      new RawSignalDataType(name:"log2_transformed", displayName:"Log2 Transformed").save()
      new RawSignalDataType(name:"background_subtracted", displayName:"Background Subtracted").save()
      new RawSignalDataType(name:"average_normalized", displayName:"Average Normalized").save()
	  new RawSignalDataType(name:"quantiled_normalized", displayName:"Quantile Normalized").save()
	  new RawSignalDataType(name:"fold_change", displayName:"Fold Change").save()
	  new RawSignalDataType(name:"log2_fold_change", displayName:"Log2 Fold Change").save()
    }

//    // set the raw signal data type for sample sets that don't have one
//    def defaultSignalType = RawSignalDataType.findByName("raw_signal")
//    SampleSet.list().each {
//      if (!it.rawSignalType)
//      {
//        it.rawSignalType = defaultSignalType
//      }
//      it.save()
//    }
//
    if (ClinicalDataSource.count() == 0)
    {
      new ClinicalDataSource(name:"labkey", displayName:"LabKey", iconName:"labkey", baseUrl:"https://accesstrial.immunetolerance.org/project/home/begin.view?").save()
    }
		//To change Datasource display Name from Labkey to Trial Share
		Sql sql = Sql.newInstance(dataSource)
		sql.execute("update clinical_data_source set display_name = 'TrialShare' where display_name = 'LabKey'")
		try {

		} catch (Exception ex) {
			println "Exception updating Labkey display name in datasource : " + ex.toString()
		}

//    GenomicDataSource.list().each {
//      if (!it.iconName)
//      {
//        it.displayName = it.name
//        it.iconName = it.name.split("\\s+")[0].toLowerCase()
//        it.save()
//      }
//    }
//    GenomicDataSource.findByName("ITN") ?: new GenomicDataSource(name:"ITN", displayName:"ITN", iconName:"itn").save()

    // update disease, sample sources and species for each sample set
//    def sampleSourceLookupList = LookupList.findByName("Sample Source")
//    def diseaseLookupList = LookupList.findByName("Disease")
//    Sql sql = Sql.newInstance(dataSource)
//    SampleSet.list().each { ss ->
//      GroovyRowResult result = sql.firstRow("select species, disease, sample_source from sample_set_sample_info where sample_set_id=${ss.id}")
//      if (result?.species)
//      {
//        ss.sampleSetSampleInfo.species = Species.findByLatin(result.species) ?: null
//      }
//      if (result?.disease)
//      {
//        def disease = LookupListDetail.findByLookupListAndName(diseaseLookupList, result.disease) ?: null
//        if (disease)
//        {
//          ss.sampleSetSampleInfo.sampleSources.add(disease)
//        }
//      }
//      if (result?.sample_source)
//      {
//        def sampleSource = LookupListDetail.findByLookupListAndName(sampleSourceLookupList, result.sample_source) ?: null
//        if (sampleSource)
//        {
//          ss.sampleSetSampleInfo.sampleSources.add(sampleSource)
//        }
//      }
//      ss.sampleSetSampleInfo?.save()
//    }
//    sql.close()

//    def adminRole = SecRole.findByAuthority('ROLE_ADMIN') ?: new SecRole(authority: 'ROLE_ADMIN').save()
      def admin = SecUser.findByUsername('admin') // ?: new SecUser(username: 'admin', password: 'password', enabled: true).save(failOnError: true)
//
//    if (!admin.authorities.contains(adminRole))
//    {
//      SecUserSecRole.create(admin, adminRole)
//    }
  	  def geneListRole = SecRole.findByAuthority('ROLE_GENELISTS') ?: new SecRole(authority: 'ROLE_GENELISTS').save()
	  if (!admin.authorities.contains(geneListRole))
	  {
		  	SecUserSecRole.create(admin, geneListRole)
	  }
	  def sampleSetApprovalRole = SecRole.findByAuthority('ROLE_SETAPPROVAL') ?: new SecRole(authority: 'ROLE_SETAPPROVAL').save()
	  if (!admin.authorities.contains(sampleSetApprovalRole))
	  {
			  SecUserSecRole.create(admin, sampleSetApprovalRole)
	  }

  	  def matApprovalRole = SecRole.findByAuthority('ROLE_MATADMIN') ?: new SecRole(authority: 'ROLE_MATADMIN').save()
	  if (!admin.authorities.contains(matApprovalRole))
	  {
			  SecUserSecRole.create(admin, matApprovalRole)
	  }
	  def powerUserRole = SecRole.findByAuthority('ROLE_POWERUSER') ?: new SecRole(authority: 'ROLE_POWERUSER').save()
	  if (!admin.authorities.contains(powerUserRole))
	  {
			  SecUserSecRole.create(admin, powerUserRole)
	  }

	  def fengRole = SecRole.findByAuthority(grailsApplication.config.dm3.feng.permissions) ?: new SecRole(authority: 'ROLE_VIEW_FENG').save()

	  // Update analysis table for empty platformType as a result of domain change for supporting RNA-seq based modular analysis
	  // See Analysis domain - 'Microarray' is the default for platformType
	  Sql db = Sql.newInstance(dataSource)
	  def upQuery = "UPDATE analysis SET platform_type = 'Microarray' WHERE platform_type = '' OR platform_type = NULL"
	  try {
		  db.execute(upQuery)
	  } catch (Error err) {
		  println "Unable to update analysis table for platform_type"
	  }

	  try {
		  MongoConnector mc = new MongoConnector()

	  } catch (Error err) {
	  	  err.printStackTrace()
		  println "Unable to connect to Mongo - please verify Mongo is running"
		  System.exit(1)
	  }
	  
	  // check for chip_probe_symbol at startup.  This is MySQL specific.
	  def _mysqlDb      = grailsApplication.config.dataSource.database
	  def _limsDb		= grailsApplication.config.dataSource.LIMS
	  def tableQuery = """SELECT COUNT(*) AS 'table' FROM information_schema.tables WHERE table_schema = '${_mysqlDb}' AND table_name = 'chip_probe_symbol'"""
	  def rowCount = sql.rows(tableQuery.toString())
	  
	  if (rowCount?.get(0)?.get('table').asBoolean()) {
		  grailsApplication.config.chipProbeSymbol = rowCount?.get(0)?.get('table').asBoolean()
	  } 
	  println "Table chip_probe_symbol: " + grailsApplication.config.chipProbeSymbol
	  
	  if (_limsDb != "none") {
		  tableQuery = """SELECT COUNT(*) AS 'table' FROM information_schema.tables WHERE table_schema = '${_limsDb}'"""
		  rowCount = sql.rows(tableQuery.toString())
		  if (! rowCount?.get(0)?.get('table').asBoolean()) {
			  println "LIMS db: override ${_limsDb} not available - now 'none'" 
			  grailsApplication.config.dataSource.LIMS = "none" 
		  }
	  }

		println "dm-stable branch"
  }
  def destroy = {
  }
}
