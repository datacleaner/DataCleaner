<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="urn:docbkx:stylesheet"/>
  <xsl:output method="html" encoding="UTF-8" indent="no"/>

<!-- 
  <xsl:template name="user.footer.content">
	<xsl:if test="@id">
		<div id="disqus_thread"></div>
		<script type="text/javascript">
			var disqus_shortname = 'datacleaner';
			var disqus_url = 'http://datacleaner.org/docs';
			var disqus_identifier = '<xsl:value-of select="@id" />';
		
			(function() {
				var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
				dsq.src = 'http://' + disqus_shortname + '.disqus.com/embed.js';
				(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
			})();
		</script>
		<noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
	</xsl:if>
  </xsl:template>
 -->

  <!--
    Important links:
    - http://www.sagehill.net/docbookxsl/
    - http://docbkx-tools.sourceforge.net/
  -->

</xsl:stylesheet>