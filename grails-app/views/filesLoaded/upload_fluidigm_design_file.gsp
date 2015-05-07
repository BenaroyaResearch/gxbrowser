<%@ page import="org.sagres.FilesLoaded" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'rankList.label', default: 'RankList')}" />
    <title>Load Fluidigm Design File</title>
  </head>
  <body>

    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
    </div>

    <div class="body">
      <h1>Load Fluidigm Design File</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

      <p>
        This form is for uploading design files for new Fluidigm focused-array assay layouts. (If the same assays are merely rearranged on the chips, that doesn't require a new file to be uploaded.)
      </p>
      <p>
        The file needs to be a tab-delimited text file, as exported from the standard Fluidigm spreadsheets. 
      </p>

	  <g:uploadForm action="uploadFluidigmDesignFile">

	    <label for="chipTypeName">
          Layout name
        </label>
        <g:textField name="chipTypeName" />
        <br />

		<label for="designFile">
		  Filename
		</label>
		<input type="file" name="designFile" />
		<br />

        <div id="housekeepingGenesFileDiv" class="section">
          <label for="housekeepingGenesFile">
            Housekeeping genes file<span class="required">*</span>
          </label>
		  <div class="input">
            <input type="file" name="housekeepingGenesFile"
				   id="housekeepingGenesFile" />
		  </div>
        </div>

        <div id="referenceSamplesFileDiv" class="section">
          <label for="referenceSamplesFile">
            Reference samples file<span class="required">*</span>
          </label>
		  <div class="input">
            <input type="file" name="referenceSamplesFile"
				   id="referenceSamplesFile" />
		  </div>
        </div>

		<label for="chipData">
		  Chip Data
		</label>
		<g:select name="chipData"
				  from="${common.chipInfo.ChipData.findAllByNameLike('Fluidigm%')}"
				  optionKey="id"
				  optionValue="name" />
		<br />
		
		<g:checkBox name="runNow" value="${false}" checked="true" />
		Run Now
		<br />

		<!--Cancel button!!!-->
		<g:submitButton name="upload" value="Upload" />
		
	  </g:uploadForm>

    </div>
  </body>
</html>
	  
