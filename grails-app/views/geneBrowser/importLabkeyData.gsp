<%@ page import="org.sagres.sampleSet.SampleSet" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>
  <title>TrialShare Data: Import</title>
</head>

<body>

<div class="topbar">
	<div class="topbar-inner fill">
	  <div class="sampleset-container">
          <g:include view="${grailsApplication.config.dm3.site.branding}"></g:include>

	    <h3><g:link controller="geneBrowser" action="list"><strong>GXB</strong></g:link></h3>
    </div>
  </div>
</div>

<div class="sampleset-container">
  <h2>TrialShare Data Importer</h2>
  <g:form enctype="multipart/form-data" action="saveLabkeyData">
    <fieldset>
      <div class="clearfix">
        <label for="sampleSetId">Sample Set</label>
        <div class="input">
          <g:select from="${SampleSet.list()}" optionKey="id" optionValue="name" name="sampleSetId"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="labkeyTab">Category (Tab)</label>
        <div class="input">
          <g:select from="${labkeyTabs}" name="labkeyTab" optionKey="key" optionValue="value"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="labkeyFile">TrialShare Excel File</label>
        <div class="input">
          <input type="file" name="labkeyFile" id="labkeyFile"/>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary" type="submit">Import TrialShare Data</button>
    </div>
  </g:form>
</div>

</body>
</html>
