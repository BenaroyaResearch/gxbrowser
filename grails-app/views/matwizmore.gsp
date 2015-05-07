<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

  <title>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Immune Tolerance Network Module Analysis Toolkit - Module Analysis Wizard</g:if>
        <g:else>Systems Immunology Toolkit - Module Analysis Wizard</g:else>
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
        <h2>Module Analysis Wizard</h2>
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
         <img src="images/cap-matwizard.png" alt="Module Analysis Wizard" style="float: left; padding: 0 20px 20px 0;" /> The Module Analysis Wizard will take you through the uploading of data into the Module Analysis Tool step-by-step, and notify you when your analyses are finished.
         <p>You can input your microarray data and any clinical or other annotation information you may have using excel spreadsheets.</p>
    <p><a href="${createLink(controller: 'MATWizard')}" class="btn primary">Start Module Analysis Wizard &raquo;</a></p>
        <img src="images/annot-matwiz.png" />
    <hr />
    <img src="images/annot-matwiz2.png" />
    %{--<p><b>1. What file do I need for the Signal Data?</b><br />--}%

        %{--A number of different filetypes are supported. You can, for example, use standard output from Affymetrix's Expression Console after RMA normalization and other processing. If so, choose "Affymetrix" as the file type and choose your chip name from the dropdown.--}%
%{--</p>--}%
    %{--<p><b>2. Why would I want to click "Annotation Options"?</b><br />--}%
    %{--If you already have data groupings defined, you can use your annotation file (for example, via file upload). The first column should contain sample IDs that match the sample--}%
    %{--IDs in the signal data file. Following columns can contain classifications of your choice. The file should contain a header with unique names of each column.--}%
    %{--If you don't have an annotation file, you may also choose to manually define groups later in the Wizard.</p>--}%
    %{--<ol class="toolfaq">--}%
        %{--<li><b>Data Uploaded</b> - Here, you can browse to find your microarray data, and get any information you need on how the data should be arranged.</li>--}%
         %{--<li><b>File type Choices: </b> - You can upload GEO data directly or you can upload your own microarray data as a spreadsheet – just choose here.</li>--}%

         %{--<li><b>Annotation Options</b> - After clicking on Annotation Options, you can upload a spreadsheet with annotation information so that you'll--}%
         %{--be better able to analyze your data. </li>--}%
    %{--</ol>--}%

   <h2 style="margin:10px 0 10px 0; border-bottom:1px solid #ddd; "><a name="faq"></a>"Module Analysis Wizard FAQ:</h2>

    <a class="faq-question" href="#" name="faq-20">What are the module parameters I can set, and how will they affect my analysis?</a>
          <div class="faq-answer" id="faq-20">

   <ul class="faq-list">
   <li><h5>Method:</h5>
   Choose a way to select probes with values that are outstanding compared to the controls:
   <ul>
   <li>Using <b>fold change</b>, individual expression value is divided by the mean of control values. This yields a relative change compared to the mean.</li>
   <li>Using <b>Z-score</b>, (individual expression value - mean of control values) / (standard deviation of control values).</li>
   </ul>
   </li>
   <li><h5>Thresholds:</h5>
   <b>Fold Change and Z-score Thresholds:</b>

   Select a minimum absolute value for an individual's probe to be considered up- or down-regulated compared to the average of the controls.
              <ul>
   <li>For the <b>fold change threshold</b>, it is typical to select values greater than one. Larger thresholds will select for genes with larger changes.</li>
   <li>The <b>Z-score threshold</b> is expected to be larger than 0. Larger values will select for genes with larger changes. Please note that thresholding is bases
   on the absolute value of a probe's Z-score.</li>

   <br />The fold change and z-score thresholds are not used in the group comparison of case vs. control. Instead, probes are chosen if they pass a p-value threshold.
   <li><b>Difference threshold:</b> All probes must first pass a minimum difference threshold, then are subject to further filtering by fold change or z-score, depending
       on user selection. Difference is calculated as the individual's probe value minus the mean of the controls' probe values. The difference threshold can be
       any positive value, larger values select for larger differences. The default threshold for difference is 100 </li></ul>
   </li>
       <li><h5>Multiple testing correction:</h5>
   When performing a large number of statistical tests, such as testing many probes to see if there is a difference between case and control, by chance some of them will appear statistically significant even though they are not. Multiple testing correction can be performed to alleviate this problem. For example, when using a p-value threshold of 0.05, you expect 5% of the probes will be significant based on chance alone. We use the Benjamini and Hochberg false-discovery rate method (Benjamini and Hochberg, J.R. Statist. Soc. B., 1995.) for multiple testing correction. This multiple testing correction is only used for the group comparison (since we can't do any statistics on the individual values).
   </li>

    <li><h5>False discovery rate: </h5>
   Cutoff for considering a probe to be statistically significant after FDR correction has been applied (as implemented in the R package LIMMA (G.K. Smyth. Statistical Applications in Genetics and Molecular Biology, 2004.). Smaller values select for probes with stronger differences between case and control.
   </li>


        </ul>

              <br /><br /></div>



    <div class="itn-footer">
         <a href="landing.gsp">Landing page</a> |  <a href="gxbmore.gsp">About the Gene Expression Browser</a>
         <g:if test="${grailsApplication.config.tools.available.contains('mat')}"> |   <a href="matmore.gsp">About the Module Analysis Tool</a></g:if>
         | About the Module Analysis Wizard
    </div>


   %{--<div id="footer">--}%
       %{--&copy; copyright info and other things.--}%

   %{--</div>--}%
</div>

  </body>
</html>