package com.app.tubeapp

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner

class VkLoginFragment : DialogFragment() , LifecycleOwner{

    companion object {
        fun newInstance() = VkLoginFragment()
    }

    private lateinit var viewModel: VkLoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.vk_login_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = VkLoginViewModel()
        lifecycle.addObserver(viewModel)
        // TODO: Use the ViewModel
    }

}