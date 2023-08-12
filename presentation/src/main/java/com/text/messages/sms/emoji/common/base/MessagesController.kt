package com.text.messages.sms.emoji.common.base

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.text.messages.sms.emoji.ads.AppUtils
import com.text.messages.sms.emoji.common.PreferencesManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.toolbar.view.*

abstract class MessagesController<ViewContract : MessagesViewContract<State>, State, Presenter : MessagesPresenter<ViewContract, State>> : LifecycleController(), LayoutContainer {

    abstract var presenter: Presenter

    private val appCompatActivity: AppCompatActivity?
        get() = activity as? AppCompatActivity

    protected val themedActivity: MessagesThemedActivity?
        get() = activity as? MessagesThemedActivity

    override var containerView: View? = null

    @LayoutRes
    var layoutRes: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(layoutRes, container, false).also {
            containerView = it
            onViewCreated()
        }
    }

    open fun onViewCreated() {
        var lang: String = PreferencesManager.getLanguage(activity)
        if (!AppUtils.isEmptyString(lang)) {
            AppUtils.setLocale(activity, lang)
        } else {
            AppUtils.setLocale(activity, "eng")
        }
    }

    fun setTitle(@StringRes titleId: Int) {
        setTitle(activity?.getString(titleId))
    }

    fun setTitle(title: CharSequence?) {
        activity?.title = title
        view?.toolbarTitle?.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    fun showBackButton(show: Boolean) {
        appCompatActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    override fun onDestroyView(view: View) {
        containerView = null
        clearFindViewByIdCache()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onCleared()
    }

}
