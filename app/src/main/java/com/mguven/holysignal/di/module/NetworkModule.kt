package com.mguven.holysignal.di.module

import android.os.Build
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mguven.holysignal.BuildConfig
import com.mguven.holysignal.TheApplication
import com.mguven.holysignal.di.interceptor.CacheInterceptor
import com.mguven.holysignal.di.interceptor.HeaderInterceptor
import com.mguven.holysignal.di.interceptor.LoggingInterceptor
import com.mguven.holysignal.network.*
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
import javax.inject.Named
import javax.inject.Singleton


@Module
class NetworkModule {

  companion object {
    const val BASE_API = "https://api.holysignal.com/v1/"
    const val GOOGLE_BASE_API = "https://www.googleapis.com/oauth2/v3/"
    const val CONNECTION_TIMEOUT = 60L //as milliseconds
    const val READ_TIMEOUT = 60L //as milliseconds
    const val APPLICATION_JSON = "application/json"
    const val HTTP_AGENT = "http.agent"
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
  }

  @Provides
  @Singleton
  @Named("retrofit")
  fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_API)
        .client(okHttpClient)
        //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
  }

  @Provides
  @Singleton
  @Named("google_retrofit")
  fun provideGoogleRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(GOOGLE_BASE_API)
        .client(okHttpClient)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
  }

  @Provides
  @Singleton
  fun provideNewsApi(@Named("retrofit") retrofit: Retrofit): NewsApi = retrofit.create(NewsApi::class.java)

  @Provides
  @Singleton
  fun provideSurahApi(@Named("retrofit") retrofit: Retrofit): SurahApi = retrofit.create(SurahApi::class.java)

  @Provides
  @Singleton
  fun provideMemberApi(@Named("retrofit") retrofit: Retrofit): MemberApi = retrofit.create(MemberApi::class.java)

  @Provides
  @Singleton
  fun provideNotesApi(@Named("retrofit") retrofit: Retrofit): NotesApi = retrofit.create(NotesApi::class.java)

  @Provides
  @Singleton
  fun provideFavouritesApi(@Named("retrofit") retrofit: Retrofit): FavouritesApi = retrofit.create(FavouritesApi::class.java)

  @Provides
  @Singleton
  fun provideOrderApi(@Named("retrofit") retrofit: Retrofit): OrderApi = retrofit.create(OrderApi::class.java)

  @Provides
  @Singleton
  fun provideDownloadApi(@Named("retrofit") retrofit: Retrofit): DownloadApi = retrofit.create(DownloadApi::class.java)

  @Provides
  @Singleton
  fun providePasswordRecoveryApi(@Named("retrofit") retrofit: Retrofit): PasswordRecoveryApi = retrofit.create(PasswordRecoveryApi::class.java)

  @Provides
  @Singleton
  fun provideGoogleApi(@Named("google_retrofit") retrofit: Retrofit): GoogleApi = retrofit.create(GoogleApi::class.java)

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
          .addHeader("phonebrand", Build.BRAND)
          .addHeader("phonemodel", Build.MODEL)
      newRequest = builder.build()
      chain.proceed(newRequest)
    }
  }


}
