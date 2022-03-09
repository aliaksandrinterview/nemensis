package com.sap.codelab.view.create

import androidx.lifecycle.ViewModel
import android.content.Context
import androidx.annotation.NonNull
import com.sap.codelab.R
import com.sap.codelab.model.EMPTY_STRING
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.coroutines.launch

/**
 * ViewModel for matching CreateMemo view. Handles user interactions.
 */
internal class CreateMemoViewModel : ViewModel() {

    private var memo = Memo(0, EMPTY_STRING, EMPTY_STRING, 0, 0, 0, false)

    /**
     * Saves the memo in it's current state.
     */
    fun saveMemo() {
        ScopeProvider.application.launch {
            Repository.instance.saveMemo(memo)
        }
    }

    /**
     * Call this method to update the memo. This is usually needed when the user changed his input.
     */
    fun updateMemo(title: String, description: String) {
        memo = Memo(title = title, description = description, id = 0, reminderDate = 0, reminderLatitude = 0, reminderLongitude = 0, isDone = false)
    }

    /**
     * @return true if the title and content are not blank; false otherwise.
     */
    fun isMemoValid(): Boolean = memo.title.isNotBlank() && memo.description.isNotBlank()

    /**
     * @return an error message if the memo text is blank; an empty string otherwise.
     */
    @NonNull
    fun getTextError(@NonNull context: Context): String = if (memo.description.isBlank()) context.getString(R.string.memo_text_empty_error) else EMPTY_STRING

    /**
     * @return an error message if the memo title is blank; an empty string otherwise.
     */
    @NonNull
    fun getTitleError(@NonNull context: Context): String = if (memo.title.isBlank()) context.getString(R.string.memo_title_empty_error) else EMPTY_STRING
}