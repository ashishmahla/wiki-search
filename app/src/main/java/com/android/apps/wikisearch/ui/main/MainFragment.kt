package com.android.apps.wikisearch.ui.main

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.apps.wikisearch.R
import com.android.apps.wikisearch.ui.adapters.HybridSearchPredictionAdapter
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.launch


class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var searchPredictionAdapter: HybridSearchPredictionAdapter
    private lateinit var searchResultAdapter: HybridSearchPredictionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            setStatusBarColor(it, Color.WHITE, true)
        }

        viewModel = ViewModelProvider(this).get()

        setupPredictionRecyclerView()
        setupSearchTextView()
    }

    private fun setupSearchTextView() {
        tietMfSearchQuery.setText(viewModel.searchQuery)
        tietMfSearchQuery.doAfterTextChanged {
            val sq = it.toString().trim()
            viewModel.searchQuery = sq
            setTopPredictionText(mtvMfSearchCompletion.text.toString(), sq)
        }

        tietMfSearchQuery.setOnFocusChangeListener { _: View, hasFocus: Boolean ->
            if (hasFocus) {
                tietMfSearchQuery.onFocusChangeListener = null
                mlMfRoot.transitionToEnd()

                this.activity?.let {
                    setStatusBarColor(it, Color.parseColor("#2B2B2B"), false)
                }
            }
        }

        tietMfSearchQuery.setOnEditorActionListener { textView: TextView, i: Int, keyEvent: KeyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                lifecycleScope.launch {
                    viewModel.getSearchPredictions(textView.text.toString()).pages.let {
                        searchResultAdapter.refreshDataSet(it)
                    }
                }
                true
            } else false
        }

        viewModel.searchPrediction.observe(viewLifecycleOwner, Observer {
            if (it != null && it.pages.isNotEmpty()) {
                setTopPredictionText(it.pages[0].title, viewModel.searchQuery ?: "")

                rvMfSearchPredictions.isVisible = true
                viewMfSpDivider.isVisible = true

                searchPredictionAdapter.refreshDataSet(it.pages)
            } else {
                rvMfSearchPredictions.isVisible = false
                viewMfSpDivider.isVisible = false

                Log.e(TAG, "Failed to load search predictions, searchPrediction: $it")
            }
        })
    }

    private fun setupPredictionRecyclerView() {
        searchPredictionAdapter = HybridSearchPredictionAdapter(isCompactAdapter = true) {
            TODO("Implement click listener")
        }

        rvMfSearchPredictions.apply {
            layoutManager = LinearLayoutManager(this@MainFragment.context)
            adapter = searchPredictionAdapter

            setHasFixedSize(true)
        }
    }

    private fun setupSearchResultRecyclerView() {
        searchResultAdapter = HybridSearchPredictionAdapter(isCompactAdapter = false) {
            TODO("Implement click listener")
        }

        rvMfSearchResults.apply {
            layoutManager = LinearLayoutManager(this@MainFragment.context)
            adapter = searchResultAdapter

            setHasFixedSize(true)
        }
    }

    private fun setTopPredictionText(predictionText: String, currSearchQuery: String) {
        mtvMfSearchCompletion.text =
            if (predictionText.startsWith(currSearchQuery) && currSearchQuery.isNotBlank()) predictionText
            else ""
    }

    private fun setStatusBarColor(
        activity: Activity,
        @ColorInt statusBarColor: Int,
        isLightStatusBar: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility =
                if (isLightStatusBar) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                else View.SYSTEM_UI_FLAG_VISIBLE

            activity.window.statusBarColor = statusBarColor
        }
    }
}