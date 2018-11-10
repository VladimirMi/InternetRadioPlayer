package io.github.vladimirmi.internetradioplayer.presentation.base

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

/**
 * Created by Vladimir Mikhalev 10.11.2018.
 */

abstract class BaseFragment<P : BasePresenter<V>, V : BaseView> : Fragment(), BaseView {

    lateinit var presenter: P

    protected abstract val layout: Int

    protected abstract fun providePresenter(): P

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = providePresenter()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupView(view)
    }

    protected abstract fun setupView(view: View)


    override fun onStart() {
        super.onStart()
        @Suppress("UNCHECKED_CAST")
        presenter.attachView(this as V)
    }

    override fun onStop() {
        presenter.detachView()
        super.onStop()
    }

    override fun onDestroy() {
        if (activity?.isFinishing == true) {
            presenter.destroy()
        }
        super.onDestroy()
    }

    //region =============== BaseView =============s=

    override fun onBackPressed(): Boolean {
        return childFragmentManager.fragments.any { it is BaseView && it.onBackPressed() }
    }

    override fun buildToolbar(builder: ToolbarBuilder) {
        builder.build(activity as ToolbarView)
    }

    override fun showToast(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    //endregion
}
