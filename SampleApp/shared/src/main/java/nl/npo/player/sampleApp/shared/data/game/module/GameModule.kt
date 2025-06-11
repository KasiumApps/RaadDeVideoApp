package nl.npo.player.sampleApp.shared.data.game.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nl.npo.player.sampleApp.shared.data.game.GameApi
import nl.npo.player.sampleApp.shared.data.game.GameRealRepository
import nl.npo.player.sampleApp.shared.domain.hackathon.GameRepository
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object GameModule {
    @Provides
    @Singleton
    fun provideGameApi(retrofit: Retrofit): GameApi = retrofit.create<GameApi>()

    @Provides
    fun provideGameRepository(api: GameApi): GameRepository = GameRealRepository(api)
}
