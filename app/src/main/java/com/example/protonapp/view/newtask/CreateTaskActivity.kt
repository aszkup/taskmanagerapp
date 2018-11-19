package com.example.protonapp.view.newtask

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.android.base.model.OperationError
import com.android.base.utils.enums.GENERAL_MESSAGE
import com.android.base.utils.extensions.hideSoftKeyboard
import com.android.base.utils.extensions.showToast
import com.android.base.utils.extensions.value
import com.android.base.utils.extensions.visible
import com.android.base.view.BaseActivity
import com.example.protonapp.R
import com.example.protonapp.model.CreateTaskViewState
import com.example.protonapp.repository.task.Task
import com.example.protonapp.utils.FileUtils
import com.example.protonapp.utils.extension.addGradientBackground
import com.example.protonapp.viewmodel.newtask.CreateTaskViewModel
import kotlinx.android.synthetic.main.activity_task_create.*
import kotlinx.android.synthetic.main.toolbar.*
import org.kodein.di.generic.instance

class CreateTaskActivity : BaseActivity() {

    private lateinit var viewModel: CreateTaskViewModel
    private val fileUtils: FileUtils by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_create)
        toolbarTitle.text = getString(R.string.create_task)
        addGradientBackground()

        viewModel = getModel(CreateTaskViewModel::class.java)
        viewModel.viewState.observe(this, Observer {
            viewStateUpdated(it, ::onNewState, showError = ::showError)
        })
        rootLayout.setOnClickListener { hideSoftKeyboard() }
        setupButtons()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        returnIntent?.data?.also { returnUri ->
            viewModel.storeSelectedFileUri(returnUri)
        }
    }

    private fun setupButtons() {
        saveButton.setOnClickListener { createTask() }
        cancelButton.setOnClickListener { finish() }
        selectFileButton.setOnClickListener { getFile() }
    }

    private fun onNewState(viewState: CreateTaskViewState) {
        viewState.taskStored?.let { stored ->
            if (stored) {
                showToast(application, getString(R.string.task_stored), GENERAL_MESSAGE)
                finish()
                return
            }
        }
        viewState.task?.let {
            nameInput.setText(it.name)
            descriptionInput.setText(it.description)
        }
        viewState.fileUri?.let {
            fileNameText.text = fileUtils.getFileName(it)
            fileNameText.visible()
            attachmentIcon.visible()
        }
    }

    private fun showError(operationError: OperationError) {
        showErrorMessage(operationError)
    }

    private fun createTask() {
        viewModel.createTask(Task(
                name = nameInput.value,
                description = descriptionInput.value,
                fileUri = ""))
    }

    private fun getFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = FILE_TYPE
        }
        startActivityForResult(intent, 0)
    }

    companion object {
        const val FILE_TYPE = "*/*"
    }
}
