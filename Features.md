## GXB Feature List

### Chipsets and Loading:

1. Can load data different chip types; currently, 25 different chips, including, ABI, Agilent, Affymetrix, Illumina, Sentrix
   'Genomic' chips (Ensembl, or UCSC genomic builds), for RNA-Seq applications
2. Extensible, can add additional chip types
3. Automated (O/N), Immediate, and directory-watch loading.
4. GEO compatibility: cross referencing from chip type to GEO platform: currently 35 different GEO platforms supported.
5. Can accept both GEO .soft format and matrix formats.
6. Quality Control - warn or reject sample set import based on missing or unknown probes.
7. Automatic data quantile normalization upon import.
8. Accepts raw, bg subtracted, or normalized expression data, also accepts 'normal space' and log2 transformed data
9. Data successfully loaded may be exported as a tab separated file.
10. Data successfully loaded may be re-exported and shared with another instance of the application.

### Sample Set Annotation Tool:

1. Study annotation fields for: Purpose, Hypothesis, Design, Variables, Controls, Methods, Addl. Info
2. General Sample annotation fields for: Disease, Biological Source, Species.
3. Protocol annotation fields for: Treatment, Growth, Extraction, Storage
4. Contact information fields: Analyst, Investigator, Primary Contact, Institution
5. Cross reference fields: Data Source, GEO Entry, Pubmed reference; extensible can add additional source databases.
6. Sample sets may be hidden from general view (for instance when in preparation), or configured for specific user authorization.

### Sample Annotation:

1. Any categorical (e.g race, gender, disease state) or numerical (e.g. clinical, laboratory or experimental values) data can be
attached to samples within the set using an Excel, or comma-separated values (CSV) spreadsheet.  Annotation data is referenced to the
expression data via the Sample ID (sample barcode, GEO GSM identifier is also be used)
2. Sample set annotation data may be viewed and edited after loading.

### Group Sets, and Sample Grouping:

1. Can specify independent 'group sets': data can be organized conceptually by timepoint, treatment, disease state or other variables.
Group sets are broken up or split into groups based on a user selected annotation field. For example, a user may specify
a 'Longitudnal' group set (by name), and organize the samples into groups based on a supplied 'timepoint' field in the sample annotation data.

### Sample Set listing:

1. With multiple datasets are loaded, the listing page may be filtered by: Principal Investigator, Platform, Species, Disease,
Sample Source, Institution or Signficant Genes.
2. The listing may also be sorted by Name, Platform, Species, Disease, Sample Source, and Sample Count.
3. The Search box may be used to search for sample sets by name, or part of a name.

### Gene Expression Browser:

#### Study and general Sample information (upper display area):
1. Annotation data connected to samples is displayed as the mouse rolls over the bar chart, or dot on boxplot.
2. Annotation data may be grouped into user defined tabs such as 'Demographics', 'Assay Results', or 'Flow data'
3. Basic Study and Gene information is also available in the upper informational display.

#### Expression data display and layout (lower display area):
1. Expression data may be viewed as a box plot (quantiles + extents), or bar chart.
2. Group display order, and group color may be set by the user.
3. Samples within a group may be sorted based on a selected continuous or categorical value.
4. Expression data may be overlayed with continuous and categorical annotation data.
5. Numerous font type and size options are available for the titles, and axes,  Borders and axes may be hidden from view.

#### Rank ordered display, and gene lists (left display area):

1. Group comparisons may be calculated and used to rank order the genes for selection in display.
2. Genes may be filtered to only include those on a given list (area of biology, structural or functional types, etc.)
3. The user may search for a specific gene in the search box, and select either a part of the gene symbol (e.g. 'IL', referring to
all named interleukins), or a unique symbol (e.g. 'IL21').

#### User Notations:
1. A user may add informational notes associated with the sample set, group set, or gene level.  Notes will then be visible based on their
association with a sample or group, or gene, and all notes may be reviewed by the users that created them.

#### Data View Capture:
1. Particular views of the data (selected gene, specific sort order, certain overlay, etc), can be memorized, and sent via e-mail as a
link, or used later in a publication or website.
2. Expression data and overlays may also be rendered as a high quality image (.svg, or .png) for publication.

#### Cross project viewing:
1. With a paricular gene selected, the user can choose to 'pivot' on the available data and select from other sample sets that have
expression values for that gene.  The left hand selector in the cross project view presents a list of sample sets rank ordered by
the change in expression for the gene of interest.  (The upper and lower informational displays remain the same)
