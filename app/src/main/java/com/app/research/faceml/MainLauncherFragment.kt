package com.app.research.faceml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.app.research.R
import com.app.research.databinding.FragmentMainLauncherBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainLauncherFragment : Fragment() {

    private var _binding: FragmentMainLauncherBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainLauncherBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnFaceDetection.setOnClickListener {
            findNavController().navigate(R.id.action_MainLauncherFragment_to_face_recognition_Fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}