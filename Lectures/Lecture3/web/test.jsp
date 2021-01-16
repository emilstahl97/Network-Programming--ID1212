<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

<%
    out.println("HelloWorld");
    out.println(session.getId());
    out.println(request.getHeader("Cookie"));
    response.addHeader("Set-Cookie", "clientId=1");
    %>

   
    </body>
</html>
