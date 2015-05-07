<%@ page import="org.sagres.rankList.RankListParams" %>
<h3>Module Analysis</h3>

<table class="zebra-striped" style="width: 650px;">
  <thead>
  <tr>
    <th style="width:260px;" style="vertical-align:top;">Group Set</th>
    <th>&nbsp;</th>
    <g:if test="${!groupSetToAnalyses.isEmpty()}">
      <th>&nbsp;</th>
    </g:if>
  </tr>
  </thead>
  <tbody>
  <g:each in="${sampleSet.groupSets.sort { it.name } }" var="groupSet">
    <tr>
      <td>${groupSet.name}</td>
      <td style="text-align: right;">
        <g:if test="${editable && hasAssociatedChipId}">
          <g:link controller="analysis" action="create" class="btn primary small" target="_blank"
                  params="[datasetName:sampleSet.name.toString().replaceAll(' ', '_') + '_'+groupSet.name.toString().replaceAll(' ', '_'), expressionDataFile:sampleSet.id, dataSetGroups:groupSet.id]">
            Create Analysis
          </g:link>
        </g:if>
        <g:if test="${editable}">
            &nbsp;
          <g:link controller="rankListParams" action="saveSampleSelection" class="btn primary small" target="_blank"
                  params="[sampleSetId:sampleSet.id, sampleSetGroupSetId:groupSet.id]">
            Create Rank Lists
          </g:link>
        </g:if>
        <g:else>

        </g:else>
      </td>
      <g:if test="${!groupSetToAnalyses.isEmpty()}">
      <td style="text-align: right;">
        <g:if test="${groupSetToAnalyses.containsKey((int)groupSet.id)}">
          <button class="btn primary small view-analysis" data-keyboard="true" data-backdrop="true" data-controls-modal="view${groupSet.id}-modal">View Analysis</button>
          <div id="view${groupSet.id}-modal" class="modal hide" >
            <div class="modal-header">
              <a href="#" class="close">×</a>
              <h4>View Analysis Results</h4>
            </div>
            <div class="modal-body" style="overflow-y:scroll; max-height:300px;">
              <table class="zebra-striped">
                <div id="delete-error" class="alert-message error" style="display:none;">There was a problem deleting the analysis.</div>
                <g:set var="acount" value="${groupSetToAnalyses.get((int)groupSet.id).size()}"/>
                <p>${acount} analysis result${acount != 1 ? "s" : ""} found. Click on a link to view.</p>
                <thead>
                  <tr><th>Analysis</th><th>Run Date</th>
                    <g:if test="${editable}">
                      <th style="width:20px;"></th>
                    </g:if>
                  </tr></thead>
                <tbody>
                <g:each in="${groupSetToAnalyses.get((int)groupSet.id)}" var="aMap" status="i">
 
                  <tr>
                    <td><g:link controller="analysis" action="show" id="${aMap.analysis}" target="_blank" onclick="closeModal('view${groupSet.id}-modal');">${aMap.name.encodeAsAbbreviate()}</g:link></td>
                    <td><g:formatDate date="${aMap.runDate}" format="MM-dd-yyyy"/></td>
                    <g:if test="${editable}">
                      <td style="text-align:center;">
                        <button class="ui-icon-cross" title="Mark this analysis for deletion" onclick="javascript:markAnalysisForDeletion(this,${aMap.analysis}, 1, 2);"/>
                      </td>

                  </tr>
                  </g:if>
                </g:each>
                </tbody>
              </table>
 <!--         <g:if test="${groupSetToAnalyses.get((int)groupSet.id).size() > 5}">
                <p style="margin-left:5px;">
                  <g:link controller="mat" action="list" params="[sampleSetId:sampleSet.id, dataSetGroups:((int)groupSet.id)]">More analysis results...</g:link>
                </p>
              </g:if> -->
            </div>
            <div class="modal-footer">
              <button class="btn primary small" onclick="closeModal('view${groupSet.id}-modal');">Cancel</button>
            </div>
          </div>
        </g:if>&nbsp;
      </td>
      </g:if>
    </tr>
  </g:each>
  </tbody>
</table>
<g:javascript>
  var markAnalysisForDeletion = function(elt,analysisId, deleteState, colspan) {
    var it = $(elt);
    $.post(getBase()+"/analysis/markForDeletion", { id: analysisId, deleteState: deleteState, colspan: colspan}, function(json) {
      // do something with message
      if (json.error)
      {
        $("div#delete-error").show().delay(3000).fadeOut(2000);
      }
      else
      {
        it.closest("tr").html(json.message);
        //it.closest("tr").html('<td colspan="3">' + json.message + '</td>');
      }
    });
  };
</g:javascript>