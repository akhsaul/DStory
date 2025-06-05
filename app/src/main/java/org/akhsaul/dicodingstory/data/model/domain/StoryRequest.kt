package org.akhsaul.dicodingstory.data.model.domain

import java.io.File

data class StoryRequest(
    val photo: File,
    val description: String,
)
