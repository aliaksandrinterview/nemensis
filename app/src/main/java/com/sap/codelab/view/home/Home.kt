package com.sap.codelab.view.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sap.codelab.R
import com.sap.codelab.model.Memo
import com.sap.codelab.voice.VoiceCommands
import com.sap.codelab.utils.coroutines.ScopeProvider
import com.sap.codelab.view.create.CreateMemo
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo
import com.sap.codelab.voice.VoiceRecognizer
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.coroutines.launch

/**
 * The main activity of the app. Shows a list of recorded memos and lets the user add new memos.
 */
internal class Home : AppCompatActivity() {

    private lateinit var adapter: MemoAdapter
    private lateinit var model: HomeViewModel
    private lateinit var menuItemShowAll: MenuItem
    private lateinit var menuItemShowOpen: MenuItem
    private val ui = ScopeProvider.newScope()
    private var voiceRecognizer: VoiceRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        //Setup observation of the memo list (that we'll update the adapter with once it changes)
        model = ViewModelProvider(this).get(HomeViewModel::class.java)
        observeViewModel(model, false)

        //Setup the recycler view and the adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = MemoAdapter(mutableListOf(), {
            //Implementation for when the user selects a row to show the detail view
                view ->
            val intent = Intent(this@Home, ViewMemo::class.java)
            intent.putExtra(BUNDLE_MEMO_ID, (view.tag as Memo).id)
            startActivity(intent)
        }, {
            //Implementation for when the user marks a memo as completed
                checkbox, isChecked ->
            model.updateMemo(checkbox.tag as Memo, isChecked)
        })
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                (recyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        fab.setOnClickListener {
            //Handles clicks on the FAB button > creates a new Memo
            startActivity(Intent(this@Home, CreateMemo::class.java))
        }
        fab.setOnLongClickListener {
            startVoiceRecognition()
            true
        }
    }

    private fun startVoiceRecognition() {
        val recognizer = voiceRecognizer ?: VoiceRecognizer.Builder(this)
            .setOnVoiceRecognitionResult(::onVoiceRecognitionResult)
            .setOnVoiceRecognitionStarted {
                fab.setImageResource(R.drawable.ic_mic)
                Toast.makeText(this, R.string.say_command, Toast.LENGTH_SHORT).show()
            }
            .setOnVoiceRecognitionPreResult {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
            .setOnVoiceRecognitionError {
                Toast.makeText(this, R.string.voice_service_error, Toast.LENGTH_LONG).show()
            }
            .setOnStop {
                fab.setImageResource(R.drawable.ic_add)
            }
            .build()
            .also { voiceRecognizer = it }
        recognizer.start()
    }

    private fun observeViewModel(@NonNull viewModel: HomeViewModel, showAll: Boolean) {
        ui.launch {
            val observables = if (showAll) viewModel.getAllMemos() else viewModel.getOpenMemos()
            //Update the model with the observables
            viewModel.setMemos(observables, this@Home)
            observables.observe(this@Home) { memoList ->
                if (memoList != null) adapter.setItems(memoList)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuItemShowAll = menu.findItem(R.id.action_show_all)
        menuItemShowOpen = menu.findItem(R.id.action_show_open)
        return true
    }

    /**
     * Handles actionbar interactions.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_all -> {
                showAllMemos()
                true
            }
            R.id.action_show_open -> {
                showOpenMemos()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAllMemos() {
        observeViewModel(model, true)
        //Switch available menu options
        menuItemShowAll.isVisible = false
        menuItemShowOpen.isVisible = true
    }

    private fun showOpenMemos() {
        observeViewModel(model, false)
        //Switch available menu options
        menuItemShowOpen.isVisible = false
        menuItemShowAll.isVisible = true
    }

    /**
     * Overridden method for process recognized results. If needn't process some of commands add empty else block
     * @param result enum collection of supported commands
     */
    private fun onVoiceRecognitionResult(result: VoiceCommands) {
        when (result) {
            VoiceCommands.CREATE_MEMO -> openCreateNewMemo()
            VoiceCommands.SHOW_ALL -> showAllMemos()
            VoiceCommands.SHOW_OPEN -> showOpenMemos()
            VoiceCommands.UNKNOWN -> unknownCommand()
        }
    }

    /**
     * Show message about unknown command to user
     **/
    private fun unknownCommand() {
        Toast.makeText(this, R.string.unknown_command, Toast.LENGTH_LONG).show()
    }

    private fun openCreateNewMemo() =
        startActivity(Intent(this@Home, CreateMemo::class.java))

    override fun onStop() {
        voiceRecognizer?.stop()
        super.onStop()
    }

    override fun onDestroy() {
        ScopeProvider.cancel(ui)
        super.onDestroy()
    }
}
