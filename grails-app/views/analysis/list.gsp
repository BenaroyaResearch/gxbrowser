<%@ page import="java.text.SimpleDateFormat; org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="matmain"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css"')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'gh-buttons.css"')}"/>

	%{--<title><g:message code="default.list.label" args="[entityName]"/></title>--}%
    <title>My Analyses</title>

<g:javascript>
  var markAnalysisForDeletion = function(elt, analysisId, deleteState, colspan) {
  	// alert("about to mark for delete: " + deleteState)
    var it = $(elt);
    $.post(getBase()+"/analysis/markForDeletion", { id: analysisId, deleteState: deleteState, colspan: colspan}, function(json) {
      // do something with message
      if (json.error)
      {
        $("div#delete-error").show().delay(3000).fadeOut(2000);
      }
      else if (deleteState == 1)
      {
        it.closest("tr").html(json.message);
        //it.closest("tr").html('<td colspan="6">' + json.message + '</td>');
      } else { // restore the page.
      	window.location.reload();
      }
    });
  };
  $(document).ready(function() {
    $("table#withresults").delegate("input.completeInput", "change", function() {
    	// alert("completeInput Change!");
    	var boxState = 0;
    	if($(this).is(":checked")) {
    		boxState = 1;
     	}
        $.ajax({
            url: getBase() + '/analysis/togglePublished',
            type: 'POST',
            data: { matId:$(this).attr("id"), state: boxState },
//            success: function() {
//              	window.location.reload();
//			}                     
       	});
    }).delegate("input.metacatInput", "change", function() {
    	// alert("metacatInput Change!");
    	var boxState = 0;
    	if($(this).is(":checked")) {
    		boxState = 1;
     	}
        $.ajax({
            url: getBase() + '/analysis/toggleMetacat',
            type: 'POST',
            data: { matId:$(this).attr("id"), state: boxState },
//            success: function() {
//              	window.location.reload();
//			}                     
       	});
    });
    
    var oldName = null;
    var updateDisplayName = function(elt) {
      var text = $.trim($(elt).text());
      var id = $(elt).attr("id");
      if (oldName !== text) {
        var args = { analysisId:id, text:text };
        $.post(getBase()+"/analysis/updateDisplayName", args, function(name) {
          $(elt).html(name);
        });
        oldName = null;
      }
    };
    $(".name-edit").bind({
      focus: function() {
        oldName = $.trim($(this).text());
      },
      blur: function() {
        updateDisplayName(this);
      },
      keyup: function(e) {
        if (e.keyCode === 13) {
          $(this).trigger("blur");
        } else if (e.keyCode === 27) {
          $(this).html(oldName);
          $(this).trigger("blur");
        }
      }
    });
  });
</g:javascript>
</head>

<body>

<div class="mat-container">

	<h2 class="mat-list">My Analyses
		<!--
		<div class="input">
                <div class="button-group" id="chartBorder">
                  <span class="yesNo button">View My Jobs</span>
                  <span class="yesNo button active">View All Jobs</span>
                </div>
        </div> 	-->
</h2>

	<div class="list">
	
		<!-- With Results Table -->
		<table id="withresults" class="zebra-striped mat-list-table tablesorter">
			<thead>
			<tr>
				<!--  <th>${message(code: 'analysis.id.label', default: 'Id')}</th> -->
        <th></th>
				<th>${message(code: 'analysis.datasetName.label', default: 'Dataset Name')}</th>
				<th>${message(code: 'analysis.runDate.label', default: 'Run Date')}</th>
				<g:if test="${matAdmin}">
					<th>${message(code: 'analysis.user.label', default: 'User')}</th>
				</g:if>
				<th>${message(code: 'analysis.published.label', default: 'Publish Results')}</th>
				<th>${message(code: 'analysis.published.label', default: 'Metacat Available')}</th>
				<th>Delete</th>
			</tr>
			</thead>
			<tbody>

			<g:each in="${jobsWithResults}" status="i" var="analysisInstance">
				<g:if test="${analysisInstance.key.user.equals(user.username) || matAdmin}">
				<tr>
					<!--  <td>${fieldValue(bean: analysisInstance.key, field: "id")}</td> -->
					<td><g:link action="show" id="${analysisInstance.key.id}" class="btn primary small">View</g:link></td>
					<td><span id="${analysisInstance.key.id}" contenteditable="true" class="name-edit">${analysisInstance.key.displayName ?: analysisInstance.key.datasetName}</span></td>
					<td><g:formatDate date="${analysisInstance?.key?.runDate}" format="MM-dd-yyyy"/></td>
					<g:if test="${matAdmin}">
						<td>${fieldValue(bean: analysisInstance.key, field: "user")}</td>
					</g:if>
					<td>
						<span class="checkbox_item" style="text-align:center;">
            				<!-- See jQuery for complete_input that uses ajax to toggle completed bit in database -->
            				<g:checkBox class="completeInput" name="complete${analysisInstance.key.id}" id="${analysisInstance.key.id}" value="${analysisInstance.key.matPublished}" />
            			</span>
            		</td>
            		<td>
						<span class="checkbox_item" style="text-align:center;">
            				<!-- See jQuery for complete_input that uses ajax to toggle completed bit in database -->
            				<g:checkBox class="metacatInput" name="metacat${analysisInstance.key.id}" id="${analysisInstance.key.id}" value="${analysisInstance.key.metacatPublished}" />
            			</span>
            		</td>
            		
                    <td style="text-align:center;">
                        <button class="ui-icon-cross" title="Mark this analysis for deletion" onclick="javascript:markAnalysisForDeletion(this,${analysisInstance.key.id}, 1, 6);"/>
                    </td>
				</tr>
			</g:if>
			</g:each>
			</tbody>
		</table>
	</div>



	<script type="text/javascript">

		function toggle(section) {
			section = section || 'noresults';
			var ele = document.getElementById(section);
			if (ele.style.display == "block") {
				ele.style.display = "none";
			} else {
				ele.style.display = "block";
			}
		}

	</script>

	<!--<a href="javascript:toggle( 'noresults');"></a>-->
	<h2>Currently Running Jobs and Jobs without Results (Error Log)</h2>

	<!-- Without Results Table -->
	<div class="list"> 
		<table id="noresults" class="zebra-striped mat-list-table tablesorter">
			<thead>
			<tr>
				<!-- <th>${message(code: 'analysis.id.label', default: 'Id')}</th> -->
				<th>${message(code: 'analysis.datasetName.label', default: 'Dataset Name')}</th>
				<th>${message(code: 'analysis.runDate.label', default: 'Run Date')}</th>
				<g:if test="${matAdmin}">
					<th>${message(code: 'analysis.user.label', default: 'User')}</th>
				</g:if>
				<th>Report</th>
				<th>Delete</th>
			</tr>
			</thead>
			<tbody>
			<g:each in="${jobsWithoutResults}" status="i" var="analysisInstance">
				<g:if test="${analysisInstance.key.user.equals(user.username) || matAdmin}">
				<tr>
					<!-- <td>${fieldValue(bean: analysisInstance.key, field: "id")}</td> -->
					%{--<td><g:link action="show" id="${analysisInstance.key.id}">${fieldValue(bean: analysisInstance.key, field: "datasetName")}</g:link></td>--}%
					<td><g:link action="show" id="${analysisInstance.key.id}">${analysisInstance.key.displayName ?: analysisInstance.key.datasetName}</g:link></td>
					<td><g:formatDate date="${analysisInstance?.key?.runDate}" format="MM-dd-yyyy"/></td>
					<g:if test="${matAdmin}">
						<td>${fieldValue(bean: analysisInstance.key, field: "user")}</td>
					</g:if>
					<td>
						<g:if test="${analysisInstance.value?.length() > 0}">
							<a href="${analysisInstance.value}">Error Log</a>
						</g:if>
						<g:if test="${analysisInstance.value?.length() == 0}">
							Unknown Error
						</g:if>
					</td>
                    <td style="text-align:center;">
                        <button class="ui-icon-delete" title="Mark this analysis for deletion" onclick="javascript:markAnalysisForDeletion(this,${analysisInstance.key.id}, 1, 5);"/>
                    </td>
					
				</tr>
				</g:if>
			</g:each>
			</tbody>
		</table>
	</div>


<g:render template="/common/emailLink"/>
<g:render template="/common/bugReporter" model="[tool:'MAT']"/>
</body>
</html>