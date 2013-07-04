<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>...</title>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/ext3/resources/css/ext-all.css"/>
<script type="text/javascript" src="<%=request.getContextPath()%>/ext3/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/ext3/ext-all.js"></script>
<style type="text/css">
	fieldset{
		padding-left:10px;
		border:1px solid #7EC0EE;
	} 	
</style>
<script type="text/javascript">

	<%
		String context = request.getContextPath();
		String type = request.getParameter("Type");
		String requestUrl = context + "/ckuploader?Type=" + type;
	%>

	window.onload = function(){
		var type = getUrlParam('Type');
		if(!type) type = "File ";
		if(type == "Image"){
			document.title = "请选择图片文件：";
		}else if(type == "Video"){
			document.title = "请选择视频文件：";
		}else{
			document.title = "请选择其他文件：";
		}
		
	}
	
	Ext.onReady(initTree);
	
	function initTree(){
		//clear value while goback!
		document.getElementById("upload").value = '';
		
		var fileTree = new Ext.tree.TreePanel({
			id : 'filestree',		
			width : 360,
			height : 400,
			//frame : false,				
			//border : false,
			autoScroll : true,
			animate : true,
			rootVisible : false,
			renderTo:'treediv',
			loader : new Ext.tree.TreeLoader({
				dataUrl : '<%=requestUrl%>',//dynamically requested url...
				requestMethod: 'GET',//intentionally use GET, not POST...
				baseParams : {}
			}),
			root : new Ext.tree.AsyncTreeNode({
						allowChildren : true,
						expanded : true,
						text : '文件根节点', // 节点名称
						draggable : false, // 是否支持拖动
						id : '0' // 节点id
					}),
			listeners : {
				'click' : function(node) {
					if(node.attributes.leaf)
						deliver(node.attributes.relativePath);
				 },				
			}
		});//end of user tree		
		
	}//end of initTree

	//Helper function to get parameters from the query string.
	function getUrlParam( paramName ) {
	    var reParam = new RegExp( '(?:[\?&]|&)' + paramName + '=([^&]+)', 'i' ) ;
	    var match = window.location.search.match(reParam) ;
	
	    return ( match && match.length > 1 ) ? match[ 1 ] : null ;
	}
	
	function deliver(fileUrl){
		trace("deliver url: " + fileUrl);
		
		var funcNum = getUrlParam( 'CKEditorFuncNum' );
		if(!funcNum) return;
		
		//deliver the video url to parent window dialog
		window.opener.CKEDITOR.tools.callFunction( funcNum, fileUrl );
		
		self.close();
	}
	
	function uploadNow(){
		
		var form = document.forms[0];		
		var fileInput = document.getElementById("upload");					
		var fileSelected = fileInput.value;
		if(!fileSelected) return;		
		
		var fileType = fileSelected.split(".")[1];
		var MediaType;
		if(fileType == "png" || fileType == "jpg") MediaType = "Image";
		if(fileType == "mp4" || fileType == "webm") MediaType = "Video";
		form.action += "?Type="+MediaType;
		
		form.submit();
		
	}
	
	function trace(msg){
		if(console) console.log(msg);
	}
	
	
</script>
</head>
<body>
	<form name="upload_form" method="post" style="margin: 4px;width: 360px;"
		action="<%=request.getContextPath()%>/ckuploader" enctype="multipart/form-data"
		onsubmit="uploadNow();return false;">
		<fieldset>
			<legend>上传本地媒体文件:</legend>
	  		<input type="file" id="upload" name="upload" style="margin: 4px;"/>
	  		<input type="submit" value=" 上传 "/>	  		
	  	</fieldset> 
	</form>	
	<div id="treediv" style="margin: 4px;"></div>
</body>
</html>