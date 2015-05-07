<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

    <title>
        Systems Immunology Toolkit - Project Tracking
    </title>
</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">

    <div class="container">
        <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>
         <h3>
            <a>
				Systems Immunology Toolkit
            </a>
        </h3>


        <ul class="nav secondary-nav">


      </ul>

    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->



<div class="container">
    <div class="page-header">
        <h2>Project Tracker</h2>
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
        <g:link controller="project" show="index">&laquo; Back to landing page</g:link>
    </div>

    <p>The Project Tracker can help you keep track of laboratory projects in their path from sample preparation, through data
    	analysis and and presentation in the <a href="gxbmore.gsp">Gene Expression Browser</a> (GXB).  You can also create private projects
     	that are visible to you, and only to you.</p>
    <p>Projects can be either imported from TG2, or created from scratch and later associated with TG2 entries, or GXB sample sets.
		Any number of tasks can be created for a project, and these tasks can be associated with resources.</p>
    <p>Gantt charts are available for both public and private project collections to help you evaluate the scheduling and workflow
    	associated with your projects.<p>
	<p>You must be logged in to create or edit a project, and to view private projects, or gantt charts for private projects</p>

   <h2 style="margin:10px 0 10px 0; border-bottom:1px solid #ddd; "><a name="faq"></a>Project Tracker FAQ:</h2>

    <a class="faq-question" href="#" name="faq-1">How do I make a project 'private'?</a>
    <div class="faq-answer" id="faq-1">In the create or edit page, the option to make a project private is near the bottom of the page.
    	<br/>
    </div>

    <a class="faq-question" href="#" name="faq-2">Can other people edit my projects?</a>
    <div class="faq-answer" id="faq-2">At this time all public projects are editable by anyone who can login.
    	This is mostly restricted to the Genomics Core, bioinformaticists, and Chaussabel lab members. You can
    	choose to make projects editable only by the owner, that can be accomplished in the create or edit pages,
    	where the option is near the bottom of the page.
    	<br/>
    </div>

    <a class="faq-question" href="#" name="faq-3">Do I need to fill in all of the information on the create page?</a>
    <div class="faq-answer" id="faq-3">No.  The recommended minimuim information is identified by a red star at the end of the input
    	field.  All other fields are optional.  We do our best to manage the display in stiuations where minimum information is missing.
    	<br/>
    </div>

    <a class="faq-question" href="#" name="faq-4">How do I set the color of a project and the associated tasks in the Gantt chart?</a>
    <div class="faq-answer" id="faq-4">On the create or edit pages, there is a field called color.  Click in that field and a color
    	chooser will be displayed with distinct individual colors. Choose a color, then click the field again to close the chooser.
    	<br/>
    </div>

    <a class="faq-question" href="#" name="faq-5">Do any of the changes I make in Project Tracker impact TG2 or SSAT/GXB data?</a>
    <div class="faq-answer" id="faq-5">No, when you import a TG2 project, you are copying that data, but not changing it.
    	Similarly when you link to TG2 or SSAT, it's just a link, not a change to those systems. 
        <br/>
    </div>

     <div class="itn-footer">
        <g:link controller="project" show="index">Landing page</g:link> |  <a href="gxbmore.gsp">About the Gene Expression Browser</a>
    </div>

	<br/>

   <div class="footer">
       Version 1.0 of Project Tracker<br/>
       &copy; 2013 Benaroya Research Institute 
   </div>
</div>

  </body>
</html>