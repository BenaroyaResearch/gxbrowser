<div id="bugReportPanel" class="pullout">
  <div class="page-header">
    <h4 style="max-width:100%">Send a Feedback Report</h4>
  </div>
  <div id="reportError" class="alert-message block-message error" style="display:none;"><strong>There was an error sending your feedback report. Please fill out the form and try again.</strong></div>
  <g:form name="bugReportForm" class="form-stacked">
    <fieldset>
      <div class="clearfix">
        <label for="sender">Your Email: <span class="send-email-help">(so we can get back to you)</span></label>
        <div class="input">
          <g:textField name="sender" class="xxlarge" onkeyup="updateSendButton(this);"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="subject">Issue:</label>
        <div class="input">
          <g:textField name="subject" class="xxlarge"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="comment">Please provide some details about the problem you're experiencing :</label>
        <div class="input">
          <g:textArea name="comment" rows="5" cols="10" class="xxlarge"/>
        </div>
      </div>
    </fieldset>
  </g:form>
  <div class="button-actions" style="clear: both;">
    <button class="btn" onclick="closeBugReportPanel();">Close</button>
    <button id="sendBtn" class="btn primary" onclick="sendBugReport();" disabled="disabled">Send</button>
  </div>
</div>
<div id="bugReportConfirmPanel" class="pullout">
  <div style="padding:10px;">
    <span style="padding-right:5px;"><strong>Your feedback report has been sent!</strong></span>
  </div>
  <div class="button-actions" style="clear: both;">
    <button class="btn" onclick="closeBugReportConfirmPanel();">Close</button>
  </div>
</div>
<g:javascript>
  var reportBug = function() {
    if (typeof generateArgs === "function") {
      var args = generateArgs();
      $.getJSON(getBase()+"/miniURL/create", args, function(json) {
        var link = "${createLink(controller:"miniURL", action:"view", absolute:"true")}".concat("/").concat(json.link);
        var subject = "${params.controller} - ${params.action}";
        var text = "Bug report - ".concat(link);
        $("form#bugReportForm #subject").val(subject);
        $("form#bugReportForm #comment").val(text);
        $("div#bugReportPanel #sendBtn").attr("disabled","disabled");
        showBugReportPanel();
      });
    } else {
      var subject = "${params.controller} - ${params.action}";
      var text = "Bug report - ${createLink(controller:params.controller, action:params.action, id:params.id, absolute:true)}";
      $("form#bugReportForm #subject").val(subject);
      $("form#bugReportForm #comment").val(text);
      $("div#bugReportPanel #sendBtn").attr("disabled","disabled");
      showBugReportPanel();
    }
  };
  var showBugReportPanel = function() {
    $("div#bugReportPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
  };
  var closeBugReportPanel = function() {
    $("div#bugReportPanel input").val("");
    $("div#bugReportPanel textarea").val("");
    $("div#bugReportPanel").hide().css({ top:0, left:0 });
    $("div#reportError").hide();
  };
  var closeBugReportConfirmPanel = function() {
    $("div#bugReportConfirmPanel").hide().css({ top:0, left:0 });
  };
  var updateSendButton = function(input) {
    // simple email regex - not the most robust
    var email = $(input).val();
    if (/\S+@\S+/.test(email)) {
      $("div#bugReportPanel #sendBtn").removeAttr("disabled");
    } else {
      $("div#bugReportPanel #sendBtn").attr("disabled","disabled");
    }
  };
  var sendBugReport = function(e) {
    var form = $("form#bugReportForm");
    var args = {
      recipients: "softdevteam@benaroyaresearch.org",
      sender: form.find("#sender").val(),
      subject: form.find("#subject").val(),
      message: form.find("#comment").val(),
      ccEmail: "abjork@benaroyaresearch.org"
    };
    $.post(getBase()+"/miniURL/emailLink", args, function(json) {
      if (json.error) {
        $("div#reportError").show();
      } else {
        $("div#reportError").hide();
        closeBugReportPanel();
        $("div#bugReportConfirmPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
      }
    });
  };
</g:javascript>

