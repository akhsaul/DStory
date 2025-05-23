package org.akhsaul.dicodingstory

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.CAMERA
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.akhsaul.core.domain.model.Story
import org.akhsaul.dicodingstory.adapter.ListStoryAdapter
import org.akhsaul.dicodingstory.databinding.DialogAddStoryBinding
import org.akhsaul.dicodingstory.databinding.FragmentHomeBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

class HomeFragment : Fragment(), KoinComponent {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: ListStoryAdapter? = null
    private val adapter get() = _adapter!!
    private var _dialogAddStoryLayout: DialogAddStoryBinding? = null
    private val dialogAddStoryLayout get() = _dialogAddStoryLayout!!
    private val viewModel: HomeViewModel by inject()

    private var isUploading = false
    private var canUseCamera = false
    private var canUseLocation = false
    private var currentImageUri: Uri? = null

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
                }

                permission == CAMERA && !isGranted -> {
                }

                permission == ACCESS_FINE_LOCATION && !isGranted -> {
                }
            }
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    @OptIn(ExperimentalTime::class)
    fun getImageUri(context: Context): Uri {
        val timeStamp = fileNameFormatter.format(Clock.System.now().toJavaInstant())
        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/DStory/")
            }
            uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        }

        return uri.let {
            val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File(filesDir, "/MyCamera/$timeStamp.jpg")
            if (imageFile.parentFile?.exists() == false) imageFile.parentFile?.mkdir()
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                imageFile
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        _adapter = ListStoryAdapter {
            if (isUploading) {
                Log.i(TAG, "onCreateView: we doing uploading..")
                return@ListStoryAdapter
            }
            Log.i(TAG, "onCreateView: item $it")
        }
        binding.rvStory.adapter = adapter
        return binding.root
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            dialogAddStoryLayout.inputPhoto.setImageURI(it)
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(requireContext())
        launcherIntentCamera.launch(currentImageUri!!)
    }

    @OptIn(ExperimentalTime::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.fab.setOnClickListener {
            if (isUploading) {
                Log.i(TAG, "onCreateView: we doing uploading..")
                return@setOnClickListener
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(arrayOf(ACCESS_FINE_LOCATION))
            }

            if (canUseLocation.not()) {
                this.requireContext().showErrorWithToast(
                    lifecycleScope, "Location permission is not granted!"
                )
                return@setOnClickListener
            }

            _dialogAddStoryLayout =
                DialogAddStoryBinding.inflate(LayoutInflater.from(requireContext()))
            dialogAddStoryLayout.btnAddPhoto.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(arrayOf(CAMERA))
                }

                if (canUseCamera.not()) {
                    this.requireContext().showErrorWithToast(
                        lifecycleScope, "Camera permission is not granted!"
                    )
                    return@setOnClickListener
                }
                startCamera()
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Story")
                .setView(dialogAddStoryLayout.root)
                .setPositiveButton("Add") { dialog, _ ->
                    binding.progress.visibility = View.VISIBLE
                    isUploading = true

                    lifecycleScope.launch {
                        delay(5000)
                        binding.progress.visibility = View.GONE
                        isUploading = false
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }

        adapter.submitList(
            buildList<Story> {
                repeat(10) {
                    add(
                        Story(
                            "story-FvU4u0Vp2S3PMsFg",
                            "Akhsaul",
                            "Tak semua keberhasilan karier di dunia digital diawali dengan mengambil pendidikan formal di bidang yang sama, sebagaimana yang Meilia Tria Andari (24) alami. Perjalanan Meilia sebagai talenta informatika justru dimulai dengan berkuliah di jurusan Matematika.",
                            "https://dicoding-assets.sgp1.cdn.digitaloceanspaces.com/blog/wp-content/uploads/2025/05/Blog-Banner-1.png",
                            Clock.System.now().toString(),
                            -1.0,
                            -1.0
                        )
                    )
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _adapter = null
        _dialogAddStoryLayout = null
    }

    companion object {
        private val fileNameFormatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")
        private const val TAG = "HomeFragment"
    }
}