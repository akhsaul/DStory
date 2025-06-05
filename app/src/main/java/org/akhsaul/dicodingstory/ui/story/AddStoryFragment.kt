package org.akhsaul.dicodingstory.ui.story

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
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
import org.akhsaul.dicodingstory.databinding.FragmentAddStoryBinding
import org.akhsaul.dicodingstory.ui.base.ProgressBarControls
import org.akhsaul.dicodingstory.util.collectOn
import org.akhsaul.dicodingstory.util.getImageUri
import org.akhsaul.dicodingstory.util.getText
import org.akhsaul.dicodingstory.util.isError
import org.akhsaul.dicodingstory.util.showErrorWithToast
import org.akhsaul.dicodingstory.util.showMessageWithDialog
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
    private var locationToken: CancellationTokenSource? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val camera = results[CAMERA]
        val fineLocation = results[ACCESS_FINE_LOCATION]
        val coarseLocation = results[ACCESS_COARSE_LOCATION]
        binding.switchGps.isChecked = fineLocation == true || coarseLocation == true

        when {
            camera == true -> {
                canUseCamera = true
            }

            fineLocation == true || coarseLocation == true -> {
                canUseLocation = true
            }

            camera == false && shouldShowRequestPermissionRationale(CAMERA) -> {
                showPermissionRationaleDialog(
                    getString(R.string.txt_camera_permission_required),
                    getString(R.string.txt_camera_dialog_message),
                    CAMERA,
                )
            }

            fineLocation == false && shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationaleDialog(
                    getString(R.string.txt_location_permission_required),
                    getString(R.string.txt_location_dialog_message),
                    ACCESS_FINE_LOCATION,
                )
            }

            coarseLocation == false && shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION) -> {
                showPermissionRationaleDialog(
                    getString(R.string.txt_location_permission_required),
                    getString(R.string.txt_location_dialog_message),
                    ACCESS_COARSE_LOCATION,
                )
            }

            camera == false -> {
                showPermissionDeniedForeverDialog(
                    getString(R.string.txt_camera_permission_required),
                    getString(R.string.txt_camera_denied_dialog_message)
                )
            }

            fineLocation == false && coarseLocation == false -> {
                showPermissionDeniedForeverDialog(
                    getString(R.string.txt_location_permission_required),
                    getString(R.string.txt_location_denied_dialog_message)
                )
            }
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            binding.inputPhoto.load(requireNotNull(currentImageUri))
        } else {
            currentImageUri = null
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentImageUri = result.data?.data
            binding.inputPhoto.load(requireNotNull(currentImageUri))
        }
    }

    private fun showPermissionRationaleDialog(title: String, message: String, permission: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.txt_grant)) { _, _ ->
                requestPermissionLauncher.launch(arrayOf(permission))
            }
            .setNegativeButton(getString(R.string.txt_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionDeniedForeverDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.txt_open_setting)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.txt_cancel)) { dialog, _ ->
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
            viewModel.addStoryResult.collectOn(viewLifecycleOwner) {
                when (it) {
                    is Result.Loading -> {
                        progressBar?.showProgressBar()
                        btnUpload.setText(R.string.txt_uploading)
                        btnUpload.isEnabled = false
                    }

                    is Result.Error -> {
                        requireContext().showErrorWithToast(
                            lifecycleScope, it.message ?: getString(R.string.txt_error_no_network),
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
                            getString(R.string.txt_add_story),
                            it.data
                        ) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
            btnAddPhotoFromCamera.setOnClickListener { onButtonCameraClicked() }
            btnAddPhotoFromGallery.setOnClickListener { onButtonGalleryClicked() }
            btnUpload.setOnClickListener { onButtonUploadClicked() }
            switchGps.setOnClickListener { onSwitchGpsClicked() }
        }
    }

    private fun onButtonCameraClicked() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(arrayOf(CAMERA))
            return
        }

        currentImageUri = requireContext().getImageUri(fileNameFormatter)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private fun onButtonGalleryClicked() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png", "image/jpeg"))
        }
        launcherIntentGallery.launch(
            Intent.createChooser(intent, getString(R.string.txt_choose_photo))
        )
    }

    private fun FragmentAddStoryBinding.onButtonUploadClicked() {
        val image = currentImageUri
        val desc = edAddDescription
        val useGps = switchGps.isChecked

        when {
            image == null -> {
                requireContext().showErrorWithToast(
                    lifecycleScope, getString(R.string.txt_error_add_photo)
                )
                return
            }

            desc.isError() -> {
                requireContext().showErrorWithToast(
                    lifecycleScope, getString(R.string.txt_error_add_desc)
                )
                return
            }

            useGps && canUseLocation.not() -> {
                requireContext().showErrorWithToast(
                    lifecycleScope, "Please grant GPS or disable it!"
                )
                return
            }

            else -> {
                if (useGps) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                ACCESS_FINE_LOCATION,
                                ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    startCollectGps()
                }
                viewModel.addStory(requireContext(), image, desc.getText()!!)
            }
        }
    }

    private fun FragmentAddStoryBinding.onSwitchGpsClicked() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            switchGps.isChecked = false
            requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }
        startCollectGps()
    }

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    private fun startCollectGps() {
        if (canUseLocation) {
            if (locationToken != null) {
                return
            }
            val tokenSource = CancellationTokenSource()
            locationToken = tokenSource
            LocationServices.getFusedLocationProviderClient(requireContext())
                .getCurrentLocation(
                    CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setMaxUpdateAgeMillis(5.minutes.inWholeMilliseconds)
                        .build(), tokenSource.token
                ).addOnSuccessListener { location ->
                    viewModel.currentLocation.tryEmit(location)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        locationToken?.cancel()
        locationToken = null
    }

    companion object {
        private val fileNameFormatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")
    }
}