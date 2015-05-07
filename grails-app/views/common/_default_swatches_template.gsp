<g:javascript>
  $(document).ready(function() {
    function hideColorPalette() {
      var visible = !$('#presetColors').is(':hidden');
      if (visible && !mouseOverActiveElement) {
        $('#presetColors').hide();
      }
    }

    var mouseOverActiveElement = false;
    $('#presetColors').live('mouseenter', function() {
      mouseOverActiveElement = true;
    }).live('mouseleave', function() {
      mouseOverActiveElement = false;
    });

    $('html, input, button').click(function() {
      hideColorPalette();
    });

    $('select').focus(function() {
      hideColorPalette();
    });
  });
</g:javascript>
<div id="presetColors">
<div>
<g:each status="i" var="color" in="${['#ec2f68','#eca41b','#e54e24','#a73d02','#b51d23','#7f251e','#5a2100','#cc4e44','#fe5b85','#b22e50',
                                      '#83285b','#651026','#360621','#51316d','#261d4e','#10355a','#18487a','#2e6aa7','#0e66c0','#5ba7f3',
                                      '#2893c0','#22918f','#02a7a4','#1b4141','#186977','#1a673f','#44cc7f','#117f41','#02a748','#9acd60',
                                      '#0d360b','#365834','#9f980b','#9d9a64','#4d5b45','#2c3419','#c08b4e','#736357','#8d530f','#5a3b07',
                                      '#322714','#a77a2e','#222222','#464646','#787878','#171717'].unique().sort()}">
  %{--<g:if test="${(i % 10) == 0}">--}%
    %{--<div>--}%
  %{--</g:if>--}%
  <div class="preset-color"><div style="background: ${color};"></div></div>
  <g:if test="${i > 0 && (i % 10) == 0}">
    </div><div>
  </g:if>
</g:each>
</div>
</div>