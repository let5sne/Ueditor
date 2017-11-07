package org.let5sne.ueditor

import groovy.transform.CompileStatic
import net.sf.json.JSONObject
import net.sf.json.JSONSerializer
import org.apache.commons.io.IOUtils
import org.moqui.context.ExecutionContextFactory
import org.moqui.context.ToolFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.charset.Charset

@CompileStatic
class UeditorToolFactory implements ToolFactory<UeditorTool> {

    protected final static Logger logger = LoggerFactory.getLogger(this.class)

    final static String TOOL_NAME = "UeditorTool"

    protected UeditorTool ueditorTool = null

    @Override
    String getName() { return TOOL_NAME }

    @Override
    void preFacadeInit(ExecutionContextFactory ecf) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.json")
        String jsonTxt = IOUtils.toString(is, Charset.forName("utf-8"))
        jsonTxt = this.filter(jsonTxt)
        JSONObject jsonObject = JSONObject.fromObject(jsonTxt)
        ueditorTool = new UeditorTool(jsonObject)
    }

    @Override
    UeditorTool getInstance(Object... parameters) {
        if (ueditorTool == null) throw new IllegalStateException("AliossToolFactory not initialized")
        return ueditorTool
    }

    // 过滤输入字符串, 剔除多行注释以及替换掉反斜杠
    private String filter(String input) {

        return input.replaceAll("/\\*[\\s\\S]*?\\*/", "");

    }
}
