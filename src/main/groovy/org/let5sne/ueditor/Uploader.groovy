package org.let5sne.ueditor

import com.baidu.ueditor.PathFormat
import com.baidu.ueditor.define.AppInfo
import com.baidu.ueditor.define.BaseState
import com.baidu.ueditor.define.FileType
import com.baidu.ueditor.define.State
import com.baidu.ueditor.upload.Base64Uploader
import org.apache.commons.fileupload.FileItemIterator
import org.apache.commons.fileupload.FileItemStream
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.moqui.Moqui
import org.moqui.context.ExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest

class Uploader {

    private static final Logger logger = LoggerFactory.getLogger(this.class);

    private HttpServletRequest request = null;
    private Map<String, Object> conf = null;
    ExecutionContext ec = null

    public Uploader(HttpServletRequest request, Map<String, Object> conf) {
        this.request = request;
        this.conf = conf;
        this.ec = Moqui.getExecutionContext()
    }

    public final State doExec() {
        String filedName = (String) this.conf.get("fieldName");
        State state = null;

        if ("true".equals(this.conf.get("isBase64"))) {
            state = Base64Uploader.save(this.request.getParameter(filedName), this.conf);
        } else {
            FileItemStream fileStream = null;
            boolean isAjaxUpload = request.getHeader("X_Requested_With") != null;

            if (!ServletFileUpload.isMultipartContent(request)) {
                return new BaseState(false, AppInfo.NOT_MULTIPART_CONTENT);
            }

            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

            if (isAjaxUpload) upload.setHeaderEncoding("UTF-8")
            try {
                FileItemIterator iterator = upload.getItemIterator(request);

                while (iterator.hasNext()) {
                    fileStream = iterator.next();

                    if (!fileStream.isFormField())
                        break;
                    fileStream = null;
                }

                if (fileStream == null) {
                    return new BaseState(false, AppInfo.NOTFOUND_UPLOAD_DATA);
                }

                String savePath = (String) conf.get("savePath");
                String originFileName = fileStream.getName();
                String suffix = FileType.getSuffixByFilename(originFileName);

                originFileName = originFileName.substring(0,originFileName.length() - suffix.length());
                savePath = savePath + suffix;

                long maxSize = ((Long) conf.get("maxSize")).longValue();

                if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
                    return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
                }

                savePath = PathFormat.parse(savePath, originFileName)
                logger.info("..........savePath:${savePath}")
                InputStream is = fileStream.openStream();
                String bucket = System.getProperty("alioss.bucket.pub")
                boolean result = ec.getTool("AliossTool",null).saveImage(bucket,savePath.replaceFirst("/",""),fileStream.openStream())
                logger.info("..........uploader ${result}")
                State storageState = new BaseState(true);
                if (result) {
                    String pubUrl
                    if(bucket) {
                        ec.getTool("AliossTool",null).getPubUrl(savePath,null)
                    }else{
                        ec.getTool("AliossTool",null).getPresignedUrl(savePath.replaceFirst("/",""),"none",null)
                    }
                    storageState.putInfo("url", pubUrl)
                    storageState.putInfo("type", suffix)
                    storageState.putInfo("original", originFileName + suffix);
                }
                is.close()
                return storageState;
            } catch (FileUploadException e) {
                return new BaseState(false, AppInfo.PARSE_REQUEST_ERROR);
            } catch (IOException e) {

            }
        }
        /*
         * { "state": "SUCCESS", "title": "1415236747300087471.jpg", "original":
         * "a.jpg", "type": ".jpg", "url":
         * "/upload/image/20141106/1415236747300087471.jpg", "size": "18827" }
         */
        logger.debug(state.toJSONString());
        return state;
    }

    private static boolean validType(String type, String[] allowTypes) {
        List<String> list = Arrays.asList(allowTypes);

        return list.contains(type);
    }
}
