package com.tripadvisor.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(val name: String, val county: String, val url: String, val description: String) : Parcelable