package com.example.protonapp.view.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.base.view.BaseFragment
import com.example.protonapp.R
import com.example.protonapp.view.main.TaskListAdapter
import com.example.protonapp.view.main.swipecallback.SwipeToStartTaskCallback
import com.example.protonapp.viewmodel.main.TasksViewModel
import kotlinx.android.synthetic.main.fragmnet_task_list.*

class FinishedTasksListFragment : BaseFragment() {

    private lateinit var viewModel: TasksViewModel
    private lateinit var tasksAdapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getModel(TasksViewModel::class.java)
        viewModel.viewState.observe(this, Observer {
            tasksAdapter.submitList(it)
            swipeLayout.isRefreshing = false
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragmnet_task_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTaskList()
        swipeLayout.setOnRefreshListener { viewModel.getFinishedTasks() }
        viewModel.getFinishedTasks()
    }

    private fun setupTaskList() {
        tasksAdapter = TaskListAdapter()
        recyclerView.adapter = tasksAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val dividerItemDecoration = DividerItemDecoration(activity, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)
        val itemTouchHelper = ItemTouchHelper(getSwipeHandler(ItemTouchHelper.LEFT))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun getSwipeHandler(swipeDirection: Int) =
            object : SwipeToStartTaskCallback(swipeDirection) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    tasksAdapter.getItemAt(viewHolder.adapterPosition)?.let {
                        viewModel.startTask(it)
                    }
                }
            }

    companion object {
        fun newInstance() = FinishedTasksListFragment()
    }
}