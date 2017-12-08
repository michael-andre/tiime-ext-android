package com.wapplix.arch

import android.app.Application
import android.app.Dialog
import android.arch.lifecycle.*
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import com.wapplix.bundleOf
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize

/**
 * Created by mike on 06/12/17.
 */
open class UiModel(application: Application) : AndroidViewModel(application) {

    private val showDialogEvent = PublishSubject.create<ShowDialogEvent>()

    fun handleOn(fragment: Fragment) = handleOn(fragment, fragment.childFragmentManager)
    fun handleOn(activity: FragmentActivity) = handleOn(activity, activity.supportFragmentManager)

    fun handleOn(lifecycleOwner: LifecycleOwner, fragmentManager: FragmentManager) {
        val state = Observable.create<Lifecycle.State> { e ->
            lifecycleOwner.lifecycle.addObserver(GenericLifecycleObserver { source, _ -> e.onNext(source.lifecycle.currentState) })
        }.replay(1).autoConnect()

        showDialogEvent
                .flatMapSingle { event ->
                    state.filter { s -> s.isAtLeast(Lifecycle.State.STARTED) }
                            .firstOrError()
                            .map { event }
                }
                .subscribe { event ->
                    event?.dialogFragment?.show(fragmentManager, event.tag)
                }
    }

    fun showConfirm(
            title: Int = 0,
            message: Int,
            positiveButton: Int = android.R.string.ok,
            negativeButton: Int = android.R.string.cancel,
            tag: String
    ) = showDialog(Config(titleRes = title, messageRes = message, positiveButtonRes = positiveButton, negativeButtonRes = negativeButton), tag)
            .filter { it == DialogInterface.BUTTON_POSITIVE }
            .map { true }

    fun showAsk(
            title: Int = 0,
            question: Int,
            positiveButton: Int = android.R.string.yes,
            negativeButton: Int = android.R.string.no,
            tag: String
    ) = showDialog(Config(titleRes = title, messageRes = question, positiveButtonRes = positiveButton, negativeButtonRes = negativeButton), tag)
            .map { it == DialogInterface.BUTTON_POSITIVE }

    fun showAsk(
            title: CharSequence? = null,
            question: CharSequence,
            positiveButton: CharSequence,
            negativeButton: CharSequence,
            tag: String
    ) = showDialog(Config(title = title, message = question, positiveButton = positiveButton, negativeButton = negativeButton), tag)
            .map { it == DialogInterface.BUTTON_POSITIVE }

    fun showDialog(config: Config, tag: String) = Maybe.create<Int> { emitter ->
        showDialogEvent.onNext(ShowDialogEvent(dialogFragment = ConfirmDialogFragment().init(config, emitter), tag = tag))
    }

    private data class ShowDialogEvent(var dialogFragment: DialogFragment, val tag: String)

    @Parcelize
    data class Config(
            val title: CharSequence? = null,
            val titleRes: Int = 0,
            val message: CharSequence? = null,
            val messageRes: Int = 0,
            val positiveButton: CharSequence? = null,
            val positiveButtonRes: Int = 0,
            val negativeButton: CharSequence? = null,
            val negativeButtonRes: Int = 0,
            val neutralButton: CharSequence? = null,
            val neutralButtonRes: Int = 0,
            val cancellable: Boolean = true
    ) : Parcelable

    class ConfirmDialogFragment : AppCompatDialogFragment() {

        private var emitter: MaybeEmitter<Int>? = null

        internal fun init(config: Config, emitter: MaybeEmitter<Int>): ConfirmDialogFragment {
            this.emitter = emitter
            this.arguments = bundleOf { putParcelable("config", config) }
            return this
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val vm = ViewModelProviders.of(this).get(VM::class.java)
            emitter = vm.init(emitter)
            val config = arguments!!.getParcelable<Config>("config")
            return AlertDialog.Builder(context!!)
                    .apply {
                        val handler: (DialogInterface, Int) -> Unit = { _, i -> emitter?.onSuccess(i) }
                        if (config.title != null) setTitle(config.title)
                        if (config.titleRes != 0) setTitle(config.titleRes)
                        if (config.message != null) setMessage(config.message)
                        if (config.messageRes != 0) setMessage(config.messageRes)
                        if (config.positiveButton != null) setPositiveButton(config.positiveButton, handler)
                        if (config.positiveButtonRes != 0) setPositiveButton(config.positiveButtonRes, handler)
                        if (config.negativeButton != null) setNegativeButton(config.negativeButton, handler)
                        if (config.negativeButtonRes != 0) setNegativeButton(config.negativeButtonRes, handler)
                        if (config.neutralButton != null) setNeutralButton(config.neutralButton, handler)
                        if (config.neutralButtonRes != 0) setNeutralButton(config.neutralButtonRes, handler)
                        setCancelable(config.cancellable)
                    }
                    .create()
        }

        override fun onDismiss(dialog: DialogInterface?) {
            emitter?.onComplete()
        }

        class VM : ViewModel() {
            private lateinit var emitter: MaybeEmitter<Int>
            internal fun init(emitter: MaybeEmitter<Int>?): MaybeEmitter<Int> {
                if (emitter != null) this.emitter = emitter
                return this.emitter
            }
        }

    }

}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(): T =
        ViewModelProviders.of(this)
                .get(T::class.java)

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(key: String): T =
        ViewModelProviders.of(this)
                .get(key, T::class.java)

inline fun <reified T : ViewModel> Fragment.getViewModel(): T =
        ViewModelProviders.of(this)
                .get(T::class.java)

inline fun <reified T : ViewModel> Fragment.getViewModel(key: String): T =
        ViewModelProviders.of(this)
                .get(key, T::class.java)