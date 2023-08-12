package com.jksol.appmodule.rate.design

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import com.jksol.appmodule.R

open class BaseRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), SimpleRatingBar {
    interface OnRatingChangeListener {
        fun onRatingChange(ratingBar: BaseRatingBar?, rating: Float, fromUser: Boolean)
    }

    private var mNumStars = 0
    private var mPadding = 20
    private var mStarWidth = 0
    private var mStarHeight = 0
    private var mMinimumStars = 0f
    private var mRating = -1f
    var mStepSize = 1f
    private var mPreviousRating = 0f
    var mIsIndicator = false
    var mIsScrollable = true
    private var mIsClickable = true
    var mIsClearRatingEnabled = true
    private var mStartX = 0f
    private var mStartY = 0f
    private var mEmptyDrawable: Drawable? = null
    private var mFilledDrawable: Drawable? = null
    private var mOnRatingChangeListener: OnRatingChangeListener? = null
    protected var mPartialViews: MutableList<PartialView>? = null
    private fun initParamsValue(typedArray: TypedArray, context: Context) {
        mNumStars = typedArray.getInt(R.styleable.BaseRatingBar_srb_numStars, mNumStars)
        mStepSize = typedArray.getFloat(R.styleable.BaseRatingBar_srb_stepSize, mStepSize)
        mMinimumStars =
            typedArray.getFloat(R.styleable.BaseRatingBar_srb_minimumStars, mMinimumStars)
        mPadding =
            typedArray.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starPadding, mPadding)
        mStarWidth = typedArray.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starWidth, 0)
        mStarHeight = typedArray.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starHeight, 0)
        mEmptyDrawable =
            if (typedArray.hasValue(R.styleable.BaseRatingBar_srb_drawableEmpty)) ContextCompat.getDrawable(
                context,
                typedArray.getResourceId(R.styleable.BaseRatingBar_srb_drawableEmpty, NO_ID)
            ) else null
        mFilledDrawable =
            if (typedArray.hasValue(R.styleable.BaseRatingBar_srb_drawableFilled)) ContextCompat.getDrawable(
                context,
                typedArray.getResourceId(R.styleable.BaseRatingBar_srb_drawableFilled, NO_ID)
            ) else null
        mIsIndicator = typedArray.getBoolean(
            R.styleable.BaseRatingBar_srb_isIndicator,
            mIsIndicator
        )
        mIsScrollable = typedArray.getBoolean(
            R.styleable.BaseRatingBar_srb_scrollable,
            mIsScrollable
        )
        mIsClickable = typedArray.getBoolean(R.styleable.BaseRatingBar_srb_clickable, mIsClickable)
        mIsClearRatingEnabled = typedArray.getBoolean(
            R.styleable.BaseRatingBar_srb_clearRatingEnabled,
            mIsClearRatingEnabled
        )
        typedArray.recycle()
    }

    private fun verifyParamsValue() {
        if (mNumStars <= 0) {
            mNumStars = 5
        }
        if (mPadding < 0) {
            mPadding = 0
        }
        if (mEmptyDrawable == null) {
            mEmptyDrawable = ContextCompat.getDrawable(context, R.drawable.ic_unfill_star)
        }
        if (mFilledDrawable == null) {
            mFilledDrawable = ContextCompat.getDrawable(context, R.drawable.ic_fill_star)
        }
        if (mStepSize > 1.0f) {
            mStepSize = 1.0f
        } else if (mStepSize < 0.1f) {
            mStepSize = 0.1f
        }
        mMinimumStars = RatingBarUtils.getValidMinimumStars(mMinimumStars, mNumStars, mStepSize)
    }

    private fun initRatingView() {
        mPartialViews = ArrayList()
        for (i in 1..mNumStars) {
            val partialView: PartialView = getPartialView(
                i,
                mStarWidth,
                mStarHeight,
                mPadding,
                mFilledDrawable,
                mEmptyDrawable
            )
            addView(partialView)
            mPartialViews!!.add(partialView)
        }
    }

    private fun getPartialView(
        partialViewId: Int, starWidth: Int, starHeight: Int, padding: Int,
        filledDrawable: Drawable?, emptyDrawable: Drawable?
    ): PartialView {
        val partialView = PartialView(context, partialViewId, starWidth, starHeight, padding)
        if (filledDrawable != null) {
            partialView.setFilledDrawable(filledDrawable)
        }
        if (emptyDrawable != null) {
            partialView.setEmptyDrawable(emptyDrawable)
        }
        return partialView
    }

    /**
     * Retain this method to let other RatingBar can custom their decrease animation.
     */
    public open fun emptyRatingBar() {
        fillRatingBar(0f)
    }

    /**
     * Use {maxIntOfRating} because if the rating is 3.5
     * the view which id is 3 also need to be filled.
     */
    public open fun fillRatingBar(rating: Float) {
        if (mPartialViews != null) {
            for (partialView in mPartialViews!!) {
                val ratingViewId = partialView.tag as Int
                val maxIntOfRating = kotlin.math.ceil(rating.toDouble())
                if (ratingViewId > maxIntOfRating) {
                    partialView.setEmpty()
                    continue
                }
                if (ratingViewId.toDouble() == maxIntOfRating) {
                    partialView.setPartialFilled(rating)
                } else {
                    partialView.setFilled()
                }
            }
        }
    }

    override var numStars: Int
        get() = mNumStars
        set(numStars) {
            if (numStars <= 0) {
                return
            }
            mPartialViews!!.clear()
            removeAllViews()
            mNumStars = numStars
            initRatingView()
        }

    private fun setRating(rating: Float, fromUser: Boolean) {

        if (rating > mNumStars) {
            this@BaseRatingBar.rating = mNumStars.toFloat()
        }
        if (rating < mMinimumStars) {
            this@BaseRatingBar.rating = mMinimumStars
        }
        if (mRating == rating) {
            return
        }
        mRating = rating
        if (mOnRatingChangeListener != null) {
            mOnRatingChangeListener!!.onRatingChange(this, mRating, fromUser)
        }
        fillRatingBar(rating)
    }

    override var rating: Float
        get() = mRating
        set(rating) {
            setRating(rating, false)
        }

    // Unit is pixel
    override var starWidth: Int
        get() = mStarWidth
        set(starWidth) {
            mStarWidth = starWidth
            if (mPartialViews != null) {
                for (partialView in mPartialViews!!) {
                    partialView.setStarWidth(starWidth)
                }
            }
        }

    // Unit is pixel
    override var starHeight: Int
        get() = mStarHeight
        set(starHeight) {
            mStarHeight = starHeight
            if (mPartialViews != null) {
                for (partialView in mPartialViews!!) {
                    partialView.setStarHeight(starHeight)
                }
            }
        }

    override var starPadding: Int
        get() = mPadding
        set(ratingPadding) {
            if (ratingPadding < 0) {
                return
            }
            mPadding = ratingPadding
            if (mPartialViews != null) {
                for (partialView in mPartialViews!!) {
                    partialView.setPadding(mPadding, mPadding, mPadding, mPadding)
                }
            }
        }

    override fun setEmptyDrawableRes(@DrawableRes res: Int) {
        val drawable = ContextCompat.getDrawable(context, res)
        drawable?.let { setEmptyDrawable(it) }
    }

    override fun setFilledDrawableRes(@DrawableRes res: Int) {
        val drawable = ContextCompat.getDrawable(context, res)
        drawable?.let { setFilledDrawable(it) }
    }

    override fun setEmptyDrawable(drawable: Drawable) {
        mEmptyDrawable = drawable
        if (mPartialViews != null) {
            for (partialView in mPartialViews!!) {
                partialView.setEmptyDrawable(drawable)
            }
        }
    }

    override fun setFilledDrawable(drawable: Drawable) {
        mFilledDrawable = drawable
        if (mPartialViews != null) {
            for (partialView in mPartialViews!!) {
                partialView.setFilledDrawable(drawable)
            }
        }
    }

    override fun setMinimumStars(@FloatRange(from = 0.0) minimumStars: Float) {
        mMinimumStars = RatingBarUtils.getValidMinimumStars(minimumStars, mNumStars, mStepSize)
    }

    override fun isIndicator(): Boolean {
        return mIsIndicator
    }

    override fun setIsIndicator(indicator: Boolean) {
        mIsIndicator = indicator
    }

    override var isScrollable: Boolean
        get() = mIsScrollable
        set(value) {
            mIsScrollable = value
        }
    override var isClickable1: Boolean
        get() = mIsClickable
        set(value) {
            mIsClickable = value
        }
    override var isClearRatingEnabled: Boolean
        get() = mIsClearRatingEnabled
        set(value) {
            mIsClearRatingEnabled = value
        }
    override var stepSize: Float
        get() = mStepSize
        set(value) {
            mStepSize = value
        }

    override fun isClickable(): Boolean {
        return mIsClickable
    }

    override fun setClickable(clickable: Boolean) {
        mIsClickable = clickable
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mIsIndicator) {
            return false
        }
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = eventX
                mStartY = eventY
                mPreviousRating = mRating
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mIsScrollable) {
                    return false
                }
                handleMoveEvent(eventX)
            }
            MotionEvent.ACTION_UP -> {
                if (!RatingBarUtils.isClickEvent(mStartX, mStartY, event) || !isClickable) {
                    return false
                }
                handleClickEvent(eventX)
            }
        }
        parent.requestDisallowInterceptTouchEvent(true)
        return true
    }

    private fun handleMoveEvent(eventX: Float) {
        if (mPartialViews != null) {
            for (partialView in mPartialViews!!) {
                if (eventX < partialView.getWidth() / 10f + mMinimumStars * partialView.getWidth()) {
                    setRating(mMinimumStars, true)
                    return
                }
                if (!isPositionInRatingView(eventX, partialView)) {
                    continue
                }
                val rating: Float = RatingBarUtils.calculateRating(
                    partialView,
                    mStepSize, eventX
                )
                if (mRating != rating) {
                    setRating(rating, true)
                }
            }
        }
    }

    private fun handleClickEvent(eventX: Float) {
        if (mPartialViews != null) {
            for (partialView in mPartialViews!!) {
                if (!isPositionInRatingView(eventX, partialView)) {
                    continue
                }
                val rating =
                    if (stepSize == 1f) (partialView.getTag() as Int).toFloat() else RatingBarUtils.calculateRating(
                        partialView,
                        stepSize, eventX
                    )
                if (mPreviousRating == rating && mIsClearRatingEnabled) {
                    setRating(mMinimumStars, true)
                } else {
                    setRating(rating, true)
                }
                break
            }
        }
    }

    private fun isPositionInRatingView(eventX: Float, ratingView: View): Boolean {
        return eventX > ratingView.left && eventX < ratingView.right
    }

    fun setOnRatingChangeListener(onRatingChangeListener: OnRatingChangeListener?) {
        mOnRatingChangeListener = onRatingChangeListener
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.rating = mRating
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss: SavedState = state as SavedState
        super.onRestoreInstanceState(ss.getSuperState())
        rating = ss.rating
    }

    companion object {
        const val TAG = "SimpleRatingBar"
    }

    /**
     * @param context      context
     * @param attrs        attributes from XML => app:mainText="mainText"
     * @param defStyleAttr attributes from default style (Application theme or activity theme)
     */
    /* Call by xml layout */
    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseRatingBar)
        var rating = typedArray.getFloat(R.styleable.BaseRatingBar_srb_rating, 0f)
        initParamsValue(typedArray, context)
        verifyParamsValue()
        initRatingView()
        rating = rating
    }
}