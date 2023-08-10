package com.yessorae.simpleupscaler.data.model

data class Data(
    val completed_at: Int,
    val created_at: Int,
    val image: String,
    val processed_at: Int,
    val progress: Int,
    val return_type: Int,
    val state: Int,
    val task_id: String,
    val type: String
)