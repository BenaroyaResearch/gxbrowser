<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

    <title>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Immune Tolerance Network Module Analysis Toolkit - Module Analysis Toolkit</g:if>
        <g:else>Systems Immunology Toolkit - Gene Expression Browser</g:else>
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
        <h2>Gene Expression Browser</h2>
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
         	<img src="images/cap-gxb.png" alt="Gene Expression Browser" style="float: left; padding: 0 20px 20px 0;" />
         </g:if>
		 <g:else>
         	<img src="images/cap-gxb-bri.png" alt="Gene Expression Browser" style="float: left; padding: 0 20px 20px 0;" />
		 </g:else>

         <p>In the GXB, you can view the expression level of all genes in your dataset, overlay the clinical data
                associated with the samples, and rank the genes, either using pre-defined lists or dynamically.</p>
         <p>Each bar in the histogram represents the expression level in a single sample from the dataset for the highlighted gene.</p>
        <p><a href="#faq">Go to the FAQ at the bottom of the page</a>.</p>
    <p><a href="${createLink(controller: 'geneBrowser')}" class="btn primary">Go to GXB &raquo;</a></p>

     </p>
     <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
        <img src="images/annot-gxb.png" />
     </g:if>
	<g:else>
        <img src="images/annot-gxb-bri.png" />
	</g:else>
    %{--<ol class="toolfaq">--}%
        %{--<li><b>Gene list</b> - The Gene list allows you to switch back and forth between genes to see expression in all of your samples. You can use the search box at top if there is a particular gene you are looking for. You can hide this list if you want a little more space to view the data. lorem</li>--}%
        %{--<li><b>Stats</b> - These genes will anchor you to which gene you're looking at, even if you hide the gene list. You can also see the expression value for this gene for the highlighted sample.</li>--}%
        %{--<li><b>Navigation tabs</b> - Each tab provides you with different information about the study, the samples, or the data. For example, the Gene tab will give basic information (from NCBI gene) about the function of this gene in humans, with links to get more info you need.</li>--}%
        %{--<li><b>Graph controls</b> - Here, you can change the way you view the data (boxplot or histogram, for example), add clinical overlays, and download the image you have created as a publication-quality graphic.</li>--}%
        %{--<li><b>Clinical overlays</b> - This is where the clinical data show up, should you choose to view them. You can hover over each box to get the value (for gender, F or M will pop up). You can also see the legend for these values using the graph controls just above the histogram bars.</li>--}%
    %{--</ol>--}%

    <h2 style="margin:10px 0 10px 0; border-bottom:1px solid #ddd; "><a name="faq"></a>GXB FAQ:</h2>

   <a class="faq-question" href="#" name="faq-12">Can we get a p-value instead of a fold change with our rank lists?</a>
    <div class="faq-answer" id="faq-12">You can’t rank by p-value in the current version of the GXB. However, you can rank by fold change, a difference, or both.<br /><br /></div>

    <a class="faq-question" href="#" name="faq-13">The graph is rather small, what screen sizes do you support and how can I view the graph in more detail? </a>
    <div class="faq-answer" id="faq-13">The GXB is optimized for screens 1024px wide or larger; it will still be viewable on smaller screens, but you will have less of an overview.
    You have the option of hiding either the Gene List panel, or the information tabs, or both in order to let the graph take up a larger
    percentage of the screen.<br /><br /></div>

    <a class="faq-question" href="#" name="faq-14">How can I change rank list? </a>
    <div class="faq-answer" id="faq-14">There are options for rank lists in the panel called "Advanced Query Panel" on the top left. If
    you would like even more options, click "Change Rank List" under Advanced in the top right of the screen.  <br /><br /></div>


<a class="faq-question" href="#" name="faq-15">Where can I find a key for the overlay legend?</a>
    <div class="faq-answer" id="faq-15">  When you have selected at least one overlay option, you may click the Overlay Legend Button; a key to the overlays will
        become visible. You can also choose to download the key as an image.
        <br /><br /></div>


    <div class="itn-footer">

        <a href="landing.gsp">Landing page</a> |  About the Gene Expression Browser
        <g:if test="${grailsApplication.config.tools.available.contains('mat')}"> | <a href="matmore.gsp">About the Module Analysis Tool</a></g:if>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}"> | <a href="matwizmore.gsp">About the Module Analysis Wizard</a></g:if>

    </div>



   %{--<div id="footer">--}%
       %{--&copy; copyright info and other things.--}%

   %{--</div>--}%
</div>

  </body>
</html>