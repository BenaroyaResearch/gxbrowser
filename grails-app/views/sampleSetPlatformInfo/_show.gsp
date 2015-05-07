<%@ page import="org.sagres.sampleSet.component.LookupList" %>
<g:javascript>
  $(document).ready(function() {
  
  	if ("${sampleSetPlatFormInfo.platform}" == "RNA-seq" || "${sampleSetPlatFormInfo.platform}" == "RNAseq") {
        $('#libraryPrepRegion').show();
  	}

    function ajaxify(type, url, data) {
      $.ajax({
        type    : type,
        url     : url,
        data    : data,
        cache   : false,
        async   : false
      });
    }

    $('#platform').change(function() {
      var name = $(this).val();
      if (name != '-select-')
      {
        ajaxify("POST", "../../sampleSetPlatformInfo/setPlatform/${sampleSetPlatFormInfo.id}", { value: name });
        $.getJSON("../../sampleSetPlatformInfo/getPlatformOptions", { ajax: "true", platform: name }, function(result) {
          var options = "<option value='-select-'>-select-</option>";
          for (var i = 0; i < result.length; i++)
          {
            options += "<option value='" + result[i] + "'>" + result[i]+ "</option>";
          }
          $("#platformOption").html(options);
        });

        $(this).find('option[value="-select-"]').remove();
        if (name == 'RNA-seq' || name == 'RNAseq') {
        	$('#libraryPrepRegion').show();
        } else {
        	$('#libraryPrepRegion').hide();
        }
      }
      else
      {
        $("#platformOption").html("<option value='-select-'>-select-</option>");
      }
    });

    $('#platformOption').change(function() {
      var name = $(this).val();
      if (name != '-select-')
      {
        ajaxify("POST", "../../sampleSetPlatformInfo/setter/${sampleSetPlatFormInfo.id}", { property: "platformOption", value: name });
        $(this).find('option[value="-select-"]').remove();
      }
    });
    
    $('#libraryPrepOption').change(function() {
      var name = $(this).val();
      if (name != '-select-')
      {
        ajaxify("POST", "../../sampleSetPlatformInfo/setter/${sampleSetPlatFormInfo.id}", { property: "libraryPrepOption", value: name });
        $(this).find('option[value="-select-"]').remove();
      }
    });
    
  });
</g:javascript>
<form>
  <fieldset>
    <div class="clearfix">
      <label for="platform"><!--Platform--> Technology:</label>
      <div class="input">
        <sec:ifLoggedIn>
        <g:select from="${platforms}" name="platform"
          noSelection="['-select-':'-select-']" optionKey="name" optionValue="name"
          value="${sampleSetPlatFormInfo.platform}" disabled="${!editable}"/>
         </sec:ifLoggedIn>
         <sec:ifNotLoggedIn>
              <span class="notlogged-replace"> ${sampleSetPlatFormInfo.platform}</span>
         </sec:ifNotLoggedIn>
      </div>
    </div>
     <div class="clearfix">
      <label for="platformOption">Platform<!--Technology-->: </label>
      <div class="input">
        <sec:ifLoggedIn>
        <g:select from="${platformOptions}" name="platformOption"
          noSelection="['-select-':'-select-']"
          value="${sampleSetPlatFormInfo.platformOption}" disabled="${!editable}"/>
        </sec:ifLoggedIn>
        <sec:ifNotLoggedIn>
           <span class="notlogged-replace"> ${sampleSetPlatFormInfo.platformOption}</span>
        </sec:ifNotLoggedIn>
      </div>
    </div>
    <div id="libraryPrepRegion" class="clearfix" style="display: none;">
      <label for="libraryPrepOption">Library Preparation:</label>
      <div class="input">
        <sec:ifLoggedIn>
        <g:select from="${libraryPrepOptions}" name="libraryPrepOption"
          noSelection="['-select-':'-select-']"
          value="${sampleSetPlatFormInfo.libraryPrepOption}" disabled="${!editable}"/>
        </sec:ifLoggedIn>
        <sec:ifNotLoggedIn>
           <span class="notlogged-replace"> ${sampleSetPlatFormInfo.libraryPrepOption}</span>
        </sec:ifNotLoggedIn>
      </div>
    </div>
    
  </fieldset>
</form>
