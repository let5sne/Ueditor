package org.let5sne.ueditor

import com.baidu.ueditor.define.ActionMap
import groovy.transform.CompileStatic
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.moqui.Moqui
import org.moqui.context.ExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class UeditorTool {
    protected final static Logger logger = LoggerFactory.getLogger(this.class)
    public JSONObject jsonObject = null
    // 涂鸦上传filename定义
    private final static String SCRAWL_FILE_NAME = "scrawl"
    // 远程图片抓取filename定义
    private final static String REMOTE_FILE_NAME = "remote"

    UeditorTool(JSONObject jsonObject) {
        this.jsonObject = jsonObject
    }

    /**
     * 获取Json配置
     * @param type
     * @return
     */
    public Map<String, Object> getConfig(int type) {
        Map<String, Object> conf = new HashMap<String, Object>()
        String savePath = null
        switch (type) {
            case ActionMap.UPLOAD_FILE:
                conf.put("isBase64", "false");
                conf.put("maxSize", this.jsonObject.getLong("fileMaxSize"));
                conf.put("allowFiles", this.getArray("fileAllowFiles"));
                conf.put("fieldName", this.jsonObject.getString("fileFieldName"));
                savePath = this.getPathFormatString("filePathFormat");
                break;
            case ActionMap.UPLOAD_IMAGE:
                conf.put("isBase64", "false");
                conf.put("maxSize", this.jsonObject.getLong("imageMaxSize"));
                conf.put("allowFiles", this.getArray("imageAllowFiles"));
                conf.put("fieldName", this.jsonObject.getString("imageFieldName"));
                savePath = this.getPathFormatString("imagePathFormat");
                break;

            case ActionMap.UPLOAD_VIDEO:
                conf.put("maxSize", this.jsonObject.getLong("videoMaxSize"));
                conf.put("allowFiles", this.getArray("videoAllowFiles"));
                conf.put("fieldName", this.jsonObject.getString("videoFieldName"));
                savePath = this.getPathFormatString("videoPathFormat");
                break;

            case ActionMap.UPLOAD_SCRAWL:
                conf.put("filename", this.SCRAWL_FILE_NAME);
                conf.put("maxSize", this.jsonObject.getLong("scrawlMaxSize"));
                conf.put("fieldName", this.jsonObject.getString("scrawlFieldName"));
                conf.put("isBase64", "true");
                savePath = this.getPathFormatString("scrawlPathFormat");
                break;

            case ActionMap.CATCH_IMAGE:
                conf.put("filename", this.REMOTE_FILE_NAME);
                conf.put("filter", this.getArray("catcherLocalDomain"));
                conf.put("maxSize", this.jsonObject.getLong("catcherMaxSize"));
                conf.put("allowFiles", this.getArray("catcherAllowFiles"));
                conf.put("fieldName", this.jsonObject.getString("catcherFieldName") + "[]");
                savePath = this.getPathFormatString("catcherPathFormat");
                break;

            case ActionMap.LIST_IMAGE:
                conf.put("allowFiles", this.getArray("imageManagerAllowFiles"));
                conf.put("dir", this.jsonObject.getString("imageManagerListPath"));
                conf.put("count", this.jsonObject.getInt("imageManagerListSize"));
                break;

            case ActionMap.LIST_FILE:
                conf.put("allowFiles", this.getArray("fileManagerAllowFiles"));
                conf.put("dir", this.jsonObject.getString("fileManagerListPath"));
                conf.put("count", this.jsonObject.getInt("fileManagerListSize"));
                break;
        }
        conf.put( "savePath", savePath )
        return conf
    }

    private String[] getArray(String key) {
        JSONArray jsonArray = this.jsonObject.getJSONArray(key)
        String[] result = new String[jsonArray.size()]
        jsonArray.toArray(result)
        return result;
    }

    private String getPathFormatString(String key){
        logger.info("..........getPathString key:${key}")
        Object o = this.jsonObject.get(key)
        if(o instanceof  String ) return this.jsonObject.getString(key)
        logger.info("..........getPathString o:${o}")
        return String.valueOf(o)
    }


}
