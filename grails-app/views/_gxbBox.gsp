<h2>Gene Expression Browser</h2>

<g:if test="${grailsApplication.config.target.audience.contains('ITN')}">
    <img src="images/cap-gxb.png" alt="Gene Expression Browser" class="cap" />
</g:if>
<g:else>
   <img src="images/cap-gxb-bri.png" alt="Gene Expression Browser" class="cap" />
</g:else>

 <div class="summary"> In the GXB, you can view the expression level of all genes in your dataset, overlay the clinical data
 associated with the samples, and rank the genes, either using pre-defined lists or dynamically.</div>

 <a href="gxbmore.gsp" class="btn">Learn more</a>   <a href="gxbNews.gsp" class="btn">Changes</a>  <a href="${createLink(controller: 'geneBrowser')}" class="btn">Go to GXB &raquo;</a>
 