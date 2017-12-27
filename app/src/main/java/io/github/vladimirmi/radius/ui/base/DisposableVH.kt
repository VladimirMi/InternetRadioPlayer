package io.github.vladimirmi.radius.ui.base

import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Vladimir Mikhalev 27.12.2017.
 */

open class DisposableVH(itemView: View,
                        val compDisp: CompositeDisposable = CompositeDisposable())
    : RecyclerView.ViewHolder(itemView), Disposable by compDisp