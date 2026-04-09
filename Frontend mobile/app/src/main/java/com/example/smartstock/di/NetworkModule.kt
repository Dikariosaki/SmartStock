package com.example.smartstock.di

import com.example.smartstock.BuildConfig
import com.example.smartstock.data.api.AuthApiService
import com.example.smartstock.data.api.InventoryApiService
import com.example.smartstock.data.api.MovementApiService
import com.example.smartstock.data.api.ProductApiService
import com.example.smartstock.data.api.RoleApiService
import com.example.smartstock.data.api.ReportApiService
import com.example.smartstock.data.api.SubcategoryApiService
import com.example.smartstock.data.api.TaskApiService
import com.example.smartstock.data.api.UserApiService
import com.example.smartstock.data.repository.AuthRepositoryImpl
import com.example.smartstock.data.repository.InventoryRepositoryImpl
import com.example.smartstock.data.repository.MovementRepositoryImpl
import com.example.smartstock.data.repository.ProductRepositoryImpl
import com.example.smartstock.data.repository.ReportRepositoryImpl
import com.example.smartstock.data.repository.TaskRepositoryImpl
import com.example.smartstock.data.repository.UserRepositoryImpl
import com.example.smartstock.data.session.AuthTokenInterceptor
import com.example.smartstock.domain.repository.AuthRepository
import com.example.smartstock.domain.repository.InventoryRepository
import com.example.smartstock.domain.repository.MovementRepository
import com.example.smartstock.domain.repository.ProductRepository
import com.example.smartstock.domain.repository.ReportRepository
import com.example.smartstock.domain.repository.TaskRepository
import com.example.smartstock.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authTokenInterceptor: AuthTokenInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideRoleApiService(retrofit: Retrofit): RoleApiService =
        retrofit.create(RoleApiService::class.java)

    @Provides
    @Singleton
    fun provideProductApiService(retrofit: Retrofit): ProductApiService =
        retrofit.create(ProductApiService::class.java)

    @Provides
    @Singleton
    fun provideSubcategoryApiService(retrofit: Retrofit): SubcategoryApiService =
        retrofit.create(SubcategoryApiService::class.java)

    @Provides
    @Singleton
    fun provideTaskApiService(retrofit: Retrofit): TaskApiService =
        retrofit.create(TaskApiService::class.java)

    @Provides
    @Singleton
    fun provideReportApiService(retrofit: Retrofit): ReportApiService =
        retrofit.create(ReportApiService::class.java)

    @Provides
    @Singleton
    fun provideInventoryApiService(retrofit: Retrofit): InventoryApiService =
        retrofit.create(InventoryApiService::class.java)

    @Provides
    @Singleton
    fun provideMovementApiService(retrofit: Retrofit): MovementApiService =
        retrofit.create(MovementApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(taskRepositoryImpl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(reportRepositoryImpl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(inventoryRepositoryImpl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindMovementRepository(movementRepositoryImpl: MovementRepositoryImpl): MovementRepository
}
