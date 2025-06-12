package nl.npo.player.sampleApp.shared.data.netwerk

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.shared.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class InitApiModule {
    @Provides
    @Singleton
    fun provideNpoIdRetrofitClient(
        okHttpClientBuilder: OkHttpClient.Builder,
        retrofitBuilder: Retrofit.Builder,
    ): Retrofit {
        val client =
            okHttpClientBuilder
                .addInterceptor { chain ->
                    val request: Request =
                        chain
                            .request()
                            .newBuilder()
                            .addHeader("x-api-key", BuildConfig.TOKEN_GAME_API_AUTH)
                            .build()
                    chain.proceed(request)
                }.build()
        return retrofitBuilder
            .baseUrl("https://quizclip-156411744834.europe-west4.run.app")
            .client(client)
            .build()
    }
}
