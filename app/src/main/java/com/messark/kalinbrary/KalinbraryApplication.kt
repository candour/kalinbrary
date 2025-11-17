package com.messark.kalinbrary

import android.app.Application
import com.messark.kalinbrary.data.StoryRepository

class KalinbraryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        StoryRepository.init(this)
    }
}