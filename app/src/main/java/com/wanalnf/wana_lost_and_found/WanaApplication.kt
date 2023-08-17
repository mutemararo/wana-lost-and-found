package com.wanalnf.wana_lost_and_found

import android.app.Application
import com.wanalnf.wana_lost_and_found.data.DefaultContainer

class WanaApplication: Application() {
    val container: DefaultContainer by lazy {
        DefaultContainer()
    }
}