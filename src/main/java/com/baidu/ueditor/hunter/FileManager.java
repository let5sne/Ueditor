package com.baidu.ueditor.hunter;

import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.MultiState;
import com.baidu.ueditor.define.State;
import org.moqui.Moqui;
import org.moqui.context.ExecutionContext;

import java.io.File;
import java.util.*;

public class FileManager {

    private String dir = null;
    private String rootPath = null;
    private String[] allowFiles = null;
    private int count = 0;

    public FileManager(Map<String, Object> conf) {

        this.rootPath = (String) conf.get("rootPath");
        this.dir = this.rootPath + (String) conf.get("dir");
        this.allowFiles = this.getAllowFiles(conf.get("allowFiles"));
        this.count = (Integer) conf.get("count");

    }

    public State listFile(int index) {

        File dir = new File(this.dir);
        State state = null;

        Collection<File> list = null;
        ExecutionContext ec = Moqui.getExecutionContext();
        // List<String> objectList = ec.getTool("UeditorTool", org.let5sne.ueditor.UeditorTool.class).listYunFiles("/");
        List<String> objectList = Collections.emptyList();

        if (index < 0 || index > objectList.size()) {
            state = new MultiState(true);
        } else {
            Object[] fileList = Arrays.copyOfRange(objectList.toArray(), index, index + this.count);
            state = this.getOSSState(fileList);
        }

        state.putInfo("start", index);
        state.putInfo("total", objectList.size());

        return state;

    }

    private State getState(Object[] files) {
        MultiState state = new MultiState(true);
        BaseState fileState = null;

        File file = null;

        for (Object obj : files) {
            if (obj == null) {
                break;
            }
            file = (File) obj;
            fileState = new BaseState(true);
            fileState.putInfo("url", "/" + PathFormat.format(this.getPath(file)));
            state.addState(fileState);
        }

        return state;

    }

    // 处理ailiyun数据
    private State getOSSState(Object[] files) {

        MultiState state = new MultiState(true);
        BaseState fileState = null;

//		File file = null;

        for (Object obj : files) {
            if (obj == null) {
                break;
            }
//			file = (File)obj;
            fileState = new BaseState(true);
            ExecutionContext ec = Moqui.getExecutionContext();
            //fileState.putInfo("url", ec.getTool("UeditorTool", org.let5sne.ueditor.UeditorTool.class).getPresignedUrl((String)obj,"xx",""));
            fileState.putInfo("url", "");
            state.addState(fileState);
        }

        return state;

    }

    private String getPath(File file) {

//		String path = file.getAbsolutePath();
//		
//		return path.replace( this.rootPath, "/" );
        String path = file.getAbsolutePath();

        String str = path.replace(this.rootPath.replaceAll("\\/", "\\\\"), "\\");

        return str;
    }

    private String[] getAllowFiles(Object fileExt) {

        String[] exts = null;
        String ext = null;

        if (fileExt == null) {
            return new String[0];
        }

        exts = (String[]) fileExt;

        for (int i = 0, len = exts.length; i < len; i++) {

            ext = exts[i];
            exts[i] = ext.replace(".", "");

        }

        return exts;

    }

}
