package org.akhsaul.core

import android.content.Context
import androidx.startup.Initializer
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.svg.SvgDecoder

@Suppress("unused")
class CoilInitializer : Initializer<ImageLoader> {
    override fun create(context: Context): ImageLoader {
        val imageLoader = ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache(MemoryCache.Builder().maxSizePercent(context, 0.25).build())
            .diskCache(
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.25)
                    .build()
            )
            .build()
        SingletonImageLoader.setSafe { imageLoader }
        return imageLoader
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}