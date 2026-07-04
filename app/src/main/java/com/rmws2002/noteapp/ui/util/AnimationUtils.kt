package com.rmws2002.noteapp.ui.util

import androidx.compose.animation.core.spring

import androidx.compose.animation.core.SpringSpec

val SpringBounce: SpringSpec<Float> = spring(dampingRatio = 0.6f, stiffness = 400f)
val SpringStiff: SpringSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 600f)
val CardPressScale = 0.97f
