<style>

  .button-actions {
      text-align: center;
      padding-top: 12px;
      margin-top:20px;
      border-top: 1px solid #ddd;
      -webkit-box-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
      -moz-box-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
      box-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
    }

</style>

<div id="forgot-password-dialog" class="pullout" style="width:600px;">
  <div class="page-header">
    <h4 style="text-align: center;">Forgot Password</h4>
  </div>
  <div style="text-align: left;">
      <p>As a TrialShare user, you will need to contact your system administrator in order to have your password reset.</p>

<p>(your  Module Analysis Toolkit password and username are the same as your TrialShare ones.)</p>

  </div>
  <div class="button-actions" style="clear:both;">
    <button class="btn" onclick="closeForgotPassword();">Close</button>
  </div>
</div>
<g:javascript>
  var showForgotPassword = function() {
    $("div#forgot-password-dialog").position({ my:"center", at:"center", of:$(window) }).show("drop");
  };
  var closeForgotPassword = function() {
    $("div#forgot-password-dialog").hide().css({ top:0, left:0 });
  };
</g:javascript>