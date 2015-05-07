
<%@ page import="common.chipInfo.ChipsLoaded" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="chipsLoadedMain" />
    <g:set var="entityName" value="${message(code: 'chipsLoaded.label', default: 'ChipsLoaded')}" />
    <title>Load BRI RNA Seq Data</title>
  </head>
  <body>

    <div class="chipsloaded-container">

    <div class="matcreate-wrapper">

      <h1>Load BRI RNA Seq Data</h1>

      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

	  %{--<p>--}%
	  %{--Select the <b>technology</b>, the <b>data file</b> to be uploaded, whether you want to run the update now, and then click on <b>Upload</b>.--}%
	  %{--</p>--}%
	  %{--<p>--}%
		%{--If you are running the upload now, it may take several minutes to complete. Please check on the upload status page to verify that the upload is finished, should you lose your connection or close your browser during the process.--}%
	  %{--</p>--}%

          <div class="centered-gray">
	  <g:uploadForm action="uploadBriRnaSeqFiles">
		
		<div class="section"><label for="chipType">
              Chip Type<span class="required">*</span>
            </label>
            <div class="input"><g:select name="chipType" from="${chipTypeList}" optionKey="id"	optionValue="name" />
            </div>
        </div>
		
		<div class="section">
		<label for="rawCountFile"> Raw count file<span class="required">*</span></label>
		<div class="input"><input type="file" name="rawCountFile" /> </div>
		</div>

		<div class="section">
		<label for="tmmNormalizedFile"> TMM-normalized file<span class="required">*</span></label>
		<div class="input"><input type="file" name="tmmNormalizedFile" /> </div>
         </div>


		<div class="section">
          <label>Run now</label>
          <div class="input" style="padding-top:5px;"><g:checkBox name="runNow" value="${false}" /> &nbsp; <span class="required"><b title="uploading now will slow down the system significantly">(not recommended)</b></span></div>

		</div>
           <p class="required">fields marked with an * are required</p>

		<g:submitButton name="upload" style="width:150px" class="btn primary large" value="Upload" />
		
	  </g:uploadForm>
   </div>
   </div>

  </div>
  </body>
</html>
	  
