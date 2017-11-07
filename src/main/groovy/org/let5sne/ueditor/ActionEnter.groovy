package org.let5sne.ueditor

import com.baidu.ueditor.define.ActionMap
import com.baidu.ueditor.define.AppInfo
import com.baidu.ueditor.define.BaseState
import com.baidu.ueditor.define.State
import com.baidu.ueditor.hunter.FileManager
import com.baidu.ueditor.hunter.ImageHunter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest

class ActionEnter {
    protected final static Logger logger = LoggerFactory.getLogger(this.class)

    private HttpServletRequest request = null;

    private String actionType = null;

    private UeditorTool ueditorTool = null;

    public ActionEnter(HttpServletRequest request, UeditorTool ueditorTool) {
        this.request = request;
        this.actionType = request.getParameter("action");
        this.ueditorTool = ueditorTool;
    }

    public String exec() {
        logger.info("..........ueditor ActionEnter exec.");
        String callbackName = this.request.getParameter("callback");

        if (callbackName != null) {

            if (!validCallbackName(callbackName)) {
                return new BaseState(false, AppInfo.ILLEGAL).toJSONString();
            }

            return callbackName + "(" + this.invoke() + ");";

        } else {
            logger.info("..........ueditor ActionEnter exec invoke.");
            return this.invoke();
        }

    }

    public String invoke() {
        logger.info("..........ueditor ActionEnter exec invoke actionType:" + actionType);
        if (actionType == null || !ActionMap.mapping.containsKey(actionType)) {
            return new BaseState(false, AppInfo.INVALID_ACTION).toJSONString();
        }

        if (this.ueditorTool == null || !this.ueditorTool.jsonObject) {
            return new BaseState(false, AppInfo.CONFIG_ERROR).toJSONString();
        }

        State state = null;

        int actionCode = ActionMap.getType(this.actionType);

        Map<String, Object> conf = null;

        switch (actionCode) {

            case ActionMap.CONFIG:
                return this.ueditorTool.jsonObject;

            case ActionMap.UPLOAD_IMAGE:
            case ActionMap.UPLOAD_SCRAWL:
            case ActionMap.UPLOAD_VIDEO:
            case ActionMap.UPLOAD_FILE:
                conf = this.ueditorTool.getConfig(actionCode);
                state = new Uploader(request, conf).doExec();
                break;

            case ActionMap.CATCH_IMAGE:
                conf = this.ueditorTool.getConfig(actionCode);
                String[] list = this.request.getParameterValues((String) conf.get("fieldName"));
                state = new ImageHunter(conf).capture(list);
                break;

            case ActionMap.LIST_IMAGE:
            case ActionMap.LIST_FILE:
                conf = this.ueditorTool.getConfig(actionCode);
                int start = this.getStartIndex();
                state = new FileManager(conf).listFile(start);
                break;
        }
        return state.toJSONString();

    }

    public int getStartIndex() {

        String start = this.request.getParameter("start");

        try {
            return Integer.parseInt(start);
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * callback参数验证
     * @param name name
     * @return boolean exists
     */
    public boolean validCallbackName(String name) {

        if (name.matches('^[a-zA-Z_]+[\\\\w0-9_]*$')) {
            return true;
        }

        return false;

    }
}
