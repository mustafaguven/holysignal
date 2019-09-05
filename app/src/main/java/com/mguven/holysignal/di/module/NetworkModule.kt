package com.mguven.holysignal.di.module

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.BuildConfig
import com.mguven.holysignal.di.interceptor.CacheInterceptor
import com.mguven.holysignal.di.interceptor.HeaderInterceptor
import com.mguven.holysignal.di.interceptor.LoggingInterceptor
import com.mguven.holysignal.network.NewsApi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
class NetworkModule {

  companion object {
    const val BASE_API = "https://newsapi.org/v2/"
    const val CONNECTION_TIMEOUT = 60L //as milliseconds
    const val READ_TIMEOUT = 60L //as milliseconds
    const val APPLICATION_JSON = "application/json"
    const val HTTP_AGENT = "http.agent"
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
  }

  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_API)
        .client(okHttpClient)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
  }

  @Provides
  @Singleton
  fun provideNewsApi(retrofit: Retrofit): NewsApi = retrofit.create(NewsApi::class.java)

  @Provides
  @Singleton
  fun provideOkHttpClient(@HeaderInterceptor headerInterceptor: Interceptor,
                          @LoggingInterceptor loggingInterceptor: Interceptor,
                          @CacheInterceptor cacheInterceptor: Interceptor,
                          theApplication: TheApplication): OkHttpClient {
    return OkHttpClient.Builder()
        .cache(Cache(theApplication.cacheDir, CACHE_SIZE))
        .addInterceptor(headerInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(cacheInterceptor)
        .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
  }

  @Provides
  @Singleton
  @CacheInterceptor
  fun provideCacheInterceptor(): Interceptor {
    return Interceptor { chain ->
      val originalResponse = chain.proceed(chain.request())
       originalResponse.newBuilder()
          .removeHeader("Pragma")
          .header("Cache-Control", "public, if-only-cached, max-age=3600")
          .build()
    }
  }

  @Provides
  @Singleton
  @LoggingInterceptor
  fun provideLoggingInterceptor(): Interceptor {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    return interceptor
  }

  @Provides
  @Singleton
  @HeaderInterceptor
  fun provideHeaderInterceptor(): Interceptor {
    return Interceptor { chain ->
      val request = chain.request()
      val newRequest: Request
      val builder = request.newBuilder()
          .addHeader("Content-Type", APPLICATION_JSON)
          .addHeader("User-Agent", System.getProperty(HTTP_AGENT) ?: "Unknown Android Agent")
          .addHeader("Accept", APPLICATION_JSON)
          .addHeader("x-api-key", BuildConfig.API_KEY)
      newRequest = builder.build()
      chain.proceed(newRequest)
    }
  }


}
