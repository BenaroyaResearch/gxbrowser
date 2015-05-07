<%@ page import="org.sagres.sampleSet.component.LookupListType; grails.converters.JSON; org.sagres.sampleSet.component.LookupList" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>


  <title>Lookup List :: ${lookupList.name}</title>

  <g:javascript>
    $(document).ready(function() {
    $(".editable-text").live("focus", function() {
      localStorage.setItem("${lookupList.id}::"+this.id, $(this).val());
    }).live("blur", function() {
      var args = { property: $(this).attr("id"), value: $(this).val() };
      $.post(getBase()+"/lookupList/setter/${lookupList.id}", args);
    }).bind("keyup", function(e) {
      var code = (e.keyCode ? e.keyCode : e.which);
      if (code === 27)
      {
          var id = this.id;
          var oldValue = localStorage.getItem("${lookupList.id}::"+id);
          $(this).val(oldValue).trigger("blur");
      }
    });

      $('.updateLookupListType').change(function() {
        var lookupList = $(this).attr("name");
        var value = $(this).val();
        if (value != '-select-')
        {
          if (value == 'Other (specify)')
          {
            $(this).parent().find('#hiddenOther').show()
            $(this).parent().find('#hiddenOther>input').focus();
          }
          else
          {
            $(this).parent().find('#hiddenOther').hide();
            $.ajax({
              type : "POST",
              url  : "../../lookupList/setter/${lookupList.id}",
              data : { property: "type", value: value },
              cache: false,
              async : false
            });
          }
        }
        else
        {
          $(this).parent().find('#hiddenOther').hide();
        }
      });

      $('.other').keypress(function(e) {
        if (e.keyCode == 13)
        {
          var lookupList = $(this).attr("id");
          var value = $(this).val();
          var hiddenField = $(this).parent();
          var newValue;
          $.ajax({
            type : "POST",
            url  : "../../lookupList/setter/${lookupList.id}",
            data : { value: value, property: "type" },
            cache: false,
            async : false,
            success: function(result) {
              hiddenField.find('.other').val("");
              hiddenField.hide();
            newValue = result;
            }
          });
          if (newValue != "duplicate")
          {
            $(this).parent().parent().find('.updateLookupListType option[value="Other"]').remove();
            $(this).parent().parent().find('.updateLookupListType').append(new Option(newValue,newValue));
            $(this).parent().parent().find('.updateLookupListType').append(new Option("Other","Other"));
          }
          else
          {
            newValue = value;
          }
          $(this).parent().parent().find('.updateLookupListType').val(newValue);
        }
      });
    });
  </g:javascript>
</head>

<body>
<div class="sampleset-container">

    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
  <div class="page-header"><h2>${lookupList.name}</h2></div>
<g:form controller="lookupList" id="${lookupList.id}">
      <fieldset>
        <legend>Edit</legend>
        <div class="clearfix">
          <label for="description">Description</label>
          <div class="input">
            <g:textArea name="description" rows="5" cols="50" class="editable-text xlarge">${lookupList.description}</g:textArea>
          </div>
        </div>
        <div class="clearfix">
          <label for="type">Type</label>
          <div class="input">
            <g:select from="${(LookupListType.list().collect { llt -> llt.name }+['Other (specify)'])}"
                name="type"
                noSelection="['-select-':'-select-']"
                value="${lookupList.type}"
                class="updateLookupListType"/>
            <div id="hiddenOther" style="display:none;" class="top-margin-less">
                    <g:textField name="${lookupList.id}" class="other xlarge"/>
                    <span class="help-block" style="margin: 0 0 0.1em 0.4em;">Press enter to add to ${lookupList.name} and ESC to cancel</span>
                  </div>
          </div>
        </div>
      </fieldset>
  <fieldset>
    <legend>Options</legend>
      <div class="clearfix">
        <label>Options</label>
        <div class="input">
          <ul class="unstyled">
          <g:if test="${lookupList.lookupDetails}">
        <g:each in="${lookupList.lookupDetails}" var="lookupDetail">
          <li>${lookupDetail.name}
            %{--<g:if test="${counts[lookupDetail.name] > 0}"> (${counts[lookupDetail.name]})--}%
            %{--</g:if>--}%
          </li>
        </g:each>
            </ul>
      </g:if>
      <g:else>
        <div class="hint" style="padding-bottom: 15px">Add some options using the text field below...</div>
      </g:else>
        </div>
        </div>
    <div class="clearfix">
      <label for="name">Add Option</label>
      <div class="input">
          <g:textField name="name"/> <button class="btn primary" type="submit" name="_action_addDetail">Go</button>
      </div>
    </div>
  </fieldset>
    </g:form>


    </div>
</div>
</body>
</html>