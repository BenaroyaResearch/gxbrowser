/*
  NcbiGeneDataImporter.groovy

  Routines for importing NCBI gene data files.
*/

package org.sagres.importer;

import groovy.sql.Sql;


//*****************************************************************************


class NcbiGeneDataTableInfo
extends TextTableInfo
{                                                       //NcbiGeneDataTableInfo
//-----------------------------------------------------------------------------

    String dbTableName;
    int taxIdField; //taxonomy (species) id
    List< String > columnNames;
    List< Integer > skipFields;
    List< Integer > hyphenNullFields;
    List< Integer > questionNullFields;
    List< Integer > intFields;
    List< Integer > dateTimeFields;
    
//-----------------------------------------------------------------------------
}                                                       //NcbiGeneDataTableInfo


//*****************************************************************************


class NcbiGeneDataImporter
{                                                        //NcbiGeneDataImporter
//-----------------------------------------------------------------------------

    static
    NcbiGeneDataTableInfo getTableInfo( String fileName, String fileSpec )
    {
        NcbiGeneDataTableInfo tableInfo = ncbiGeneFileTypes[ fileName ];
        if ( tableInfo == null )
        {
            String msg = "No table info for " + fileName;
            throw new ImportException( msg );
        }
        tableInfo.separator = TextTableSeparator.TAB;
        tableInfo.firstRow = 1;
        tableInfo.numColumns =
                tableInfo.columnNames.size() + tableInfo.skipFields.size();
        tableInfo.numRows = -1; //account for header
        new File( fileSpec ).eachLine { line, lineNum ->
            ++tableInfo.numRows;
        }

        return tableInfo;
    }

//=============================================================================

    static
    List parseRow( String[] fieldStrings, NcbiGeneDataTableInfo tableInfo,
                   int lineNum = 0, List< String > errors = null )
    {
        List fieldVals = [];
        fieldStrings.eachWithIndex { str, i ->
            if ( tableInfo.skipFields.contains( i ) == false )
            {
                if (  ((str == '-') &&
                       tableInfo.hyphenNullFields.contains( i )) ||
                      ((str == '?') &&
                       tableInfo.questionNullFields.contains( i )) )
                {
                    fieldVals.add( null );
                }
                else if ( tableInfo.intFields.contains( i ) )
                {
                    Integer val = Importer.parseInteger( str );
                    if ( (val == null) && errors )
                    {
                        String msg = "Invalid integer on line " + lineNum +
                                ": " + str;
                        errors.add( msg );
                    }
                    fieldVals.add( val );
                }
                else if ( tableInfo.dateTimeFields.contains( i ) )
                {   
                    if ( errors )
                    {   //Solely for validation; we store the raw string
                        try
                        {
                            Date.parse( "yyyy-MM-dd HH:mm", str );
                        }
                        catch ( java.text.ParseException exc )
                        {
                            String msg = "Invalid date on line " + lineNum +
                                    ": " + str;
                            errors.add( msg );
                        }
                    }
                    fieldVals.add( str );
                }
                else
                {
                    fieldVals.add( str );
                }
            }
        }
    }

//=============================================================================

    static
    void loadDataToDb( String bulkLoadSpec, NcbiGeneDataTableInfo tableInfo,
                       Sql sql )
    {
        List< String > fields = [ "version" ];
        fields.addAll( tableInfo.columnNames );
        Importer.deleteAllFromDb( tableInfo.dbTableName, sql );
        Importer.loadDataToDb( bulkLoadSpec, tableInfo.dbTableName,
                               fields, sql );
    }
    
//=============================================================================

    private
    static
    Map< String, NcbiGeneDataTableInfo > ncbiGeneFileTypes =
            [
                "gene2accession": new NcbiGeneDataTableInfo(
                    dbTableName: "gene_accession",
                    columnNames: [ "tax_id", "gene_id", "status",
                                   "rna_nucleotide_accession",
                                   "rna_nucleotide_gi",
                                   "protein_accession", "protein_gi",
                                   "genomic_nucleotide_accession",
                                   "genomic_nucleotide_gi",
                                   "start_position", "end_position",
                                   "assembly" ],
                    taxIdField: 0,
                    skipFields: [],
                    hyphenNullFields: [ 2, 3, 4, 5, 6, 7, 8, 9, 10, 12 ],
                    questionNullFields: [ 11 ],
                    intFields: [ 0, 1, 9, 10 ],
                    dateTimeFields: [] ),
                "gene_group": new NcbiGeneDataTableInfo(
                    dbTableName: "gene_group",
                    columnNames: [ "tax_id", "gene_id", "relationship",
                                   "other_tax_id", "other_gene_id" ],
                    taxIdField: 0,
                    skipFields: [],
                    hyphenNullFields: [],
                    questionNullFields: [],
                    intFields: [ 0, 1, 3, 4 ],
                    dateTimeFields: [] ),
                "gene2pubmed": new NcbiGeneDataTableInfo(
                    dbTableName: "gene_pubmed",
                    columnNames: [ "tax_id", "gene_id", "pubmed_id" ],
                    taxIdField: 0,
                    skipFields: [],
                    hyphenNullFields: [],
                    questionNullFields: [],
                    intFields: [ 0, 1, 2 ],
                    dateTimeFields: [] ),
                "gene2refseq": new NcbiGeneDataTableInfo(
                    dbTableName: "gene_refseq",
                    columnNames: [ "tax_id", "gene_id", "status",
                                   "rna_nucleotide_accession",
                                   "rna_nucleotide_gi",
                                   "protein_accession", "protein_gi",
                                   "genomic_nucleotide_accession",
                                   "genomic_nucleotide_gi",
                                   "start_position", "end_position",
                                   "assembly" ],
                    taxIdField: 0,
                    skipFields: [],
                    hyphenNullFields: [ 2, 3, 4, 5, 6, 7, 8, 9, 10, 12 ],
                    questionNullFields: [ 11 ],
                    intFields: [ 0, 1, 9, 10 ],
                    dateTimeFields: [] ),
                "generifs_basic": new NcbiGeneDataTableInfo(
                    dbTableName: "gene_rif",
                    columnNames: [ "tax_id", "gene_id", "pubmed_id",
                                   "last_update" ],
                    taxIdField: 0,
                    skipFields: [ 4 ],
                    hyphenNullFields: [],
                    questionNullFields: [],
                    intFields: [ 0, 1, 2 ],
                    dateTimeFields: [ 3 ] ),
                "gene2unigene": new NcbiGeneDataTableInfo(
                    dbTableName: "gene_unigene",
                    columnNames: [ "gene_id", "unigene_cluster" ],
                    taxIdField: -1,
                    skipFields: [],
                    hyphenNullFields: [],
                    questionNullFields: [],
                    intFields: [ 0 ],
                    dateTimeFields: [] ),
                "hiv_interactions": new NcbiGeneDataTableInfo(
                    dbTableName: "hiv_interactions",
                    columnNames: [ "tax_id1", "gene_id1",
                                   "product_accession1", "product_name1",
                                   "interaction",
                                   "tax_id2", "gene_id2",
                                   "product_accession2", "product_name2",
                                   "pubmed_list", "last_update", "gene_rif" ],
                    taxIdField: 5,
                    skipFields: [],
                    hyphenNullFields: [],
                    questionNullFields: [],
                    intFields: [ 0, 1, 5, 6 ],
                    dateTimeFields: [ 10 ] )
            ];


//-----------------------------------------------------------------------------
}                                                        //NcbiGeneDataImporter


//*****************************************************************************


class NcbiGeneDataValidator
{                                                       //NcbiGeneDataValidator
//-----------------------------------------------------------------------------

    void init( NcbiGeneDataTableInfo tableInfo, List< String > warnings )
    {
        m_tableInfo = tableInfo;
        m_warnings = warnings;
    }

//=============================================================================

    void beforeTable( )
    {
        m_errors = [];
    }

//=============================================================================

    void validateRow( String[] fields, int lineNum )
    {
        if ( fields.size() != m_tableInfo.numColumns )
        {
            String msg = "Line " + lineNum + " has " + fields.size() +
                    " fields. Expecting " + m_tableInfo.numColumns;
            m_errors.add( msg );
        }
        else
        {
            NcbiGeneDataImporter.parseRow( fields, m_tableInfo,
                                           lineNum, m_errors );
        }
    }

//=============================================================================

    void afterTable( )
    {
        if ( m_errors.size() > 0 )
        {
            throw new ImportException( m_errors );
        }
    }

//=============================================================================

    NcbiGeneDataTableInfo m_tableInfo;
    List< String > m_errors;
    List< String > m_warnings;

//-----------------------------------------------------------------------------
}                                                       //NcbiGeneDataValidator


//*****************************************************************************


class NcbiGeneDataBulkLoadBuilder
extends FileBuilderFromTextTable
{                                                         //NcbiBulkLoadBuilder
//-----------------------------------------------------------------------------

    NcbiGeneDataBulkLoadBuilder( String bulkLoadSpec,
                                 NcbiGeneDataTableInfo tableInfo )
    {
        super( bulkLoadSpec );
        m_tableInfo = tableInfo;
    }

//=============================================================================

    @Override
    void processRow( String[] fields, int lineNum )
    {
        List fieldVals = NcbiGeneDataImporter.parseRow( fields, m_tableInfo );
        fieldVals.add( 0, 0 ); //push version
        Importer.writeBulkLoadLine( m_writer, fieldVals );
    }
    
//=============================================================================

    NcbiGeneDataTableInfo m_tableInfo;
    
//-----------------------------------------------------------------------------
}                                                         //NcbiBulkLoadBuilder


//*****************************************************************************
