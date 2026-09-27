package com.aspire.asat.vps.constant;

public class WebApiUrlConstants {

    public final static String API_PREFIX = "/api";
    public final static String API_VERSION = "/v1";
    public final static String API_URI_ROOT = API_PREFIX + API_VERSION;
    public final static String PATH_VAR_ID = "/{id}";
    public final static String PATH_VAR_SEARCH = "/search";
    public final static String PATH_VAR_EXPORT = "/export";
    public final static String PATH_VAR_CONTENT_TEXT = "/text";
    public final static String PATH_VAR_CONTENT_LINK = "/link";
    public final static String PATH_VAR_CONTENT_SLIDE = "/slide";
    public final static String PATH_VAR_CONTENT_PDF = "/pdf";
    public final static String PATH_VAR_CONTENT_MARKDOWN = "/mark-down";
    public final static String PATH_VAR_CONTENT_STORY_BLOCK = "/story-block";


    //Content
    public final static String VIDEO_PROCESS_API = API_URI_ROOT + "/video-process";


    //Storage
    public final static String STORAGE_API = API_URI_ROOT + "/storages";

}
