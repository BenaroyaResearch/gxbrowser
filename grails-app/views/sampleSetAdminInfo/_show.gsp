<g:javascript>
  $(document).ready(function() {
    $(".ui-icon-date").click(function() {
      var textField = $(this).closest(".input-append").find("input:text");
      if (!textField.is(":disabled"))
      {
        textField.datepicker("show");
        textField.datepicker("widget").position({
          my: "left top",
          at: "left bottom",
          of: textField
        });
      }
    });
    $('.datepicker').datepicker({
      changeMonth: true,
      changeYear: true
    }).change(function() {
      var newDate = $(this).val();
      var name = $(this).attr('name');
      $.ajax({
        type : "POST",
        url  : "../../sampleSetAdminInfo/setter/${sampleSetAdminInfo.id}",
        data : { property: name, value: newDate },
        cache: false,
        async : false
      });
    }).focus(function() {
      $('.datepicker').datepicker("widget").position({
        my: "left top",
        at: "left bottom",
        of: this
      });
    });
  });
</g:javascript>
<g:form name="sampleSetAdminInfo">
  <fieldset>
    <legend>Contributors / Contact Information</legend>

    <div class="clearfix">
      <div class="input">Please include address, email and telephone.</div>
    </div>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::analyst">Analyst</label>

      <div class="input">
        <input type="text" id="${sampleSetAdminInfo.id}::analyst" name="${sampleSetAdminInfo.id}::analyst" class="editable-text xxlarge"
                     value="${sampleSetAdminInfo.analyst}" ${editable ? "" : "disabled='true'"}/>
      </div>
    </div>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::principleInvestigator">Principal Investigator</label>

      <div class="input">
        <input type="text" id="${sampleSetAdminInfo.id}::principleInvestigator" name="${sampleSetAdminInfo.id}::principleInvestigator" class="editable-text xxlarge"
                     value="${sampleSetAdminInfo.principleInvestigator}" ${editable ? "" : "disabled='true'"}/>
      </div>
    </div>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::institution">Institution</label>

      <div class="input">
        <input type="text" id="${sampleSetAdminInfo.id}::institution" name="${sampleSetAdminInfo.id}::institution" class="editable-text xxlarge"
                     value="${sampleSetAdminInfo.institution}" ${editable ? "" : "disabled='true'"}/>
      </div>
    </div>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::contactPersons">Contact person(s)</label>

      <div class="input">
        <textarea id="${sampleSetAdminInfo.id}::contactPersons" name="${sampleSetAdminInfo.id}::contactPersons" class="editable-text xxlarge"
                      rows="5" cols="50" ${editable ? "" : "disabled='true'"}>${sampleSetAdminInfo.contactPersons}</textarea>
      </div>
    </div>
  </fieldset>
  <fieldset>
    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::irbApprovalNumber">IRB Approval #</label>

      <div class="input">
        <input type="text" id="${sampleSetAdminInfo.id}::irbApprovalNumber" name="${sampleSetAdminInfo.id}::irbApprovalNumber" class="editable-text medium"
                     value="${sampleSetAdminInfo.irbApprovalNumber}" ${editable ? "" : "disabled='true'"}/>
      </div>
    </div>

    <div class="clearfix">
      <label for="samplesSent">Date samples sent</label>

      <div class="input">
        <div class="input-append">
          <input type="text" id="samplesSent" name="samplesSent" class="datepicker medium"
                     value="${sampleSetAdminInfo.samplesSent?.format('MM/dd/yyyy')}" ${editable ? "" : "disabled='true'"}/>
          <label class="add-on">
            <span class="ui-icon-date"></span>
          </label>
        </div>
      </div>
    </div>

    <div class="clearfix">
      <label for="resultsNeeded">
        Date results needed</label>

      <div class="input">
        <div class="input-append">
          <input type="text" id="resultsNeeded" name="resultsNeeded" class="datepicker medium"
                       value="${sampleSetAdminInfo.resultsNeeded?.format('MM/dd/yyyy')}" ${editable ? "" : "disabled='true'"}/>
          <label class="add-on">
            <span class="ui-icon-date"></span>
          </label>
        </div>
        <span class="help-block bri-help-block" style="clear:both;">If you have a specific deadline, you may specify</span>
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Billing Information</legend>

    <div class="clearfix">
      <div class="input">
        <p>
          If this study is not part of a Chaussabel Lab grant, please provide billing information.
          If you need a quote, please contact Vivian Gersuk at <g:link
            url="mailto:vgersuk@benaroyaresearch.org">vgersuk@benaroyaresearch.org</g:link>.
        </p>
      </div>
    </div>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::billTo">Bill to</label>

      <div class="input">
        <input type="text" id="${sampleSetAdminInfo.id}::billTo" name="${sampleSetAdminInfo.id}::billTo" class="editable-text xlarge"
                     value="${sampleSetAdminInfo.billTo}" ${editable ? "" : "disabled='true'"}/>
      </div>
    </div>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::billToAddress">Address</label>

      <div class="input">
        <textarea id="${sampleSetAdminInfo.id}::billToAddress" name="${sampleSetAdminInfo.id}::billToAddress" class="editable-text xlarge"
                    rows="5" cols="50" ${editable ? "" : "disabled='true'"}>${sampleSetAdminInfo.billToAddress}</textarea>
      </div>
    </div>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::billToReference">Reference # or<br/>Project Code</label>

      <div class="input">
        <input type="text" id="${sampleSetAdminInfo.id}::billToReference" name="${sampleSetAdminInfo.id}::billToReference" class="editable-text xlarge"
                     value="${sampleSetAdminInfo.billToReference}" ${editable ? "" : "disabled='true'"}/>
        <span class="help-block bri-help-block">Enter if applicable</span>
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Additional Information</legend>

    <div class="clearfix">
      <label for="${sampleSetAdminInfo.id}::comments">Comments</label>

      <div class="input">
        <textarea id="${sampleSetAdminInfo.id}::comments" name="${sampleSetAdminInfo.id}::comments" class="editable-text xxlarge"
                    rows="5" cols="50" ${editable ? "" : "disabled='true'"}>${sampleSetAdminInfo.comments}</textarea>
      </div>
    </div>
  </fieldset>
</g:form>
