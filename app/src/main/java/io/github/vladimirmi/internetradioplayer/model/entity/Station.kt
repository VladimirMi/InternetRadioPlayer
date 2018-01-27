package io.github.vladimirmi.internetradioplayer.model.entity

import android.os.Parcel
import android.os.Parcelable
import java.util.*


/**
 * Created by Vladimir Mikhalev 04.10.2017.
 */

data class Station(val uri: String,
                   val name: String,
                   val group: String = "",
                   val genre: List<String> = emptyList(),
                   val url: String = "",
                   val bitrate: Int = 0,
                   val sample: Int = 0,
                   val favorite: Boolean = false,
                   val id: String = UUID.randomUUID().toString()) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createStringArrayList(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uri)
        parcel.writeString(name)
        parcel.writeString(group)
        parcel.writeStringList(genre)
        parcel.writeString(url)
        parcel.writeInt(bitrate)
        parcel.writeInt(sample)
        parcel.writeByte(if (favorite) 1 else 0)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }

        fun nullObject() = Station(" ", " ")

        fun Station.isNull() = uri.isBlank() && name.isBlank()
    }
}



