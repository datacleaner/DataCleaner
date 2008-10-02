<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:tiles="http://tiles.apache.org/tags-tiles" version="2.0">
	<jsp:directive.page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
	<jsp:text>
		<![CDATA[ <?xml version="1.0" encoding="UTF-8" ?> ]]>
	</jsp:text>
	<jsp:text>
		<![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
	</jsp:text>
	<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
			<title><tiles:insertAttribute name="title" /></title>
			<style type="text/css">
				@IMPORT url("../../css/datacleaner.css");
			</style>
		</head>
		<body>
			<div id="banner"><img src="../../images/dialog_banner.png" alt="" /></div>
			<div id="content">
				<tiles:insertAttribute name="content" />
			</div>
			<div id="footer">
				<p><a href="http://www.eobjects.dk/datacleaner/">DataCleaner version 1.0</a></p>
			</div>
		</body>
	</html>
</jsp:root>