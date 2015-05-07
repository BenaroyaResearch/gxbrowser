/*
  FocusedArrayDataTable.groovy

  Routines for importing focused-array expression data from a text table
*/

package org.sagres.importer;

import java.util.regex.Matcher;


//*****************************************************************************


class FocusedArrayDataTableInfo
extends TextTableInfo
{                                                   //FocusedArrayDataTableInfo
//-----------------------------------------------------------------------------

    String samplePlate;
    String assayPlate;
    Map headerInfo;
    List< FocusedArraySampleInfo > samples;
    List< FocusedArrayAssayInfo > assays;

//-----------------------------------------------------------------------------
}                                                   //FocusedArrayDataTableInfo


//*****************************************************************************


class FocusedArrayDataRawDatum
{
	String sampleWell;
	String assayWell;
	Float ctValue;
	Float ctQuality;
	String ctCall;
	String ctThreshold;
	Float tmInRange;
	Float tmOutRange;
	Float tmPeakRatio;
}


//*****************************************************************************


class FocusedArrayDataTable
{                                                       //FocusedArrayDataTable
//-----------------------------------------------------------------------------

	static
	FocusedArraySampleAssayDatum parseRow( String[] fields,
										   FocusedArrayDataTableInfo tableInfo,
										   Closure getRawDatum )
	{
		FocusedArraySampleAssayDatum datum =
				new FocusedArraySampleAssayDatum( );
		FocusedArrayDataRawDatum rawDatum =
				getRawDatum( fields, tableInfo );
		for ( FocusedArraySampleInfo sampleInfo : tableInfo.samples )
		{
			if ( sampleInfo.well == rawDatum.sampleWell )
			{
				datum.sampleInfo = sampleInfo;
				break;
			}
		}
		for ( FocusedArrayAssayInfo assayInfo : tableInfo.assays )
		{
			if ( assayInfo.well == rawDatum.assayWell )
			{
				datum.assayInfo = assayInfo;
				break;
			}
		}
		if ( datum.sampleInfo == null )
		{
			String msg = "Failed to get sampleInfo";
			throw new ImportException( msg );
			println( msg );
			println( "fields=" + fields );
			println( "rawDatum.sampleWell=" + rawDatum.sampleWell );
			println( "tableInfo.samples = " + tableInfo.samples*.well );
		}
		if ( datum.assayInfo == null )
		{
			String msg = "Failed to get assayInfo";
			throw new ImportException( msg );
			println( msg );
			println( "rawDatum.assayWell=" + rawDatum.assayWell );
			println( "tableInfo.assays = " + tableInfo.assays*.well );
		}
		datum.ctValue = rawDatum.ctValue;
		datum.ctQuality = rawDatum.ctQuality;
		datum.ctCall = rawDatum.ctCall;
		datum.ctThreshold = rawDatum.ctThreshold;
		datum.tmInRange = rawDatum.tmInRange;
		datum.tmOutRange = rawDatum.tmOutRange;
		datum.tmPeakRatio = rawDatum.tmPeakRatio;
		return datum;
	}

//-----------------------------------------------------------------------------
}                                                       //FocusedArrayDataTable


//*****************************************************************************


class FocusedArrayDataTableValidator
extends FocusedArrayDataValidator
{                                                   //FocusedArrayDataValidator
//-----------------------------------------------------------------------------

	void init( FocusedArrayDataTableInfo tableInfo,
			   Closure getRawDatum,
			   List< String > warnings )
	{
        m_tableInfo = tableInfo;
		m_getRawDatum = getRawDatum;
        m_warnings = warnings;
	}

//=============================================================================

    void beforeTable( )
    {
        start( );
    }

//=============================================================================

    void validateRow( String[] fields, int lineNum )
    {
        if ( checkFieldCount( m_tableInfo, fields, lineNum ) == false )
        {
            return;
        }
		FocusedArrayDataRawDatum rawDatum =
				m_getRawDatum( fields, m_tableInfo );
		if ( rawDatum.sampleWell == "" )
		{
			String msg = "Bad or no sample well on line " + lineNum;
			throw new ImportException( msg );
		}
		if ( rawDatum.assayWell == "" )
		{
			String msg = "Bad or no assay well on line " + lineNum;
			throw new ImportException( msg );
		}
		if ( rawDatum.ctValue == null )
		{
			String msg = "Bad or no Ct Value on line " + lineNum;
			throw new ImportException( msg );
		}
		if ( rawDatum.ctQuality == null )
		{
			String msg = "Bad or no Ct Quality on line " + lineNum;
			throw new ImportException( msg );
		}
		if ( rawDatum.ctCall == null )
		{
			String msg = "Bad or no Ct Call on line " + lineNum;
			throw new ImportException( msg );
		}
	}

//=============================================================================

    void afterTable( )
    {
        finish( );
    }

//=============================================================================

    protected FocusedArrayDataTableInfo m_tableInfo;
	protected Closure m_getRawDatum;
    
//-----------------------------------------------------------------------------
}                                                   //FocusedArrayDataValidator


//*****************************************************************************


class FocusedArrayMultitableBulkLoadBuilder
extends FileBuilderFromTextMultitable
{                                       //FocusedArrayMultitableBulkLoadBuilder
//-----------------------------------------------------------------------------

	FocusedArrayMultitableBulkLoadBuilder( String bulkLoadSpec,
										   Closure getRawDatum )
	{
		super( bulkLoadSpec );
		m_getRawDatum = getRawDatum;
	}
										   
//=============================================================================

    @Override
    void processRow( TextTableInfo tableInfo,
                     String[] fields, int lineNum )
    {
		FocusedArrayDataTableInfo dataTableInfo =
				(FocusedArrayDataTableInfo) tableInfo;
		assert( dataTableInfo );
        if ( fields.size() != tableInfo.numColumns )
        {
            return;
        }
		FocusedArraySampleAssayDatum datum =
				FocusedArrayDataTable.parseRow( fields, tableInfo,
												m_getRawDatum );
		FocusedArrayData.writeBulkLoadLine( m_writer, datum );
	}

//=============================================================================

	protected Closure m_getRawDatum;

//-----------------------------------------------------------------------------
}                                       //FocusedArrayMultitableBulkLoadBuilder


//*****************************************************************************
