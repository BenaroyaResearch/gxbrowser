<%@ page import="org.sagres.mat.Version" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="matmain" />
        <g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}" />
				<link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css"')}"/>
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
		<ul class="breadcrumb">
  <li><a href="${createLink(controller: 'version')}">Home</a> <span class="divider">/</span></li>
  <li><a href="${createLink(controller: 'version', action:'create')}">Create Version</a> <span class="divider">/</span></li>
  <li><a href="${createLink(controller: 'version')}">list versions</a> <span class="divider">/</span></li>
  <li class="active">Associate versions with chip types</li>
</ul>



		</div><!--end mat_sidecol -->
		<div class="mat_maincol">
			<g:form action="saveAssociations"  class="mat-mainform">
				<div class="mat-upload-form">
					<table>
						<tr><td>Chip Type</td><TD>Gen 2 Mapping</TD> <TD>Gen 3 Mapping</TD></tr>

					<g:each var="key" status="i" in="${chipTypes}">
						<TR>
							<TD>
						${key.value.manufactorer}:${key.value.chipTypeName}
						</TD>
						<TD>

							<select name="chip_id_${key.key}" id="chip_id_${key.key}">
							  <option value="-1">None</option>
								<g:each in="${versions}"  var="version">
									<option value="${version.id}"
										<g:if test="${key.value.versionId == version.id}">
											selected="selected"
										</g:if>
									>
										${version.versionName}
									</option>
								</g:each>
							</select>
						<td>
								<select name="g3_chip_id_${key.key}" id="g3_chip_id_${key.key}">
							  <option value="-1">None</option>
								<g:each in="${versions}"  var="version">
									<option value="${version.id}"
										<g:if test="${key.value.gen3Id == version.id}">
											selected="selected"
										</g:if>
									>
										${version.versionName}
									</option>
								</g:each>
							</select>
						</td>
					</g:each>
					</TD>
					</TR>

					</table>

					<g:submitButton name="save" value="save"/>
				</div>
			</g:form>





		</div><!--end maincol-->



    </body>
</html>
