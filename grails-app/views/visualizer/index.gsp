<%@ page import="org.sagres.mat.Version" %>
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Visualizer: MAT Plots</title>
    <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'fav_mat.ico')}" type="image/x-icon" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-1.2.0.css')}"/>
    <g:javascript src="jquery-min.js"/>
  </head>
  <body>
    <div class="container" style="margin-top:20px;">
      <h2>Visualizer</h2>
      <g:form action="matPlot" enctype="multipart/form-data">
      <fieldset>
        <div class="clearfix">
          <label for="chartname">Name</label>
          <div class="input">
            <g:textField name="chartname" style="height:26px;"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="fileType">MAT File Type(s)</label>
          <div class="input">
            <g:select from="['fc':'Fold Change', 'difference':'Difference', 'posNeg':'Positive & Negative']" name="fileType" value="difference"
                      optionKey="key" optionValue="value" onchange="updateFileInputs();"/>
          </div>
        </div>
        <div class="clearfix" id="input-matFile1" >
          <label for="matFile1">Difference File</label>
          <div class="input">
            <input type="file" name="matFile1" id="matFile1"/>
          </div>
        </div>
        <div class="clearfix" id="input-matFile2" style="display:none;">
          <label for="matFile2">Negative File</label>
          <div class="input">
            <input type="file" name="matFile2" id="matFile2"/>
          </div>
        </div>
        <div class="clearfix" id="input">
        	<label for="sampleSetId">Sample Set Id (not required)</label>
        	<div class="input">
        		<g:textField name="sampleSetId" id="sampleSetId" style="height:26px;"/>
       		</div>
        </div>
        <div class="clearfix" id="input">
        	<label for="modVersionName">Module Version Name (not required)</label>
        	<div class="input">
        		<g:select from="${Version.list()}" name="modVersionName" id="modVersionName" value="IlluminaV3" optionKey="versionName" optionValue="versionName"/>
       		</div>
        </div>
        <div class="clearfix">
          <label for="designFile">Design File (not required)</label>
          <div class="input">
            <input type="file" name="designFile" id="designFile"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="delimiter">Delimiter</label>
          <div class="input">
            <g:select from="['tsv':'TSV','csv':'CSV']" name="delimiter" optionKey="key" optionValue="value" value="csv"/>
          </div>
        </div>
      </fieldset>
      <div class="actions">
        <button class="btn primary" type="submit">Draw MAT Plot</button>
        <g:link class="btn primary" action="list">Saved MAT Plots</g:link>
      </div>
    </g:form>
    <g:javascript>
      var updateFileInputs = function() {
        var fileType = $("#fileType").val();
        if (fileType === "difference" || fileType === "fc" || fileType === "probelvl") {
          $("div#input-matFile1").show();
          $("div#input-matFile2").hide();
          var label = $("#fileType option:selected").text().concat(" File");
          $("div#input-matFile1 label").html(label);
        } else if (fileType === "posNeg") {
          $("div#input-matFile1").show();
          $("div#input-matFile2").show();
          $("div#input-matFile1 label").html("Positive File");
        }
      };
    </g:javascript>
    </div>
  </body>
</html>