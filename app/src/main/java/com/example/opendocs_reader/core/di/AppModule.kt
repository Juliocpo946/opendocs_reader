package com.example.opendocs_reader.core.di

import android.content.Context
import com.example.opendocs_reader.core.data.repository.DocRepositoryImpl
import com.example.opendocs_reader.core.domain.repository.DocRepository

object AppModule {
    @Volatile
    private var docRepository: DocRepository? = null

    fun provideDocRepository(context: Context): DocRepository {
        return docRepository ?: synchronized(this) {
            val instance = DocRepositoryImpl(context.applicationContext)
            docRepository = instance
            instance
        }
    }
}