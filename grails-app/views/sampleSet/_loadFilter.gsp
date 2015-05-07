<g:javascript>
  var loadFilter = function() {
    $("form#load-filter").submit();
  };
</g:javascript>
<div id="loadfilter-modal" class="modal hide" style="width: 300px;">
  <div class="modal-header">
    <a href="#" class="close">×</a>
    <h3>Load Filter</h3>
  </div>
  <div class="modal-body">
    <g:form name="load-filter" controller="sampleSet" action="loadFilter">
    <p>
      <g:if test="${savedFilters?.size() > 0}">
        Select a filter to load:<br/>
        <g:select from="${savedFilters}" name="filterName"/>
      </g:if>
      <g:else>
        No save filters found.
      </g:else>
    </p>
    </g:form>
  </div>
  <div class="modal-footer">
    <button class="btn primary" onclick="loadFilter();closeModal('loadfilter-modal');" ${savedFilters?.isEmpty() ? "disabled='true'" : ""}>Load</button>
    <button class="btn" onclick="closeModal('loadfilter-modal');">Cancel</button>
  </div>
</div>