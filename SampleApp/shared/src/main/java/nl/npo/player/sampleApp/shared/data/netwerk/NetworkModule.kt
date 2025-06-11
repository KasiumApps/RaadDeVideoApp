package nl.npo.player.sampleApp.shared.data.netwerk

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi
            .Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    fun provideOkHttpBuilder(
        @ApplicationContext context: Context,
    ): OkHttpClient.Builder =
        OkHttpClient.Builder().apply {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            addInterceptor(loggingInterceptor)
            addInterceptor(ChuckerInterceptor.Builder(context).build())
        }

    @Provides
    fun provideRetrofitBuilder(moshi: Moshi): Retrofit.Builder =
        Retrofit
            .Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
}
