package common.chipInfo

class ChipType
{
	static constraints = {
		name(blank: false, maxSize: 32)
		chipData()
        technology(nullable: false)
		importDirectoryName()
		symbolColumn(nullable: true)
		refSeqColumn(nullable: true)
		accessionNumberColumn(nullable: true)
		sequenceColumn(nullable: true)
		synonymColumn(nullable: true)
    entrezGeneColumn(nullable: true)
		moduleVersionId(nullable: true)
		moduleGen3MappingId(nullable: true)

		active(blank: false, range:-1..1)
	}

	static mapping =
	{
		sort id: 'desc'
	}

	String name
	int active = 1
	ChipData chipData
    Technology technology

	/** name of the directory where we expect to find the files to import: base upload dir/ChipType.importDirectoryName/file */
	String importDirectoryName
	/** name of the database table there the valid probes for this chip type are found */
	String probeListTable
	/** the column in the importProbeList table to use for
	 the probe verification, these will be used to dynamically generate the query to get the valid probe list */
	String probeListColumn

	/*
	the column name references should be provided so that we can use the information in the UI, with the exception of
	symbolColumn they are optional. If symbol column is not provided the system will still work, but genes will not
	 be queryable by symbol in the gxb
	 */

	/** column name in the probeListTable for gene symbol */
	String symbolColumn
	/** column name in the probeListTable for gene's refSeq */
	String refSeqColumn
	/** column name in the probeListTable for gene's sequence */
	String sequenceColumn
	/** column name in the probeListTable for gene's accession number */
	String accessionNumberColumn
	/** column name in the probeListTable for synonyms of the current probe/gene */
	String synonymColumn
  /** entrez gene id in the probeListTable **/
  String entrezGeneColumn

	/** Id for version file**/
	Long moduleVersionId =-1

		/** Id for Gen3 version file**/
	Long moduleGen3MappingId =-1

	String toString()
	{
		return name
	}

}
