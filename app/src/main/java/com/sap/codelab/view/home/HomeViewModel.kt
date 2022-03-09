package com.sap.codelab.view.home

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Home Activity.
 */
internal class HomeViewModel : ViewModel() {

    private var memos: LiveData<List<Memo>> = EmptyLiveData()

    suspend fun getAllMemos(): LiveData<List<Memo>> {
        return withContext(Dispatchers.Default) {
            Repository.instance.getAll()
        }
    }

    suspend fun getOpenMemos(): LiveData<List<Memo>> {
        return withContext(Dispatchers.Default) {
            Repository.instance.getOpen()
        }
    }

    fun updateMemo(memo: Memo, isChecked: Boolean) {
        ScopeProvider.application.launch {
            //We'll only forward the update if the memo has been checked, since we don't offer to uncheck memos right now
            if (isChecked) {
                Repository.instance.saveMemo(memo.copy(isDone = isChecked))
            }
        }
    }

    fun setMemos(newMemos: LiveData<List<Memo>>, lifecycleOwner: LifecycleOwner) {
        //Remove observers from existing memos
        memos.removeObservers(lifecycleOwner)
        //Update the memos that are being observed with the given ones
        memos = newMemos
    }
}

/**
 * Dummy class to avoid NPEs. This is basically an empty set of live data.
 */
private class EmptyLiveData : LiveData<List<Memo>>()