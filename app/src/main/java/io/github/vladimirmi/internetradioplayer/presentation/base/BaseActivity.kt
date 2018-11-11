package io.github.vladimirmi.internetradioplayer.presentation.base

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View

/**
 * Created by Vladimir Mikhalev 11.11.2018.
 */

abstract class BaseActivity<P : BasePresenter<V>, V : BaseView>
    : AppCompatActivity(), BaseView, ToolbarView by ToolbarViewImpl() {

    lateinit var presenter: P
    protected val activityView: View by lazy {
        findViewById<View>(android.R.id.content)
    }

    protected abstract val layout: Int

    protected abstract fun providePresenter(): P

    protected abstract fun setupView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        presenter = providePresenter()
        setupView()
        setToolbarHost(this)
    }

    override fun onStart() {
        super.onStart()
        @Suppress("UNCHECKED_CAST")
        presenter.attachView(this as V)
    }

    override fun onStop() {
        presenter.detachView()
        dismissToolbarMenu()
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) {
            presenter.destroy()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!handleBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun handleBackPressed(): Boolean {
        return supportFragmentManager.fragments.any { it is BaseView && it.handleBackPressed() }
    }

    override fun showMessage(resId: Int) {
        Snackbar.make(activityView, resId, Snackbar.LENGTH_SHORT).show()
    }

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        prepareOptionsMenu(menu)
        return super.onPrepareOptionsMenu(menu)
    }
}
