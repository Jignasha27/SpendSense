package com.spendsense.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.spendsense.data.local.SpendSenseDatabase
import com.spendsense.data.repository.TransactionRepositoryImpl
import com.spendsense.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    init {
        System.loadLibrary("sqlcipher")
    }

    @Provides
    @Singleton
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
        masterKey: MasterKey
    ): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "spendsense_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideSpendSenseDatabase(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): SpendSenseDatabase {
        var dbPassphrase = sharedPreferences.getString("db_passphrase", null)
        if (dbPassphrase == null) {
            dbPassphrase = java.util.UUID.randomUUID().toString()
            sharedPreferences.edit().putString("db_passphrase", dbPassphrase).apply()
        }
        
        val factory = SupportOpenHelperFactory(dbPassphrase.toByteArray())
        
        return Room.databaseBuilder(
            context,
            SpendSenseDatabase::class.java,
            "spendsense_db"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration() // Added for the schema change
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(db: SpendSenseDatabase): TransactionRepository {
        return TransactionRepositoryImpl(
            userDao = db.userDao,
            expenseDao = db.expenseDao,
            incomeDao = db.incomeDao,
            budgetDao = db.budgetDao,
            goalDao = db.goalDao
        )
    }
}
