<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jstl/core_rt" version="2.0">

	<h1>Module list</h1>
	<p>This is a list of installed modules in this DataCleaner
	webmonitor instance. Refer to the <a
		href="http://www.eobjects.dk/trac/wiki/DataCleanerUserGuide">DataCleaner
	User Guide</a> for more details on configuring additional modules.</p>
	<h2>Profiler modules</h2>
	<ul class="discrete-list">
		<c:forEach var="pd" items="${profileDescriptors}">
			<li><c:out value="&lt;img src='../../${pd.iconPath}' alt='' /&gt; "
				escapeXml="false" /> <c:out value="${pd.displayName}" /></li>
		</c:forEach>
	</ul>
	<h2>Validator modules</h2>
	<ul class="discrete-list">
		<c:forEach var="vrd" items="${validationRuleDescriptors}">
			<li><c:out value="&lt;img src='../../${vrd.iconPath}' alt='' /&gt; "
				escapeXml="false" /> <c:out value="${vrd.displayName}" /></li>
		</c:forEach>
	</ul>
</jsp:root>