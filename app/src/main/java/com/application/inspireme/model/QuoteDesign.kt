package com.application.inspireme.model

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity

/**
 * Data class to store quote design settings
 */
data class QuoteDesign(
    // Text content
    var quoteText: String = "",
    
    // Background settings
    var backgroundColor: Int = Color.WHITE,
    // backgroundImageUri field removed
    
    // Text style settings
    var fontStyle: Int = Typeface.NORMAL,     // 0: DEFAULT, 1: SANS_SERIF, 2: SERIF, 3: MONOSPACE
    var fontSize: Float = 16f,
    var textStyle: Int = 0,                   // 0: NORMAL, 1: BOLD, 2: ITALIC, 3: UNDERLINE
    var textAlignment: Int = Gravity.START,   // Gravity.START, Gravity.CENTER, Gravity.END
    var textColor: Int = Color.BLACK,
    
    // Metadata
    var createdAt: Long = System.currentTimeMillis(),
    var authorName: String = ""
)