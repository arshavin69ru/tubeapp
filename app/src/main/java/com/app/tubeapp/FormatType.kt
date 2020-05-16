package com.app.tubeapp

enum class FormatType(val format : String) {
    BEST("best"), // best video plus audio auto
    WORST("worst"), // worst video plus audio
    BESTVIDEOAUDIO("bestvideo+bestaudio"), // best video plus audio custom
    BESTAUDIO("bestaudio"), // best audio only
    WORSTAUDIO("worstaudio"),
    THREEGP("3gp"),
    AAC("aac"),
    FLV("flv"),
    MP4("mp4"),
    OGG("ogg"),
    WAV("wav"),
    WEBM("webm")
    //aac, flv, m4a, mp3, mp4, ogg, wav, webm
}
