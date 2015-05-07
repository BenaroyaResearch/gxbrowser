<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

    <title>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Immune Tolerance Network Module Analysis Toolkit - FAQ</g:if>
        <g:else>
            Systems Immunology Toolkit - FAQ
        </g:else>
    </title>

</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">
	%{--<div class="topbar-inner bri-fill">--}%
    <div class="container">
           <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>
          <h3>
            <a>
                <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Module Analysis Toolkit</g:if>
                <g:else>
                    Systems Immunology Toolkit
                </g:else>
            </a>
        </h3>


        <ul class="nav secondary-nav">
        %{--<sec:ifNotLoggedIn>--}%
          %{--<li class="dropdown login-dropdown">--}%
            %{--<a class="faq-question" href="#" class="dropdown-toggle" onclick="showLoginForm();">Login</a>--}%
            %{--<div class="dropdown-menu itn-dropdown login-dropdown">--}%
              %{--<div class="login-content">--}%
                %{--<form action="${request.contextPath}/j_spring_security_check" method="post">--}%
                %{--<label>Username:</label><br/>--}%
                %{--<input type="text" name="j_username" id="username"/>--}%
                %{--<label>Password: </label><br/>--}%
                %{--<input type="password" name="j_password" id="password" class="login-pw"/>--}%
                %{--<g:hiddenField name="${SpringSecurityUtils.securityConfig.successHandler.targetUrlParameter}" value="${request.requestURL}"/>--}%
                %{--<g:link controller="secUser" action="forgotPassword" class="btn small forgot-password">Forgot password</g:link>--}%
                %{--<button class="btn primary login-button" type="submit">Login</button>--}%
                %{--</form>--}%
              %{--</div>--}%
            %{--</div>--}%
          %{--</li>--}%
            %{--<li><g:link controller="login" action="auth" params="['lastVisitedPage':request.requestURL]">Login</g:link></li>--}%
        %{--</sec:ifNotLoggedIn>--}%

      </ul>

    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->

<div class="container">
    <div class="page-header">
        <h2>Frequently Asked Questions</h2>
    </div>

    <style type="text/css">
	.faq-answer {
	display:none;}

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



    <h2 style="margin:10px 0 10px 0; border-bottom:1px solid #ddd; ">GXB FAQ:</h2>

   	<a class="faq-question" href="#" name="faq-14">How can I change rank list? </a>
    <div class="faq-answer" id="faq-14">There are options for rank lists in the panel called "Rank List" just above the main expression display.<br /><br /></div>

   	<a class="faq-question" href="#" name="faq-12">Can we get a p-value instead of a fold change with our rank lists?</a>
    <div class="faq-answer" id="faq-12">You can’t rank by p-value in the current version of the GXB. However, you can rank by fold change, difference value, or both.<br /><br /></div>

    <a class="faq-question" href="#" name="faq-13">The graph is rather small, what screen sizes do you support and how can I view the graph in more detail? </a>
    <div class="faq-answer" id="faq-13">The GXB is optimized for screens 1024px wide or larger; it will still be viewable on smaller screens, but you will have less of an overview.
    You have the option of hiding either the Gene List panel, or the information tabs, or both in order to let the graph take up a larger
    percentage of the screen.<br /><br /></div>

 
	<a class="faq-question" href="#" name="faq-15">Where can I find a key for the overlay legend?</a>
    <div class="faq-answer" id="faq-15">  When you have selected at least one overlay option, you may click the Overlay Legend Button; a key to the overlays will
        become visible. You can also choose to download the key as an image.
        <br /><br /></div>
        
	<g:if test="${grailsApplication.config.tools.available.contains('mat')}">
    <h2 style="margin:10px 0 10px 0; border-bottom:1px solid #ddd; ">MAT FAQ:</h2>

    <a class="faq-question" href="#" name="faq-17">What are the supported file types? </a>
   	<div class="faq-answer" id="faq-17">	A list of the supported file types can be found <a href="supported.gsp">here</a> .<br /><br /></div>

     <a class="faq-question" href="#" name="faq-1">What datasets were used to build the tool? </a>
     <div class="faq-answer" id="faq-1">The tool was built using 9 immunologically different datasets. These were 2 bacterial sepsis datasets
     (<em>Burkholderia mallei</em>), 1 acute HIV dataset, 1 liver transplant dataset, 2 adult systemic lupus erythematosus datasets, 1 systemic onset
     juvenile idiopathic arthritis dataset, 1 tuberculosis dataset, and 1 B-cell deficiency dataset.<br />
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
    co-clustered completely with thresholds applied over all 9 data sets and considered these modules round one of modules.</li>
    <li>We repeated step 3 decreasing the number of datasets that the probes must cluster over from 8 datasets (round two of modules) to 1
    dataset (round nine of modules). </li>
    <li>Modules within the same round, indicating that the probes clustered over the same number of datasets, were ordered from largest to smallest so
    module 5.1 has more probes than module 5.2, which has more probes than 5.3 , and so on.  </li>
</ol>
    </div>

     <a class="faq-question" href="#" name="faq-4">How does this tool (and the images it makes) differ from the Chaussabel Immunity 2008 paper?</a>
    <div class="faq-answer" id="faq-4"><a href="http://www.ncbi.nlm.nih.gov/pubmed/18631455" target="_blank">The Chaussabel Immunity 2008 paper</a> described modules
    that were built from different datasets, and which used PBMC samples and an Affymetrix chip.
    Furthermore, the paper was limited to a specific set of examples, whereas  MAT  can be run dynamically on new data.   <br /><br /></div>

     <a class="faq-question" href="#" name="faq-5">Who were the healthy controls for the original datasets?</a>
    <div class="faq-answer" id="faq-5">Healthy controls were age, gender, and race matched to each disease.   <br /><br /></div>

     <a class="faq-question" href="#" name="faq-6">Are the modules pre-defined, or can we change them ourselves?</a>
    <div class="faq-answer" id="faq-6">The modules are predefined and cannot be changed.  Any dataset can be run against the modules, but
    the probes assigned to each module won't change.  <br /><br /></div>

     <a class="faq-question" href="#" name="faq-7">Will you ever change the modules (i.e. will I be able to repeat an analysis and get the same results next year?)</a>
    <div class="faq-answer" id="faq-7">This tool will not change unless you modify some settings or change the data yourself. However, there will be future
    generations of modules that run alongside this one – but they'll always be separate, and you will be able to consistently get the same results from the
    same module generation.<br /><br /></div>

     <a class="faq-question" href="#" name="faq-8">How does MAT compare to gene ontology tools like DAVID or IPA? </a>
    <div class="faq-answer" id="faq-8">
        MAT allows you to study transcript expression by using data-driven gene sets.  DAVID and IPA are knowledge-driven and do not expect similar expression
        patterns like MAT does.  For instance a pathway studied in IPA or DAVID may include some genes that are up-regulated and some genes that are down-regulated,
        while the modules in MAT expect the probes in a gene set to behave similarly.  MAT, DAVID and IPA all allow functional annotation of your data, but
        the methods behind that annotation are different.  Some of the modules in MAT were functionally annotated using tools such as DAVID and IPA.
        <br /><br /></div>

    <a class="faq-question" href="#" name="faq-9">Do we always use LIMMA to generate the difference plots? </a>
    <div class="faq-answer" id="faq-9">Yes, the group comparison (case vs. control) is performed using LIMMA to test for statistically significant
    differences between the groups.  We investigated other options, such as SAM, but felt that LIMMA would be more appropriate if the sample sizes were small.
        <br /><br /></div>

     <a class="faq-question" href="#" name="faq-10">Does the MAT work for RNA-seq data? </a>

    <div class="faq-answer" id="faq-10">Support for RNA-seq data is planned, but not available in this release. Please check the version notes on later versions to
    see if or RNA-seq data are supported.  <br /><br /></div>

    <a class="faq-question" href="#" name="faq-16">What were the criteria for dataset inclusion? </a>
   <div class="faq-answer" id="faq-16">	To insure there were four types of perturbations to the immune system: autoimmune, infectious disease,
     immune deficiency, and tolerance.<br /><br /></div>


     <a class="faq-question" href="#" name="faq-11">What about responder/nonresponder or before/after treatment analyses? </a>
    <div class="faq-answer" id="faq-11">While the tool wasn't built for these types of analyses, it will run them. Just choose one group to
    be “cases” and one to be "controls" (you can easily choose custom labels for the case and control groups) - and you’ll be able to see which modules go
    up/down in the case group.    <br /><br />

    </div>
	</g:if>
   <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
       <h2 style="margin:10px 0 10px 0; border-bottom:1px solid #ddd; ">MATWizard FAQ:</h2>

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

           <br /><br />
       </div>
</g:if>


       <div class="itn-footer">

        <a href="landing.gsp">Landing page</a> |  <a href="gxbmore.gsp">About the Gene Expression Browser</a>
        <g:if test="${grailsApplication.config.tools.available.contains('mat')}"> | <a href="matmore.gsp">About the Module Analysis Tool</a></g:if>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}"> | <a href="matwizmore.gsp">About the Module Analysis Wizard</a></g:if>

    </div>


   %{--<div id="footer">--}%
       %{--&copy; copyright info and other things.--}%

   %{--</div>--}%
</div>

  </body>
</html>