package org.akhsaul.dicodingstory.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText
import org.akhsaul.dicodingstory.R

class TextInput : TextInputEditText {
    var isError = false
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        val currentText = text?.toString()
        isError = validate(currentText).not()
    }

    private fun validate(text: String?): Boolean {
        if (text == null) {
            error = null
            return false
        }

        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isEmail = variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        val isPersonName = variation == InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        val isPassword = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        var isValid = false
        when {
            text.isBlank() -> {
                error = context.getString(R.string.txt_error_required)
            }

            isEmail && Patterns.EMAIL_ADDRESS.matcher(text).matches().not() -> {
                error = context.getString(R.string.txt_error_email)
            }

            isPassword && text.length < 8 -> {
                error = context.getString(R.string.txt_error_password)
            }

            isPersonName && text.any { it.isLetter().not() } -> {
                error = context.getString(R.string.txt_error_name)
            }

            else -> {
                error = null
                isValid = true
            }
        }
        return isValid
    }
}