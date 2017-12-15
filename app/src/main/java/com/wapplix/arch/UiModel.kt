package com.wapplix.arch

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by mike on 12/12/17.
 */
abstract class UiModel<T : LifecycleOwner>(application: Application) : AndroidViewModel(application) {

    private val onStartedEvents = PublishSubject.create<(T) -> Unit>()

    fun handleOn(lifecycleOwner: T) {
        val state = Observable.create<Lifecycle.State> { e ->
            val obs = GenericLifecycleObserver { source, _ ->
                val state = source.lifecycle.currentState
                if (state == Lifecycle.State.DESTROYED) e.onComplete()
                else e.onNext(state)
            }
            e.setCancellable { lifecycleOwner.lifecycle.removeObserver(obs) }
            lifecycleOwner.lifecycle.addObserver(obs)
        }
                .onTerminateDetach()
                .replay(1)
                .autoConnect()
        onStartedEvents
                .flatMapMaybe { event ->
                    state.filter { s -> s.isAtLeast(Lifecycle.State.STARTED) }
                            .firstElement()
                            .map { event }
                }
                .onTerminateDetach()
                .subscribe(
                        { action -> action(lifecycleOwner) },
                        { e -> Log.e("UiModel", "Error", e) }
                )
    }

    fun onUi(action: T.() -> Unit) {
        onStartedEvents.onNext(action)
    }

}

inline fun <TFragment : Fragment, reified VM : UiModel<TFragment>> TFragment.getUiModel(): VM =
        getViewModel<VM>().apply { handleOn(this@getUiModel) }

inline fun <TActivity : FragmentActivity, reified VM : UiModel<TActivity>> TActivity.getUiModel(): VM =
        getViewModel<VM>().apply { handleOn(this@getUiModel) }