package com.app.research.faceml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.research.databinding.FragmentFaceRecognitionBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FaceRecognitionFragment : Fragment() {

    private var _binding: FragmentFaceRecognitionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFaceRecognitionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//            binding.graphicOverlay.draw(canvas)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}