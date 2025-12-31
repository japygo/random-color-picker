package com.japygo.study.randomcolorpicker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.japygo.study.randomcolorpicker.ads.AdMobManager

@Composable
fun ExitConfirmDialog(
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val nativeAd = AdMobManager.nativeAd

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Are you sure you want to exit?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Native Ad
                if (nativeAd != null) {
                    NativeAdContentView(nativeAd)
                } else {
                    // Fallback Text if ad not loaded
                    Text(
                        text = "We hope you enjoyed the app!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exit Button (Red Text)
                    TextButton(
                        onClick = onExit,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Exit", color = Color(0xFFF44336), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    // Cancel Button (Gray Text)
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Cancel", color = Color.Gray, fontSize = 14.sp)
                    }

                    // Review Button (Blue Text)
                    TextButton(
                        onClick = {
                            openPlayStoreReview(context)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Review", color = Color(0xFF2196F3), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NativeAdContentView(nativeAd: NativeAd) {
    AndroidView(
        factory = { context ->
            // Root NativeAdView
            val adView = NativeAdView(context)
            adView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Main Container (Vertical)
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // Add padding inside the ad view
                setPadding(16, 16, 16, 16)
            }

            // --- 1. Header Area (Icon | Headline, Stars, Advertiser) ---
            val headerRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 12
                }
            }

            // Icon
            val iconView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    rightMargin = 16
                }
                scaleType = ImageView.ScaleType.FIT_CENTER
                id = android.view.View.generateViewId()
            }
            headerRow.addView(iconView)
            adView.iconView = iconView

            // Text Column
            val textCol = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            // Headline
            val headlineView = TextView(context).apply {
                id = android.view.View.generateViewId()
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.Black.toArgb())
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            textCol.addView(headlineView)
            adView.headlineView = headlineView

            // Secondary Row (Stars | Advertiser)
            val secondaryRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // RatingBar
            val ratingBar = RatingBar(context, null, android.R.attr.numStars).apply {
                id = android.view.View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                numStars = 5
                stepSize = 0.5f
                setIsIndicator(true)
                // Scale down logic
                scaleX = 0.7f
                scaleY = 0.7f
                pivotX = 0f
                pivotY = 0.5f // Align center vertically
            }
            secondaryRow.addView(ratingBar)
            adView.starRatingView = ratingBar
            
            // Advertiser
            val advertiserView = TextView(context).apply {
                id = android.view.View.generateViewId()
                textSize = 12f
                setTextColor(Color.Gray.toArgb())
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = 8
                }
            }
            secondaryRow.addView(advertiserView)
            adView.advertiserView = advertiserView

            textCol.addView(secondaryRow)
            headerRow.addView(textCol)
            container.addView(headerRow)

            // --- 2. Media View ---
            val mediaView = com.google.android.gms.ads.nativead.MediaView(context).apply {
                id = android.view.View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT // Let it size itself based on content
                ).apply {
                    // Min height to ensure visibility while loading or if aspect ratio is small
                    height = 500 // Start with a fixed size/min size preference
                    // Note: setMediaContent usually resizes it. 
                    // To strictly follow WRAP_CONTENT with ratio, we often let it be.
                    bottomMargin = 12
                }
            }
            container.addView(mediaView)
            adView.mediaView = mediaView

            // --- 3. Body View ---
            val bodyView = TextView(context).apply {
                id = android.view.View.generateViewId()
                textSize = 14f
                setTextColor(Color.DarkGray.toArgb())
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            }
            container.addView(bodyView)
            adView.bodyView = bodyView

            // --- 4. Call To Action & Store & Price ---
            val bottomRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // Price
            val priceView = TextView(context).apply {
                id = android.view.View.generateViewId()
                textSize = 13f
                setTextColor(Color.Black.toArgb())
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = 12
                }
            }
            bottomRow.addView(priceView)
            adView.priceView = priceView

            // Store
            val storeView = TextView(context).apply {
                id = android.view.View.generateViewId()
                textSize = 13f
                setTextColor(Color.Black.toArgb())
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    rightMargin = 12
                }
            }
            bottomRow.addView(storeView)
            adView.storeView = storeView

            // CTA Button (stretches to fill rest)
            val ctaView = android.widget.Button(context).apply {
                id = android.view.View.generateViewId()
                setBackgroundColor(Color(0xFF4CAF50).toArgb())
                setTextColor(Color.White.toArgb())
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            bottomRow.addView(ctaView)
            adView.callToActionView = ctaView

            container.addView(bottomRow)

            // ADD Container to AdView
            adView.addView(container)

            // --- POPULATE AND CONFIGURE VISIBILITY ---
            
            // Headline
            headlineView.text = nativeAd.headline
            
            // Icon
            if (nativeAd.icon == null) {
                iconView.visibility = android.view.View.GONE
            } else {
                iconView.setImageDrawable(nativeAd.icon?.drawable)
                iconView.visibility = android.view.View.VISIBLE
            }

            // Star Rating
            if (nativeAd.starRating == null) {
                ratingBar.visibility = android.view.View.GONE
            } else {
                ratingBar.rating = nativeAd.starRating!!.toFloat()
                ratingBar.visibility = android.view.View.VISIBLE
            }

            // Advertiser
            if (nativeAd.advertiser == null) {
                advertiserView.visibility = android.view.View.GONE
            } else {
                advertiserView.text = nativeAd.advertiser
                advertiserView.visibility = android.view.View.VISIBLE
            }

            // Body
            if (nativeAd.body == null) {
                bodyView.visibility = android.view.View.GONE
            } else {
                bodyView.text = nativeAd.body
                bodyView.visibility = android.view.View.VISIBLE
            }

            // Media
            if (nativeAd.mediaContent != null) {
                mediaView.mediaContent = nativeAd.mediaContent
                // mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP) // Optional, usually handled
            }

            // Price
            if (nativeAd.price == null) {
                priceView.visibility = android.view.View.GONE
            } else {
                priceView.text = nativeAd.price
                priceView.visibility = android.view.View.VISIBLE
            }

            // Store
            if (nativeAd.store == null) {
                storeView.visibility = android.view.View.GONE
            } else {
                storeView.text = nativeAd.store
                storeView.visibility = android.view.View.VISIBLE
            }

            // Call To Action
            if (nativeAd.callToAction == null) {
                ctaView.visibility = android.view.View.GONE
            } else {
                ctaView.text = nativeAd.callToAction
                ctaView.visibility = android.view.View.VISIBLE
            }

            // Final Commit
            adView.setNativeAd(nativeAd)
            adView
        }
    )
}

fun openPlayStoreReview(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: android.content.ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
    }
}
