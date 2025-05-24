package org.akhsaul.dicodingstory

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
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
import org.akhsaul.core.domain.model.Story
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
        Toast.LENGTH_LONG
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
fun Story.createAtLocalTime(formatter: DateTimeFormatter): String = Instant.parse(this.createdAt)
    .toJavaInstant()
    .atZone(ZoneId.systemDefault())
    .format(formatter)