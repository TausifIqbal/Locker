package com.applocker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.applocker.data.AppInfo
import com.applocker.data.AppLockRepository
import com.applocker.utils.AppUtils
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppLockRepository.getInstance(application)
    private val allAppsLiveData = MutableLiveData<List<AppInfo>>()
    private var currentQuery: String = ""

    init {
        loadApps()
    }

    fun appsLiveData(): LiveData<List<AppInfo>> = allAppsLiveData

    fun loadApps() {
        allAppsLiveData.value = AppUtils.getLaunchableApps(getApplication(), repository)
    }

    fun toggleLock(packageName: String, lock: Boolean) {
        repository.updateLockState(packageName, lock)
        allAppsLiveData.value = allAppsLiveData.value.orEmpty().map { appInfo ->
            if (appInfo.packageName == packageName) {
                appInfo.copy(isLocked = lock)
            } else {
                appInfo
            }
        }
    }

    fun filterApps(showLocked: Boolean): List<AppInfo> {
        val query = currentQuery.trim().lowercase(Locale.getDefault())
        return allAppsLiveData.value.orEmpty()
            .asSequence()
            .filter { it.isLocked == showLocked }
            .filter {
                query.isBlank() ||
                    it.appName.lowercase(Locale.getDefault()).contains(query) ||
                    it.packageName.lowercase(Locale.getDefault()).contains(query)
            }
            .toList()
    }

    fun setQuery(query: String) {
        currentQuery = query
        allAppsLiveData.value = allAppsLiveData.value.orEmpty()
    }
}
