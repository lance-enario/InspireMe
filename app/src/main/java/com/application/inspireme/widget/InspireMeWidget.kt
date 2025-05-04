package com.application.inspireme.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.application.inspireme.R
import com.application.inspireme.api.FirebaseManager
import com.application.inspireme.model.Quote
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class InspireMeWidget : AppWidgetProvider() {
    companion object {
        private const val TAG = "InspireMeWidget"
        private const val ACTION_REFRESH = "ACTION_REFRESH"
        private const val ACTION_LIKE_QUOTE = "ACTION_LIKE_QUOTE"
        private var cachedQuotes: List<Quote> = emptyList()
        private var lastFetchTime: Long = 0
        private const val CACHE_EXPIRY_MS = 3600000 // 1 hour cache
        private val recentlyShownQuotes = LinkedHashSet<String>()
        private const val MAX_RECENT_QUOTES = 10

        private var currentQuote: Quote? = null
        private var isLiked = false
        private val categories = listOf(
            "motivation", "inspiration", "life", "wisdom", "love",
            "success", "leadership", "happiness", "change", "perseverance",
            "mindfulness", "growth", "courage", "gratitude", "resilience",
            "friendship", "creativity", "humility", "forgiveness", "patience",
            "integrity", "self-reflection", "empathy", "purpose", "justice",
            "harmony", "knowledge", "hope", "anger", "fear", "general"
        )

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            fetchRandomQuote(context, appWidgetManager, appWidgetId)
        }

        private fun fetchRandomQuote(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            showLoadingState(context, appWidgetManager, appWidgetId)

            FirebaseManager.getAllQuotes(
                onSuccess = { allQuotes ->
                    if (allQuotes.isEmpty()) {
                        showEmptyState(context, appWidgetManager, appWidgetId)
                        return@getAllQuotes
                    }

                    // Filter out recently shown quotes
                    val availableQuotes = if (recentlyShownQuotes.size >= MAX_RECENT_QUOTES) {
                        allQuotes.filter { !recentlyShownQuotes.contains(it.id) }
                    } else {
                        allQuotes
                    }

                    // If we've filtered all quotes, reset the tracker
                    val quotesToUse = if (availableQuotes.isEmpty()) {
                        recentlyShownQuotes.clear()
                        allQuotes
                    } else {
                        availableQuotes
                    }

                    // Select random quote
                    val randomQuote = quotesToUse.shuffled().first()
                    currentQuote = randomQuote

                    // Track this quote
                    recentlyShownQuotes.add(randomQuote.id)
                    if (recentlyShownQuotes.size > MAX_RECENT_QUOTES) {
                        recentlyShownQuotes.remove(recentlyShownQuotes.first())
                    }

                    checkLikeStatus(context, appWidgetManager, appWidgetId)
                },
                onFailure = { error ->
                    Log.e(TAG, "Error fetching quotes: $error")
                    showEmptyState(context, appWidgetManager, appWidgetId)
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchRandomQuote(context, appWidgetManager, appWidgetId)
                    }, 3000)
                }
            )
        }

        private fun getRandomFromCache(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            if (cachedQuotes.isEmpty()) {
                showEmptyState(context, appWidgetManager, appWidgetId)
                return
            }

            // Get a truly random quote using proper shuffling
            currentQuote = cachedQuotes.shuffled().first()
            checkLikeStatus(context, appWidgetManager, appWidgetId)
        }

        private fun showLoadingState(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.inspire_me_widget)
            views.setTextViewText(R.id.appwidget_text, context.getString(R.string.loading_quotes))
            views.setTextViewText(R.id.widget_author, "")
            views.setImageViewResource(R.id.like_icon, R.drawable.ic_like)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun updateWidgetUI(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            quote: Quote
        ) {
            val views = RemoteViews(context.packageName, R.layout.inspire_me_widget)

            // Set quote text
            views.setTextViewText(R.id.appwidget_text, quote.quote)

            // Set author name
            val authorName = quote.author ?: context.getString(R.string.unknown_author)
            views.setTextViewText(R.id.widget_author, authorName)

            // Set creator name (use default if null)
            // This part is for the account that posted the quote
//            val creatorName = quote.createdBy?.name ?: context.getString(R.string.default_creator_name)
//            views.setTextViewText(R.id.widget_creator_name, creatorName)

            // Set creator profile image (use default if null)
            views.setImageViewResource(R.id.widget_creator_profile, R.drawable.personfileld)

            // Set like button appearance
            val likeIcon = if (isLiked) R.drawable.ic_liked else R.drawable.ic_like
            views.setImageViewResource(R.id.like_icon, likeIcon)

            // Set click handlers
            setClickIntents(context, views, appWidgetId)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun setClickIntents(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int
        ) {
            // Refresh intent - will fetch new random quote
            val refreshIntent = Intent(context, InspireMeWidget::class.java).apply {
                action = ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, refreshPendingIntent)

            // Like intent
            val likeIntent = Intent(context, InspireMeWidget::class.java).apply {
                action = ACTION_LIKE_QUOTE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val likePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                likeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.like_button, likePendingIntent)
        }

        private fun checkLikeStatus(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                isLiked = false
                updateWidgetUI(context, appWidgetManager, appWidgetId, currentQuote ?: return)
                return
            }

            currentQuote?.id?.let { quoteId ->
                FirebaseManager.checkIfQuoteLiked(userId, quoteId) { liked ->
                    isLiked = liked
                    currentQuote?.let { quote ->
                        updateWidgetUI(context, appWidgetManager, appWidgetId, quote)
                    }
                }
            }
        }

        private fun showEmptyState(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.inspire_me_widget)

            views.setTextViewText(R.id.appwidget_text,
                context.getString(R.string.no_quotes_available))
            views.setTextViewText(R.id.widget_author, "")
            views.setTextViewText(R.id.widget_creator_name, "")
            views.setImageViewResource(R.id.widget_creator_profile, R.drawable.personfileld)

            views.setImageViewResource(R.id.like_icon, R.drawable.ic_like)

            setClickIntents(context, views, appWidgetId)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun handleLikeAction(context: Context, appWidgetId: Int) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            currentQuote?.let { quote ->
                if (isLiked) {
                    FirebaseManager.unlikeQuote(userId, quote.id) { success ->
                        if (success) {
                            isLiked = false
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            updateWidgetUI(context, appWidgetManager, appWidgetId, quote)
                        } else {
                            Toast.makeText(context, "Failed to unlike quote", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    FirebaseManager.likeQuote(userId, quote) { success ->
                        if (success) {
                            isLiked = true
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            updateWidgetUI(context, appWidgetManager, appWidgetId, quote)
                        } else {
                            Toast.makeText(context, "Failed to like quote", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_REFRESH -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    fetchRandomQuote(context, appWidgetManager, appWidgetId)
                }
            }
            ACTION_LIKE_QUOTE -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    handleLikeAction(context, appWidgetId)
                }
            }
            else -> super.onReceive(context, intent)
        }
    }
}