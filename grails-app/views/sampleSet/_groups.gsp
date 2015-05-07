<style type="text/css">
  #groupset-table { width: auto; }
</style>
<g:javascript>
  $(document).ready(function() {
    $("button#create-groupset").click(function() {
      var form = $(this).closest("form");
      var groupSetName = form.find("input#name").val().trim();
      if (groupSetName === "")
      {
        form.find("div.input").append("<span class='alert-message error'><strong>Whoops!</strong> Please enter a name for the group set.</span>");
      }
      else
      {
        form.submit();
      }
      return false;
    });

    $(".groupset-default").click(function() {
      var previousDefault = $("button.groupset-default[disabled]");
      previousDefault.closest("tr").find("td.defaultIcon").html("")
      previousDefault.removeAttr("disabled");
      $(this).attr("disabled", true);
      $(this).closest("tr").find("td.defaultIcon").html("<span class='label'>Default</span>");
      var groupSetId = this.id.split("-")[1];
      $.post(getBase()+"/sampleSet/setDefaultGroupSet/"+${sampleSet.id}, { groupSetId: groupSetId });
    });
  });
</g:javascript>
%{--<h3>Create Group Set</h3>--}%
%{--<g:form name="create-groupset-form" controller="datasetGroupSet" action="create" id="${sampleSet.id}">--}%
  %{--<fieldset>--}%
    %{--<div class="clearfix">--}%
      %{--<label for="name">New Group Set</label>--}%
      %{--<div class="input">--}%
        %{--<input type="text" id="name" name="name" class="large" ${editable ? "" : "disabled='true'"}/> <button id="create-groupset" type="submit" class="btn primary" ${editable ? "" : "disabled='true'"}>Create</button>--}%
      %{--</div>--}%
    %{--</div>--}%
  %{--</fieldset>--}%
%{--</g:form>--}%
<h3>
  Group Sets
  %{--<div style="padding: 10px 0;">--}%
  <g:if test="${sampleSet.groupSets}">
  <small style="margin-left:20px;">
  	<sec:ifLoggedIn>
		<g:link controller="sampleSetTabConfigurator" action="create" id="${sampleSet.id}" class="btn info">Clinical Tab Configuration</g:link>
  		<g:link controller="annotation" action="show" id="${sampleSet.id}" class="btn info">Overlay Configuration</g:link>
  	</sec:ifLoggedIn>
      	<g:link controller="geneBrowser" action="show" id="${sampleSet.id}" target="_blank" class="btn primary">View in Gene Expression Browser</g:link>
    %{--<g:link controller="geneBrowser" action="show" id="${sampleSet.id}" class="btn primary small" target="_blank" style="margin-left: 30px;">View Group Sets in Gene Expression Browser</g:link>--}%
  </small>
  </g:if>
  %{--</div>--}%
</h3>
  <table id="groupset-table" class="zebra-striped">
     <thead>
      <tr><th>Name</th><th>&nbsp;</th><th>&nbsp;</th><g:if test="${editable}"><th style="text-align:center;">Default Rank List</th></g:if></tr>
     </thead>
    <tbody>
  <g:if test="${sampleSet.groupSets}">
    <g:set var="defaultGroupSet" value="${sampleSet.defaultGroupSet?.id}"/>
    <g:each in="${sampleSet.groupSets.sort { it.name }}" var="groupSet">
      <g:set var="isDefault" value="${groupSet.id == defaultGroupSet}"/>
      <tr>
        <td>${groupSet.name}</td>
        <td class="defaultIcon">
          <g:if test="${isDefault}"><span class="label">Default</span></g:if>&nbsp;
        </td>
        <td style="text-align:right; min-width:170px;">

          <g:link controller="datasetGroupSet" action="view" id="${groupSet.id}" target="_blank" class="btn small primary">View</g:link>
          <sec:ifLoggedIn>
              &nbsp;<button id="groupset-${groupSet.id}" class="groupset-default btn small" ${isDefault || !editable ? "disabled='true'" : ""}>Make Default</button>
		  </sec:ifLoggedIn>
        </td>
        <g:if test="${editable}">
        <td style="text-align:center;">
          <g:if test="${groupSetToRankLists.get(groupSet.id)}">
            <g:select from="${groupSetToRankLists.get(groupSet.id)}" optionKey="id" optionValue="name"
                      name="defaultRankList" class="medium" value="${groupSet.defaultRankList?.id}"
                      noSelection="['-1':'-select a rank list-']" onchange="setDefaultRankList(this,${groupSet.id});"/>
          </g:if>
          <g:else>
            <span style="font-style: italic;">None Available</span>
          </g:else>
        </td>
        </g:if>
      </tr>
    </g:each>
  </g:if>
  </tbody>
  <tfoot>
    <tr>
      <td colspan="3" style="text-align:right;">
       <sec:ifLoggedIn>
           <button class="btn primary" id="new-groupset-btn"   ${!editable ? "disabled='true'" : ""}>New Group Set</button>
        </sec:ifLoggedIn>

        <sec:ifNotLoggedIn>

            <em>In order to make new Group Sets or change settings, please log in.</em>
        </sec:ifNotLoggedIn>
      </td>
    </tr>
  </tfoot>
  </table>

<div id="new-groupset-modal" class="modal hide" style="width:350px;">
  <div class="modal-header">
    <h4>New Group Set</h4>
  </div>
  <div class="modal-body">
    <g:form name="create-groupset-form" controller="datasetGroupSet" action="create" id="${sampleSet.id}">
      Name <g:textField name="name" style="margin-left:5px;"/>
    </g:form>
  </div>
  <div class="modal-footer">
    <button class="btn primary" id="create-groupset-btn" ${!editable ? "disabled='true'" : ""}>Create</button>
    <button class="btn primary" id="cancel-groupset-btn">Cancel</button>
  </div>
</div>
<g:javascript>
  $("button#new-groupset-btn").click(function() {
    $("div#new-groupset-modal").show();
    $("form#create-groupset-form>input#name").focus();
  });
  $("button#create-groupset-btn").click(function() {
    $("form#create-groupset-form").submit();
  });
  $("button#cancel-groupset-btn").click(function() {
    $("div#new-groupset-modal").hide();
  });

  var setDefaultRankList = function(input,groupSetId) {
    var rankListId = $(input).val();
    if (rankListId !== -1)
    {
      var args = { id:groupSetId, rankListId:rankListId };
      $.post(getBase()+"/datasetGroupSet/setDefaultRankList", args);
    }
  };
</g:javascript>