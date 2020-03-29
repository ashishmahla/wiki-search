package com.android.apps.wikisearch.ui.main

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.apps.wikisearch.R
import com.android.apps.wikisearch.models.Page
import com.android.apps.wikisearch.ui.adapters.HybridSearchPredictionAdapter
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.launch


class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var searchPredictionAdapter: HybridSearchPredictionAdapter
    private lateinit var searchResultAdapter: HybridSearchPredictionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            setStatusBarColor(it, Color.parseColor("#F7F6F5"), true)
        }

        viewModel = ViewModelProvider(this).get()

        setupPredictionRecyclerView()
        setupSearchTextView()
        setupSearchResultRecyclerView()
    }

    private fun setupSearchTextView() {
        tietMfSearchQuery.setText(viewModel.searchQuery)
        tietMfSearchQuery.doAfterTextChanged {
            val sq = it.toString().trim()
            if (sq != viewModel.searchQuery) {
                viewModel.isSubmitted = false
                viewModel.searchQuery = sq
                setTopPredictionText(mtvMfSearchCompletion.text.toString(), sq)
            }
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

        tietMfSearchQuery.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                submitSearchText(tietMfSearchQuery.text.toString().trim())

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

                setTopPredictionText("", viewModel.searchQuery ?: "")

                Log.e(TAG, "Failed to load search predictions, searchPrediction: $it")
            }
        })
    }

    private fun setupPredictionRecyclerView() {
        searchPredictionAdapter = HybridSearchPredictionAdapter(isCompactAdapter = true) {
            tietMfSearchQuery.setText(it.title)
            submitSearchText(it.title)
        }

        rvMfSearchPredictions.apply {
            layoutManager = LinearLayoutManager(this@MainFragment.context)
            adapter = searchPredictionAdapter

            setHasFixedSize(true)
        }
    }

    private fun setupSearchResultRecyclerView() {
        searchResultAdapter = HybridSearchPredictionAdapter(isCompactAdapter = false) {
            launchWebView(it)
        }

        rvMfSearchResults.apply {
            layoutManager = LinearLayoutManager(this@MainFragment.context)
            adapter = searchResultAdapter

            setHasFixedSize(true)
        }

        viewModel.searchResult?.let {
            searchResultAdapter.refreshDataSet(it.pages)
            viewModel.isSubmitted = true
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

    private fun submitSearchText(searchText: String) {
        hideKeyboard()
        viewModel.isSubmitted = true
        lifecycleScope.launch {
            try {
                viewModel.getSearchPredictions(searchText).let {
                    viewModel.searchResult = it
                    searchResultAdapter.refreshDataSet(it.pages)
                }
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
    }

    private fun launchWebView(page: Page) {
        val action = MainFragmentDirections.actionMainFragmentToWebViewFragment(
            page.title, page.fullUrl
        )

        findNavController().navigate(action)
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus
        view?.let { v ->
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}