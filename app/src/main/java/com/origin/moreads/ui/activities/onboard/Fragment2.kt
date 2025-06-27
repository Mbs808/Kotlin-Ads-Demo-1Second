package com.origin.moreads.ui.activities.onboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.origin.moreads.R


class Fragment2 : Fragment() {

    var tvNext: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_2, container, false)

        tvNext = view.findViewById(R.id.tvNext)

        tvNext?.setOnClickListener {
            (activity as? OnBoardingActivity)?.moveToNextPage()
        }
        return view
    }

    companion object {

    }
}