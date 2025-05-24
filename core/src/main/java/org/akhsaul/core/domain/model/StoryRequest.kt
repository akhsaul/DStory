package org.akhsaul.core.domain.model

import java.io.File

data class StoryRequest(
    val photo: File,
    val description: String,
)
