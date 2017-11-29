package io.github.vladimirmi.radius.ui

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import io.github.vladimirmi.radius.R
import io.github.vladimirmi.radius.extensions.dp

class TagView(context: Context, tag: String, action: ((TagView) -> Unit)?) :
        TextView(context) {
    var picked = false
        private set

    init {
        if (action != null) {
            setOnClickListener({
                pick()
                run(action)
            })
        }
        val padding = 8 * context.dp
        setPadding(padding, padding, padding, padding)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        text = tag
        id = tag.hashCode()
        setupView()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        if (params is FlexboxLayout.LayoutParams) {
            val margin = (6 * context.dp)
            params.setMargins(margin, margin, margin, margin)
        }
        super.setLayoutParams(params)
    }

    fun pick() {
        picked = !picked
        setupView()
    }

    private fun setupView() {
        if (picked) {
            setBackgroundResource(R.drawable.btn_tag_accent)
            setTextColor(ContextCompat.getColor(context, R.color.accentColor))
        } else {
            setBackgroundResource(R.drawable.btn_tag)
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }
}