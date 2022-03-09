package com.sap.codelab.view.detail

import androidx.lifecycle.ViewModel
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ViewModel for matching ViewMemo view.
 */
internal class ViewMemoViewModel : ViewModel() {

    /**
     * Loads the memo whose id matches the given memoId from the database.
     */
    suspend fun getMemo(memoId: Long): Memo {
        return withContext(Dispatchers.Default) {
            Repository.instance.getMemoById(memoId)
        }
    }
}