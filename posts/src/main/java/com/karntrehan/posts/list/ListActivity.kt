package com.karntrehan.posts.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.karntrehan.posts.R
import com.karntrehan.posts.list.data.ListViewModelFactory
import com.karntrehan.posts.list.data.local.Post
import com.karntrehan.posts.list.di.ListDH
import com.mpaani.core.networking.Outcome
import kotlinx.android.synthetic.main.activity_list.*
import javax.inject.Inject

class ListActivity : AppCompatActivity() {

    private val component by lazy { ListDH.component() }

    @Inject
    lateinit var viewModelFactory: ListViewModelFactory

    @Inject
    lateinit var adapter: ListAdapter

    private val storesViewModel: ListViewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(ListViewModel::class.java) }

    private val context: Context by lazy { this }

    private val TAG = "ListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        component.inject(this)
        setUpToolbar()

        rvPosts.adapter = adapter

        srlPosts.setOnRefreshListener { storesViewModel.refreshPosts() }

        Log.d(TAG, "onCreate: " + component.toString())

        storesViewModel.getPosts()
        initiateDataListener()
    }

    private fun initiateDataListener() {
        storesViewModel.postsOutcome.observe(this, Observer<Outcome<List<Post>>> { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    srlPosts.isRefreshing = outcome.loading
                    Toast.makeText(context, "Loading: " + outcome.loading, Toast.LENGTH_LONG).show()
                }
                is Outcome.Success -> {
                    Log.d(TAG, "onCreate: Success: ")
                    Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
                    adapter.setData(outcome.data)
                }
                is Outcome.Failure -> {
                    Log.d(TAG, "onCreate: failure: " + outcome.e)
                    Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setUpToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStop() {
        super.onStop()
        if (this.isFinishing)
            ListDH.distroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}