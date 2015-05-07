<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

      <link rel="stylesheet" href="${resource(dir: 'css', file: 'gxb.css')}"/>

  <title>
      <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
      	Immune Tolerance Network Module Analysis Toolkit
      </g:if>
      <g:elseif test="${grailsApplication.config.target.audience.contains('HIPC')}">
      	HIPC Innate Immune Profiling Compendium
      </g:elseif>
      <g:elseif test="${grailsApplication.config.target.audience.contains('ICAC')}">
      	ICAC Systems Immunology Toolkit
      </g:elseif>
      <g:else>
      	Systems Immunology Toolkit
      </g:else>
  </title>

  <g:javascript src="jquery-ui-min.js"/>
  <g:javascript src="application.js"/>
  <g:javascript src="jquery-min.js"/>
  <g:javascript src="jquery-ui-min.js"/>
  <g:javascript src="common.js"/>
  <g:javascript src="bootstrap-dropdown.js"/>
  <g:javascript src="bootstrap-modal.js"/>

</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">

    <div class="container">
        <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>
         <h3>
            <a>
                <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
                	Module Analysis Toolkit
                </g:if>
			    <g:elseif test="${grailsApplication.config.target.audience.contains('HIPC')}">
			    	Innate Immune Profiling Compendium
			    </g:elseif>
			    <g:elseif test="${grailsApplication.config.target.audience.contains('ICAC')}">
      				Systems Immunology Toolkit
      			</g:elseif>
      			<g:else>
   					Systems Immunology Toolkit
      			</g:else>
            </a>
        </h3>


        <ul class="nav secondary-nav">


      </ul>

    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->


<div class="container">
    <div class="page-header">
        <h2>
            <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
            	Immune Tolerance Network Module Analysis Toolkit
            </g:if>
		    <g:elseif test="${grailsApplication.config.target.audience.contains('HIPC')}">
		    	HIPC Innate Immune Profiling Compendium
		    </g:elseif>
			<g:elseif test="${grailsApplication.config.target.audience.contains('ICAC')}">
      			ICAC Systems Immunology Toolkit
      		</g:elseif>
   			<g:else>
   				Systems Immunology Toolkit
   			</g:else>
        </h2>
    </div>
	<g:if test="${grailsApplication.config.target.audience.contains('HIPC')}">    
	    <p>These tools will allow you to view HIPC Innate Compendium data on a gene-by-gene basis, overlay clinical data, evaluate gene expression using a modular framework, and compare your data to other sample sets.</p>
	</g:if>
	<g:elseif test="${grailsApplication.config.target.audience.contains('ICAC')}">    
	    <p>These tools will allow you to view ICAC expression data on a gene-by-gene basis, overlay clinical data, evaluate gene expression using a modular framework, and compare your data to other sample sets.</p>
	</g:elseif>
	<g:elseif test="${grailsApplication.config.target.audience.contains('GXB')}"> <!-- GXB Paper -->
		<p>This tool will allow you to view public microarray data on a gene-by-gene basis, overlay clinical data, compare datasets and diseases, and get a quick functional interpretation of your favorite genes.</p>
	</g:elseif>
	<g:else>
	    <p>These tools will collectively allow you to upload microarray data, view that data on a gene-by-gene basis, overlay clinical data, analyze your data using a modular framework, compare your data to other datasets and diseases, and get a quick functional interpretation for the genes in your genelist.</p>
	</g:else>
    <div class="row">
        <div class="span17">
        
            <h4>Use the tools:</h4>

            <div class="tool-link" style="margin-right:15px;">
				<g:include view="_gxbBox.gsp"></g:include>
            </div>
			<g:if test="${grailsApplication.config.tools.available.contains('mat')}">
             <div class="tool-link">
             	<g:include view="_matBox.gsp"></g:include>
            </div>
            </g:if>
               %{--start only if ITN--}%
            %{--<g:if test="${grailsApplication.config.dm3.authenticate.labkey}">--}%

             %{--<div class="tool-link" style="margin-right:15px;">--}%
<%--             	<g:include view="_wizardBox.gsp"></g:include>--%>
             %{--</div>--}%
                %{--</g:if>--}%
            %{--end only of itn--}%

			<div class="user-links" style="margin-right:15px;">
			<g:if test="${grailsApplication.config.tools.available.contains('rb') }">
            <div class="tool-link">
             	<g:include view="_rbBox.gsp"></g:include>
            </div>
				</div>
            	<div class="user-links" style="float: right;">
            </g:if>
            
                <h4>Getting started</h4>

				<g:if test="${grailsApplication.config.tools.available.contains('mat')}">
                <sec:ifLoggedIn>
                    <a href="${createLink(controller: 'analysis')}" class="user-link">My Analyses</a>
                </sec:ifLoggedIn>
                </g:if>

                <a href="tutorials.gsp" class="user-link">Tutorials</a>
                <a href="faq.gsp" class="user-link">FAQs</a>
            <g:link onclick="sendFeedback();return false;" class="user-link" title="Send Feedback">Send Feedback</g:link>
                %{--<a href="" onclick="reportBug();return false;" class="user-link">Send feedback</a>--}%

                  <g:javascript>
                    var sendFeedback = function()
                      {
                        var subject = "Systems Immunology Toolkit ";
                        var text = "Feedback:\r\n\r\n";
                        var encodedText = encodeURIComponent(text);
                        window.location = "mailto:softdevteam@benaroyaresearch.org?subject=" + subject + "&body=" + encodedText;

                      };

                    </g:javascript>


        </div>


             %{--<a href="${createLink(controller: 'mat')}" class="tool-link">Module Analysis Tool</a>--}%
             %{--<a href="${createLink(controller: 'MATWizard')}" class="tool-link">MATWizard</a>--}%

        </div>







     </div><!--end row-->

    <div class="itn-footer">

        Landing page |  <a href="gxbmore.gsp">About the Gene Expression Browser</a>   
        <g:if test="${grailsApplication.config.tools.available.contains('mat')}"> | <a href="matmore.gsp">About the Module Analysis Tool</a></g:if>
        <g:if test="${grailsApplication.config.tools.available.contains('wizard')}"> | <a href="matwizmore.gsp">About the Module Analysis Wizard</a></g:if>

    </div>

         %{--<g:render template="/common/feedback"/>--}%

   %{--<div id="footer">--}%
       %{--&copy; copyright info and other things.--}%

   %{--</div>--}%
</div>

  </body>
</html>