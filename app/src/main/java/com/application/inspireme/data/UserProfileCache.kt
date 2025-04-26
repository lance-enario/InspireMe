package com.application.inspireme.data

import com.application.inspireme.R

object UserProfileCache {
    var username: String = "Default Name"
    var bio: String = "No bio available"
    var customBannerUri: String? = null
    var customProfileUri: String? = null
    var bannerResId: Int = R.drawable.banner3
    var profileResId: Int = R.drawable.profile
    var isDataLoaded = false
    var lastUpdateTime: Long = 0

    fun clear() {
        username = "Default Name"
        bio = "No bio available"
        customBannerUri = null
        customProfileUri = null
        bannerResId = R.drawable.banner3
        profileResId = R.drawable.profile
        isDataLoaded = false
        lastUpdateTime = 0
    }
}