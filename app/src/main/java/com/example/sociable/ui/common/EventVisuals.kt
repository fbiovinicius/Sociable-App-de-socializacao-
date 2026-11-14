package com.example.sociable.ui.common

import androidx.annotation.DrawableRes
import com.example.sociable.R
import com.example.sociable.data.model.EventCategory

/** Cada categoria usa um dos quatro pinos desenhados pela UI. */
@DrawableRes
fun EventCategory.pinRes(): Int = when (this) {
    EventCategory.PARTY -> R.drawable.ic_pin_party
    EventCategory.MUSIC -> R.drawable.ic_pin_show
    EventCategory.FOOD -> R.drawable.ic_pin_party
    EventCategory.SPORTS -> R.drawable.ic_pin_people
    EventCategory.TECH -> R.drawable.ic_pin_show
    EventCategory.ART -> R.drawable.ic_pin_show
    EventCategory.ACADEMIC -> R.drawable.ic_pin_people
    EventCategory.OUTDOORS -> R.drawable.ic_pin_people
    EventCategory.RELIGION -> R.drawable.ic_pin_party
    EventCategory.OTHER -> R.drawable.ic_pin_secret
}

@DrawableRes
fun pinRes(category: EventCategory, isSecret: Boolean): Int =
    if (isSecret) R.drawable.ic_pin_secret else category.pinRes()
