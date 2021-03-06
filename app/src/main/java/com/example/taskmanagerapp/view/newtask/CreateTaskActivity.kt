package com.example.taskmanagerapp.view.newtask

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.Nullable
import androidx.lifecycle.Observer
import com.android.base.model.OperationError
import com.android.base.model.OperationStatus
import com.android.base.model.Success
import com.android.base.utils.BaseMessage
import com.android.base.utils.Event
import com.android.base.utils.enums.GENERAL_MESSAGE
import com.android.base.utils.extensions.hideSoftKeyboard
import com.android.base.utils.extensions.showToast
import com.android.base.utils.extensions.value
import com.android.base.utils.extensions.visible
import com.android.base.view.BaseActivity
import com.example.taskmanagerapp.R
import com.example.taskmanagerapp.model.CreateTaskViewState
import com.example.taskmanagerapp.repository.task.Task
import com.example.taskmanagerapp.utils.FileUtils
import com.example.taskmanagerapp.utils.extension.addGradientBackground
import com.example.taskmanagerapp.viewmodel.newtask.CreateTaskViewModel
import com.f2prateek.dart.Dart
import com.f2prateek.dart.InjectExtra
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import kotlinx.android.synthetic.main.activity_task_create.*
import kotlinx.android.synthetic.main.toolbar.*
import org.kodein.di.generic.instance

/**
 * Create Task [Activity]
 */
class CreateTaskActivity : BaseActivity() {

    private lateinit var viewModel: CreateTaskViewModel
    private val fileUtils: FileUtils by instance()
    private var uri: Uri? = null

    @Nullable
    @JvmField
    @InjectExtra(TASK)
    var task: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Dart.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_create)
        toolbarTitle.text = getString(R.string.create_task)
        addGradientBackground()

        viewModel = getModel(CreateTaskViewModel::class.java)
        viewModel.viewState.observe(this, Observer {
            viewStateUpdated(it, ::onNewState, showError = ::showError)
        })
        viewModel.taskStoredStatus.observe(this, Observer {
            onStoredStatus(it)
        })
        task?.let { initWithTask(it) }

        rootLayout.setOnClickListener { hideSoftKeyboard(); it.requestFocus() }
        keywordsInput.apply {
            addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
            addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
            addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
        }
        setupButtons()
    }

    private fun onStoredStatus(event: Event<OperationStatus>) {
        val content = event.getContentIfNotHandled()
        when (content) {
            is Success<*> -> {
                showMessage(content.data as BaseMessage)
                finish()
            }
            is OperationError -> {
                showMessage(content.message)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        returnIntent?.data?.also { returnedUri ->
            val takeFlags = returnIntent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION)
            grantUriPermission(packageName, returnedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(returnedUri, takeFlags)
            uri = returnedUri
            viewModel.storeSelectedFileUri(returnedUri)
        }
    }

    private fun initWithTask(task: Task) {
        viewModel.init(task)
        toolbarTitle.text = getString(R.string.edit_task)
    }

    private fun setupButtons() {
        saveButton.setOnClickListener { if (areFieldsValid()) createTask() }
        cancelButton.setOnClickListener { finish() }
        selectFileButton.setOnClickListener { getFile() }
    }

    private fun onNewState(viewState: CreateTaskViewState) {
        viewState.task?.let {
            nameInput.setText(it.name)
            descriptionInput.setText(it.description)
            it.keywords?.let { keywords ->
                val keywordsList = keywords.split(KEYWORDS_SEPARATOR)
                keywordsInput.setText(keywordsList)
            }
        }
        viewState.fileUri?.let {
            uri = it
            fileNameText.text = fileUtils.getFileName(it)
            fileNameText.visible()
            attachmentIcon.visible()
        }
    }

    private fun showError(operationError: OperationError) {
        showErrorMessage(operationError)
    }

    private fun createTask() {
        val taskToStore = Task(
                name = nameInput.value,
                description = descriptionInput.value.trim(),
                fileUri = "",
                keywords = getKeywords())
        task?.let {
            viewModel.saveTask(taskToStore)
            return
        }
        viewModel.createTask(taskToStore)
    }

    private fun getFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = FILE_TYPE
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, 0)
    }

    private fun areFieldsValid() = when {
        nameInput.value.isEmpty() -> {
            showToast(this, R.string.no_name_specified, GENERAL_MESSAGE)
            false
        }
        uri == null -> {
            showToast(this, R.string.no_file_selected, GENERAL_MESSAGE)
            false
        }
        else -> true
    }

    private fun getKeywords() =
            if (keywordsInput.chipValues.isNotEmpty())
                keywordsInput.chipValues.joinToString(separator = KEYWORDS_SEPARATOR)
            else null

    companion object {
        const val FILE_TYPE = "*/*"
        const val TASK = "task"
        const val KEYWORDS_SEPARATOR = ","
    }
}
