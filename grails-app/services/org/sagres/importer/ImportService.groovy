/*
  ImportService.groovy

  Services for importing files into database
*/

package org.sagres.importer;


import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import grails.plugin.mail.MailService;
import javax.sql.DataSource;
import groovy.sql.Sql;
import org.sagres.sampleSet.MongoDataService;
import org.sagres.sampleSet.SampleSetService;
import common.chipInfo.ChipData;
import common.chipInfo.ChipType;
import common.chipInfo.GenomicDataSource;
import org.sagres.sampleSet.SampleSet;
import org.sagres.sampleSet.FocusedArraySampleSet;


//*****************************************************************************


class ImportService
{                                                               //ImportService
//-----------------------------------------------------------------------------

    //Why doesn't injection work here?
	static transactional = true;
    def grailsApplication = new DefaultGrailsApplication( );
    SampleSetService sampleSetService = new SampleSetService( );

//=============================================================================

    ImportService( )
    {
        m_impl = new ImportServiceImpl( this,
										grailsApplication.config,
                                        sampleSetService );
    }

//-----------------------------------------------------------------------------

    void setSqlDataSource( DataSource sqlDataSource )
    {
        assert( sqlDataSource );
        Sql sql = Sql.newInstance( sqlDataSource );
        if ( sql == null )
        {
            throw new ImportException( "Unable to get Sql connection" );
        }
        m_impl.setSql( sql );
    }

//-----------------------------------------------------------------------------

    void setMongoDataService( MongoDataService mongoDataService )
    {
        assert( mongoDataService );
        m_impl.setMongoDataService( mongoDataService );
    }

//-----------------------------------------------------------------------------

    void setMailService( MailService mailService )
    {
        assert( mailService );
        m_impl.setMailService( mailService );
    }

//=============================================================================

    public
    List< Map > getExpressionFileFormats( )
    {
        return m_expressionFileFormats;
    }

//-----------------------------------------------------------------------------

    public
    Map getExpressionFileFormat( String name )
    {
        return m_expressionFileFormats.find { it.name == name };
    }

//=============================================================================

    public
    Map importExpressionDataFiles( List< String > fileSpecs,
                                   Map params )
    {
        ChipType chipType;
        GenomicDataSource genomicDataSource;
        if ( params.chipTypeId )
        {
            chipType = ChipType.get( params.chipTypeId );
        }
        if ( params.genomicDataSourceId )
        {
            genomicDataSource =
                    GenomicDataSource.get( params.genomicDataSourceId );
        }
        Map formatValidation = params.formatValidation ?: [:];
        Map dataValidation = params.dataValidation ?: [:];
        String emailRecipient = params.emailRecipient;

        switch ( params.type )
        {
        case "General Expression Data":
            return importGeneralExpressionDataFile(
                fileSpecs[ 0 ], chipType, genomicDataSource,
                formatValidation, dataValidation,
                emailRecipient );
        case "Plain Signal Data":
            return importPlainSignalDataFile(
                fileSpecs[ 0 ], chipType, genomicDataSource,
                formatValidation, dataValidation,
                emailRecipient );
        case "GEO Family (soft)":
            String sampleLabelMapSpec =
                    (fileSpecs.size() > 1 ? fileSpecs[ 1 ] : null);
            return importGEOFamilyFile( fileSpecs[ 0 ], sampleLabelMapSpec,
                                        genomicDataSource,
                                        emailRecipient );
        case "GEO Series Matrix (txt)":
            return importGEOSeriesMatrixFile( fileSpecs[ 0 ],
                                              genomicDataSource,
                                              emailRecipient );
        case "RnaSeqCountAndTmm":
            return importRnaSeqGeneCountFiles(
                fileSpecs[ 0 ], fileSpecs[ 1 ], chipType, genomicDataSource,
                emailRecipient );
        case "FocusedArray":
            return importFocusedArrayDataFile(
                fileSpecs[ 0..-3], fileSpecs[ -2 ], fileSpecs[ -1 ],
				chipType, genomicDataSource, emailRecipient );
        default:
            return [ success: false,
                     message: "Unknown expression data file type: " +
                     params.type ];
        }
    }

//-----------------------------------------------------------------------------

    public
    Map importGeneralExpressionDataFile( String fileSpec,
                                         ChipType chipType,
                                         GenomicDataSource genomicDataSource,
                                         Map formatValidation = [:],
                                         Map dataValidation = [:],
                                         String emailRecipient = null )
    {
        Closure getChipType = { chipType }
        ArrayDataTableFormatValidator formatValidator =
                new ArrayDataTableFormatValidator( formatValidation );
        ArrayDataTableValidator dataValidator =
                new ArrayDataTableValidator( dataValidation );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            return ArrayDataAnnotations.makeFromArrayDataSamples( samples );
        }

        return m_impl.importArrayDataTableFile(
            fileSpec, getChipType, genomicDataSource,
            GeneralExpressionData.getTableInfo,
            formatValidator, dataValidator, getAnnotations,
            emailRecipient );
    }

//.............................................................................

    public
    Map importPlainSignalDataFile( String fileSpec,
                                         ChipType chipType,
                                         GenomicDataSource genomicDataSource,
                                         Map formatValidation = [:],
                                         Map dataValidation = [:],
                                         String emailRecipient = null )
    {
        Closure getChipType = { chipType }
        ArrayDataTableFormatValidator formatValidator =
                new ArrayDataTableFormatValidator( formatValidation );
        ArrayDataTableValidator dataValidator =
                new ArrayDataTableValidator( dataValidation );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            return ArrayDataAnnotations.makeFromArrayDataSamples( samples );
        }

        return m_impl.importArrayDataTableFile(
            fileSpec, getChipType, genomicDataSource,
            PlainSignalData.getTableInfo,
            formatValidator, dataValidator, getAnnotations,
            emailRecipient );
    }
    
//-----------------------------------------------------------------------------

    public
    Map importSoftFile( String fileSpec,
                        String sampleLabelMapSpec,
                        GenomicDataSource genomicDataSource,
                        String emailRecipient = null )
    {
        if ( fileSpec =~ /.*GSE\d+_family.soft$/ )
        {
            return importGEOFamilyFile( fileSpec, sampleLabelMapSpec,
                                        genomicDataSource,
                                        emailRecipient );
        }
        else if ( fileSpec =~ /GSE\d+.*_series_matrix(.txt)*/ )
        {
            return importGEOSeriesMatrixFile( fileSpec, genomicDataSource,
                                              emailRecipient );
        }
        else if ( fileSpec =~ /GDS\d+_full.soft/ )
        {
            return importGEOFullFile( fileSpec, genomicDataSource,
                                      emailRecipient );
        }
        else
        {
            return [ success: false,
                     message: "Unknown Soft file type" ];
        }
    }

//.............................................................................

    public
    Map importGEOFamilyFile( String fileSpec,
                             String sampleLabelMapSpec,
                             GenomicDataSource genomicDataSource,
                             String emailRecipient = null )
    {
        Closure getChipType =
                { return SoftFamilyFile.getChipType( fileSpec ); }
        ArrayDataMultitableValidator dataValidator =
                new ArrayDataMultitableValidator( requireSignal: false );
        Closure getAnnotations = {
            List< ArrayDataSample > samples,
            Map< String, String > sampleLabelMap ->

            ArrayDataAnnotations annots =
                    SoftFamilyFile.getAnnotations( fileSpec, sampleLabelMap );
            annots.getIdsFromArrayDataSamples( samples );
            return annots;
        }

        return m_impl.importArrayDataMultitableFile(
            fileSpec, getChipType, genomicDataSource,
            SoftFamilyFile.getTableInfo,
            dataValidator, getAnnotations,
            emailRecipient,
            sampleLabelMapSpec, ArrayDataMultitable.fixupDatum );
    }

//.............................................................................

    public
    Map importGEOSeriesMatrixFile( String fileSpec,
                                   GenomicDataSource genomicDataSource,
                                   String emailRecipient = null )
    {
        Closure getChipType =
                { return SoftSeriesMatrixFile.getChipType( fileSpec ); }
        ArrayDataTableFormatValidator formatValidator =
                new ArrayDataTableFormatValidator( );
        ArrayDataTableValidator dataValidator =
                new ArrayDataTableValidator(
                    requireSignal: false );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            ArrayDataAnnotations annots =
                    SoftSeriesMatrixFile.getAnnotations( fileSpec );
            annots.getIdsFromArrayDataSamples( samples );
            return annots;
        }

        return m_impl.importArrayDataTableFile(
            fileSpec, getChipType, genomicDataSource,
            SoftSeriesMatrixFile.getTableInfo,
            formatValidator, dataValidator, getAnnotations,
            emailRecipient );
    }

//.............................................................................

    public
    Map importGEOFullFile( String fileSpec,
                           GenomicDataSource genomicDataSource,
                           String emailRecipient = null )
    {
        Closure getChipType =
                { return SoftFullFile.getChipType( fileSpec ); }
        ArrayDataTableFormatValidator formatValidator =
                new ArrayDataTableFormatValidator( );
        ArrayDataTableValidator dataValidator =
                new ArrayDataTableValidator(
                    requireSignal: false );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            ArrayDataAnnotations annots =
                    SoftFullFile.getAnnotations( fileSpec );
            annots.getIdsFromArrayDataSamples( samples );
            return annots;
        }

        return m_impl.importArrayDataTableFile(
            fileSpec, getChipType, genomicDataSource,
            SoftFullFile.getTableInfo,
            formatValidator, dataValidator, getAnnotations,
            emailRecipient );
    }

//=============================================================================

    public
    Map importRnaSeqGeneCountFiles( String rawCountSpec,
                                    String tmmNormalizedSpec,
                                    ChipType chipType,
                                    GenomicDataSource genomicDataSource,
                                    String emailRecipient = null )
    {
        return m_impl.importRnaSeqGeneCountFiles(
            rawCountSpec, tmmNormalizedSpec, chipType, genomicDataSource,
            emailRecipient );
    }

//-----------------------------------------------------------------------------

    public
    Map importRnaSeqTmmNormalizedFile( String tmmNormalizedSpec,
                                       SampleSet sampleSet,
                                       ChipType chipType,
                                       GenomicDataSource genomicDataSource,
                                       String emailRecipient = null )
    {
        return m_impl.importRnaTmmNormalizedFile(
            tmmNormalizedSpec, sampleSet, chipType, genomicDataSource,
            emailRecipient );
    }

//=============================================================================

    public
    Map importFocusedArrayDataFile( List< String > fileSpecs,
									String housekeepingGenesFileSpec,
									String referenceSamplesFileSpec,
                                    ChipType chipType,
                                    GenomicDataSource genomicDataSource,
                                    String emailRecipient = null )
    {
        Closure getTableInfo;
		Closure getRawDatum;
        if ( chipType.chipData.manufacturer == "Fluidigm" )
        {
            getTableInfo = FluidigmArrayDataFile.getTableInfo;
			getRawDatum = FluidigmArrayDataFile.getRawDatum;
        }
        else
        {
            return [ success: false,
					 message: "Unsupported focused-array type"
				   ];
        }
        return m_impl.importFocusedArrayDataFile(
            fileSpecs, housekeepingGenesFileSpec, referenceSamplesFileSpec,
			chipType, genomicDataSource, getTableInfo, getRawDatum,
            emailRecipient );
    }

//-----------------------------------------------------------------------------

	public
	Map importFocusedArrayFoldChangeResults(
		SampleSet sampleSet, Long foldChangeParamsId, String fcFileSpec,
		FocusedArraySampleSet focusedArraySampleSet,
		SampleSetService sampleSetService, Sql sql )
	{
		return FocusedArrayData.importFoldChangeResults(
			sampleSet, foldChangeParamsId, fcFileSpec,
			focusedArraySampleSet, sampleSetService, sql );
	}
	
//=============================================================================

    public
    Map validateAndExportExpressionDataFiles( List< String > fileSpecs,
                                              Map params )
    {
        assert( params.outDataFileSpec );
        assert( params.outSamplesFileSpec );
        ChipType chipType;
        if ( params.chipTypeId )
        {
            chipType = ChipType.get( params.chipTypeId );
        }
        Map formatValidation = params.formatValidation ?: [:];
        Map dataValidation = params.dataValidation ?: [:];

        switch ( params.type )
        {
        case "General Expression Data":
            return validateAndExportGeneralExpressionDataFile(
                fileSpecs[ 0 ],
                params.outDataFileSpec, params.outSamplesFileSpec,
                chipType,
                formatValidation, dataValidation );
        case "Plain Signal Data":
            return validateAndExportPlainSignalDataFile(
                fileSpecs[ 0 ],
                params.outDataFileSpec, params.outSamplesFileSpec,
                chipType,
                formatValidation, dataValidation );
        case "GEO Family (soft)":
            return validateAndExportGEOFamilyFile(
                fileSpecs[ 0 ],
                params.outDataFileSpec, params.outSamplesFileSpec );
        case "GEO Series Matrix (txt)":
            return validateAndExportGEOSeriesMatrixFile(
                fileSpecs[ 0 ],
                params.outDataFileSpec, params.outSamplesFileSpec );
        // case "RnaSeqCount":
        //     return validateAndExportRnaSeqGeneCountFile(
        //         fileSpecs[ 0 ], chipType );
        default:
            return [ success: false,
                     message: "Unknown expression data file type: " +
                     params.type ];
        }
    }

//-----------------------------------------------------------------------------

    public
    Map validateAndExportGeneralExpressionDataFile( String fileSpec,
                                                    String outDataFileSpec,
                                                    String outSamplesFileSpec,
                                                    ChipType chipType,
                                                    Map formatValidation = [:],
                                                    Map dataValidation = [:] )
    {
        Closure getChipType = { chipType }
        ArrayDataTableFormatValidator formatValidator =
                new ArrayDataTableFormatValidator( formatValidation );
        ArrayDataTableValidator dataValidator =
                new ArrayDataTableValidator( dataValidation );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            return ArrayDataAnnotations.makeFromArrayDataSamples( samples );
        }

        return m_impl.validateAndExportArrayDataTableFile(
            fileSpec, outDataFileSpec, outSamplesFileSpec,
            getChipType, GeneralExpressionData.getTableInfo,
            formatValidator, dataValidator, getAnnotations );
    }

//.............................................................................

    public
    Map validateAndExportPlainSignalDataFile( String fileSpec,
                                              String outDataFileSpec,
                                              String outSamplesFileSpec,
                                              ChipType chipType,
                                              Map formatValidation = [:],
                                              Map dataValidation = [:] )
    {
        Closure getChipType = { chipType }
        ArrayDataTableFormatValidator formatValidator =
                new ArrayDataTableFormatValidator( formatValidation );
        ArrayDataTableValidator dataValidator =
                new ArrayDataTableValidator( dataValidation );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            return ArrayDataAnnotations.makeFromArrayDataSamples( samples );
        }

        return m_impl.validateAndExportArrayDataTableFile(
            fileSpec, outDataFileSpec, outSamplesFileSpec,
            getChipType, PlainSignalData.getTableInfo,
            formatValidator, dataValidator, getAnnotations );
    }
    
//.............................................................................

    public
    Map validateAndExportGEOFamilyFile( String inFileSpec,
                                        String outDataFileSpec,
                                        String outSamplesFileSpec )
    {
        Closure getChipType =
                { return SoftFamilyFile.getChipType( inFileSpec ); }
        ArrayDataMultitableValidator validator =
                new ArrayDataMultitableValidator( requireSignal: false );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            ArrayDataAnnotations annots =
                    SoftFamilyFile.getAnnotations( inFileSpec );
            annots.getIdsFromArrayDataSamples( samples );
            return annots;
        }
        return m_impl.validateAndExportArrayDataMultitableFile(
            inFileSpec, outDataFileSpec, outSamplesFileSpec,
            getChipType, SoftFamilyFile.getTableInfo,
            validator, getAnnotations, ArrayDataMultitable.fixupDatum );
    }

//.............................................................................

    public
    Map validateAndExportGEOSeriesMatrixFile( String inFileSpec,
                                              String outDataFileSpec,
                                              String outSamplesFileSpec )
    {
        Closure getChipType =
                { return SoftSeriesMatrixFile.getChipType( inFileSpec ); }
        ArrayDataTableFormatValidator formatValidator =
                new ArrayDataTableFormatValidator( );
        ArrayDataTableValidator dataValidator =
                new ArrayDataTableValidator(
                    requireSignal: false );
        Closure getAnnotations = { List< ArrayDataSample > samples ->
            ArrayDataAnnotations annots =
                    SoftSeriesMatrixFile.getAnnotations( inFileSpec );
            annots.getIdsFromArrayDataSamples( samples );
            return annots;
        }

        return m_impl.validateAndExportArrayDataTableFile(
            inFileSpec, outDataFileSpec, outSamplesFileSpec,
            getChipType, SoftSeriesMatrixFile.getTableInfo,
            formatValidator, dataValidator, getAnnotations );
    }

//=============================================================================

    public
    Map importAnnotationFile( String fileSpec, ChipType chipType )
    {
        if ( chipType.chipData.manufacturer == "Illumina" )
        {
            return m_impl.importAnnotationTableFile(
                fileSpec, chipType, IlluminaChipAnnotationFile.getTableInfo );
        }
        else if ( chipType.chipData.manufacturer == "Affymetrix" )
        {
            return m_impl.importAnnotationTableFile(
                fileSpec, chipType, GenericChipAnnotationFile.getTableInfo,
                TextTableSeparator.CSV );
        }
        else if ( chipType.chipData.manufacturer == "UCSC" )
        {
            return m_impl.importAnnotationTableFile(
                fileSpec, chipType, GenericChipAnnotationFile.getTableInfo,
                TextTableSeparator.TAB );
        }
        else if ( chipType.chipData.manufacturer == "Ensembl" )
        {
            return m_impl.importAnnotationTableFile(
                fileSpec, chipType, GenericChipAnnotationFile.getTableInfo,
                TextTableSeparator.TAB );
        }
    }

//-----------------------------------------------------------------------------

    public
    Map importSoftAnnotationFile( String fileSpec )
    {
        ChipType chipType = SoftAnnotationFile.getChipType( fileSpec );
        return m_impl.importAnnotationTableFile(
            fileSpec, chipType, SoftAnnotationFile.getTableInfo );
    }

//-----------------------------------------------------------------------------

    public
    Map importGeoAnnotationTableFile( String fileSpec )
    {
        ChipType chipType = GeoAnnotationTableFile.getChipType( fileSpec );
        return m_impl.importAnnotationTableFile(
            fileSpec, chipType, GenericChipAnnotationFile.getTableInfo,
            TextTableSeparator.TAB );
    }

//-----------------------------------------------------------------------------

    public
    Map importFluidigmDesignFile( String fileSpec,
								  String housekeepingGenesFileSpec,
								  String referenceSamplesFileSpec,
                                  String chipTypeName,
                                  ChipData chipData )
    {
        Map chipTypeInfo = FluidigmDesignFile.getChipTypeInfo();
        List< String > dbTableColumns = FluidigmDesignFile.getDbTableColumns();
        return m_impl.importFocusedArrayDesignFile(
            fileSpec, housekeepingGenesFileSpec, referenceSamplesFileSpec,
			chipTypeName, chipData, chipTypeInfo,
            FluidigmDesignFile.getTableInfo, dbTableColumns );
    }

//=============================================================================

    public
    Map importStdRankList( String fileSpec )
    {
        return m_impl.importStdRankList( fileSpec );
    }

//-----------------------------------------------------------------------------

    public
    Map importStdRankList( String fileSpec,
                           Long sampleSetId, Long groupSetId )
    {
        return m_impl.importStdRankList( fileSpec, sampleSetId, groupSetId );
    }

//-----------------------------------------------------------------------------

    public
    Map importGenericRankList( String fileSpec,
                               Long sampleSetId, Long groupSetId,
                               String description,
                               TextTableSeparator separator,
                               int firstRow,
                               int probeIdColumn, int valueColumn,
                               boolean descending = true )
    {
        return m_impl.importGenericRankList(
            fileSpec, sampleSetId, groupSetId, description, separator, firstRow,
            probeIdColumn, valueColumn, descending );
    }

//=============================================================================

    public
    Map importNcbiGeneFile( String fileSpec )
    {
        return m_impl.importNcbiGeneFile( fileSpec );
    }

//=============================================================================

    private ImportServiceImpl m_impl;
    
    private List< Map > m_expressionFileFormats =
            [
                [
                    name: "General Expression Data",
                    requiresChipType: true,
                    validationLevel: 2,
                    defaultGenomicDataSource: "Benaroya" //!!!
                ],
                [
                    name: "Plain Signal Data",
                    requiresChipType: true,
                    validationLevel: 1,
                    defaultGenomicDataSource: "Benaroya" //!!!
                ],
                [
                    name: "GEO Family (soft)",
                    requiresChipType: false,
                    validationLevel: 0,
                    defaultGenomicDataSource: "GEO"
                ],
                [
                    name: "GEO Series Matrix (txt)",
                    requiresChipType: false,
                    validationLevel: 0,
                    defaultGenomicDataSource: "GEO"
                ]
            ];

//-----------------------------------------------------------------------------
}                                                               //ImportService


//*****************************************************************************
