/*
  ArrayDataImporter.groovy

  Routines for importing micro-array and similar data.
*/

package org.sagres.importer

import java.util.regex.Matcher;
import java.sql.SQLException;
import groovy.sql.Sql;
//import org.codehaus.groovy.grails.commons.GrailsApplication;
import groovy.util.ConfigObject;
import org.sagres.sampleSet.SampleSet;
import org.sagres.sampleSet.SampleSetService;
import common.chipInfo.ChipType;
import common.chipInfo.ChipsLoaded;


//*****************************************************************************


class ArrayDataSample
{                                                             //ArrayDataSample
//-----------------------------------------------------------------------------

    Integer id;
    String label; //a.k.a. barcode
    Double signalTotal = 0.0;
    
//-----------------------------------------------------------------------------
}                                                             //ArrayDataSample


//*****************************************************************************


class ArrayDataDatum
{                                                              //ArrayDataDatum
//-----------------------------------------------------------------------------

    Double signal;
    Double pVal;
    String call;
    
//-----------------------------------------------------------------------------
}                                                              //ArrayDataDatum


//*****************************************************************************


class ArrayDataImporter
{                                                           //ArrayDataImporter
//-----------------------------------------------------------------------------

    static
    boolean alreadyLoaded( String fileName )
    {
        ChipsLoaded[] loadsOfThis =
                ChipsLoaded.findAllByFilename( fileName );
		for ( ChipsLoaded load : loadsOfThis )
		{
			if ( (load.loadStatus == ImportStatus.COMPLETE.record) ||
				 (load.loadStatus == ImportStatus.WARNING.record) )
			{
				return true;
			}
		}
		return false;
    }

//-----------------------------------------------------------------------------

    static
    ChipsLoaded createLogRecord( String fileName, Map fields = [:] )
    {
        ChipsLoaded logRecord =
                new ChipsLoaded( filename: fileName,
                                 loadStatus: ImportStatus.STARTING.record );
        assert( logRecord );
        fields.each { key, value ->
            logRecord[ key ] = value;
        }
        if ( logRecord.save( flush: true ) == null )
        {
            List< String > errors = [ "Error saving ChipsLoaded record:" ];
            logRecord.errors.each { err ->
                errors.add( err );
            }
            throw new ImportException( errors );
        }
        return logRecord;
    }

//=============================================================================

    static
    Map< String, Boolean > getChipProbes( ChipType chipType, Sql sql )
    {
        assert( sql );
        Map< String, Boolean > chipProbes = new HashMap< String, Boolean >();
        String query;
        try
        {
            query = "SELECT " + chipType.probeListColumn +
                    " FROM " + chipType.probeListTable +
                    " WHERE ((" + chipType.probeListColumn + " IS NOT NULL)" +
                    " AND (" + chipType.probeListColumn + " <> ''))";
            sql.eachRow( query ) { row ->
                chipProbes[ row[ 0 ] ] = false;
            }
            if ( chipProbes.size() == 0 )
            {
                String msg = "No probes listed in column " +
                        chipType.probeListColumn +
                        " of table " + chipType.probeListTable;
                throw new ImportException( msg );
            }
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Query: " + query + "\n" +
                    "Message: " + exc.message
            throw new ImportException( msg );
        }
        return chipProbes;
    }

//=============================================================================

    static
    void convertCallToPVal( ArrayDataDatum datum )
    {
        if ( (datum.pVal == null) && (datum.call != null) )
        {
            if ( datum.call == "P" ) //present
            {
                datum.pVal = 0.0;
            }
            else if ( datum.call == "M" ) //marginal
            {                       //Where both DETECTION P-VALUE and ABS_CALL
                datum.pVal = 0.06;  // are given, M is used for ranges 0.04-0.06
            }                       // or 0.05-0.065, so this is representative.
            else if ( datum.call == "A" ) //absent
            {
                datum.pVal = 1.0;
            }
        }
    }

//=============================================================================

    static
    void writeBulkLoadLine( PrintWriter writer, String probeId,
                            ArrayDataSample sample, ArrayDataDatum datum )
    {
        Importer.writeBulkLoadLine( writer, [ sample.id, probeId,
                                              datum.signal, datum.pVal ] );
    }

//-----------------------------------------------------------------------------

    static
    void loadDataToDb( String bulkLoadSpec, Sql sql )
    {
        Importer.loadDataToDb( bulkLoadSpec, "array_data_detail",
                               [ "array_data_id", "affy_id",
                                 "signal", "detection" ],
                               sql );
    }

//=============================================================================

    static
    int createSampleSet( long chipsLoadedId, SampleSetService service )
    {
        int sampleSetId = service.createSampleSetFromChipID( chipsLoadedId );
        if ( (sampleSetId == null) || (sampleSetId <= 0) )
        {
            throw new ImportException( "Unable to create sample set" );
        }
        return sampleSetId;
    }

//=============================================================================

    static
    Map< String, String > parseMatchedSamplesFile(
        String fileSpec, TextTableSeparator separator = TextTableSeparator.CSV )
    {
        //The format of the file is a CSV whose rows begin with the sample
        // labels. Samples that are to be matched (combined) are listed
        // on consecutive lines, and these groups are separated by blank lines.
        Map< String, String > sampleLabelMap = [:];
        List< String > labels = [];
        new File( fileSpec ).eachLine { line, lineNum ->
            if ( lineNum < 2 )
                return; //skip header
            String fields = TextTable.splitRow( line, separator );
            if ( fields.size() > 0 )
            {
                labels.add( fields[ 0 ] );
            }
            else
            {
                if ( labels.size() > 0 )
                {
                    String newLabel = labels.join( "+" );
                    for ( String label : labels )
                    {
                        sampleLabelMap[ (label) ] = newLabel;
                    }
                    labels = [];
                }
            }
        }
        return sampleLabelMap;
    }

//=============================================================================

    static
    void quantileNormalize( long sampleSetId,
                            SampleSetService service, Sql sql )
    {
        if ( service.quantileNormalizeSignals( sampleSetId, sql ) == false )
        {
            throw new ImportException(
                "Unable to quantile normalize sample set" );
        }
    }

//-----------------------------------------------------------------------------

    static
    void generateDefaultRankList( long sampleSetId,
                                  SampleSetService service, Sql sql )
    {
        if ( service.generateDefaultRankList( sampleSetId, sql ) == false )
        {
            throw new ImportException(
                "Unable to generate default rank list" );
        }
    }

//-----------------------------------------------------------------------------

    static
    void calcPalx( long sampleSetId, SampleSetService service, 
                   Sql sql, ConfigObject config )
    {
        if ( service.calcPalx( sampleSetId, sql,
                config.palx.defaultDetectionPVal,
                config.palx.defaultFractionPresent ) == false )
        {
            throw new ImportException( "Unable to calc PALX" );
        }
    }

//=============================================================================

    static
    void cleanup( boolean succeeded, AuxFileSpecs auxSpecs,
                  List< ArrayDataSample > samples, Long sampleSetId,
                  Sql sql )
    {
        Importer.cleanup( succeeded, auxSpecs );
        if ( succeeded )
        {
            return;
        }
		//The rest is a partial rollback due to failure.
		// Partial because we leave the chips_loaded record intact,
		// with the error report, and because I may have forgotten something.
        String command;
        try
        {
			command = "DELETE FROM array_data_detail" +
					"  WHERE array_data_id = ?";
			for ( ArrayDataSample sample : samples )
			{
				if ( sample.id )
					sql.execute( command, [ sample.id ] );
			}

			command = "DELETE FROM array_data_detail_quantile_normalized" +
					"  WHERE array_data_id = ?";
			for ( ArrayDataSample sample : samples )
			{
				if ( sample.id )
					sql.execute( command, [ sample.id ] );
			}

			command = "DELETE FROM array_data" +
                    "  WHERE id = ?";
			for ( ArrayDataSample sample : samples )
			{
				if ( sample.id )
					sql.execute( command, [ sample.id ] );
			}

            if ( sampleSetId )
            {
                command = "DELETE FROM rank_list_detail" +
                        "  WHERE rank_list_id IN" +
                        "    (SELECT id FROM rank_list" +
                        "      WHERE sample_set_id = ?)";
                sql.execute( command, [ sampleSetId ] );

                command = "DELETE FROM rank_list" +
                        "  WHERE sample_set_id = ?";
                sql.execute( command, [ sampleSetId ] );

                SampleSet sampleSet = SampleSet.get( sampleSetId );
                if ( sampleSet )
                {
                    sampleSet.delete( flush: true );
                }
            }
//!!!sample_set_palx, sample_set_palx_detail
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new ImportException( msg );
        }
    }

//-----------------------------------------------------------------------------
}                                                           //ArrayDataImporter


//*****************************************************************************


class ArrayDataValidator
{                                                          //ArrayDataValidator
//-----------------------------------------------------------------------------

    protected
    void start( )
    {
        m_numProbeRowErrors = 0;
        m_errors = [];
		m_unknownProbes = new HashSet< String >();
		m_missingProbes = new HashSet< String >();
        m_numNegativeSignals = 0;
    }

//=============================================================================

    protected
    boolean checkFieldCount( TextTableInfo tableInfo,
                             String[] fields, int lineNum )
    {
        if ( fields.size() != tableInfo.numColumns )
        {
            String msg = "Line " + lineNum + " has " + fields.size() +
                    " fields. Expecting " + tableInfo.numColumns;
            m_errors.add( msg );
            ++m_numProbeRowErrors;
            return false;
        }
        return true;
    }

//-----------------------------------------------------------------------------

    protected
    void checkProbeId( String probeId )
    {
        boolean probeKnown;
        if ( isProbeIdListedFunc )
        {
            probeKnown = isProbeIdListedFunc( probeId, m_chipProbes );
        }
        else
        {
            probeKnown = m_chipProbes.containsKey( probeId );
        }

        if ( probeKnown == false )
        {
            if ( m_unknownProbes.contains( probeId ) == false )
            {
                m_unknownProbes.add( probeId );
            }
        }
        else  // This marks the particular probe as seen.
        {
            m_chipProbes[ probeId ] = true;
        }
    }

//-----------------------------------------------------------------------------

    protected
    boolean checkDatum( ArrayDataDatum datum, String probeId, int lineNum )
    {
        boolean ok = true;

        if ( datum.signal == null )
        {
            String msg = "No (or bad) signal for " + probeId +
                    " on line " + lineNum;
            if ( requireSignal )
            {
                m_errors.add( msg );
                ok = false;
            }
            else
            {
                m_warnings.add( msg );
            }
        }

        if ( datum.pVal == null )
        {
            if ( requireDetection )
            {
                String msg = "No (or bad) p-value for " + probeId +
                        " on line " + lineNum;
                m_errors.add( msg );
                ok = false;
            }
        }
        else if ( (datum.pVal < 0.0) || (datum.pVal > 1.0) )
        {
            String msg = "P-value out of range for " + probeId +
                    " on line " + lineNum;
            m_errors.add( msg );
            ok = false;
        }

        if ( requireNegatives && (datum.signal < 0.0) )
        {
            ++m_numNegativeSignals;
        }

        return ok;
    }

//=============================================================================

    protected
    void finish( int numSamples, int numProbes )
    {
        List< String > errorsHead = [];
        List< String > warningsHead = [];

		m_chipProbes.each { k, v ->
			if (!v) {
				m_missingProbes.add(k)
			}
		}

        final double unknownProbeIdsThreshold =
                requireKnownProbeId ? 0.0001 : 0.2;
        println "requireKnownProbeId : $requireKnownProbeId   - threshold : $unknownProbeIdsThreshold"

		boolean unknownProbeIdsError = requireExactlyProbes;
		boolean missingProbeIdsError = requireExactlyProbes;
		
		int numUnknownProbeIds = m_unknownProbes.size();
		int numMissingProbeIds = m_missingProbes.size();
		
        double unknownProbeIdsFraction =
                (numProbes > 0) ? numUnknownProbeIds / (double) numProbes : 0.0;
        println "$numUnknownProbeIds / (double) $numProbes = $unknownProbeIdsFraction"
		
        if ( numUnknownProbeIds > 0 )
        {
            if ( unknownProbeIdsFraction >= unknownProbeIdsThreshold )
            {
                println " $unknownProbeIdsFraction >= $unknownProbeIdsThreshold"
                unknownProbeIdsError = true;
            }
            String msg = numUnknownProbeIds + " unknown probe IDs" + 
                    " (" + unknownProbeIdsFraction.round( 3 ) +
                    " of " + numProbes + ")";
            if ( unknownProbeIdsError )
            {
                errorsHead.add( msg );
            }
            else
            {
                warningsHead.add( msg );
            }
        }

		if ( numMissingProbeIds > 0)
		{
			String msg = numMissingProbeIds + " missing probe IDs (of " + m_chipProbes.size() + ")";
			
			if ( missingProbeIdsError )
			{
				errorsHead.add( msg );
			}
			else
			{
				warningsHead.add( msg );
			}
		}
		
        if ( m_numProbeRowErrors > 0 )
        {
            errorsHead.add( m_numProbeRowErrors + " probe rows have errors." );
        }

        if ( requireNegatives )
        {
            double negativeSignalErrorThreshold = 0.0;
            double negativeSignalWarningThreshold = 0.15;
            double numSignals = numSamples * numProbes;
            double negFraction =
                    (numSignals > 0) ? (m_numNegativeSignals / numSignals) :
                    0;
            String msg =  m_numNegativeSignals + " negative signals found (" +
                    negFraction.round( 3 ) + " of signals)";
            if ( negFraction <= negativeSignalErrorThreshold )
            {
                errorsHead.add( msg );
            }
            else if ( negFraction <= negativeSignalWarningThreshold )
            {
                warningsHead.add( msg );
            }
        }

        m_unknownProbes.each { probeId ->
            String msg = "Unknown probe ID: " + probeId;
            if ( unknownProbeIdsError )
            {
                m_errors.add( msg );
            }
            else
            {
                m_warnings.add( msg );
            }
        }

		m_missingProbes.each { probeId ->
			String msg = "Missing probe ID: " + probeId;
			if ( missingProbeIdsError )
			{
				m_errors.add( msg );
			}
			else
			{
				m_warnings.add( msg );
			}
		}

        m_errors.addAll( 0, errorsHead );
        m_warnings.addAll( 0, warningsHead );

        if ( m_errors.size() > 0 )
        {
            throw new ImportException( m_errors );
        }
    }

//=============================================================================

    protected boolean requireKnownProbeId = false;
	protected boolean requireExactlyProbes = false;  // require all probes and only those probes in a chipset.
    protected boolean requireSignal = true;
    protected boolean requireDetection = false;
    protected boolean requireNegatives = false;
    protected Closure isProbeIdListedFunc;

    protected Map< String, Boolean > m_chipProbes;
    protected List< String > m_errors;
    protected List< String > m_warnings;
    protected int m_numProbeRowErrors;
	protected Set< String > m_unknownProbes;
	protected Set< String > m_missingProbes;
    protected int m_numNegativeSignals;

//-----------------------------------------------------------------------------
}                                                          //ArrayDataValidator


//*****************************************************************************
