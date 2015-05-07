<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>

    <title>
        <g:if test="${grailsApplication.config.target.audience.contains('ITN')}">Immune Tolerance Network Module Analysis Toolkit</g:if>
        <g:else>Systems Immunology Toolkit</g:else>
    </title>

</head>

<body>
<div class="topbar">
	<div class="topbar-inner fill">
	%{--<div class="topbar-inner bri-fill">--}%
    <div class="container">
       <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>
         %{--<h3><a>Systems Immunology Toolkit</a></h3>--}%


        <ul class="nav secondary-nav">


      </ul>

    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->



<div class="container">
    <div class="page-header">
        <h2>Module Generation Descriptions</h2>
    </div>
     <h3>Generation 1</h3>
   <p>Generation 1 is described in the 2008 Immunity paper, <a href="http://www.ncbi.nlm.nih.gov/pubmed/18631455" target="_blank">"A Modular Analysis Framework for Blood Genomics Studies: Application to Systemic
   Lupus Erythematosus"</a>.  This generation has 4,742 probes assigned to 28 modules.  The cell type was PBMCs and the platform was Affymetrix HG-U133A &amp; U133B.
   There were 8 biological conditions/diseases used to create generation 1: arthritis, lupus, T1D, melanoma, 3 acute infections and liver transplant.

    <h3>Generation 2</h3>
    <p>Generation 2 has 14,424 probes assigned to 260 modules.  The cell type was whole blood and the platform was Illumina HT-12 V2.  There were 9
    biological conditions/diseases used to create generation 2.
    %{--liver transplant, TB, B-cell deficiency, 2 Burkholderia, 2 lupus, SoJIA, and CHAVI.</p>--}%

    <table class="zebra-striped">

        <thead>
            <tr>
                <th>Perturbation</th>
                <th>Disease</th>
                <th># cases</th>
                <th># controls</th>
            </tr>
        </thead>

            <tr>
                <td rowspan="4" style="vertical-align: top;">Infectious</td>
                <td>Melioidosis/Sepsis</td>
                <td>9</td>
                <td>24</td>
            </tr>

         <tr>

                <td>Melioidosis/Sepsis</td>
                <td>9</td>
                <td>19</td>
            </tr>

         <tr>

                <td>HIV</td>
                <td>36</td>
                <td>46</td>
            </tr>

         <tr>

                <td>TB</td>
                <td>6</td>
                <td>9</td>
            </tr>

         <tr>
                <td rowspan="3"style="vertical-align: top;">Autoimmune</td>
                <td>SLE</td>
                <td>14</td>
                <td>80</td>
            </tr>

         <tr>

                <td>SLE</td>
                <td>12</td>
                <td>15</td>
            </tr>

         <tr>

                <td>SoJIA</td>
                <td>21</td>
                <td>65</td>
            </tr>

         <tr>
                <td>Immune Deficiency</td>
                <td>B-cell deficiency</td>
                <td>9</td>
                <td>10</td>
            </tr>

         <tr>
                <td>Tolerance</td>
                <td>Liver Transplant</td>
                <td>16</td>
                <td>10</td>
            </tr>



    </table>


    <g:if test="${!grailsApplication.config.target.audience.contains('ITN')}"><h3>Generation 3</h3>
    <p>Generation 3 has 14,504 probes assigned to 382 modules.  The cell type was whole blood and the platform was Illumina HT-12 V3.  There were 16
    biological conditions/diseases used to create generation 3: TB, staph, sepsis, HIV, Flu, RSV, B-cell deficiency, liver transplant, pregnancy, melanoma stage
    IV, Kawasaki, juvenile dermatomyositis, COPD, MS untreated, Pediatric SLE, and SoJIA.
    </p>
        </g:if>




   %{--<div id="footer">--}%
       %{--&copy; copyright info and other things.--}%

   %{--</div>--}%
</div>

  </body>
</html>