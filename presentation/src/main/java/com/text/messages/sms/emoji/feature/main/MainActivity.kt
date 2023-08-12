package com.text.messages.sms.emoji.feature.main

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.jksol.appmodule.utils.Constants
import com.text.messages.sms.emoji.BuildConfig
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.ads.AdvertiseHandler
import com.text.messages.sms.emoji.ads.AppUtils
import com.text.messages.sms.emoji.common.Navigator
import com.text.messages.sms.emoji.common.androidxcompat.drawerOpen
import com.text.messages.sms.emoji.common.base.MessagesThemedActivity
import com.text.messages.sms.emoji.common.util.PreferencesManager
import com.text.messages.sms.emoji.common.util.RateMeNowDialog
import com.text.messages.sms.emoji.common.util.extensions.*
import com.text.messages.sms.emoji.feature.blocking.BlockingDialog
import com.text.messages.sms.emoji.feature.conversations.ConversationItemTouchCallback
import com.text.messages.sms.emoji.feature.conversations.ConversationsAdapter
import com.text.messages.sms.emoji.feature.customview.bottomsheet.BottomSheet
import com.text.messages.sms.emoji.manager.BillingManager
import com.text.messages.sms.emoji.manager.PermissionManager
import com.text.messages.sms.emoji.repository.SyncRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.contacts_activity.*
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_activity.bannerAdLayout
import kotlinx.android.synthetic.main.main_activity.cancel
import kotlinx.android.synthetic.main.main_activity.toolbar
import kotlinx.android.synthetic.main.main_activity.toolbarTitle
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import javax.inject.Inject

class MainActivity : MessagesThemedActivity(), MainView {

    @Inject
    lateinit var blockingDialog: BlockingDialog

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var conversationsAdapter: ConversationsAdapter

    @Inject
    lateinit var drawerBadgesExperiment: DrawerBadgesExperiment

    @Inject
    lateinit var searchAdapter: SearchAdapter

    @Inject
    lateinit var itemTouchCallback: ConversationItemTouchCallback

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var permissionManager: PermissionManager

    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy { compose.clicks() }
    override val languageIntent by lazy { img_tool_lang.clicks() }
    override val queryClearedIntent: Observable<*> by lazy { toolbarSearch.clicks() }

    override val drawerOpenIntent: Observable<Boolean> by lazy {
        drawerLayout.drawerOpen(Gravity.START)
            .doOnNext {
                dismissKeyboard()
                try {
                    clearSelection()
                } catch (e: java.lang.Exception) {

                }
            }
    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val navigationIntent: Observable<NavItem> by lazy {
        Observable.merge(
            listOf(
                backPressedSubject,
                inbox.clicks().map { NavItem.INBOX },
                archived.clicks().map { NavItem.ARCHIVED },
                backup.clicks().map { NavItem.BACKUP },
                scheduled.clicks().map { NavItem.SCHEDULED },
                blocking.clicks().map { NavItem.BLOCKING },
                settings.clicks().map { NavItem.SETTINGS },
                plus.clicks().map { NavItem.PLUS },
                help.clicks().map { NavItem.HELP },
                invite.clicks().map { NavItem.INVITE },
                language.clicks().map { NavItem.LANGUAGE },
                feedback.clicks().map { NavItem.FEEDBACK },
                rateus.clicks().map { NavItem.RATEUS },
                privacyPolicy.clicks().map { NavItem.PRIVACY_POLICY })
        )
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val plusBannerIntent by lazy { plusBanner.clicks() }
    override val dismissRatingIntent by lazy { rateDismiss.clicks() }
    override val rateIntent by lazy { rateOkay.clicks() }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }

    //    override val changelogMoreIntent by lazy { changelogDialog.moreClicks }
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }
    private val toggle by lazy {
        ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.main_drawer_open_cd,
            0
        )
    }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }

    //    private val changelogDialog by lazy { ChangelogDialog(this) }
    private val snackbar by lazy { findViewById<View>(R.id.snackbar) }
    private val syncing by lazy { findViewById<View>(R.id.syncing) }
    private val backPressedSubject: Subject<NavItem> = PublishSubject.create()

    private var rateUsDialog: Dialog? = null
    private var bottomSheetDialog: BottomSheet? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel.bindView(this)
        onNewIntentIntent.onNext(intent)
        advertiseHandler.loadNativeAds(
            object : AdvertiseHandler.Listener() {
                override fun onAdsLoadCompleted() {
                    super.onAdsLoadCompleted()
                    advertiseHandler.showNativeAds(
                        this@MainActivity,
                        bannerAdLayout,
                        false,
                        R.layout.banner_native_ads_admob_youtube,
                        null
                    )
                }
            },
            resources.getString(R.string.admob_native_ads_id),
            this@MainActivity
        )



        Constants.isSplashScreen = false
        (snackbar as? ViewStub)?.setOnInflateListener { _, _ ->
            snackbarButton.clicks()
                .autoDispose(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe(snackbarButtonIntent)
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->
            //syncingProgress?.progressTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
            syncingProgress?.progressTintList =
                ColorStateList.valueOf(resources.getColor(R.color.tools_theme))
            syncingProgress?.indeterminateTintList =
                ColorStateList.valueOf(resources.getColor(R.color.tools_theme))
            //syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.blockingFirst().theme)
        }

        toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

        itemTouchCallback.adapter = conversationsAdapter
        conversationsAdapter.autoScrollToStart(recyclerView)

        // Don't allow clicks to pass through the drawer layout
        drawer.clicks().autoDispose(scope()).subscribe()

        // Set the theme color tint to the recyclerView, progressbar, and FAB
        theme.autoDispose(scope())
            .subscribe { theme ->
                // Set the color for the drawer icons
                val states = arrayOf(

                    /*intArrayOf(android.R.attr.state_activated),
                    intArrayOf(-android.R.attr.state_activated)*/
                    intArrayOf(R.attr.my_drawable),
                    intArrayOf(-android.R.attr.state_activated)
                )

                resolveThemeColor(android.R.attr.textColorSecondary)
                    .let { textSecondary ->
                        ColorStateList(
                            states,
                            intArrayOf(theme.theme, textSecondary)
                        )
                    }
                    .let { tintList ->
                        /*inboxIcon.imageTintList = tintList
                        archivedIcon.imageTintList = tintList*/
                    }

                // Miscellaneous views
                listOf(plusBadge1, plusBadge2).forEach { badge ->
                    //badge.setBackgroundTint(theme.theme)
                    badge.setBackgroundTint(resources.getColor(R.color.tools_theme))
                    badge.setTextColor(theme.textPrimary)
                }
                /*syncingProgress?.progressTintList = ColorStateList.valueOf(theme.theme)
                syncingProgress?.indeterminateTintList = ColorStateList.valueOf(theme.theme)*/
                syncingProgress?.progressTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.tools_theme))
                syncingProgress?.indeterminateTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.tools_theme))
                /*plusIcon.setTint(theme.theme)
                rateIcon.setTint(theme.theme)*/
                plusIcon.setTint(resources.getColor(R.color.tools_theme), false)
                rateIcon.setTint(resources.getColor(R.color.tools_theme), false)
                //.setTint(theme.theme)
                //compose.setTint(theme.theme)
                //compose.setBackgroundTint(theme.theme)

                // Set the FAB compose icon color
                //compose.setTint(theme.textPrimary)
            }

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            toolbarSearch.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
        }

        if (AppUtils.isEmptyString(
                com.text.messages.sms.emoji.common.PreferencesManager.getLanguage(
                    this
                )
            )
        ) {
            AppUtils.setLocale(this, "en")
        } else {
            AppUtils.setLocale(
                this,
                com.text.messages.sms.emoji.common.PreferencesManager.getLanguage(this)
            )
        }
        if (!permissionManager.isDefaultSms()) {
            setAsDefaultLay.setVisible(true)
            mainLayout.setVisible(false)
        } else {
            loadInterAds()
            mainLayout.setVisible(true)
            setAsDefaultLay.setVisible(false)
        }

        btnSetAsDefault.setOnClickListener {
            navigator.showDefaultSmsDialog(this)
        }

        
    }


    public fun loadInterAds() {
        Log.e(
            "TAG204",
            "loadInterAds: " + advertiseHandler.isInterstitialAdsAvailableToShow(this@MainActivity)
        )
        if (advertiseHandler.isAppStartUpAdsEnabled) {
            advertiseHandler.loadInterstitialAds(this@MainActivity,
                object : AdvertiseHandler.Listener() {
                    override fun onAdsLoadCompleted() {
                        if (advertiseHandler.isAppStartUpAdsEnabled && advertiseHandler.isInterstitialAdsAvailableToShow(
                                this@MainActivity
                            )
                        ) {
                            advertiseHandler.showInterstitialAds(
                                this@MainActivity,
                                true,
                                object : AdvertiseHandler.Listener() {
                                    override fun onAdsLoadFailed() {
                                        onAdsClosed()
                                    }

                                    override fun onAdsClosed() {

                                    }

                                },
                                false
                            )
                        }
                    }


                })
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run(onNewIntentIntent::onNext)
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            finish()
            return
        }

        val addContact = when (state.page) {
            is Inbox -> state.page.addContact
            is Archived -> state.page.addContact
            else -> false
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        img_tool_lang.setVisible(toolbarSearch.visibility == View.VISIBLE)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)

        toolbarSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.length > 1) {
                    toolbarSearch.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    cancel.isVisible = true
                } else {
                    val img: Drawable = getResources().getDrawable(R.drawable.ic_action_sear)
                    toolbarSearch.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                    cancel.isVisible = false
                }
            }
        })

        toolbar.menu.findItem(R.id.archive)?.isVisible =
            state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible =
            state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible = addContact && selectedConversations != 0
        toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0

        listOf(plusBadge1, plusBadge2).forEach { badge ->
//            badge.isVisible = drawerBadgesExperiment.variant && !state.upgraded
        }
//        plus.isVisible = state.upgraded
//        plusBanner.isVisible = !state.upgraded
//        rateLayout.setVisible(state.showRating)

        compose.setVisible(state.page is Inbox /*|| state.page is Archived*/)
        conversationsAdapter.emptyView =
            empty.takeIf { state.page is Inbox || state.page is Archived }
        searchAdapter.emptyView = empty.takeIf { state.page is Searching }

        when (state.page) {
            is Inbox -> {
                showBackButton(state.page.selected > 0)
                title = getString(R.string.main_title_selected, state.page.selected)
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(recyclerView)
                empty.setText(R.string.inbox_empty_text)
            }

            is Searching -> {
                showBackButton(true)
                if (recyclerView.adapter !== searchAdapter) recyclerView.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.inbox_search_empty_text)
            }

            is Archived -> {
                showBackButton(state.page.selected > 0)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (recyclerView.adapter !== conversationsAdapter) recyclerView.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(null)
                empty.setText(R.string.archived_empty_text)
            }
        }

        inbox.isActivated = state.page is Inbox
        archived.isActivated = state.page is Archived

        when (state.syncing) {
            is SyncRepository.SyncProgress.Idle -> {
                syncing.isVisible = false
                snackbar.isVisible =
                    !state.defaultSms || !state.smsPermission || !state.contactPermission
            }

            is SyncRepository.SyncProgress.Running -> {
                syncing.isVisible = true
                syncingProgress.max = state.syncing.max
                progressAnimator.apply {
                    setIntValues(
                        syncingProgress.progress,
                        state.syncing.progress
                    )
                }.start()
                syncingProgress.isIndeterminate = state.syncing.indeterminate
                snackbar.isVisible = false
            }
        }

        when {
            !state.defaultSms -> {
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.main_default_sms_message)
                snackbarButton?.setText(R.string.main_default_sms_change)
            }

            !state.smsPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_sms)
                snackbarButton?.setText(R.string.main_permission_allow)
            }

            !state.contactPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_contacts)
                snackbarButton?.setText(R.string.main_permission_allow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (LanguageActivity.languageChanged) {
            LanguageActivity.languageChanged = false
            val intent = Intent(this, MainActivity::class.java)
            // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            finish()
            startActivity(intent)
            activityResumedIntent.onNext(true)
            updateLanguage(this)
        } else {
            //loadInterstitialAdd()
        }
        if (!viewModel.isDefaultDialogOpen) {
            //showInterstitialAds()
        } else {
            viewModel.isDefaultDialogOpen = false
        }
        activityResumedIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityResumedIntent.onNext(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //AdvertiseHandler.getInstance().isNeedOpenAdRequest = false
        AdvertiseHandler.getInstance().disableAppOpenAds()
        if (AppUtils.isContextActive(this)) {
            if (permissionManager.isDefaultSms()) {
                setAsDefaultLay.setVisible(false)
                mainLayout.setVisible(true)
                loadInterAds()
            } else {

                setAsDefaultLay.setVisible(true)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        advertiseHandler!!.isSplashAdShown = false
        hideBottomSheetDialog()
        disposables.dispose()
    }

    override fun showBackButton(show: Boolean) {
        toggle.onDrawerSlide(drawer, if (show) 1f else 0f)
        toggle.drawerArrowDrawable.color = when (show) {
            true -> resolveThemeColor(android.R.attr.textColorSecondary)
            false -> resolveThemeColor(android.R.attr.textColorPrimary)
        }
    }

    override fun requestDefaultSms() {
        //navigator.showDefaultSmsDialog(this)
    }

    override fun requestPermissions() {
        //AdvertiseHandler.getInstance().isNeedOpenAdRequest = false
        AdvertiseHandler.getInstance().disableAppOpenAds()
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS
            ), 0
        )
    }

    override fun drawerevent(value: Boolean) {
        if (value) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (!drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun setMenuBackground(isMenu: Boolean) {
        if (isMenu) {
            inbox.background = resources.getDrawable(R.drawable.rounded_rectangle_light_wh)
            archived.background = null
        } else {
            archived.background = resources.getDrawable(R.drawable.rounded_rectangle_light_wh)
            inbox.background = null
        }
    }

    override fun drawerclose() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun showRateUsPopup() {
        PreferencesManager.RateUs(this)
    }

    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun themeChanged() {
        recyclerView.scrapViews()
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(this, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        /*AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
            .setPositiveButton(R.string.button_delete) { _, _ ->
                confirmDeleteIntent.onNext(
                    conversations
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()*/
        val context: Context =
            ContextThemeWrapper(this@MainActivity, R.style.AppTheme2)
        MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_rounded)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
            .setPositiveButton(R.string.button_delete) { _, _ ->
                confirmDeleteIntent.onNext(
                    conversations
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    override fun clearQuery() {
        toolbarSearch.text = null
        val img: Drawable = getResources().getDrawable(R.drawable.ic_action_sear)
        toolbarSearch.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
    }

//    override fun showChangelog(changelog: ChangelogManager.CumulativeChangelog) {
//        changelogDialog.show(changelog)
//    }

    override fun showArchivedSnackbar() {
        Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        if (conversationsAdapter.selection.size > 0) {
            homeIntent.onNext(Unit)
        } else {
            if (drawer.isVisible) {
                drawerLayout.closeDrawer(GravityCompat.START)
                return
            }

            if (hideRateUseDialog()) {
                return
            }

            if (advertiseHandler!!.isNativeAdsAvailable(this)) {
                val view1 = LayoutInflater.from(this).inflate(R.layout.exit_native_ads_view, null)
                advertiseHandler!!.finalLoadNativeAds(
                    this, view1.findViewById(R.id.bannerAdLayout), true,
                    R.layout.exit_native_ads, null, false
                )
                if (!PreferencesManager.getRate(this@MainActivity)) {
                    showRateDialog(this, 5, false)
                } else {
                    backPressedSubject.onNext(NavItem.BACK)
                }
                /*val builder: BottomSheet.Builder = BottomSheet.Builder(this, true)
                //            builder.setBackgroundColor(Color.TRANSPARENT);
                builder.view = view1
                builder.setOnDismissListener { dialog ->
                    bottomSheetDialog = null
                    if (AppUtils.isContextActive(this)) {
                        if (!PreferencesManager.getRate(this@MainActivity)) {
                            Handler(Looper.myLooper()!!).postDelayed(
                                {
                                    if (!PreferencesManager.getRate(this@MainActivity)) {
                                        showRateDialog(this, 5, false)
                                    }
                                },
                                200
                            )
                        }
                    }
                }*/
                /*val view = view1.findViewById<View>(R.id.tapToExitLay)
                view?.setOnClickListener { v: View? ->
                    builder.setOnDismissListener(null)
                    hideBottomSheetDialog()
                    finish()
                }
                bottomSheetDialog = builder.show()*/
            } else if (!PreferencesManager.getRate(this@MainActivity)) {
                showRateDialog(this, 5, false)
            } else {
                /*if (getVisibleFragment() == null) {
                    finish()
                } else {
                    backPressedSubject.onNext(NavItem.BACK)
                }*/
                backPressedSubject.onNext(NavItem.BACK)
                //finish()
            }
        }
    }

    private fun hideRateUseDialog(): Boolean {
        if (rateUsDialog != null && rateUsDialog!!.isShowing) {
            rateUsDialog!!.dismiss()
            rateUsDialog = null
            return true
        }
        return false
    }

    private fun hideBottomSheetDialog() {
        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) {
            bottomSheetDialog!!.dismiss()
        }
        bottomSheetDialog = null
    }

    fun showRateDialog(context: Activity?, ratingCount: Int, isMenu: Boolean) {

        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) {
            return
        }

        if (context != null) {
            var rating_count = ratingCount.toFloat()
            val view = LayoutInflater.from(context).inflate(R.layout.rateme_dialog, null)
            val img_rate_cancel = view.findViewById<ImageView>(R.id.img_rate_cancel)
            val btn_rate_feedback = view.findViewById<Button>(R.id.btn_rate_feedback)
            val rotationRatingBar = view.findViewById<RatingBar>(R.id.ratingBar)
            val txt_rate_main = view.findViewById<TextView>(R.id.txt_rate_main)
            val img_rate_emoji = view.findViewById<ImageView>(R.id.img_rate_emoji)
            rateUsDialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .show()
            rotationRatingBar.onRatingBarChangeListener =
                OnRatingBarChangeListener { ratingBar, rating, b ->
                    rating_count = rating
                    if (rating == 0f) {
                        ratingBar.rating = 1f
                        txt_rate_main.text = context.getString(R.string.oh_no)
                        btn_rate_feedback.text = context.getString(R.string.drawerfeedback)
                        img_rate_emoji.setImageDrawable(context.resources.getDrawable(R.drawable.rating_first))
                    } else if (rating == 1f) {
                        txt_rate_main.text = context.getString(R.string.oh_no)
                        btn_rate_feedback.text = context.getString(R.string.drawerfeedback)
                        img_rate_emoji.setImageDrawable(context.resources.getDrawable(R.drawable.rating_first))
                    } else if (rating == 2f) {
                        txt_rate_main.text = context.getString(R.string.oh_no)
                        btn_rate_feedback.text = context.getString(R.string.drawerfeedback)
                        img_rate_emoji.setImageDrawable(context.resources.getDrawable(R.drawable.rating_second))
                    } else if (rating == 3f) {
                        txt_rate_main.text = context.getString(R.string.oh_no)
                        btn_rate_feedback.text = context.getString(R.string.drawerfeedback)
                        img_rate_emoji.setImageDrawable(context.resources.getDrawable(R.drawable.rating_three))
                    } else if (rating == 4f) {
                        txt_rate_main.text = context.getString(R.string.we_like)
                        btn_rate_feedback.text = context.getString(R.string.drawerfeedback)
                        img_rate_emoji.setImageDrawable(context.resources.getDrawable(R.drawable.rating_four))
                    } else if (rating == 5f) {
                        txt_rate_main.text = context.getString(R.string.we_like)
                        btn_rate_feedback.text = context.getString(R.string.rate_on_playstore)
                        img_rate_emoji.setImageDrawable(context.resources.getDrawable(R.drawable.rating_five))
                    }
                }

//            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            img_rate_cancel.setOnClickListener { v: View? ->
                if (isMenu) {
                    hideRateUseDialog()
                } else {
                    hideRateUseDialog()
                    context.finish()
                }
            }
            btn_rate_feedback.setOnClickListener { v: View? ->
                hideRateUseDialog()
                if (rating_count > 4) {
                    PreferencesManager.setRateUs(context, true)
                    PreferencesManager.RateUs(this)
                } else {
                    PreferencesManager.setRateUs(context, true)
                    goToFeedBack(context);
                }
            }
            rateUsDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            rateUsDialog!!.window!!.setLayout(
                context.resources.displayMetrics.widthPixels - 50,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun getVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = this@MainActivity.supportFragmentManager
        val fragments: List<Fragment> = fragmentManager.getFragments()
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible()) return fragment
            }
        }
        return null
    }

    fun goToFeedBack(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(RateMeNowDialog.MAIL_TO_FEEDBACK))
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) + " Support")
        intent.putExtra(Intent.EXTRA_TEXT, StringBuilder("\n\n")
            .append("\n\n--- Tell Us Your Thoughts above this line ---\n\n")
            .append("Package: ${context.packageName}\n")
            .append("Version: ${BuildConfig.VERSION_NAME}\n")
            .append("Version Code: ${BuildConfig.VERSION_CODE}\n")
            .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
            .append("SDK: ${Build.VERSION.SDK_INT}\n")
            .append("Upgraded"
                .takeIf { BuildConfig.FLAVOR != "noAnalytics" }
                .takeIf { billingManager.upgradeStatus.blockingFirst() } ?: "")
            .toString())
        startActivityExternal(intent, context)
    }

    private fun startActivityExternal(intent: Intent, context: Context) {
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
    }
}
