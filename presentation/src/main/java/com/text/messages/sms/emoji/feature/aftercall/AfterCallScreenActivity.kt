package com.chating.messages.feature.aftercall


//class AfterCallScreenActivity : QkThemedActivity(), AfterCallView {
//
//    @Inject
//    lateinit var viewModelFactory: ViewModelProvider.Factory
//
//    @Inject
//    lateinit var navigator: Navigator
//
//    @Inject
//    lateinit var permissionManager: PermissionManager
//
//    private val viewModel by lazy {
//        ViewModelProviders.of(
//            this,
//            viewModelFactory
//        )[AfterCallViewModel::class.java]
//    }
//
//    private var countDownTimerWithPause: CountDownTimerWithPause? = null
//
//    override val callIntent by lazy { imgCall.clicks() }
//    override val messageIntent by lazy { imgMessage.clicks() }
//
//    private var phoneNoString: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        AndroidInjection.inject(this)
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.after_call_screen_activity)
//
//        viewModel.bindView(this)
//
//        advertiseHandler.showAfterCallScreenNativeAds(
//            this,
//            bannerAdLayout,
//            true,
//            R.layout.native_ads_admob
//        )
//
//        imgClose.setOnClickListener { finish() }
//    }
//
//    override fun onStart() {
//        advertiseHandler.disableAppOpenAds()
//        super.onStart()
//    }
//
//    override fun onResume() {
//        advertiseHandler.disableAppOpenAds()
//        super.onResume()
//
//        startTimer()
//    }
//
//    override fun onUserLeaveHint() {
//        super.onUserLeaveHint()
//
//        finish()
//    }
//
//    private fun startTimer() {
//        if (countDownTimerWithPause == null) {
//            countDownTimerWithPause =
//                object : CountDownTimerWithPause(sessionManager.screenTime, 1000, true) {
//                    override fun onTick(millisUntilFinished: Long) {
//                        if (AppUtils.isContextActive(this@AfterCallScreenActivity)) {
//                            var millisUntilFinished1 =
//                                (ceil((millisUntilFinished / 1000).toDouble()).toInt()).toString()
//                            if (millisUntilFinished1.length == 1) {
//                                millisUntilFinished1 = "0$millisUntilFinished1"
//                            }
//
//                            millisUntilFinished1 = "00:$millisUntilFinished1"
//                            txtScreenTimer.text = millisUntilFinished1
//                        }
//                    }
//
//                    override fun onFinish() {
//                        if (AppUtils.isContextActive(this@AfterCallScreenActivity)) {
//                            finish()
//                        }
//                    }
//                }
//            (countDownTimerWithPause as CountDownTimerWithPause).create()
//        }
//    }
//
//    private fun cancelCounter() {
//        if (countDownTimerWithPause != null) {
//            (countDownTimerWithPause as CountDownTimerWithPause).cancel()
//        }
//        countDownTimerWithPause = null
//    }
//
//    override fun render(state: AfterCallState) {
//        if (state.hasError) {
//            finish()
//            return
//        }
//
////        imgPlaceHolder.isVisible = state.recipient == null
//        imgContactAvatar.isVisible = state.recipient != null
//
//        var name = state.recipient?.contact?.name
//        if (name == null || name.trim().isEmpty()) {
//            name = getString(R.string.after_call_unknown)
//        }
//        txtContactName.text = name
//
//        txtNumber.text = state.phoneNo
//        imgContactAvatar.setRecipient(state.recipient)
//    }
//
//    override fun makePhoneCall(phoneNo: String) {
//        phoneNoString = phoneNo
//        if (permissionManager.hasCalling()) {
//            finalMakePhoneCall()
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.CALL_PHONE), MAKE_CALL_PERMISSION_CODE
//            )
//        }
//    }
//
//    private fun finalMakePhoneCall() {
//        val callIntent =
//            Intent(if (permissionManager.hasCalling()) Intent.ACTION_CALL else Intent.ACTION_DIAL)
//        callIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//
//        callIntent.data = Uri.parse("tel:$phoneNoString")
//        navigator.startActivityExternal(callIntent)
//        finish()
//    }
//
//    override fun sendMessage(conversation: Conversation?) {
//        val smsIntent = Intent(this@AfterCallScreenActivity, ComposeActivity::class.java)
//        smsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        smsIntent.putExtra("threadId", conversation!!.id)
//        navigator.startActivityExternal(smsIntent)
//        finish()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == MAKE_CALL_PERMISSION_CODE) {
//            finalMakePhoneCall()
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == MAKE_CALL_PERMISSION_CODE) {
//            finalMakePhoneCall()
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        cancelCounter()
//        sessionManager.disableScreenAdded(true)
//    }
//
//    override fun finish() {
//        super.finishAndRemoveTask();
//    }
//}