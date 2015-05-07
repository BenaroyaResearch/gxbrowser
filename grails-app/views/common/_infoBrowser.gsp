<div id="info-browser" class="container-fluid pullout" style="max-width:500px">
  <div class="page-header">
    <h4 style="max-width:100%">Info Browser</h4>
  </div>
  <img id="info-loading" src="${resource(dir:'images/icons', file:'loading_filter.gif')}" class="loadingIcon" alt="Loading..."/>
  <div id="info-browser-content" style="overflow-y:auto;max-height:400px;"></div>
  <div class="button-actions" style="clear:both;">
    <button class="btn" onclick="closeInfoBrowserPanel();">Close</button>
  </div>
</div>
<g:javascript>
  var setInfoLoading = function() {
    $("img#info-loading").show();
    $("div#info-browser-content").html("");
  };
  var setInfoBrowserTitle = function(title) {
    $("div#info-browser div.page-header h4").html(title);
  };
  var setInfoBrowserContent = function(html) {
    $("img#info-loading").hide();
    $("div#info-browser-content").html(html);
  };
  var showInfoBrowser = function() {
    $("div#info-browser").position({ my:"center", at:"center", of:$(window) }).show("drop");
  };
  var closeInfoBrowserPanel = function() {
    $("div#info-browser").hide().css({ top:0, left:0 });
  };
</g:javascript>