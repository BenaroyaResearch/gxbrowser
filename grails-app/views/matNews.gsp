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
        <h2>Changes and News for Module Analysis Tool</h2>
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
    <ul>
       	<li><span class="custom-width-medium" style="color: black;">Version 1.56:</span>
			<p>Preliminary support for module analysis on RNA-seq datasets: via platformType.</p>
		</li>
		
        <li><span class="custom-width-medium" style="color: black;">Version 1.55:</span>
			<p>Only the owner of a module analysis may publish/unpublish from the MAT listing page.</p>
			<p>Listing filters rearranged on MAT listing page</p>
		</li>
    
    	<li><span class="custom-width-medium" style="color: black;">Version 1.50:</span>
			<p>matCat collections are now owned with specific users.  Only the owner of a collection may edit that collection, however anyone can view or interrogate the collection.</p>
			<p>Added this Changes and News File.</p>
		</li>
    </ul>

</div>

  </body>
</html>