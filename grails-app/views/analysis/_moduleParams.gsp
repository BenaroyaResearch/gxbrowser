<div id="modParamsPanel" class="pullout" style="width:700px; height:450px;">
  <div class="page-header">
    <h4 style="max-width:100%">Module Analysis Parameters</h4>
  </div>
  <p style="text-align: left;">The analysis shown here was performed
  <g:if test="${sampleSetName}">on the <em>'${sampleSetName}'</em> sample set</g:if> using <span style="font-weight:bold;">
${groups.keySet().join(" and ")}</span> as the groups.
To compare these groups, the <span style="font-weight:bold;">${analysisInstance.deltaType == "fold" ? "fold change" : "zcore" }</span> method was used with
a cutoff of <span style="font-weight:bold;">${analysisInstance.deltaType == "fold" ? analysisInstance.foldCut : foldCut.zscoreCut}</span>.
  The ${analysisInstance.multipleTestingCorrection == "TRUE" ? "false discovery rate" : "p-value threshold" } was set to
  <span style="font-weight:bold;"><span name="pvalue-text">${Math.round(analysisInstance.fdr * 100)}</span> percent</span>,
    and multiple testing correction was <span style="font-weight:bold;">${analysisInstance.multipleTestingCorrection == "TRUE" ? "On" : "Off" }</span>.
    <a href="${resource(dir: "", file :"matwizmore.gsp#faq")}" target="_blank">Details on the parameters can be found in the FAQ</a>.
</p>
  %{--<div><a onclick="showMoreDetails(this);return false;" style="cursor:pointer;">Show details...</a></div>--}%
  <div id="more-details">
      <div style="height:250px; overflow: auto;">
    <table class="zebra-striped">
      <thead><tr><th>Parameter</th><th>Value</th></tr></thead>
      <tbody>
        <g:each in="${analysisInstance.returnScriptParameters()}" var="p">
          <tr><td>${p.key.encodeAsHumanize()}</td>
            <td>
              <g:if test="${p.key == 'false_discovery_rate'}">
                <span name='pvalue-text-orig'>
              </g:if>
              ${p.value}
              <g:if test="${p.key == 'false_discovery_rate'}">
                </span>
              </g:if>
            </td>
          </tr>
        </g:each>
      </tbody>
    </table>
          </div>
  </div>
  <div class="button-actions" style="clear:both; position:absolute; bottom:10px; width:80%; left:70px;">
    <button class="btn" onclick="closeModuleParams();">Close</button>
  </div>
</div>
<g:javascript>
  var showModuleParams = function() {
    $("div#modParamsPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
  };
  var closeModuleParams = function() {
    $("div#modParamsPanel").hide().css({ top:0, left:0 });
  };
  var showMoreDetails = function(elt) {
    var moreDetails = $("div#modParamsPanel div#more-details");
    if (moreDetails.is(":visible")) {
      $(elt).html("Show details...");
    } else {
      $(elt).html("Hide details...");
    }
    moreDetails.toggle();
  };
</g:javascript>