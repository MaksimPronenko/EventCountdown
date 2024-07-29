package pronenko.eventcountdown

import android.app.Application
import android.content.Context
import androidx.room.Room
import pronenko.eventcountdown.data.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import pronenko.eventcountdown.data.EventsDao
import pronenko.eventcountdown.data.Repository
import pronenko.eventcountdown.domain.UniqueIdGenerator
import pronenko.eventcountdown.ui.editor.EditorViewModel
import pronenko.eventcountdown.ui.info.InfoViewModel
import pronenko.eventcountdown.ui.main.MainViewModel

class App : Application() {

    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "db"
        )
            .fallbackToDestructiveMigration()
            .build()

        startKoin {
            androidContext(this@App)
            modules(
                module{
                    viewModel<MainViewModel> {
                        MainViewModel(repository = get())
                    }
                    viewModel<EditorViewModel> {
                        EditorViewModel(repository = get(), uniqueIdGenerator = get())
                    }
                    viewModel<InfoViewModel> {
                        InfoViewModel(repository = get())
                    }
                    single<Repository> {
                        Repository(dao = get())
                    }
                    single<EventsDao> {
                        provideBicycleDao(context = get())
                    }
                    single<UniqueIdGenerator> {
                        UniqueIdGenerator(context = get())
                    }
                }
            )
        }
    }

    private fun provideBicycleDao(context: Context): EventsDao {
        return (context as App).db.eventsDao()
    }
}