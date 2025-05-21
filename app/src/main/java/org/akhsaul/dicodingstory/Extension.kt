package org.akhsaul.dicodingstory

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

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
            delay(2.seconds)
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