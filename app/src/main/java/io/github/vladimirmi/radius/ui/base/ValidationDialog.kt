package io.github.vladimirmi.radius.ui.base

import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.afterTextChangeEvents
import io.github.vladimirmi.radius.R
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.regex.Pattern

/**
 * Created by Vladimir Mikhalev 13.11.2017.
 */

abstract class ValidationDialog<T>(layoutId: Int, viewGroup: ViewGroup)
    : BaseDialog(layoutId, viewGroup) {

    private val colorNormal = ContextCompat.getColor(viewGroup.context, R.color.grey_500)
    private val colorError = ContextCompat.getColor(viewGroup.context, R.color.red_800)
    private val colorText = ContextCompat.getColor(viewGroup.context, R.color.black)

    protected val NAME_PATTERN: Pattern = Pattern.compile(".{3,20}")
    protected val DESCRIPTION_PATTERN: Pattern = Pattern.compile(".{3,400}", Pattern.DOTALL)

    protected val compDisp = CompositeDisposable()

    protected fun EditText.validate(pattern: Pattern, nameField: TextView, error: String)
            : Observable<Boolean> {
        val name = nameField.text
        val drawable = background as GradientDrawable
        return afterTextChangeEvents()
                .skipInitialValue()
                .map { pattern.matcher(it.editable().toString()).matches() }
                .doOnNext { matches ->
                    if (matches) {
                        drawable.setStroke(3, colorNormal)
                        nameField.setTextColor(colorText)
                        nameField.text = name
                    } else {
                        drawable.setStroke(3, colorError)
                        nameField.setTextColor(colorError)
                        nameField.text = error
                    }
                }
    }

    protected fun validateForm(observables: List<Observable<Boolean>>): Observable<Boolean> =
            Observable.combineLatest(observables, { it.all { it as Boolean } })

    override fun open() {
        super.open()
        if (compDisp.size() == 0) compDisp.add(listenFields())
    }

    override fun close() {
        super.close()
        clearDialog()
        compDisp.clear()
    }

    abstract protected fun listenFields(): Disposable
    abstract protected fun setupDialog()
    abstract protected fun clearDialog()
}