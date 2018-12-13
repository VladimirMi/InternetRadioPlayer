package io.github.vladimirmi.internetradioplayer.presentation.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import io.github.vladimirmi.internetradioplayer.R


/**
 * Created by Vladimir Mikhalev 30.09.2018.
 */

class SeekBarDialogPreference : DialogPreference {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.dialogPreferenceStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var progress = 0
        set(value) {
            field = value
            summary = createSummary(value)
            persistInt(value)
        }

    init {
        progress = getPersistedInt(progress)
        summary = createSummary(progress)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        progress = if (defaultValue == null) getPersistedInt(0)
        else defaultValue as Int
    }

    override fun getDialogLayoutResource(): Int {
        return R.layout.pref_seekbar
    }

    fun createSummary(progress: Int): String {
        return context.resources.getQuantityString(R.plurals.plural_second, progress, progress)
    }
}
