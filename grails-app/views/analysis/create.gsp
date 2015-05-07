<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="matmain"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>
<script type="text/javascript" src="http://twitter.github.com/bootstrap/1.3.0/bootstrap-tabs.js">

</script>
	<g:javascript>

$(function () {
$('#tabs').tabs();
loadDoc();
})

    function loadDoc() {
			var reload = "${reload}";
			if (reload == "true") {
				loadAvailableSampleGroups(${sampleGroupId});
				toggleS('parameters');
			}
    }

		function toggleS(section) {
			section = section || 'displayDefaultParams';
			var ele = document.getElementById(section);
			if (ele.style.display != "none") {
				ele.style.display = "none";
			} else {
				ele.style.display = "";
			}
		}

		var dataSelection = 'uploadData';
		function toggleUploadS() {
			var eleUpload = document.getElementById("uploadData");
			var eleSelect = document.getElementById("preloaded");
			if (dataSelection == "uploadData") {
				dataSelection = 'preLoadedData';
				eleUpload.style.display = "block";
				eleSelect.style.display = "none";
			} else {
				dataSelection = "uploadData";
				eleUpload.style.display = "none";
				eleSelect.style.display = "block";
			}
		}


		function loadAvailableSampleGroups(groupId) {
			var e = document.getElementById("expressionDateFile");
			var sampleSetId = e.options[e.selectedIndex].value;
			var url = "${createLink(controller: 'analysis', action: 'getAvailableGroupSets')}?sampleId=".concat(sampleSetId);
			setChipId(sampleSetId);
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
					var results = JSON.parse(xmlhttp.responseText);
					//var d = document.getElementById("datasetGroup");
					//d.style.display = "";
					var dd = document.getElementById("dataSetGroupSelector");
					for (i=dd.options.length-1;i>=1;i--)
					{
						dd.remove(i);
					}
					for (var key in results.groups) {
						dd.add(new Option(results.groups[key], key), null);
					}
					//Now to set the value if it was passed in
					if (arguments.length > 0) {
						dd.value = groupId;
					}
				}
			}
			xmlhttp.open("GET", url, true);
			xmlhttp.send();
		}


		function setChipId(sampleId) {
			var url = "${createLink(controller: 'analysis', action: 'getChipIdForSampleSet')}?sampleId=".concat(sampleId);
			var xmlhttp = new XMLHttpRequest();
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
					var results = JSON.parse(xmlhttp.responseText);
					//var d = document.getElementById("datasetGroup");
					var mv = document.getElementById("chipId");
					mv.value=results.versionId;
					//alert('version:' + results.versionId);
					checkGenerationSupport();
				}
			}
			xmlhttp.open("GET", url, true);
			xmlhttp.send();

		}

		function checkGenerationSupport() {

			var chipId= $("#chipId").val();
		  $.getJSON('${createLink(action : "chipSupportsMultipleVersions")}?chipId='+ chipId,
		  function(data) {
		  if ($.trim(data.supported) == "true") {
			  	$("#generationSupport").css("display", "");
		  	} else {
			  	$("#generationSupport").css("display", "none");
		  	}
		  });
		};




	</g:javascript>

</head>


<body onload="loadDoc();">



<div class="mat-container">
<div class="matcreate-wrapper">
<ul class="breadcrumb">
	<li><a href="${createLink(controller: 'mat')}">Home</a> <span class="divider">/</span></li>
	<li class="active">Create Analysis</li>
</ul>

<h1>Create Analysis</h1>
<g:if test="${flash.message}">
	<div class="message alert-message">${flash.message}</div>
</g:if>
<g:hasErrors bean="${analysisInstance}">
    <g:eachError bean="${analysisInstance}">
         <div class="alert-message error">
           <g:message error="${it}"/>
        </div>
    </g:eachError>

</g:hasErrors>
<g:uploadForm action="save" enctype="multipart/form-data" class="mat-mainform">
<div class="mat-upload-form">
	<g:if test="${analysisInstance.id > 0}">
		<g:hiddenField name="id" value="${analysisInstance.id}"/>
	</g:if>

	<div class="formsection formsection1 <g:if test="${errorFields.contains('datasetName')}">error</g:if>">
		<label for="datasetName">Analysis Name:<span class="required">*</span></label>
		<g:textField id="datasetName" name="datasetName" value="${analysisInstance?.datasetName}"/> <span
		class="help-inline">(no underscores or periods)</span>
	</div>


	<div class="formsection formsection2">
				<input type="radio" name="toggle-load" value="preloaded" id="toggle-preloaded"
					 onclick="javascript:toggleUploadS();" checked=""/>
		<label for="toggle-preloaded" class="nowidth">Use Preloaded Data</label> &nbsp;&nbsp;
		<input type="radio" name="toggle-load" value="uploaded" id="toggle-uploaded"
					 onclick="javascript:toggleUploadS();" />
		<label for="toggle-uploaded" class="nowidth">Upload Data</label>

	</div>

	<div id="preloaded">

		<div class="formsection formsection3 <g:if test="${errorFields.contains('expressionDataFile')}">error</g:if>">
			<label for="expressionDateFile" >Microarray Data:<span class="required">*</span></label>
			<g:select id="expressionDateFile" from="${dbDataSets}" optionKey="key" name="expressionDataFile"  value="${sampleSetId}"
								optionValue="value" onchange="javascript:loadAvailableSampleGroups();"/>

		</div>

		<div class="formsection formsection4 <g:if test="${errorFields.contains('sampleGroupId')}">error</g:if>" id="sampleGroupDiv" >
			<label for="dataSetGroupSelector" >Select Group:<span class="required">*</span></label>
			<select id="dataSetGroupSelector" name="dataSetGroups"  >
				<option selected="selected" value="-1">Select Sample Group:<span class="required">*</span></option></select>

			<div style="padding:5px 0 0 120px">
				(you can also use the <a href="${createLink(controller: 'sampleSet')}">Annotation Tool</a> to create a new one )
			</div>

		</div>
	</div>

	<div id="uploadData" style="display: none">
		<div class="formsection formsection3">
            <div style="padding-bottom:20px; margin-top:-20px;">
                For a simplified uploading process, <a href="${createLink(controller: 'MATWizard')}">Module Analysis Wizard</a> will take you through the uploading of data into the Module Analysis Tool step-by-step, and notify you when your analyses are finished.
            </div>
			<label for="expressionFile">Microarray Data: <span class="required">*</span></label>
			<input type="file" id="expressionFile" name="expressionFile" <g:if test="${errorFields.contains('expressionDataFile')}"> class="file error" </g:if><g:else> class="file"</g:else>  />
			%{--(<a href="/MAT/data/microarray_format.html">format guide</a>)--}%
		</div>

		<div class="formsection formsection4">
			<label for="designFile">Design File: <span class="required">*</span></label>
			<input type="file" id="designFile" name="designFile" class="file"/>
		</div>
	</div>

	<div class="formsection formsection5">

		<a class="btn small" onclick="javascript:toggleS('parameters');">Change default parameters</a>
		<input name="Run Analysis" value="Run Analysis" id="Run Analysis" class="btn large primary mat-form-submit"
					 type="submit"/>
        <p class="required" style="font-size: 11px; font-style:italic; padding-top:10px;">fields marked with * are required</p>

	</div><!--end formsection-->
</div>
	<div id="parameters" style="display:none;">


		<div id="tabs">

			<ul class="tabs" data-tabs="tabs">
				<li><a href="#tabs1" class="tab">Preprocessing</a></li>
				<li><a href="#tabs2" class="tab">Data</a></li>
				<li><a href="#tabs3" class="tab">Statistical</a></li>
			</ul>

<div id="adv-params" class="tab-content">

<div id="tabs1">
	<div class="formsection-small">
		<label for="performNormalize">Perform Quantile Normalization</label>
		<g:select name="performNormalize" from="${analysisInstance.constraints.performNormalize.inList}"
							value="${analysisInstance?.performNormalize}" valueMessagePrefix="analysis.performNormalize"/>
	</div>

	<div class="formsection-small">
		<label for="performFloor">Perform Flooring</label>
		<g:select name="performFloor" from="${analysisInstance.constraints.performFloor.inList}"
							value="${analysisInstance?.performFloor}" valueMessagePrefix="analysis.performFloor"/>
	</div>
<!-- TODO This next is conditional to if perform flooring true -->


	<div class="formsection-small">
		<label for="minExpression">Min Expression</label>
		<g:textField name="minExpression" value="${fieldValue(bean: analysisInstance, field: 'minExpression')}"
								 class="mat-small"/>
	</div>

	<div class="formsection-small">
		<label for="performLog2">Perform Log2</label>
		<g:select name="performLog2" from="${analysisInstance.constraints.performLog2.inList}"
							value="${analysisInstance?.performLog2}" valueMessagePrefix="analysis.performLog2"/>
	</div>


	<div class="formsection-small">
		<label for="performPALO">Perform PALO</label>
		<g:select name="performPALO" from="${analysisInstance.constraints.performPALO.inList}"
							value="${analysisInstance?.performPALO}" valueMessagePrefix="analysis.performPALO"/>
	</div>

<!-- TODO This next is conditional to if perform PALO true -->


	<div class="formsection-small">
		<label for="PALX">PALX percent</label>
		<g:textField name="PALX" value="${fieldValue(bean: analysisInstance, field: 'PALX')}" class="mat-small"/>
	</div>

	<div class="formsection-small">
		<label for="rangeCut">Range Cut</label>
		<g:textField name="rangeCut" value="${fieldValue(bean: analysisInstance, field: 'rangeCut')}" class="mat-small"/>
	</div>


</div><!--end tab content for preprocessing -->

<div class="active" id="tabs2" >

	<div class="formsection-small">
		<label for="chipId">Chip Selection:</label>
		<select id="chipId" name="chipId" onchange="javascript:checkGenerationSupport();">
			<g:each in="${availableChips.keySet()}" var="chip" status="i">
				<option
					value="${chip}"
				>${availableChips.get(chip)}</option>
			</g:each>
		</select>
	</div>

	<div class="formsection-small" style="display:none;" id="generationSupport">
		<label for="inputType">Module Generation:</label>
		<g:select name="modGeneration" from="${analysisInstance.constraints.modGeneration.inList}"
							value="${analysisInstance?.modGeneration}" valuemessageprefix="analysis.modGeneration"/>
	</div>

	<div class="formsection-small">
		<label for="signalPattern">Signal pattern:</label>
		<g:textField name="signalPattern" value="${analysisInstance?.signalPattern}"/>
	</div>

	<div class="formsection-small">
		<label for="inputType">input type:</label>
		<g:select name="inputType" from="${analysisInstance.constraints.inputType.inList}"
							value="${analysisInstance?.inputType}" valuemessageprefix="analysis.inputType"/>
	</div>

<!-- todo add in chip generation where supported -->


</div><!--end tab content for data-->

<div id="tabs3" >
	<div class="formsection-small">
		<label for="deltaType">Metric</label>
		<g:select name="deltaType" from="${analysisInstance.constraints.deltaType.inList}"
							value="${analysisInstance?.deltaType}" valueMessagePrefix="analysis.deltaType"/>
	</div>

	<div class="formsection-small">
		<label for="foldCut">Metric threshold</label>
		<g:textField name="foldCut" value="${fieldValue(bean: analysisInstance, field: 'foldCut')}" class="mat-small"/>
	</div>

	<div class="formsection-small">
		<label>Intensity Difference Threshold</label>
		<g:textField name="deltaCut" value="${fieldValue(bean: analysisInstance, field: 'deltaCut')}" class="mat-small"/>
	</div>


	<div class="formsection-small">
		<label for="zMeasure">Measure of center</label>
		<g:select name="zMeasure" from="${analysisInstance.constraints.zMeasure.inList}"
							value="${analysisInstance?.zMeasure}" valueMessagePrefix="analysis.zMeasure"/>
	</div>


	<div class="formsection-small">
	<label for="multipleTestingCorrection">Multiple Testing Correction</label>
		<g:select name="multipleTestingCorrection" from="${analysisInstance.constraints.multipleTestingCorrection.inList}"
							value="${analysisInstance?.multipleTestingCorrection}" valueMessagePrefix="analysis.multipleTestingCorrection"/>
	</div>

<!-- TODO change wording based on value of MTC if f for MTC then text should be p-value threshold-->

	<div class="formsection-small">
	<label for="fdr">False Discovery Rate</label>
		<g:textField name="fdr" value="${fieldValue(bean: analysisInstance, field: 'fdr')}" class="mat-small"/>
	</div>



</div><!-- end tab content for stats -->
<div> <!-- end tabs-content -->



		</div>

</div>
</g:uploadForm>
</div>
</div>
<g:render template="/common/bugReporter" model="[tool:'MAT']"/>
</body>
</html>
