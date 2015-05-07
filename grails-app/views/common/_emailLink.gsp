<div id="emailLinkPanel" class="pullout">
    <div class="page-header">
      <h4 style="max-width:100%">Email Link</h4>
    </div>
      <div id="emailError" class="alert-message block-message error" style="display:none;"><strong>There was an error sending your email. Please check your information and try again.</strong></div>
      <g:form name="emailLinkForm" class="form-stacked">
        <fieldset>
          <div class="clearfix">
            <label for="recipients">Recipients: <span class="send-email-help">(separate multiple addresses with a comma or semicolon)</span></label>
            <div class="input">
              <g:textField name="recipients" class="xxlarge" onkeyup="javascript:updateLinkSendButton();"/>
            </div>
          </div>
          <div class="clearfix">
            <label for="sender">Your Email:</label>
            <div class="input">
              <g:textField name="sender" class="xxlarge" onkeyup="javascript:updateLinkSendButton();"/>
              <div  class="attach-email-copy" style="text-align:left;">
               <label for="copyMe"><g:checkBox name="copyMe"/> <span>Send a copy of this email to me</span></label>
            </div>
            </div>
          </div>
          <div class="clearfix">
            <label for="subject">Subject:</label>
            <div class="input">
              <g:textField name="subject" class="xxlarge" value="Gene Expression Browser Link"/>
            </div>
          </div>
          <div class="clearfix">
            <label for="message">Message:</label>
            <div class="input">
              <g:textArea name="message" rows="5" cols="10" class="xxlarge"/>
            </div>
          </div>
        </fieldset>
      </g:form>
      <div class="button-actions" style="clear: both;">
        <button class="btn" onclick="closeEmailPanel();">Close</button>
        <button id="sendBtn" class="btn primary" onclick="sendEmail();" disabled="disabled">Send</button>
      </div>
  </div>
  <div id="emailConfirmPanel" class="pullout">
    <div style="padding:10px;">
      <span id="confirmMessage" style="padding-right:5px;"><strong>Your email has been sent!</strong></span>
    </div>
    <div class="button-actions" style="clear: both;">
      <button class="btn" onclick="closeEmailConfirmPanel();">Close</button>
    </div>
  </div>
  <div id="copyLinkPanel" class="pullout">
    <div class="page-header">
      <h4 style="max-width:100%">Copy Link</h4>
    </div>
    <div style="padding:10px;">
      <g:textField name="copyLinkValue" class="xlarge"/>
    </div>
    <div class="button-actions" style="clear: both;">
      <button class="btn" onclick="closeLinkPanel();">Close</button>
    </div>
  </div>
  <g:javascript>
    var showEmailPanel = function() {
      $("div#emailLinkPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
    };
    var showLinkPanel = function(link) {
      $("div#copyLinkPanel #copyLinkValue").val(link);
      $("div#copyLinkPanel").position({ my:"center", at:"center", of:$(window) }).show("drop", function() {
        $("div#copyLinkPanel #copyLinkValue").select();
      });
    };
    var closeEmailPanel = function() {
      $("div#emailLinkPanel input").val("");
      $("div#emailLinkPanel textarea").val("");
      $("div#emailLinkPanel input[type='checkbox']").attr("checked",false);
      $("div#emailLinkPanel").hide().css({ top:0, left:0 });
      $("div#emailError").hide();
    };
    var closeEmailConfirmPanel = function() {
      $("div#emailConfirmPanel").hide().css({ top:0, left:0 });
    };
    var closeLinkPanel = function() {
      $("div#copyLinkPanel").hide().css({ top:0, left:0 });
    };
    var updateLinkSendButton = function() {
      // simple email regex - not the most robust
      var sender = $("form#emailLinkForm #sender").val();
      var receiver = $("form#emailLinkForm #recipients").val();
      if (/\S+@\S+/.test(sender) && /\S+@\S+/.test(receiver)) {
        $("div#emailLinkPanel #sendBtn").removeAttr("disabled");
      } else {
        $("div#emailLinkPanel #sendBtn").attr("disabled","disabled");
      }
    };
    var sendEmail = function(e) {
      var form = $("form#emailLinkForm");
      var args = {
        recipients: form.find("#recipients").val(),
        sender: form.find("#sender").val(),
        subject: form.find("#subject").val(),
        message: form.find("#message").val()
      };
      if (form.find("#copyMe").is(":checked")) {
        args.ccEmail = args.sender;
      }
      $.post(getBase()+"/miniURL/emailLink", args, function(json) {
        if (json.error) {
          $("div#emailError").show();
        } else {
          $("div#emailError").hide();
          closeEmailPanel();
          $("div#emailConfirmPanel").position({ my:"center", at:"center", of:$(window) }).show("drop");
        }
      });
    };
  </g:javascript>