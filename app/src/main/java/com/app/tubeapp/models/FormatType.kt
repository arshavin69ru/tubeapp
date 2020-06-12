package com.app.tubeapp.models

enum class FormatType(val format: String) {
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
    WEBM("webm"),
    FOUR_K("401"),
    TWO_K("400"),
    FULL_HD("137"),
    HD("136"),
    MEDIUM("135"),
    LOW("134"),
    VERY_LOW("133"),
    AUDIO_ONLY("144/bestaudio")


    //aac, flv, m4a, mp3, mp4, ogg, wav, webm
}
