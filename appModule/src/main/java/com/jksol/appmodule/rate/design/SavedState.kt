package com.jksol.appmodule.rate.design

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class SavedState : View.BaseSavedState {
    var rating = 0f

    constructor(superState: Parcelable?) : super(superState) {}

    private constructor(`in`: Parcel) : super(`in`) {
        rating = `in`.readFloat()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeFloat(rating)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(`in`: Parcel): SavedState {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}