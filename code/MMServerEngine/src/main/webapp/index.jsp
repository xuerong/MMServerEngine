<%@ page import="com.mm.engine.framework.tool.helper.BeanHelper" %>
<%@ page import="com.mm.engine.framework.control.gm.GmService" %>
<%@ page import="com.mm.engine.framework.control.ServiceHelper" %>
<%@ page import="java.lang.reflect.Method" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.mm.engine.framework.control.gm.Gm" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<% GmService gmService = BeanHelper.getServiceBean(GmService.class);
    Map<String, Method> gmMethods = ServiceHelper.getGmMethod();
    // 生成html string
    StringBuilder sb = new StringBuilder("<select onchange='show(this);' width = '400px' height='600px' size='10'>");
    for(Map.Entry<String,Method> entry : gmMethods.entrySet()){
        Gm gm = entry.getValue().getAnnotation(Gm.class);
        sb.append("<option  value ='"+entry.getKey()+"' describe='"+gm.describe()+"'>"+entry.getKey()+"</option>");
    }
    sb.append("</select>");
%>

<html>
<head>
    <title>index.jsp</title>
</head>
<body>

index    .jsp<br/>
<a href="/HelloAction"> HelloAction</a> <br>
<a href="/webSocket">WebsocketAction</a>  <br>
<a href="websocket.html">websocket.html</a>  <br>
<div id="describe" style="width: 600px;height: 200px;">describe</div>
<%=sb.toString() %>

</body>

<script type="text/javascript">
    function show(select){
        var div = document.getElementById("describe");
        div.innerText = select.options[select.selectedIndex].describe;
    }
</script>
</html>