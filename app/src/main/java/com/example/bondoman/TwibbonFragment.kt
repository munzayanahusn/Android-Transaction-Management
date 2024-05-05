package com.example.bondoman

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.bondoman.databinding.FragmentTwibbonBinding
import java.io.File


class TwibbonFragment : Fragment() {
    private var _binding: FragmentTwibbonBinding? = null

    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var currentTwibbonIndex = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTwibbonBinding.inflate(inflater, container, false)

        if (!allPermissionGranted()) {
            requestPermissions(
                Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        } else {
            startCamera()
        }

        binding.captureBtnTwibbon.setOnClickListener{
            capturePhoto()
        }

        binding.changeCamera.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            startCamera()
        }

            binding.changeTwibbon.setOnClickListener {
                currentTwibbonIndex = when (currentTwibbonIndex) {
                    1 -> 2
                    2 -> 3
                    else -> 1
                }
                val twibbonResourceId = when (currentTwibbonIndex) {
                    1 -> R.drawable.twibbon1
                    2 -> R.drawable.twibbon2
                    3 -> R.drawable.twibbon3
                    else -> R.drawable.twibbon1
                }
                binding.twibbonView.setImageResource(twibbonResourceId)
            }

        binding.cobaLagiBtnTwibbon.setOnClickListener {
            binding.imagePreview.visibility = View.GONE

            binding.captureBtnTwibbon.visibility = View.VISIBLE
            binding.changeCamera.visibility = View.VISIBLE
            binding.changeTwibbon.visibility = View.VISIBLE

            binding.cobaLagiBtnTwibbon.visibility = View.GONE
        }
        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also { mPreview ->
                mPreview.setSurfaceProvider(binding.cameraViewTwibbon.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.d(Constants.TAG, "Start camera error: ", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun allPermissionGranted() = Constants.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
        _binding = null
    }

    private fun createTemporaryFile(): File {
        val storageDir: File? = requireContext().externalCacheDir
        return File.createTempFile(
            "temp",
            ".jpg",
            storageDir
        ).apply {
            deleteOnExit()
        }
    }

    private fun capturePhoto() {
        val photoFile = createTemporaryFile()

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)

                    activity?.runOnUiThread {
                        binding.imagePreview.visibility = View.VISIBLE
                        binding.imagePreview.setImageURI(savedUri)

                        binding.captureBtnTwibbon.visibility = View.GONE
                        binding.changeCamera.visibility = View.GONE
                        binding.changeTwibbon.visibility = View.GONE

                        binding.cobaLagiBtnTwibbon.visibility = View.VISIBLE
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("ImageCapture", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }


}