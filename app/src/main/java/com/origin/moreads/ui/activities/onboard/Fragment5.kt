package com.origin.moreads.ui.activities.onboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.origin.moreads.R
import com.origin.moreads.extensions.prefsHelper
import com.origin.moreads.extensions.startIntent
import com.origin.moreads.ui.activities.main.MainActivity


class Fragment5 : Fragment() {

    var tvGetStart: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_5, container, false)

        tvGetStart = view.findViewById(R.id.tvGetStart)

        tvGetStart?.setOnClickListener {

            requireActivity().startIntent(MainActivity::class.java)
        }

        return view
    }

    companion object {

    }

}