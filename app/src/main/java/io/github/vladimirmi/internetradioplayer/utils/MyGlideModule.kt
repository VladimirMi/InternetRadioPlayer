package io.github.vladimirmi.internetradioplayer.utils

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions


/**
 * Created by Vladimir Mikhalev 25.03.2019.
 */

@GlideModule
class MyGlideModule : AppGlideModule() {

    companion object {
        private const val DISK_CACHE_SIZE = 1024 * 1024 * 50L // 50 MB
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE))
                .setDefaultRequestOptions(RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565))
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}