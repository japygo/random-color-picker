package com.japygo.study.randomcolorpicker.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    // Ad Unit IDs from BuildConfig (injected from local.properties or release.properties)
    private const val BANNER_AD_UNIT_ID = com.japygo.study.randomcolorpicker.BuildConfig.ADMOB_BANNER_ID
    private const val INTERSTITIAL_AD_UNIT_ID = com.japygo.study.randomcolorpicker.BuildConfig.ADMOB_INTERSTITIAL_ID
    private const val NATIVE_AD_UNIT_ID = com.japygo.study.randomcolorpicker.BuildConfig.ADMOB_NATIVE_ID

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    
    // Cached Ads
    private var bannerAdView: AdView? = null
    var nativeAd: com.google.android.gms.ads.nativead.NativeAd? = null
        private set
    
    // Retry Logic
    private var currentRetryAttempt = 0
    private const val MAX_RETRY_ATTEMPTS = 3

    // Frequency Capping: Show ad every 3 times.
    private const val AD_FREQUENCY = 3
    private var cameraUsageCount = 0

    fun initialize(context: Context) {
        val appContext = context.applicationContext
        MobileAds.initialize(appContext) {
            // Load ads after initialization
            loadBanner(appContext)
            loadNativeAd(appContext)
        }
        loadInterstitial(appContext)
    }

    private fun loadBanner(context: Context) {
        bannerAdView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BANNER_AD_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
    }
    
    fun loadNativeAd(context: Context) {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { ad: com.google.android.gms.ads.nativead.NativeAd ->
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak.
                if (nativeAd != null) {
                    nativeAd?.destroy()
                }
                nativeAd = ad
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                }
            })
            .withNativeAdOptions(
                com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    @Composable
    fun BannerAd(modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                // 1. Try to use cached view
                bannerAdView?.let { adView ->
                    // Remove from previous parent (ViewGroup) if exists to allow reparenting
                    (adView.parent as? android.view.ViewGroup)?.removeView(adView)
                    adView
                } ?: run {
                    // 2. Fallback: Create new using Application Context to prevent Activity leaks
                    // Cache it for future reuse
                    AdView(context.applicationContext).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }.also { bannerAdView = it }
                }
            },
            onRelease = {
                // Do NOT destroy the cached AdView here to allow reuse across screens.
                // It will be kept alive as a singleton.
                // If we destroyed it, we would lose the loaded ad.
            }
        )
    }

    fun loadInterstitial(context: Context) {
        if (isAdLoading || interstitialAd != null) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        // Use ApplicationContext to prevent Activity leaks during loading
        val appContext = context.applicationContext

        InterstitialAd.load(
            appContext,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                    
                    // Retry logic with exponential backoff
                    if (currentRetryAttempt < MAX_RETRY_ATTEMPTS) {
                        currentRetryAttempt++
                        val delayMillis = (1000 * Math.pow(2.0, currentRetryAttempt.toDouble())).toLong()
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            loadInterstitial(appContext)
                        }, delayMillis)
                    }
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    currentRetryAttempt = 0 // Reset retry counter on success
                }
            }
        )
    }

    /**
     * Handles the flow when exiting the camera screen.
     * Increments usage count and decides whether to show an ad.
     * Navigation should occur BEFORE calling this method.
     */
    fun handleCameraExit(activity: Activity) {
        cameraUsageCount++
        
        if (shouldShowAd() && interstitialAd != null) {
            showInterstitial(activity)
        } else {
            // If ad not ready, try loading one for next time
            if (interstitialAd == null) {
                loadInterstitial(activity)
            }
        }
    }

    private fun showInterstitial(activity: Activity) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                cleanupAd()
                loadInterstitial(activity) // Pre-load next ad
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                cleanupAd()
            }
            
            override fun onAdShowedFullScreenContent() {
                // Ad showed
                interstitialAd = null // Clear reference immediately after show to avoid reuse
            }
        }
        interstitialAd?.show(activity)
        resetCounter()
    }

    private fun cleanupAd() {
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
    }

    private fun shouldShowAd(): Boolean {
        return cameraUsageCount >= AD_FREQUENCY
    }

    private fun resetCounter() {
        cameraUsageCount = 0
    }
}
