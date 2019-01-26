package io.github.vladimirmi.internetradioplayer.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/**
 * Created by Vladimir Mikhalev 10.11.2018.
 */

abstract class BaseFragment<P : BasePresenter<V>, V : BaseView> : Fragment(), BaseView {

    lateinit var presenter: P
    private lateinit var toast: Toast

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
        presenter.destroy()
        super.onDestroy()
    }

    //region =============== BaseView =============s=

    override fun handleBackPressed(): Boolean {
        return childFragmentManager.fragments.any { it is BackPressListener && it.handleBackPressed() }
    }

    override fun showToast(resId: Int) {
        if (::toast.isInitialized) toast.cancel()
        toast = Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun showSnackbar(resId: Int) {
        view?.let {
            Snackbar.make(it, resId, Snackbar.LENGTH_SHORT).show()
        }
    }

    //endregion

    protected val isPresenterInit: Boolean get() = ::presenter.isInitialized
}
