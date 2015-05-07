
<%@ page import="org.sagres.rankList.RankList" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'rankList.label', default: 'RankList')}" />
    <title>Load Std Rank List</title>
  </head>
  <body>

    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
    </div>

    <div class="body">
      <h1>Load Std Rank List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

	  <g:uploadForm action="uploadStdRankList">
		
        <div>
        <label for "sampleSetId">
          Sample Set ID
        </label>
        <g:textField name="sampleSetId" value="" />
        </div>

        <div>
        <label for "groupSetId">
          Group Set ID
        </label>
        <g:textField name="groupSetId" value="" />
        </div>

		<label for="file">
		  Filename
		</label>
		<input type="file" name="rankListFile" />
		<br />

		<g:checkBox name="runNow" value="${true}" checked="true" />
		Run Now
		<br />

		<g:submitButton name="upload" value="Upload" />
		
	  </g:uploadForm>

      <h2>Explanation</h2>
      <p>
        A standard rank list file has a specific format which is deduced based
        on a file naming convention. Sections of the file name are separated by
        underscores ('_'). The first section must be 'ds' followed by the
        sample set ID number. The second section must be one of:
        <ul>
          <li>'' (blank), for an overall comparison</li>
          <li>'diff', for rank based on difference</li>
          <li>'fc', for rank based on fold-change</li>
          <li>'diffFc', for rank based on both difference and fold-change</li>
        </ul>
        The remaining section(s) are descriptive text.
      </p>
      <p>
        The file itself is expected to be a comma-separated (CSV) file, with
        columns as specified in the rank_list_type table. The data follow a
        one-line header.
      </p>
      
    </div>
  </body>
</html>
	  
