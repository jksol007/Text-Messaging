package com.text.messages.sms.emoji.common.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.text.messages.sms.emoji.feature.main.BaseActivity
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.toolbar.*

abstract class MessagesActivity : BaseActivity() {

    protected val menu: Subject<Menu> = BehaviorSubject.create()

//    @SuppressLint("InlinedApi")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//        }
//
//        onNewIntent(intent)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setSupportActionBar(toolbar)
        title = title // The title may have been set before layout inflation
    }

    override fun setTitle(titleId: Int) {
        title = getString(titleId)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        toolbarTitle?.text = title
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        if (menu != null) {
            this.menu.onNext(menu)
        }
        return result
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        if (menu != null) {
            this.menu.onNext(menu)
        }
        return result
        //return super.onCreateOptionsMenu(menu)
    }

    protected open fun showBackButton(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

}