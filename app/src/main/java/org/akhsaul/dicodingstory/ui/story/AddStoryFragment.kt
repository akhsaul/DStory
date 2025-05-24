package org.akhsaul.dicodingstory.ui.story

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.akhsaul.dicodingstory.databinding.FragmentAddStoryBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class AddStoryFragment : Fragment(), KoinComponent {
    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddStoryViewModel by viewModel()
    private var isUploading = false
    private var canUseCamera = false
    private var canUseLocation = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { permission, isGranted ->
            when {
                permission == CAMERA && isGranted -> {
                    canUseCamera = true
                }

                permission == ACCESS_FINE_LOCATION && isGranted -> {
                    canUseCamera = false
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { fragmentRootView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply top padding to account for the status bar.
            // Other paddings (left, right, bottom) might be handled by MainActivity's root listener
            // or can be selectively applied here if this fragment needs different behavior.
            fragmentRootView.updatePadding(top = insets.top)
            windowInsets // Return original insets to allow other listeners or default handling
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}