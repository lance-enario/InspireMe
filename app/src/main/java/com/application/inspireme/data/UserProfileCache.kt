package com.application.inspireme.data

import com.application.inspireme.R

object UserProfileCache {
    var username: String = "Default Name"
    var bio: String = "No bio available"
    var bannerId: String = "banner3"  // Default banner
    var profileId: String = "capybara"  // Default profile
    var isDataLoaded: Boolean = false
    var lastUpdateTime: Long = 0

    // Make sure all 6 banners are defined here with their correct resource IDs
    val bannerImages = mapOf(
        "banner1" to R.drawable.banner1,
        "banner2" to R.drawable.banner2,
        "banner3" to R.drawable.banner3,
        "banner4" to R.drawable.banner4,
        "banner5" to R.drawable.banner5,
        "banner6" to R.drawable.banner6
    )

    val profileImages = mapOf(
        "capybara" to R.drawable.capybara,
        "cat_footprint" to R.drawable.cat_footprint,
        "cat" to R.drawable.cat_icon,
        "corgi" to R.drawable.corgi,
        "dog_paw" to R.drawable.dog_paw,
        "dog" to R.drawable.dog,
        "doge" to R.drawable.doge,
        "duck" to R.drawable.duck,
        "gorilla" to R.drawable.gorilla
    )

    fun getCurrentProfileDrawable(): Int {
        return profileImages[profileId] ?: R.drawable.capybara
    }

    fun getCurrentBannerDrawable(): Int {
        return bannerImages[bannerId] ?: R.drawable.banner3
    }

    fun clear() {
        username = "Default Name"
        bio = "No bio available"
        bannerId = "banner3"
        profileId = "capybara"
        isDataLoaded = false
        lastUpdateTime = 0
    }
}