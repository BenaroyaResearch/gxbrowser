
<%@ page import="org.sagres.rankList.RankList" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'rankList.label', default: 'RankList')}" />
    <title>Load Generic Rank List</title>
  </head>
  <body>

    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
    </div>

    <div class="body">
      <h1>Load Generic Rank List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

	  <g:uploadForm action="uploadGenericRankList">

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

        <div>
        <label for "description">
          Description
        </label>
        <g:textField name="description" value="" />
        </div>

        <div>
		<label for="file">
		  Filename
		</label>
		<input type="file" name="rankListFile" />
		</div>

        <div>
        <label for "format">
          Format
        </label>
        <g:radioGroup name="format" values="[ 'CSV', 'TSV' ]"
                      labels="[ 'CSV (comma-separated)', 'TSV (tab-separated)' ]"
                      value="csv">
          ${it.radio} ${it.label}
        </g:radioGroup>
        </div>

        <div>
        <label for "headerLines">
          Header lines (number of lines before data)
        </label>
        <g:textField name="headerLines" value="1" />
        </div>

        <div>
        <label for "probeIdColumn">
          Probe Id column (0 based)
        </label>
        <g:textField name="probeIdColumn" value="0" />
        </div>

        <div>
        <label for "valueColumn">
          Value column (0 based)
        </label>
        <g:textField name="valueColumn" value="1" />
        </div>

        <div>
        <label for "sortOrder">
          Value sort order
        </label>
        <g:radioGroup name="sortOrder" values="[ 'descending', 'ascending' ]"
                      labels="[ 'Descending (large values first)', 'Ascending (small values first)' ]"
                      value="descending">
          ${it.radio} ${it.label}
        </g:radioGroup>
        </div>
        
		<g:submitButton name="upload" value="Upload" />
		
	  </g:uploadForm>

      <h2>Explanation</h2>
      <p>
        A generic rank list file assigns a value to probe IDs which is used to
        rank those probes. This form lets you specify the form of the file you
        wish to upload.
      </p>
      <p>
        The file data may be separated by either commas (CSV) or tabs (TSV).
        It can have header lines preceding the data, which will be skipped.
        You need to specify the columns containing the probe IDs and the values.
        Column numbering begins at 0 for the first column, 1 for the second,
        etc.
        You must also specify the sort order. In a rank list the first probes,
        those with the smallest rank numbers, are considered the most 
        significant. If the values in the file are larger for more significant
        probes, then choose Descending. Otherwise, Ascending.
      </p>
      
    </div>
  </body>
</html>
	  
