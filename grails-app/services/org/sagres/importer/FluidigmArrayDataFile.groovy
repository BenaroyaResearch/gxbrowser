/*
  FluidigmArrayDataFile.groovy

  Routines for importing Fluidigm focused-array expression data
*/

package org.sagres.importer;

import java.util.regex.Matcher;


//*****************************************************************************


class FluidigmArrayDataTableInfo
extends FocusedArrayDataTableInfo
{                                                  //FluidigmArrayDataTableInfo
//-----------------------------------------------------------------------------

    int sampleAssayWellColumn;
    int sampleNameColumn;
    int sampleTypeColumn;
    int sampleConcColumn;
    int targetColumn;
    int ctValueColumn;
    int ctQualityColumn;
    int ctCallColumn;
    int ctThresholdColumn;
    int tmInRangeColumn;
    int tmOutRangeColumn;
    int tmPeakRatioColumn;

//-----------------------------------------------------------------------------
}                                                  //FluidigmArrayDataTableInfo


//*****************************************************************************


class FluidigmArrayDataFile
{                                                       //FluidigmArrayDataFile
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        FluidigmArrayDataTableInfo tableInfo ->

			assert( tableInfo );

            tableInfo.separator = TextTableSeparator.CSV;
            tableInfo.firstRow = 12;

			boolean gotPlateNumbers = false;
			Matcher m = ( fileSpec =~ /S(\d+)A(\d+)/ );
			if ( m.count == 1 )
			{
				if ( m[ 0 ].size() < 3 )
				{
					String msg = "An error occurred parsing the file name";
					throw new ImportException( msg );
				}
				tableInfo.samplePlate = m[ 0 ][ 1 ];
				tableInfo.assayPlate = m[ 0 ][ 2 ];
				gotPlateNumbers = true;
			}

            String[] headings1, headings2;

			tableInfo.headerInfo = [:];
			tableInfo.samples = [];
			tableInfo.assays = [];

            new File( fileSpec ).eachLine { line, lineNum ->
                if ( lineNum <= 7 )
                {
                    KeyValue keyVal = KeyValue.split( line, ',' );
                    tableInfo.headerInfo[ (keyVal.key) ] = keyVal.value;
                    if ( (keyVal.key == "Chip Run Info") &&
						 (gotPlateNumbers == false) )
                    {
                        m = ( keyVal.value =~ /S(\d+)A(\d+)/ );
                        if ( (m.count != 1) || (m[ 0 ].size() < 3) )
                        {
                            String msg = "Error identifying SnAn in Chip Run Info string";
                            throw new ImportException( msg );
                        }
                        tableInfo.samplePlate = m[ 0 ][ 1 ];
                        tableInfo.assayPlate = m[ 0 ][ 2 ];
                    }
                }
                else if ( lineNum == 11 )
                {
                    headings1 = TextTable.splitRow( line, tableInfo.separator );
                }
                else if ( lineNum == 12 )
                {
                    headings2 = TextTable.splitRow( line, tableInfo.separator );
                    processHeadings( headings1, headings2, tableInfo );
                    tableInfo.numRows = 0;
                }
                else if ( (lineNum > 12) && (line.size() > 0) )
                {
                    ++tableInfo.numRows;
                    String[] values = TextTable.splitRow( line, tableInfo.separator );
                    getSampleAssayInfo( values, tableInfo );
                }
            }            
    }

//-----------------------------------------------------------------------------

    private
    static
    void processHeadings( String[] headings1, String[] headings2,
                          FluidigmArrayDataTableInfo tableInfo )
    {
        List< String > colNames = [];
        if ( headings1.size() != headings2.size() )
        {
            String msg = "Column heading sizes do not match";
            throw new ImportException( msg );
        }
		tableInfo.numColumns = headings1.size();
        for ( int i = 0; i < headings1.size(); ++i )
        {
            String name = headings1[ i ] + " " + headings2[ i ];
            name = name.toLowerCase();
            name = name.replaceAll( /[ ()]/, "_" );
            colNames.add( name );
        }
        for ( int i = 0; i < colNames.size(); ++i )
        {
            String name = colNames[ i ];
            if ( name == 'chamber_id' )
            {
                tableInfo.sampleAssayWellColumn = i;
            }
            else if ( name == 'sample_name' )
            {
                tableInfo.sampleNameColumn = i;
            }
            else if ( name == 'sample_type' )
            {
                tableInfo.sampleTypeColumn = i;
            }
            else if ( name == 'sample_rconc' )
            {
                tableInfo.sampleConcColumn = i;
            }
            else if ( name == 'evagreen_name' )
            {
                tableInfo.targetColumn = i;
            }
            else if ( name == 'ct_value' )
            {
                tableInfo.ctValueColumn = i;
            }
            else if ( name == 'ct_quality' )
            {
                tableInfo.ctQualityColumn = i;
            }
            else if ( name == 'ct_call' )
            {
                tableInfo.ctCallColumn = i;
            }
            else if ( name == 'ct_threshold' )
            {
                tableInfo.ctThresholdColumn = i;
            }
            else if ( name == 'tm_in_range' )
            {
                tableInfo.tmInRangeColumn = i;
            }
            else if ( name == 'tm_out_range' )
            {
                tableInfo.tmOutRangeColumn = i;
            }
            else if ( name == 'tm_peak_ratio' )
            {
                tableInfo.tmPeakRatioColumn = i;
            }
        }

		if ( tableInfo.sampleAssayWellColumn == null )
		{
			String msg = "No Chamber ID (sample-assay well) column found";
			throw new ImportException( msg );
		}
		if ( tableInfo.sampleNameColumn == null )
		{
			String msg = "No Sample Name column found";
			throw new ImportException( msg );
		}
		if ( tableInfo.targetColumn == null )
		{
			String msg = "No Target (EvaGreen Name) column found";
			throw new ImportException( msg );
		}
    }

//-----------------------------------------------------------------------------

    private
    static
    void getSampleAssayInfo( String[] values,
                             FluidigmArrayDataTableInfo tableInfo )
    {
		Map wells = getSampleAndAssayWells( values, tableInfo );
		String sampleName = values[ tableInfo.sampleNameColumn ];
		String sampleType = values[ tableInfo.sampleTypeColumn ];
		Float sampleConcentration = values[ tableInfo.sampleConcColumn ];
		String target = values[ tableInfo.targetColumn ];

		boolean newSample = true;
		for ( FocusedArraySampleInfo sampleInfo : tableInfo.samples )
		{
			if ( (sampleInfo.plate == tableInfo.samplePlate) &&
				 (sampleInfo.well == wells.sampleWell) )
			{
				newSample = false;
				if ( sampleName != sampleInfo.name )
				{
					String msg = "Sample names (" + sampleName + " and " +
							sampleInfo.name + ") differ for plate " +
							tableInfo.samplePlate +
							" well " + wells.sampleWell;
					throw new ImportException( msg );
				}
				//Could check type and concentration, too
				break;
			}
		}
		if ( newSample )
		{
			FocusedArraySampleInfo sampleInfo = new FocusedArraySampleInfo( );
			sampleInfo.plate = tableInfo.samplePlate;
			sampleInfo.well = wells.sampleWell;
			sampleInfo.name = sampleName;
			sampleInfo.type = sampleType;
			sampleInfo.concentration = sampleConcentration;
			tableInfo.samples.add( sampleInfo );
		}

		boolean newAssay = true;
		for ( FocusedArrayAssayInfo assayInfo : tableInfo.assays )
		{
			if ( (assayInfo.plate == tableInfo.assayPlate) &&
				 (assayInfo.well == wells.assayWell) )
			{
				newAssay = false;
				if ( target != assayInfo.target )
				{
					String msg = "Assay targets (" + target + " and " +
							assayInfo.target + ") differ for plate " +
							tableInfo.assayPlate +
							" well " + wells.assayWell;
					throw new ImportException( msg );
				}
				break;
			}
		}
		if ( newAssay )
		{
			FocusedArrayAssayInfo assayInfo = new FocusedArrayAssayInfo( );
			assayInfo.plate = tableInfo.assayPlate;
			assayInfo.well = wells.assayWell;
			assayInfo.target = target;
			tableInfo.assays.add( assayInfo );
		}
    }

//=============================================================================

	private
    static
	Map getSampleAndAssayWells( String[] values,
								FluidigmArrayDataTableInfo tableInfo )
	{
		String sampleAssayWell = values[ tableInfo.sampleAssayWellColumn ];
		Matcher m = ( sampleAssayWell =~ /S(\d+)-A(\d+)/ );
		if ( (m.count != 1) || (m[ 0 ].size() < 3) )
		{
			String msg = "Error getting well numbers from Chamber ID";
			throw new ImportException( msg );
		}
		return [
			sampleWell: m[ 0 ][ 1 ],
			assayWell: m[ 0 ][ 2 ]
		];
	}

//=============================================================================

	static
	Closure getRawDatum = {
		String[] values,
		FluidigmArrayDataTableInfo tableInfo ->

			FocusedArrayDataRawDatum datum =
					new FocusedArrayDataRawDatum( );

			if ( tableInfo.sampleAssayWellColumn != null )
			{
				Map wells = getSampleAndAssayWells( values, tableInfo );
				datum.sampleWell = wells.sampleWell;
				datum.assayWell = wells.assayWell;
			}
			if ( datum.sampleWell == null )
			{
				String msg = "Missing sample well value";
				throw new ImportException( msg );
				println( msg );
				println( "getRawDatum:  values[tableInfo.sampleAssayWellColumn]=" +
						 values[tableInfo.sampleAssayWellColumn] );
			}
			if ( datum.assayWell == null )
			{
				String msg = "Missing assay well value";
				throw new ImportException( msg );
				println( msg );
				println( "getRawDatum:  values[tableInfo.sampleAssayWellColumn]=" +
						 values[tableInfo.sampleAssayWellColumn] );
			}
			if ( tableInfo.ctValueColumn != null )
			{
				datum.ctValue =
						Importer.parseDouble( values[ tableInfo.ctValueColumn ] );
			}
			if ( tableInfo.ctQualityColumn != null )
			{
				datum.ctQuality =
						Importer.parseDouble( values[ tableInfo.ctQualityColumn ] );
			}
			if ( tableInfo.ctCallColumn != null )
			{
				datum.ctCall = values[ tableInfo.ctCallColumn ];
			}
			if ( tableInfo.ctThresholdColumn != null )
			{
				datum.ctThreshold = values[ tableInfo.ctThresholdColumn ];
			}
			if ( tableInfo.tmInRangeColumn != null )
			{
				datum.tmInRange =
						Importer.parseDouble( values[ tableInfo.tmInRangeColumn ] );
			}
			if ( tableInfo.tmOutRangeColumn != null )
			{
				datum.tmOutRange =
						Importer.parseDouble( values[ tableInfo.tmOutRangeColumn ] );
			}
			if ( tableInfo.tmPeakRatioColumn != null )
			{
				datum.tmPeakRatio =
						Importer.parseDouble( values[ tableInfo.tmPeakRatioColumn ] );
			}

			return datum;
	}

//-----------------------------------------------------------------------------
}                                                       //FluidigmArrayDataFile


//*****************************************************************************
