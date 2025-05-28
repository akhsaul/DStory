package org.akhsaul.dicodingstory.ui.story

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil3.load
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.akhsaul.core.data.Result
import org.akhsaul.dicodingstory.R
import org.akhsaul.dicodingstory.collectOn
import org.akhsaul.dicodingstory.databinding.FragmentAddStoryBinding
import org.akhsaul.dicodingstory.getImageUri
import org.akhsaul.dicodingstory.getText
import org.akhsaul.dicodingstory.showErrorWithToast
import org.akhsaul.dicodingstory.showMessageWithDialog
import org.akhsaul.dicodingstory.ui.base.ProgressBarControls
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

class AddStoryFragment : Fragment() {
    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddStoryViewModel by viewModel()
    private var canUseCamera = false
    private var canUseLocation = false
    private var currentImageUri: Uri? = null
    private var progressBar: ProgressBarControls? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { permission, isGranted ->
            when {
                permission == CAMERA && isGranted -> {
                    canUseCamera = true
                }

                permission == ACCESS_FINE_LOCATION && isGranted -> {
                    canUseLocation = true
                }

                permission == CAMERA && shouldShowRequestPermissionRationale(
                    CAMERA
                ) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected, and what
                    // features are disabled if it's declined. In this UI, include a
                    // "cancel" or "no thanks" button that lets the user continue
                    // using your app without granting the permission.
                    // Show your custom rationale dialog here.
                    showPermissionRationaleDialog(
                        "Camera Permission Required",
                        CAMERA,
                        "To add a photo to your story, please allow camera access."
                    )
                }

                permission == ACCESS_FINE_LOCATION && shouldShowRequestPermissionRationale(
                    ACCESS_FINE_LOCATION
                ) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected, and what
                    // features are disabled if it's declined. In this UI, include a
                    // "cancel" or "no thanks" button that lets the user continue
                    // using your app without granting the permission.
                    // Show your custom rationale dialog here.
                    showPermissionRationaleDialog(
                        "Location Permission Required",
                        ACCESS_FINE_LOCATION,
                        "This app needs location permission to add a story. Please grant the permission."
                    )
                }

                permission == CAMERA && !isGranted -> {
                    showPermissionDeniedForeverDialog(
                        "Camera Permission Required",
                        "You have denied camera permission. Please go to app settings and grant the permission manually."
                    )
                }

                permission == ACCESS_FINE_LOCATION && !isGranted -> {
                    showPermissionDeniedForeverDialog(
                        "Location Permission Required",
                        "You have denied location permission. Please go to app settings and grant the permission manually."
                    )
                }
            }
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            binding.inputPhoto.load(requireNotNull(currentImageUri))
        } else {
            Log.w(TAG, "Failed to get photo from camera")
            currentImageUri = null
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentImageUri = result.data?.data
            binding.inputPhoto.load(requireNotNull(currentImageUri))
        } else {
            Log.w(TAG, "Failed to get photo from gallery")
        }
    }

    private fun showPermissionRationaleDialog(title: String, message: String, permission: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(arrayOf(permission))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionDeniedForeverDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Open settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProgressBarControls) {
            progressBar = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        canUseCamera = ContextCompat.checkSelfPermission(
            requireContext(),
            CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        canUseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            viewModel.addStoryResult.collectOn(
                lifecycleScope,
                viewLifecycleOwner
            ) {
                when (it) {
                    is Result.Loading -> {
                        progressBar?.showProgressBar()
                        btnUpload.setText(R.string.txt_uploading)
                        btnUpload.isEnabled = false
                    }

                    is Result.Error -> {
                        requireContext().showErrorWithToast(
                            lifecycleScope, it.message,
                            onShow = {
                                progressBar?.hideProgressBar()
                            },
                            onHidden = {
                                btnUpload.setText(R.string.txt_upload)
                                btnUpload.isEnabled = true
                            }
                        )
                    }

                    is Result.Success -> {
                        progressBar?.hideProgressBar()
                        btnUpload.setText(R.string.txt_upload)
                        btnUpload.isEnabled = true
                        requireContext().showMessageWithDialog(
                            "Add Story",
                            it.data
                        ) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
            btnAddPhotoFromCamera.setOnClickListener(::onButtonCameraClicked)
            btnAddPhotoFromGallery.setOnClickListener(::onButtonGalleryClicked)
            btnUpload.setOnClickListener(::onButtonUploadClicked)
        }
    }

    private fun onButtonCameraClicked(@Suppress("unused") v: View) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(arrayOf(CAMERA))
        }

        if (canUseCamera.not()) {
            this.requireContext().showErrorWithToast(
                lifecycleScope, "Camera permission is not granted!"
            )
            return
        }

        currentImageUri = requireContext().getImageUri(fileNameFormatter)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private fun onButtonGalleryClicked(@Suppress("unused") v: View) {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpeg"))
        }
        launcherIntentGallery.launch(
            Intent.createChooser(intent, "Pilih Photo (PNG, JPG, JPEG)")
        )
    }

    private fun onButtonUploadClicked(@Suppress("unused") v: View) {
        if (canUseLocation.not()) {
            this.requireContext().showErrorWithToast(
                lifecycleScope, "Location permission is not granted!"
            )
            return
        }

        val image = currentImageUri
        val desc = binding.edAddDescription.getText()

        when {
            image == null -> {
                requireContext().showErrorWithToast(
                    lifecycleScope, "Please add photo first!"
                )
                return
            }

            desc == null -> {
                requireContext().showErrorWithToast(
                    lifecycleScope, "Please add description first!"
                )
                return
            }

            else -> {
                viewModel.addStory(requireContext(), image, desc)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION))
        }

        if (canUseLocation) {
            val locationToken = CancellationTokenSource()
            LocationServices.getFusedLocationProviderClient(requireContext())
                .getCurrentLocation(
                    CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setMaxUpdateAgeMillis(5.minutes.inWholeMilliseconds)
                        .build(), locationToken.token
                ).addOnSuccessListener { location ->
                    viewModel.currentLocation.tryEmit(location)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private val fileNameFormatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")
        private const val TAG = "AddStoryFragment"
    }
}