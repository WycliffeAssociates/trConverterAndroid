package bible.translationtools.converter.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDirectoryProvider(
        @ApplicationContext context: Context
    ): DirectoryProvider {
        return DirectoryProviderImpl(context)
    }
}