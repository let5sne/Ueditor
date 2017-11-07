package org.let5sne.ueditor

import groovy.transform.CompileStatic
import org.moqui.Moqui
import org.moqui.context.ExecutionContext
import org.moqui.context.ToolFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@CompileStatic
class UeditorServlet extends HttpServlet {
    protected final static Logger logger = LoggerFactory.getLogger(this.class)

    @Override
    void doPost(HttpServletRequest request, HttpServletResponse response) {
        enter(request,response)
    }

    @Override
    void doGet(HttpServletRequest request, HttpServletResponse response) {
        enter(request,response)
    }

    public void enter(HttpServletRequest request, HttpServletResponse response){
        ExecutionContext ec = Moqui.getExecutionContext()
        request.setCharacterEncoding( "utf-8" )
        response.setHeader("Content-Type" , "text/html")
        response.getWriter().write(new ActionEnter( request, (org.let5sne.ueditor.UeditorTool)ec.getTool("UeditorTool",org.let5sne.ueditor.UeditorTool.class) ).exec())
    }

}
