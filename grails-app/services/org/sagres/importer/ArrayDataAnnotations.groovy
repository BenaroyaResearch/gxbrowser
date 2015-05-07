/*
  ArrayDataAnnotations.groovy

  Classes and functions related to import of annotations of samples and
  sample sets/series/studies.
*/

package org.sagres.importer;

import org.sagres.sampleSet.MongoDataService;
import org.sagres.sampleSet.SampleSetService;
import org.sagres.sampleSet.SampleSet;
import common.ArrayData;


//*****************************************************************************


class ArrayDataSampleAnnotations
{                                                  //ArrayDataSampleAnnotations
//-----------------------------------------------------------------------------

    Integer id;
    String label;
    Map< String, String > annotations = [:];

//-----------------------------------------------------------------------------
}                                                  //ArrayDataSampleAnnotations


//*****************************************************************************


class ArrayDataAnnotations
{                                                        //ArrayDataAnnotations
//-----------------------------------------------------------------------------

    Map< String, List< String > > series = [:];
    List< ArrayDataSampleAnnotations > samples = [];

//=============================================================================

    static
    ArrayDataAnnotations makeFromArrayDataSamples(
        List< ArrayDataSample > arrayDataSamples )
    {
        ArrayDataAnnotations annots = new ArrayDataAnnotations();
        arrayDataSamples.each { sample ->
            ArrayDataSampleAnnotations sampleAnnots =
                    new ArrayDataSampleAnnotations( id: sample.id,
                                                    label: sample.label );
            annots.samples.add( sampleAnnots );
        }
        return annots;
    }

//=============================================================================

    void getIdsFromArrayDataSamples( List< ArrayDataSample > arrayDataSamples )
    {
        samples.each { sample ->
            ArrayDataSample ads = arrayDataSamples.find { ads ->
                ads.label == sample.label;
            }
            sample.id = ads.id;
        }
    }

//-----------------------------------------------------------------------------

    void getIdsFromDb( )
    {
        samples.each { sample ->
            ArrayData arrayData = ArrayData.findByBarcode( sample.label );
            if ( arrayData == null )
            {
                String msg = "No array_data sample found for barcode " +
                        sample.label;
                throw new ImportException( msg );
            }
            sample.id = arrayData.id;
        }
    }

//=============================================================================

    void saveToDb( SampleSet sampleSet,
                   MongoDataService mongoSvce, SampleSetService sampleSetSvce )
    {
        //!!!To do: save series annotations.

        Set< String > keys = new HashSet< String >();
        samples.each { sample ->
            sample.annotations.each { key, value ->
                mongoSvce.updateSample( -1, sample.id, key, value );
                keys.add( key );
            }
        }
        mongoSvce.updateFields( keys );
        sampleSetSvce.populateSampleSetSpreadsheet( sampleSet.id, mongoSvce );
    }

//=============================================================================

    void exportSamplesTable( String outFileSpec,
                         TextTableSeparator separator = TextTableSeparator.CSV )
    {
        PrintWriter writer;
        try
        {
            Set< String > keys = new TreeSet< String >();
            samples.each { sample ->
                keys.addAll( sample.annotations.keySet() );
            }

            writer = new PrintWriter( outFileSpec );

            List< String > rowFields = [ "sample_label" ];
            rowFields.addAll( keys );
            String rowStr = TextTable.joinRow( rowFields, separator, false );
            writer.print( rowStr );

            samples.each { sample ->
                writer.print( "\n" );

                rowFields = [ sample.label ];
                keys.each { key ->
                    if ( sample.annotations.containsKey( key ) )
                        rowFields.add( sample.annotations[ (key) ] );
                    else
                        rowFields.add( "" );
                }
                rowStr = TextTable.joinRow( rowFields, separator, false );
                writer.print( rowStr );
            }
        }
        catch ( FileNotFoundException exc )
        {
            throw new ImportException( "Unable to create " + outFileSpec +
                                       "\n" + exc.message );
        }
        finally
        {
            if ( writer )
            {
                writer.flush( );
                writer.close( );
            }
        }
    }

//-----------------------------------------------------------------------------
}                                                        //ArrayDataAnnotations


//*****************************************************************************
