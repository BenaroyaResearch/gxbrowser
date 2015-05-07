<g:form>
  <fieldset>
    <legend>Publish to Gene Expression Browser</legend>
    <div class="clearfix">
      <label>Select Group Sets</label>
      <div class="input">
        <ul class="inputs-list">
        <g:each in="${sampleSet.groupSets}" var="groupSet">
          <li>
            <label>
              <g:checkBox name="pubGroupSet-${groupSet.id}"/>
              <span class="chkboxLabel">${groupSet.name}</span>
            </label>
          </li>
        </g:each>
        </ul>
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Configure Sample Set Sharing</legend>
    <div class="clearfix">
      <label>Security Settings</label>
      <div class="input">
        <ul class="inputs-list">
          <li>
            <label>
              <g:checkBox name="publicPrivate"/>
              <span class="chkboxLabel">Public/Private</span>
            </label>
          </li>
        </ul>
      </div>
    </div>
  </fieldset>
</g:form>