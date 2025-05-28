package org.akhsaul.dicodingstory

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun Context.showMessageWithDialog(
    title: String,
    message: String,
    onOk: () -> Unit
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK") { dialog, _ ->
            onOk()
            dialog.dismiss()
        }
        .setCancelable(false)
        .show()
}

fun TextInputLayout.getText(): String? = editText?.text?.toString()

fun Context?.showErrorWithToast(
    scope: LifecycleCoroutineScope,
    message: String? = null,
    onShow: () -> Unit = {},
    onHidden: () -> Unit = {}
) = showMessageWithToast(
    scope,
    message ?: "No internet available",
    onShow,
    onHidden
)

fun Context?.showMessageWithToast(
    scope: LifecycleCoroutineScope,
    message: String,
    onShow: () -> Unit = {},
    onHidden: () -> Unit = {}
) {
    if (this == null) return

    val toast = Toast.makeText(
        this,
        message,
        Toast.LENGTH_SHORT
    )

    if (Build.VERSION.SDK_INT >= 30) {
        toast.callBack(onShow, onHidden)
    } else {
        toast.show()
        onShow()
        scope.launch(Dispatchers.IO) {
            delay((3.2).seconds)
            onHidden()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun Toast.callBack(onShow: () -> Unit, onHidden: () -> Unit) {
    addCallback(object : Toast.Callback() {
        override fun onToastShown() {
            onShow()
        }

        override fun onToastHidden() {
            onHidden()
            removeCallback(this)
        }
    })
    show()
}

/**
 * shorthand for using [repeatOnLifecycle]
 * */
fun <T> Flow<T>.collectOn(
    scope: LifecycleCoroutineScope,
    owner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>
) {
    scope.launch {
        owner.lifecycle.repeatOnLifecycle(state) {
            this@collectOn.collect(collector)
        }
    }
}

fun Context.showConfirmationDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    onYes: () -> Unit
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Yes") { _, _ ->
            onYes()
        }
        .setNegativeButton("No", null)
        .setCancelable(false)
        .show()
}

fun Context.showExitConfirmationDialog(onYes: () -> Unit) {
    showConfirmationDialog(
        R.string.app_name,
        R.string.exit_confirm_msg,
        onYes
    )
}

@OptIn(ExperimentalTime::class)
fun Instant.formatToZone(
    formatter: DateTimeFormatter,
    zone: ZoneId = ZoneId.systemDefault()
): String = this.toJavaInstant()
    .atZone(zone)
    .format(formatter)

@OptIn(ExperimentalTime::class)
fun Context.getImageUri(fileNameFormatter: DateTimeFormatter): Uri {
    val timeStamp = Clock.System.now().formatToZone(fileNameFormatter)
    var uri: Uri? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/DStory/")
        }
        uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    return uri ?: run {
        val filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(filesDir, "/DStory/$timeStamp.jpg")
        if (imageFile.parentFile?.exists() == false) imageFile.parentFile?.mkdir()
        FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )
    }
}