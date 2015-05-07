<%@ page import="java.sql.Timestamp" %><g:if test="${tg2SampleInfo.isEmpty()}">
  <div>No matching samples found in TG2</div>
</g:if>
<g:else>
<div class="scrollable-container no-wrap-content" style="max-height: 600px">
<table class="zebra-striped pretty-table" id="tg2-sampleinfo-table">
  <thead>
  <tr>
    <g:each in="${tg2SampleHeaders}" var="header">
      <th>${header.encodeAsHumanize()}</th>
    </g:each>
  </tr>
  </thead>
  <tbody>
    <g:each in="${tg2SampleInfo.keySet()}" var="sampleId">
      <g:set var="tg2Values" value="${(Map)tg2SampleInfo.get(sampleId)}"/>
      <tr>
        <g:each in="${tg2SampleHeaders}" var="h">
          <td>
            <g:if test="${tg2Values.get(h) instanceof Timestamp}">
              <g:formatDate date="${tg2Values.get(h)}" format="yyyy-MM-dd"/>
            </g:if>
            <g:else>
              ${tg2Values.get(h)}
            </g:else>
          </td>
        </g:each>
      </tr>
    </g:each>
  </tbody>
</table>
</div>
</g:else>