<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

    <title>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Immune Tolerance Network Module Analysis Toolkit - Module Analysis Tool</g:if>
        <g:else>Systems Immunology Toolkit - Module Analysis Tool</g:else>
    </title>

</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">

    <div class="container">
        <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>
         <h3>
            <a>
                <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Module Analysis Toolkit</g:if>
                <g:else>Systems Immunology Toolkit</g:else>
            </a>
        </h3>


        <ul class="nav secondary-nav">


      </ul>

    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->



<div class="container">
    <div class="page-header">
        <h2>Module Analysis Tool</h2>
    </div>




    <style type="text/css">
	.faq-answer {
	display:none;}
     .container {color:#666666;}

    </style>
    <script type="text/javascript">
    $(document).ready(function() {
        $("a[name^='faq-']").each(function() {
            $(this).click(function() {
                if( $("#" + this.name).is(':hidden') ) {
                    $("#" + this.name).fadeIn('fast');
                } else {
                    $("#" + this.name).fadeOut('fast');
                }
                return false;
            });
        });
    });
    </script>

    <div class="itn-header">

        <a href="landing.gsp">&laquo; Back to landing page</a>

    </div>

     <p>
          <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
            <img src="images/cap-mat.png" alt="Module Analysis Tool" style="float: left; padding: 0 20px 20px 0;" />
          </g:if>
		  <g:else>
            <img src="images/cap-mat-bri.png" alt="Module Analysis Tool" style="float: left; padding: 0 20px 20px 0;" />
		  </g:else>

         The MAT will analyze your data to find pre-defined groupings of coodinately expressed genes (modules), will help you look at
         the functional annotation of those modules, and will let you view a molecular fingerprint of gene expression for each individual
         sample in your dataset. This tool compares any two groups;
         it is built on the idea of comparing patients to healthy controls for any disease, but can also be used to compare different treatment groups or timepoints.</p>
         <p><a href="#faq">Go to the FAQ at the bottom of the page</a>.</p>

    <p><a href="${createLink(controller: 'mat')}" class="btn primary">Go to MAT &raquo;</a></p>
    <p style="clear:both;"><a href="moduleDesc.gsp">Go here for module descriptions</a></p>

    <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
        <img src="images/annot-mat.png" />
    </g:if>
	<g:else>
        <img src="images/annot-mat-bri.png" alt="Module Analysis Tool"/>
	</g:else>

    %{--<ol class="toolfaq">--}%
        %{--<li><b>Plot Type</b> - Use these buttons to switch between analysis styles – look at each individual sample separately to come up with a molecular fingerprint for each patient, or compare the groups as a whole. When comparing groups (patients vs controls, for example), you can look at plots analyzed by the % of probes in the module that differ between groups or using a Gene Set Analysis (<a href="http://www-stat.stanford.edu/~tibs/GSA/" target="_blank">reference on GSA</a>). </li>--}%

         %{--<li><b>Display Options</b> - This section allows you to change the number and style of modules displayed. You can switch from the standard 62 module plot to one which includes all 260 modules, or use a pie chart instead of color gradation to show change in expression.</li>--}%
         %{--<li><b>Annotation Key</b> - Here you can decide whether/how to view functional annotations of each module. For a downloadable key (great for presentations) you can choose "full," or leave it in default settings and scroll over any colored module box to get its annotation.</li>--}%
         %{--<li><b>Graph Options</b> - Use these buttons to modify your view of the data, or to download what you’re seeing for presentation or publication. You can also download the data with the csv button, or resize the image if you have a lot of samples or want to zoom in.</li>--}%
    %{--</ol>--}%

   <h2 style="margin:10px 0 10px 0; border-bottom:1px solid #ddd; "><a name="faq"></a>MAT FAQ:</h2>
      <a class="faq-question" href="#" name="faq-17">What are the supported file types? </a>
   <div class="faq-answer" id="faq-17">	A list of the supported file types can be found <a href="supported.gsp">here</a> .<br /><br /></div>

     <a class="faq-question" href="#" name="faq-1">What datasets were used to build the tool? </a>
    <div class="faq-answer" id="faq-1">The tool was built using 9 immunologically different datasets. These were 2 bacterial sepsis datasets
    (<em>Burkholderia mallei</em>), 1 acute HIV dataset, 1 liver transplant dataset, 2 adult systemic lupus erythematosus datasets, 1 systemic onset
    juvenile idiopathic arthritis dataset, 1 tuberculosis, and 1 B-cell deficiency.<br />
        <a href="moduleDesc.gsp">View  a more complete description of the modules here.</a><br /><br /></div>

     <a class="faq-question" href="#" name="faq-2">How were the modules constructed?</a>
    <div class="faq-answer" id="faq-2">The following algorithm was used for module construction:
    <ol>
    <li> Within each dataset, the data were normalized and clustered using k-means to determine which probes showed similar patterns in that dataset.</li>
    <li>Then, using all datasets, we created a co-cluster matrix, which counted the number of times each pair of probes clustered together in
    all of the datasets studied.  For instance, probes ILMN_1693428 (TNNC2) and ILMN_1744912 (CTTN) clustered together in all 9 data sets so
    these 2 probes would have a count of 9 in the co-cluster matrix.  The co-cluster matrix will be a NxN lower triangular matrix, where
    N is the number of probes.       </li>
    <li>Using the co-cluster matrix/graph, we found probes that completely co-clustered across all datasets.  Some thresholds were applied to deal
    with random errors in the data.  For instance, 4 probes may have completely co-clustered across the 9 datasets and a 5th probe may have
    co-clustered with 3 of those probes across the 9 data sets and may co-cluster with the 4th probe in only 8 data sets.
    This is probably just an error that the 5th and 4th probe did not show co-clustering over all 9 data sets and thus, these 5
    probes would be considered a module (gene set) that co-clustered completely over all 9 data sets.  We found all sets of probes that
    co-clustered completely with thresholds over all 9 data sets and considered these modules round one of modules.</li>
    <li>We repeated step 3 decreasing the number of datasets that the probes must cluster over from 8 datasets (round two of modules) to 1
    dataset (round nine of modules). </li>
    <li>Modules within the same round, indicating that the probes clustered over the same number of datasets, were ordered from largest to smallest so
    module 5.1 has more probes than module 5.2, which has more probes than 5.3 , and so on. </li>
   </ol>
    </div>

     <a class="faq-question" href="#" name="faq-4">How does this tool (and the images it makes) differ from the Chaussabel Immunity 2008 paper?</a>
    <div class="faq-answer" id="faq-4"><a href="http://www.ncbi.nlm.nih.gov/pubmed/18631455" target="_blank">The Chaussabel Immunity 2008 paper</a> described modules that were built from different datasets, and which used PBMC samples and an Affymetrix chip.
    Furthermore, the paper was limited to a specific set of examples, whereas MAT can be run dynamically on new data.   <br /><br /></div>

     <a class="faq-question" href="#" name="faq-5">Who were the healthy controls for the original datasets?</a>
    <div class="faq-answer" id="faq-5">Healthy controls were age, gender, and race matched to each disease.   <br /><br /></div>

     <a class="faq-question" href="#" name="faq-6">Are the modules pre-defined, or can we change them ourselves?</a>
    <div class="faq-answer" id="faq-6">The modules are predefined and cannot be changed.  Any dataset can be run against the modules, but
    the probes assigned to each module won’t change.  <br /><br /></div>

     <a class="faq-question" href="#" name="faq-7">Will you ever change the modules (i.e. will I be able to repeat an analysis and get the same results next year?)</a>
    <div class="faq-answer" id="faq-7">This tool will not change unless you modify some settings or change the data yourself. However, there will be future generations of modules modulestools that
    run alongside this one – but they’ll always be separate, and you will be able to consistently get the same results from the same module generation.<br /><br /></div>

     <a class="faq-question" href="#" name="faq-8">How does MAT compare to gene ontology tools like DAVID or IPA? </a>
    <div class="faq-answer" id="faq-8">MAT allows you to study transcript expression by using data-driven gene sets.  DAVID and IPA are knowledge-driven and do not expect similar expression
        patterns like MAT does.  For instance a pathway studied in IPA or DAVID may include some genes that are up-regulated and some genes that are down-regulated,
        while the modules in MAT expect the probes in a gene set to behave similarly.  MAT, DAVID and IPA all allow functional annotation of your data, but
        the methods behind that annotation are different.  Some of the modules in MAT were functionally annotated using tools such as DAVID and IPA. <br /><br /></div>

    <a class="faq-question" href="#" name="faq-9">Do we always use LIMMA to generate the difference plots? </a>
    <div class="faq-answer" id="faq-9">Yes, the group comparison (case vs. control) is performed using LIMMA to test for statistically significant
    differences between the groups.  We investigated other options, such as SAM, but felt that LIMMA would be more appropriate if the sample sizes were small. <br /><br /></div>

     <a class="faq-question" href="#" name="faq-10">Does the MAT work for RNA-seq data? </a>

    <div class="faq-answer" id="faq-10">Support for RNA-seq data is planned, but not available in this release. Please check the version notes on
    later versions to see if or RNA-seq data are supported.  <br /><br /></div>

    <a class="faq-question" href="#" name="faq-16">What were the criteria for dataset inclusion? </a>
   <div class="faq-answer" id="faq-16">	To insure there were four types of perturbations to the immune system: autoimmune, infectious disease,
     immune deficiency, and tolerance.<br /><br /></div>


     <a class="faq-question" href="#" name="faq-11">What about responder/nonresponder or before/after treatment analyses? </a>
    <div class="faq-answer" id="faq-11">While the tool wasn't built for these types of analyses, it will  run them. Just choose one group to
    be “cases” and one to be "controls" (you can easily choose custom labels for the case and control groups) - and you’ll be able to see which modules go
    up/down in the case group.    <br /><br /></div>

    <div class="itn-footer">
        <a href="landing.gsp">Landing page</a> |  <a href="gxbmore.gsp">About the Gene Expression Browser</a>
        | About the Module Analysis Tool
        <g:if test="${grailsApplication.config.tools.available.contains('wizard')}"> | <a href="matwizmore.gsp">About the Module Analysis Wizard</a></g:if>
    </div>


   %{--<div id="footer">--}%
       %{--&copy; copyright info and other things.--}%

   %{--</div>--}%
</div>

  </body>
</html>