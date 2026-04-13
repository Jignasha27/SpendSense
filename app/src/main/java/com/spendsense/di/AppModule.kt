package com.spendsense.di

import android.app.Application
import androidx.room.Room
import com.spendsense.data.local.SpendSenseDatabase
import com.spendsense.data.repository.TransactionRepositoryImpl
import com.spendsense.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSpendSenseDatabase(app: Application): SpendSenseDatabase {
        return Room.databaseBuilder(
            app,
            SpendSenseDatabase::class.java,
            "spendsense_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(db: SpendSenseDatabase): TransactionRepository {
        return TransactionRepositoryImpl(
            userDao = db.userDao,
            expenseDao = db.expenseDao,
            incomeDao = db.incomeDao,
            budgetDao = db.budgetDao
        )
    }
}
