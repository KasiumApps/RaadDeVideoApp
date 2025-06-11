package nl.npo.player.sampleApp.shared.data.netwerk

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
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
        val client = okHttpClientBuilder.build()
        return retrofitBuilder
            .baseUrl("https://quizclip-156411744834.europe-west4.run.app")
            .client(client)
            .build()
    }
}
