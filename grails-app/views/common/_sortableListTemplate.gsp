<div class="scrollable-list">
  <div class="scrollbar"><div class="track"><div class="thumb"></div></div></div>
  <div class="viewport">
    <div class="overview">
    <ol id="${name}" class="sortable-list${isEmpty ? " empty-list" : ""}">
      <g:each in="${items}" var="item">
        <g:if test="${optionKey && optionValue}">
          <li id="${item.getProperty(optionKey)}">${item}</li>
        </g:if>
        <g:else>
          <li id="">${item}</li>
        </g:else>
      </g:each>
    </ol>
    </div>
  </div>
</div>