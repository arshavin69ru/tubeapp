package com.app.tubeapp

class YoutubeDownloader {

    enum class Options(val option: String) {
        FORMAT("-f"),
        OUTPUT("-o"),
        LIST_THUMBNAILS("--list-thumbnails"),
        GET_ID("--get-id"),
        GET_DESC("--get-description"),
        GET_DURATION("--get-duration"),
        GET_FILENAME("--get-filename"),
        GET_FORMAT("--get-format")
    }

    enum class FileNames(val title : String){
        TITLE_EXT("%(title)s.%(ext)s"),
        TITLE_ID_EXT("%(title)s-%(id)s.%(ext)s")
    }
}