<g:javascript>
  $(document).ready(function() {
    $('#confirmation-dialog').dialog({
      autoOpen  : false,
      title     : 'Are you sure?',
      resizable : false,
      width     : 300,
			height    : 160,
			modal     : true,
      zIndex    : 10300
    });
  });
</g:javascript>
<div id="confirmation-dialog">
  <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>${message}</p>
</div>