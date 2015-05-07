<h2>Module Analysis Tool</h2>

<g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
 <img src="images/cap-mat.png" alt="Module Analysis Tool" class="cap" />
</g:if>
<g:else>
 <img src="images/cap-mat-bri.png" alt="Module Analysis Tool" class="cap" />
</g:else>

<div class="summary">The MAT will analyze your data to find pre-defined groupings of coordinately expressed genes (modules), will help you look at the functional annotation of
those modules, and will let you view a molecular fingerprint of gene expression for each individual sample in your dataset.</div>

<a href="matmore.gsp" class="btn">Learn more</a>  <a href="matNews.gsp" class="btn">Changes</a>   <a href="${createLink(controller: 'mat')}" class="btn">Go to MAT &raquo;</a>
<g:if test="${grailsApplication.config.tools.available.contains('metacat') }">
	<a href="${createLink(controller: 'metaCat')}" class="btn">MetaCat &raquo;</a>
</g:if>
