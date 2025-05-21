package org.akhsaul.dicodingstory.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText

class TextInput : TextInputEditText {
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
        validate(currentText)
    }

    private fun validate(text: String?) {
        if (text == null) {
            error = null
            return
        }

        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isEmail = variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        val isPersonName = variation == InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        val isPassword = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD

        when {
            text.isBlank() -> {
                error = "Required field"
            }

            isEmail && Patterns.EMAIL_ADDRESS.matcher(text).matches().not() -> {
                error = "Email is not valid"
            }

            isPassword && text.length < 8 -> {
                error = "Minimum 8 characters"
            }

            isPersonName && text.any { it.isLetter().not() } -> {
                error = "Name only contains letters"
            }

            else -> {
                error = null
            }
        }
    }
}