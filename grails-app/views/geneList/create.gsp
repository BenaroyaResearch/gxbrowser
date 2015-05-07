<%@ page import="org.sagres.geneList.GeneList; org.sagres.geneList.GeneListCategory" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'geneList.label', default: 'Gene List')}"/>
  <title>Gene List: Import</title>
</head>

<body>
<div class="topbar">
	<div class="topbar-inner itn-fill">
	%{--<div class="topbar-inner bri-fill">--}%
    <div class="sampleset-container">
			<!--<img src="../images/itn_logo_2.png" style="margin-top: 7px; margin-right: 5px; float: left;"/>    -->
      <h3 style="display: inline"><g:link controller="geneBrowser" action="list"><strong>GXB</strong></g:link></h3>
      <ul class="nav secondary-nav">
        <li><g:link controller="sampleSet" action="list" target="_blank">Annotation Tool</g:link></li>
      </ul>
    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->


  <ul class="nav secondary-nav pills">
    <li><g:link controller="geneList" class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
  </ul>

<div class="body">
  <h2>Gene List Importer</h2>
  <g:form enctype="multipart/form-data">
  	<input type="hidden" name="user.id" value="${user?.id}" />
    <fieldset>
      <div class="clearfix">
        <label for="name">Name</label>
        <div class="input">
          <g:textField name="name" class="xlarge"/>
        </div>
      </div>
      <div class="clearfix">
      	<g:set var="coption" value="${params?.geneListCategory?.id}" />
      	<label for="category">Category</label>
      	<div class="input">
      		<g:if test="${editable == false}">
      			<g:set var="coption" value="${GeneListCategory.findByName('User Defined').id}"/>
      			<input type="hidden" name="geneListCategory.id" value="${coption}">
      		</g:if>
      		<g:select name="GeneListCategory.id" from="${GeneListCategory.list()}"
      			optionKey="id" optionValue="name"
      			value="${coption}" disabled="${!editable}"
      			noSelection="['':'-Choose a Category-']"/>
      	</div>
      </div>
      <div class="clearfix">
        <label for="description">Description</label>
        <div class="input">
          <g:textArea name="description" class="xlarge" cols="50" rows="5"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="geneListFile">Gene List File</label>
        <div class="input">
          <input type="file" name="geneListFile" id="geneListFile"/>
          <div>Column Format 1 (TSV file): GeneID, Taxonomy Name, Symbol, RefSeqIDs, Full Name</div>
          <div>Alternate Format (Text file): Symbol</div>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary" type="submit" name="_action_save">Import Gene List</button>
      <button class="btn" type="submit" name="_action_cancel">Cancel</button>
    </div>
  </g:form>
</div>

</body>
</html>
