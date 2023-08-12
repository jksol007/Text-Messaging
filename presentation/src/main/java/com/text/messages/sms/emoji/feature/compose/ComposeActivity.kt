package com.text.messages.sms.emoji.feature.compose

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.text.messages.sms.emoji.R
import com.text.messages.sms.emoji.ads.AdvertiseHandler
import com.text.messages.sms.emoji.ads.AppUtils
import com.text.messages.sms.emoji.common.Navigator
import com.text.messages.sms.emoji.common.base.MessagesThemedActivity
import com.text.messages.sms.emoji.common.util.DateFormatter
import com.text.messages.sms.emoji.common.util.extensions.*
import com.text.messages.sms.emoji.common.widget.MessagesEditText
import com.text.messages.sms.emoji.feature.compose.editing.ChipsAdapter
import com.text.messages.sms.emoji.feature.contacts.ContactsActivity
import com.text.messages.sms.emoji.feature.main.SoftKeyBoardPopup
import com.text.messages.sms.emoji.model.Attachment
import com.text.messages.sms.emoji.model.Recipient
import com.text.messages.sms.emoji.model.logDebug
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.compose_activity.bannerAdLayout
import kotlinx.android.synthetic.main.compose_activity.contentView
import kotlinx.android.synthetic.main.compose_activity.toolbar
import kotlinx.android.synthetic.main.compose_activity.toolbarTitle
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ComposeActivity : MessagesThemedActivity(), ComposeView, MessagesEditText.PopupListener,
    SoftKeyBoardPopup.PopupMenuListener {

    companion object {
        private const val SelectContactRequestCode = 0
        private const val TakePhotoRequestCode = 1
        private const val AttachPhotoRequestCode = 2
        private const val AttachContactRequestCode = 3

        private const val CameraDestinationKey = "camera_destination"
    }

    var address: String = ""

    @Inject
    lateinit var attachmentAdapter: AttachmentAdapter

    @Inject
    lateinit var chipsAdapter: ChipsAdapter

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var messageAdapter: MessagesAdapter

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val activityVisibleIntent: Subject<Boolean> = PublishSubject.create()
    override val chipsSelectedIntent: Subject<HashMap<String, String?>> = PublishSubject.create()
    override val chipDeletedIntent: Subject<Recipient> by lazy { chipsAdapter.chipDeleted }
    override val menuReadyIntent: Observable<Unit> = menu.map { Unit }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val sendAsGroupIntent by lazy { sendAsGroupBackground.clicks() }
    override val messageClickIntent: Subject<Long> by lazy { messageAdapter.clicks }
    override val messagePartClickIntent: Subject<Long> by lazy { messageAdapter.partClicks }
    override val messagesSelectedIntent by lazy { messageAdapter.selectionChanges }
    override val cancelSendingIntent: Subject<Long> by lazy { messageAdapter.cancelSending }
    override val attachmentDeletedIntent: Subject<Attachment> by lazy { attachmentAdapter.attachmentDeleted }

    //override val textChangedIntent by lazy { message.textChanges() }
    override val textChangedIntent by lazy { edt_msg.textChanges() }
    override val attachIntent by lazy {
        Observable.merge(
            //attach.clicks(),
            attachingBackground.clicks(),
            img_attach.clicks()
        )
    }

    override val cameraIntent: Subject<Unit> = PublishSubject.create()
    override val galleryIntent: Subject<Unit> = PublishSubject.create()
    override val scheduleIntent: Subject<Unit> = PublishSubject.create()
    override val attachContactIntent: Subject<Unit> = PublishSubject.create()
    override val locationIntent: Subject<Unit> = PublishSubject.create()

    /*override val cameraIntent by lazy {
        Observable.merge(
            *//*camera.clicks(), cameraLabel.clicks(),*//*
            *//*cl_camera.clicks(),
            img_camera.clicks()*//*
        popup
        )
    }
    override val galleryIntent by lazy {
        Observable.merge(
            *//*gallery.clicks(), galleryLabel.clicks(),*//*
            cl_gallery.clicks(),
            img_gallery.clicks()
        )
    }
    override val scheduleIntent by lazy {
        Observable.merge(
            *//* schedule.clicks(),
             scheduleLabel.clicks(),*//*
            cl_sch_msg.clicks(),
            img_sch_msg.clicks()
        )
    }
    override val attachContactIntent by lazy {
        Observable.merge(
            *//*contact.clicks(),
            contactLabel.clicks(),*//*
            cl_contact.clicks(),
            img_contact.clicks()
        )
    }*/
    override val attachmentSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val contactSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val inputContentIntent by lazy { edt_msg.inputContentSelected }

    //override val inputContentIntent by lazy { message.inputContentSelected }
    override val scheduleSelectedIntent: Subject<Long> = PublishSubject.create()
    override val changeSimIntent by lazy { sim.clicks() }
    override val scheduleCancelIntent by lazy { scheduledCancel.clicks() }
    override val sendIntent by lazy { /*send.clicks()*/img_send.clicks() }
    override val openAttIntent by lazy { /*send.clicks()*/img_attach.clicks() }
    override val viewQksmsPlusIntent: Subject<Unit> = PublishSubject.create()
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[ComposeViewModel::class.java]
    }

    private var cameraDestination: Uri? = null

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    var isNativeAdLoading = false
    var isNativeAdLoaded = false
    lateinit var menuKeyboard: SoftKeyBoardPopup
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var dialog: ProgressDialog
    private var checkScheduler: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)
        showBackButton(true)
        dialog = ProgressDialog(this)
        viewModel.bindView(this)
        hideChatMenu()
        checkScheduler = intent.getBooleanExtra("Check", false)
        contentView.layoutTransition = LayoutTransition().apply {
            disableTransitionType(LayoutTransition.CHANGING)
        }
        if (checkScheduler as Boolean) {
            requestDatePicker()
        }

//      advertiseHandler.showBannerAds(applicationContext,bannerAdLayout);
      //  advertiseHandler.showBannerAds(bannerAdLayout)
        handler = Handler(Looper.myLooper()!!)
        runnable = Runnable { handleNativeAdVisibility(false) }
//
        /*if (advertiseHandler.isNativeAdsAvailable(this)) {
            advertiseHandler.finalLoadNativeAds(
                this,
                bannerAdLayout,
                false,
                R.layout.banner_native_ads_admob_youtube,
                object : AdvertiseHandler.AdsListener {
                    override fun onAdsLoadStart() {
                        isNativeAdLoading = true
                    }

                    override fun onAdsOpened() {
                        onAdsClose()
                    }

                    override fun onAdsClose() {
                        isNativeAdLoading = false
                        isNativeAdLoaded = true

                        nativeAdsHandler()
                    }

                    override fun onAdsLoadFailed() {
                        isNativeAdLoading = false
                        isNativeAdLoaded = false

                        nativeAdsHandler()
                    }

                },
                false
            )
        } else {
            nativeAdsShow()
        }*/
        /*advertiseHandler!!.showNativeAds(
            this@ComposeActivity,
            bannerAdLayout,
            true,
            R.layout.banner_native_ads_admob_youtube,
            null
        )*/
        chipsAdapter.view = chips

        chips.itemAnimator = null
        chips.layoutManager = FlexboxLayoutManager(this)

        messageAdapter.autoScrollToStart(messageList)
        messageAdapter.emptyView = messagesEmpty

        messageList.setHasFixedSize(true)
        messageList.adapter = messageAdapter

        attachments.adapter = attachmentAdapter

        //message.supportsInputContent = true
        edt_msg.supportsInputContent = true
        edt_msg.popupListener = this

        menuKeyboard = SoftKeyBoardPopup(
            this,
            contentView,
            edt_msg,
            edt_msg,
            img_attach,
            listener = this
        )

        theme
            //.doOnNext { loading.setTint(it.theme) }
            .doOnNext { loading.setTint(resources.getColor(R.color.tools_theme)) }
            /*.doOnNext { attach.setBackgroundTint(it.theme) }
            .doOnNext { attach.setTint(it.textPrimary) }*/
            /*.doOnNext { img_attach.setBackgroundTint(it.theme) }
            .doOnNext { img_attach.setTint(it.theme) }*/
            /*.doOnNext { img_send.setBackgroundTint(it.theme) }
            .doOnNext { img_camera.setTint(it.theme) }
            .doOnNext { img_gallery.setTint(it.theme) }
            .doOnNext { img_contact.setTint(it.theme) }
            .doOnNext { img_location.setTint(it.theme) }
            .doOnNext { img_sch_msg.setTint(it.theme) }*/
            //.doOnNext { img_attach.setTint(it.textPrimary) }
            .doOnNext { messageAdapter.theme = it }
            .autoDispose(scope())
            .subscribe()

        window.callback = ComposeWindowCallback(window.callback, this)

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            edt_msg.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
        }
    }

    fun hideChatMenu() {
        edt_msg.setVisible(false)
        img_send.setVisible(false)
        img_attach.setVisible(false)
        simIndex.setVisible(false)
        sim.setVisible(false)
        bannerAdLayout.setVisible(false)
        senderNotSupport.setVisible(false)
    }

    override fun onStart() {
        super.onStart()
        activityVisibleIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityVisibleIntent.onNext(false)
    }

    override fun render(state: ComposeState) {
        if (state.hasError) {
            finish()
            return
        }

        threadId.onNext(state.threadId)

        title = when {
            state.selectedMessages > 0 -> getString(
                R.string.compose_title_selected,
                state.selectedMessages
            )
            state.query.isNotEmpty() -> state.query
            else -> state.conversationtitle
        }

        toolbarSubtitle.setVisible(state.query.isNotEmpty())
        toolbarSubtitle.text = getString(
            R.string.compose_subtitle_results, state.searchSelectionPosition,
            state.searchResults
        )

        toolbarTitle.setVisible(!state.editingMode)
        chips.setVisible(state.editingMode)
        //composeBar.setVisible(!state.loading)

        // Don't set the adapters unless needed
        if (state.editingMode && chips.adapter == null) chips.adapter = chipsAdapter

        toolbar.menu.findItem(R.id.add)?.isVisible = state.editingMode
        toolbar.menu.findItem(R.id.call)?.isVisible =
            !state.editingMode && state.selectedMessages == 0 && state.query.isEmpty()
        toolbar.menu.findItem(R.id.info)?.isVisible =
            !state.editingMode && state.selectedMessages == 0
                    && state.query.isEmpty()
        toolbar.menu.findItem(R.id.copy)?.isVisible =
            !state.editingMode && state.selectedMessages > 0
        toolbar.menu.findItem(R.id.details)?.isVisible =
            !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.delete)?.isVisible =
            !state.editingMode && state.selectedMessages > 0
        toolbar.menu.findItem(R.id.forward)?.isVisible =
            !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.previous)?.isVisible =
            state.selectedMessages == 0 && state.query.isNotEmpty()
        toolbar.menu.findItem(R.id.next)?.isVisible =
            state.selectedMessages == 0 && state.query.isNotEmpty()
        toolbar.menu.findItem(R.id.clear)?.isVisible =
            state.selectedMessages == 0 && state.query.isNotEmpty()

        chipsAdapter.data = state.selectedChips

        loading.setVisible(state.loading)

        sendAsGroup.setVisible(state.editingMode && state.selectedChips.size >= 2)
        sendAsGroupSwitch.isChecked = state.sendAsGroup

        messageList.setVisible(!state.editingMode || state.sendAsGroup || state.selectedChips.size == 1)
        messageAdapter.data = state.messages
        messageAdapter.highlight = state.searchSelectionId

        scheduledGroup.isVisible = state.scheduled != 0L
        scheduledTime.text = dateFormatter.getScheduledTimestamp(state.scheduled)

        attachments.setVisible(state.attachments.isNotEmpty())
        attach_back.setVisible(state.attachments.isNotEmpty())
        attachmentAdapter.data = state.attachments


        //attach.animate().rotation(if (state.attaching) 135f else 0f).start()
        //attaching.isVisible = state.attaching
        //cl_attach_view.isVisible = state.attaching
        /*if (cl_attach_view.visibility == View.VISIBLE) {
            toggle()
        }*/
        //attachingBackground.isVisible = state.attaching

        if (!senderNotSupport.isVisible) {
            composeBar.visibility = if (state.attaching) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
        }

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        if (state.messages != null) {
            if (state.messages.second.size > 0) {
//                Log.i("TAG", "render: state.messages.second :- " + (state.messages.second[0]?.address))
                address = (state.messages.second[0]?.address.toString().replace(",".toRegex(), ""))
            } else if (state.messages.first.recipients.size > 0) {
//                Log.i("TAG", "render: state.messages.first :- " + (state.messages.first.recipients[0]?.address))
                address = (state.messages.first.recipients[0]?.address.toString()
                    .replace(",".toRegex(), ""))
            }
        }

        sim.setVisible(state.subscription != null)
        simIndex.setVisible(state.subscription != null)
        sim.contentDescription = getString(R.string.compose_sim_cd, state.subscription?.displayName)
        simIndex.text = state.subscription?.simSlotIndex?.plus(1)?.toString()

        /*send.isEnabled = state.canSend
        send.imageAlpha = if (state.canSend) 255 else 128*/
        img_send.isEnabled = state.canSend
        img_send.imageAlpha = if (state.canSend) 255 else 128

//        Log.i("TAG", "render: address :- $address")

//        address = if (address.length > 10) {
//            address.replace("\\s".toRegex(), "").replace(Regex("[()\\-\\s]"), "").substring(3)
//        } else {
//            address.replace("\\s".toRegex(), "").replace(Regex("[()\\-\\s]"), "")
//        }


        if (address.isNotEmpty()) {
            Log.i("TAG", "render: " + address.substring(0, 1).matches("^[a-zA-Z]*\$".toRegex()))
            if (address.substring(0, 1).matches("^[a-zA-Z]*\$".toRegex())) {

                if (address == "insert-address-token") {
                    senderNotSupport.setVisible(false)
                    handleNativeAdVisibility(true)

                    //attach.animate().rotation(if (state.attaching) 135f else 0f).start()
                    //attaching.isVisible = state.attaching
                    attachingBackground.isVisible = state.attaching

                } else {
                    senderNotSupport.setVisible(true)
                    handleNativeAdVisibility(false)
                    //nativeAdsShow()

                    //attach.setVisible(false, View.GONE)
                    img_attach.setVisible(false, View.GONE)
                    //message.setVisible(false)
                    edt_msg.setVisible(false)
                    //send.setVisible(false)
                    img_send.setVisible(false)
                    img_attach.setVisible(false)
                    img_send.setVisible(false)
                    attachments.setVisible(false)
                    attach_back.setVisible(false)
                    counter.setVisible(false)
                    simIndex.setVisible(false)
                    sim.setVisible(false)
                    //composeBar.setVisible(false)
                    //attach.isVisible = false
                    img_attach.isVisible = false
                    //attaching.isVisible = false
                    menuKeyboard.dismiss()
                    attachingBackground.isVisible = false
                }

            } else {
                senderNotSupport.setVisible(false)
                handleNativeAdVisibility(true)
                //nativeAdsShow()

                //attach.animate().rotation(if (state.attaching) 135f else 0f).start()
                //attaching.isVisible = state.attaching
                //attachingBackground.isVisible = state.attaching

            }

            //nativeAdsShow()
        }

    }

    /*private fun nativeAdsShow() {
        if (!isNativeAdLoading) {
            if (isNativeAdLoaded) {
                nativeAdsHandler()
            } else {
                isNativeAdLoading = true
                AdvertiseHandler.getInstance().showNativeAds(this,
                    bannerAdLayout,
                    false,
                    R.layout.banner_native_ads_admob_youtube,
                    object : AdvertiseHandler.AdsListener {
                        override fun onAdsLoadStart() {
                            isNativeAdLoading = true
                        }

                        override fun onAdsOpened() {
                            onAdsClose()
                        }

                        override fun onAdsClose() {
                            isNativeAdLoading = false
                            isNativeAdLoaded = true

                            nativeAdsHandler()
                        }

                        override fun onAdsLoadFailed() {
                            isNativeAdLoading = false
                            isNativeAdLoaded = false

                            nativeAdsHandler()
                        }

                    })
            }
        }
    }*/

    private fun nativeAdsHandler() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 1000)
    }

    private fun handleNativeAdVisibility(isSender: Boolean) {
        if (isSender) {
            if (AppUtils.isContextActive(this)) {
                if (senderNotSupport.visibility == View.VISIBLE && isNativeAdLoaded) {
                    bannerAdLayout.setVisible(true)

//                    advertiseHandler.loadBannerAds(this@ComposeActivity,
//                        bannerAdLayout,null)
                } else {
                    bannerAdLayout.setVisible(false)
                }
            }
        }
    }

    override fun clearSelection() = messageAdapter.clearSelection()

    override fun showDetails(details: String) {
        /*AlertDialog.Builder(this)
            .setTitle(R.string.compose_details_title)
            .setTitle(R.string.compose_details_title)
            .setMessage(details)
            .setCancelable(true)
            .show()*/
        val context: Context = ContextThemeWrapper(this, R.style.AppTheme2)
        MaterialAlertDialogBuilder(
            context,
            R.style.MaterialAlertDialog_rounded
        )
            .setTitle(R.string.compose_details_title)
            .setTitle(R.string.compose_details_title)
            .setMessage(details)
            .setCancelable(true)
            .show();
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(this)
    }

    override fun requestStoragePermission() {
        //AdvertiseHandler.getInstance().isNeedOpenAdRequest = false
        AdvertiseHandler.getInstance().disableAppOpenAds()
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            0
        )
    }

    override fun requestSmsPermission() {
        //AdvertiseHandler.getInstance().isNeedOpenAdRequest = false
        AdvertiseHandler.getInstance().disableAppOpenAds()
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ), 0
        )
    }

    override fun requestDatePicker() {
        var calendar = Calendar.getInstance()
        DatePickerDialog(
            this, R.style.AppPickerDialog,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar = Calendar.getInstance()
                var sHour = calendar.get(Calendar.HOUR_OF_DAY)
                var sMinute = calendar.get(Calendar.MINUTE)

                val diff = 60 - sMinute
                val increaseValue = 2

                if (diff <= increaseValue) {
                    sHour += 1
                    sMinute = increaseValue - diff.coerceAtLeast(0)
                } else {
                    sMinute += increaseValue
                }
                TimePickerDialog(
                    this, R.style.AppPickerDialog,
                    TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        scheduleSelectedIntent.onNext(calendar.timeInMillis)
                    },
                    /*calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(this)*/
                    sHour, sMinute, DateFormat.is24HourFormat(this)
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()

        // On some devices, the keyboard can cover the date picker
        //message.hideKeyboard()
        edt_msg.hideKeyboard()
    }

    override fun requestContact() {
        val intent = Intent(Intent.ACTION_PICK)
            .setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

        startActivityForResult(Intent.createChooser(intent, null), AttachContactRequestCode)
    }

    override fun showContacts(sharing: Boolean, chips: List<Recipient>) {
        //message.hideKeyboard()
        edt_msg.hideKeyboard()
        val serialized =
            HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        val intent = Intent(this, ContactsActivity::class.java)
            .putExtra(ContactsActivity.SharingKey, sharing)
            .putExtra(ContactsActivity.ChipsKey, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    override fun themeChanged() {
        messageList.scrapViews()
    }

    override fun showKeyboard() {
        /*message.postDelayed({
            message.showKeyboard()
        }, 200)*/
        edt_msg.postDelayed({
            edt_msg.showKeyboard()
        }, 200)
    }

    override fun requestCamera() {
        cameraDestination = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            .let { timestamp ->
                ContentValues().apply {
                    put(
                        MediaStore.Images.Media.TITLE,
                        timestamp
                    )
                }
            }
            .let { cv -> contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, cameraDestination)
        startActivityForResult(Intent.createChooser(intent, null), TakePhotoRequestCode)
    }

    override fun requestGallery() {
        val intent = Intent(Intent.ACTION_PICK)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, false)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType("image/*")
        startActivityForResult(Intent.createChooser(intent, null), AttachPhotoRequestCode)
    }

    override fun setDraft(draft: String) {
        /*message.setText(draft)
        message.setSelection(draft.length)*/
        edt_msg.setText(draft)
        edt_msg.setSelection(draft.length)
    }

    override fun scrollToMessage(id: Long) {
        messageAdapter.data?.second
            ?.indexOfLast { message -> message.id == id }
            ?.takeIf { position -> position != -1 }
            ?.let(messageList::scrollToPosition)
    }

    override fun showQksmsPlusSnackbar(message: Int) {
        Snackbar.make(contentView, message, Snackbar.LENGTH_LONG).run {
            setAction(R.string.button_more) { viewQksmsPlusIntent.onNext(Unit) }
            //setActionTextColor(colors.theme().theme)
            setActionTextColor(resources.getColor(R.color.tools_theme))
            show()
        }
    }

    override fun requestAttBtn() {
        if (attachingBackground.visibility == View.VISIBLE) {
            attachingBackground.visibility == View.GONE
        } else {
            attachingBackground.visibility == View.VISIBLE
        }

        if (menuKeyboard.isShowing) {
            attachingBackground.visibility = View.GONE
            menuKeyboard.dismiss()
        } else {
            attachingBackground.visibility = View.VISIBLE
            menuKeyboard.show()
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager =
            (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

    override fun requestLocation() {
        if (!isConnected()) {
            makeToast("Please check your internet connection !")
            return
        }
        if (isLocationEnabled()) {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                logDebug("location" + "" + location)
                if (location == null) {
                    dialog.setMessage("Location Fetching !")
                    try {
                        if (!isFinishing) {
                            dialog.show()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    requestNewLocationData()
                } else {
                    if (!isFinishing && dialog.isShowing) {
                        dialog.dismiss()
                    }
                    attachingBackgroundHide(location)
                }
            }
        } else {
            makeToast("Turn on location", Toast.LENGTH_LONG)
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, 7)
        }
    }

    override fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 0
        )
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        return super.onCreateOptionsMenu(menu)
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun getColoredMenuItems(): List<Int> {
        return super.getColoredMenuItems() + R.id.call
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //AdvertiseHandler.getInstance().isNeedOpenAdRequest = false
        AdvertiseHandler.getInstance().disableAppOpenAds()
        when {
            requestCode == SelectContactRequestCode -> {
                chipsSelectedIntent.onNext(data?.getSerializableExtra(ContactsActivity.ChipsKey)
                    ?.let { serializable -> serializable as? HashMap<String, String?> }
                    ?: hashMapOf())
            }
            requestCode == TakePhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                cameraDestination?.let(attachmentSelectedIntent::onNext)
            }
            requestCode == AttachPhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.clipData?.itemCount
                    ?.let { count -> 0 until count }
                    ?.mapNotNull { i -> data.clipData?.getItemAt(i)?.uri }
                    ?.forEach(attachmentSelectedIntent::onNext)
                    ?: data?.data?.let(attachmentSelectedIntent::onNext)
            }
            /*requestCode == AttachContactRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.data?.let(contactSelectedIntent::onNext)
            }*/
            requestCode == AttachContactRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.data?.let(contactSelectedIntent::onNext)
                var contactData: Uri = data!!.data!!;
                val cur: Cursor? = contentResolver.query(contactData, null, null, null, null)
                if (cur!!.getCount() > 0) { // thats mean some resutl has been found
                    if (cur.moveToNext()) {
                        var contact: String = ""
                        val id: String =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                        val name: String =
                            cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        Log.e("Names", name)
                        contact = name
                        if (cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                                .toInt() > 0
                        ) {
                            val phones: Cursor? = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null,
                                null
                            )
                            /*while (phones!!.moveToNext()) {
                                val phoneNumber: String =
                                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                Log.e("Number", phoneNumber)
                                contact += "/n "+phoneNumber
                            }*/
                            val phoneNumber: String =
                                cur!!.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            Log.e("Number", phoneNumber)
                            contact += "\n " + phoneNumber
                            phones!!.close()
                        }
                        edt_msg.setText(contact)

                    }
                }
                cur.close()
            }
            requestCode == 7 ->
                if (isLocationEnabled()) {

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    if (ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                        val location: Location? = task.result
                        logDebug("location$location")
                        if (location == null) {
                            dialog.setMessage("Location Fetching !")
                            try {
                                if (!isFinishing)
                                    dialog.show()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                            requestNewLocationData()
                        } else {
                            dialog.dismiss()
                            attachingBackgroundHide(location)
                        }
                    }
                }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1
        mLocationRequest.fastestInterval = 1
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                it
            )
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
            }
            attachingBackgroundHide(locationResult.lastLocation)

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(CameraDestinationKey, cameraDestination)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        cameraDestination = savedInstanceState.getParcelable(CameraDestinationKey)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onBackPressed() = backPressedIntent.onNext(Unit)

    override fun onDestroy() {
        menuKeyboard.clear()
        super.onDestroy()

        handler.removeCallbacks(runnable)
    }

    override fun getPopup(): PopupWindow {
        return menuKeyboard
    }

    override fun dismissPopup() {
        attachingBackground.visibility = View.GONE
    }

    override fun menuName(menu: String) {
        attachingBackground.visibility = View.GONE
        when (menu) {
            getString(R.string.camera) -> {
                cameraIntent.onNext(Unit)
            }
            getString(R.string.gallery) -> {
                galleryIntent.onNext(Unit)
            }
            getString(R.string.contacts) -> {
                attachContactIntent.onNext(Unit)
            }
            getString(R.string.location) -> {
                locationIntent.onNext(Unit)
            }
            getString(R.string.schedule_message) -> {
                scheduleIntent.onNext(Unit)
            }
        }
    }

    fun attachingBackgroundHide(mLastLocation: Location?) {
        logDebug("location$mLastLocation")
        if (edt_msg.text!!.isNotEmpty()) {
            edt_msg.append("\n")
        }
        edt_msg.setText("[Current Location]:\nhttps://www.google.com/maps/search/?api=1&query=" + mLastLocation?.latitude.toString() + "," + mLastLocation?.longitude.toString())
        edt_msg.setSelection(edt_msg.text!!.length)
        edt_msg.showKeyboard()
    }
}
