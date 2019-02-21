package io.github.vladimirmi.internetradioplayer.presentation.base

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar

/**
 * Created by Vladimir Mikhalev 13.02.2019.
 */

abstract class BaseViewGroup<P : BasePresenter<V>, V : BaseView> @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), BaseView  {

    lateinit var presenter: P

    protected abstract fun providePresenter(): P

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupView()
    }

    abstract fun setupView()

    override fun onStart() {
        if (!::presenter.isInitialized) presenter = providePresenter()
        @Suppress("UNCHECKED_CAST")
        presenter.attachView(this as V)
    }

    override fun onStop() {
        presenter.detachView()
    }

    override fun onDestroy() {
        presenter.destroy()
    }

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    override fun showSnackbar(resId: Int) {
        Snackbar.make(this, resId, Snackbar.LENGTH_SHORT).show()
    }

    override fun handleBackPressed(): Boolean {
        return false
    }
}