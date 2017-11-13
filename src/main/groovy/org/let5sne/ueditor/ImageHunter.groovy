package org.let5sne.ueditor

import com.baidu.ueditor.PathFormat
import com.baidu.ueditor.define.*
import org.moqui.Moqui
import org.moqui.context.ExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ImageHunter {

    private static final Logger logger = LoggerFactory.getLogger(this.class);

    private String filename = null;
    private String savePath = null;
    private String rootPath = null;
    private List<String> allowTypes = null;
    private long maxSize = -1;

    private List<String> filters = null;

    ExecutionContext ec = null

    public ImageHunter ( Map<String, Object> conf ) {

        this.filename = (String)conf.get( "filename" );
        this.savePath = (String)conf.get( "savePath" );
        this.rootPath = (String)conf.get( "rootPath" );
        this.maxSize = (Long)conf.get( "maxSize" );
        this.allowTypes = Arrays.asList( (String[])conf.get( "allowFiles" ) );
        this.filters = Arrays.asList( (String[])conf.get( "filter" ) )
        this.ec = Moqui.getExecutionContext()

    }

    public State capture (String[] list ) {

        MultiState state = new MultiState( true );

        for ( String source : list ) {
            state.addState( captureRemoteData( source ) );
        }

        return state;

    }

    public State captureRemoteData ( String urlStr ) {

        HttpURLConnection connection = null;
        URL url = null;
        String suffix = null;

        try {
            logger.info("..........ImageHunter captureRemoteData:${urlStr}")
            String aliossUrl = System.getProperty("alioss.bucket.pub.url")
            if (urlStr.startsWith(aliossUrl)){
                logger.info("..........ImageHunter ali urlStr,do nothing!")
                State storageState = new BaseState(true);
                storageState.putInfo("url", urlStr)
                storageState.putInfo("source", urlStr)
                return storageState
            }
            url = new URL( urlStr );

            if ( !validHost( url.getHost() ) ) {
                return new BaseState( false, AppInfo.PREVENT_HOST );
            }

            connection = (HttpURLConnection) url.openConnection();

            connection.setInstanceFollowRedirects( true );
            connection.setUseCaches( true );

            if ( !validContentState( connection.getResponseCode() ) ) {
                return new BaseState( false, AppInfo.CONNECTION_ERROR );
            }

            suffix = MIMEType.getSuffix( connection.getContentType() );

            if ( !validFileType( suffix ) ) {
                return new BaseState( false, AppInfo.NOT_ALLOW_FILE_TYPE );
            }

            if ( !validFileSize( connection.getContentLength() ) ) {
                return new BaseState( false, AppInfo.MAX_SIZE );
            }

            String savePath = this.getPath( this.savePath, this.filename, suffix )
            logger.info("..........ImageHunter savePath:${savePath}")
            String bucket = System.getProperty("alioss.bucket.pub")
            boolean result = ec.getTool("AliossTool",null).saveImage(bucket,savePath.replaceFirst("/",""),connection.getInputStream())
            State storageState = new BaseState(true);
            if (result) {
                String pubUrl
                if(bucket) {
                    pubUrl = ec.getTool("AliossTool",null).getPubUrl(savePath,null)
                }else{
                    pubUrl = ec.getTool("AliossTool",null).getPresignedUrl(savePath.replaceFirst("/",""),"none",null)
                }
                storageState.putInfo("url", pubUrl)
                storageState.putInfo("source", urlStr)
            }
            return storageState;

        } catch ( Exception e ) {
            return new BaseState( false, AppInfo.REMOTE_FAIL );
        }

    }

    private String getPath ( String savePath, String filename, String suffix  ) {

        return PathFormat.parse( savePath + suffix, filename );

    }

    private boolean validHost ( String hostname ) {
        try {
            InetAddress ip = InetAddress.getByName(hostname);

            if (ip.isSiteLocalAddress()) {
                return false;
            }
        } catch (UnknownHostException e) {
            return false;
        }

        return !filters.contains( hostname );

    }

    private boolean validContentState ( int code ) {

        return HttpURLConnection.HTTP_OK == code;

    }

    private boolean validFileType ( String type ) {

        return this.allowTypes.contains( type );

    }

    private boolean validFileSize ( int size ) {
        return size < this.maxSize;
    }

}
