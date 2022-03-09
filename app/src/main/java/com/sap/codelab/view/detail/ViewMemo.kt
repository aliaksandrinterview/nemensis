package com.sap.codelab.view.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sap.codelab.R
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_create_memo.*
import kotlinx.coroutines.launch

const val BUNDLE_MEMO_ID: String = "memoId"

/**
 * Activity that allows a user to see the details of a memo.
 */
internal class ViewMemo : AppCompatActivity() {

    private val ui = ScopeProvider.newScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_memo)
        setSupportActionBar(toolbar)
        //Initialize views with the passed memo id
        val model = ViewModelProvider(this).get(ViewMemoViewModel::class.java)
        if (savedInstanceState == null) {
            ui.launch {
                val id = intent.getLongExtra(BUNDLE_MEMO_ID, -1)
                val memo = model.getMemo(id)
                //Update the UI
                memo_title.setText(memo.title)
                memo_description.setText(memo.description)
                memo_title.isEnabled = false
                memo_description.isEnabled = false
            }
        }
    }

    override fun onDestroy() {
        ScopeProvider.cancel(ui)
        super.onDestroy()
    }
}
