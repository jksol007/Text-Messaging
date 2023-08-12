package com.jksol.appmodule.ads;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jksol.appmodule.R;
import com.jksol.appmodule.rate.RateDialog;

import org.jetbrains.annotations.NotNull;

public class ExitDialogNative extends BottomSheetDialogFragment {
    View view;
    boolean isExit = false;
    dialogDismiss listener;
    NativeAd mNativeRateAd;

    public interface dialogDismiss {
        void onDismiss(boolean isExit);
    }

    public ExitDialogNative() {
    }

    public ExitDialogNative(NativeAd mNativeRateAd, dialogDismiss listener) {
        this.mNativeRateAd = mNativeRateAd;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.bottom_navigation_exit, container, false);
        initView();
        getDialog().setOnShowListener(dialog -> {
            new Handler(Looper.myLooper()).postDelayed(() -> {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setSkipCollapsed(true);
            }, 0);
        });
        return view;
    }

    private void initView() {
        TextView tv_exit = view.findViewById(R.id.tv_exit);
        FrameLayout adLayout = view.findViewById(R.id.adLayout);
        populateUnifiedNativeAdView1(
                getContext(),
                adLayout,
                mNativeRateAd,
                true,
                false);

        tv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onDismiss(true);
                }
                isExit = true;
                dismiss();
                getActivity().finish();
            }
        });
        //tv_exit.setOnClickListener { v: View? -> this.finish() }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = dialog.getWindow();
            if (window != null) {
                DisplayMetrics metrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                GradientDrawable dimDrawable = new GradientDrawable();

                GradientDrawable navigationBarDrawable = new GradientDrawable();
                navigationBarDrawable.setShape(GradientDrawable.RECTANGLE);
                navigationBarDrawable.setColor(getResources().getColor(R.color.white));

                Drawable[] layers = {dimDrawable, navigationBarDrawable};

                LayerDrawable windowBackground = new LayerDrawable(layers);
                windowBackground.setLayerInsetTop(1, metrics.heightPixels);

                window.setBackgroundDrawable(windowBackground);
            }
        }
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {

        if (mNativeRateAd != null) {
            mNativeRateAd.destroy();
            mNativeRateAd = null;
            if (!isExit) {
                if (listener != null) {
                    listener.onDismiss(false);
                }
            }
        }
        super.onDismiss(dialog);
    }

    public void populateUnifiedNativeAdView1(Context context, FrameLayout frameLayout, NativeAd nativeAd, boolean isShowMedia, boolean isGrid) {
        if (isNetworkAvailable(context)) {
            LayoutInflater inflater = LayoutInflater.from(context);
            // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
            NativeAdView adView;
            adView = (NativeAdView) inflater.inflate(R.layout.layout_big_native_ad_mob1, null);
            if (frameLayout != null) {
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }
            try {
                if (isShowMedia) {
                    MediaView mediaView = adView.findViewById(R.id.mediaView);
                    mediaView.setMediaContent(nativeAd.getMediaContent());
//            mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                    mediaView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            if (child instanceof ImageView) {
                                ImageView imageView = (ImageView) child;
                                imageView.setAdjustViewBounds(true);
                            }
                        }

                        @Override
                        public void onChildViewRemoved(View parent, View child) {
                        }
                    });

                    adView.setMediaView(mediaView);
                }
//
//                if (color == 1) {
//                    mediaView.setVisibility(View.VISIBLE);
//                } else {
//                    mediaView.setVisibility(View.GONE);
//                }
                adView.setHeadlineView(adView.findViewById(R.id.adTitle));
                adView.setBodyView(adView.findViewById(R.id.adDescription));
                adView.setIconView(adView.findViewById(R.id.adIcon));
                ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
                adView.setAdvertiserView(adView.findViewById(R.id.adAdvertiser));

//                adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
                if (nativeAd.getBody() == null) {
                    adView.getBodyView().setVisibility(View.INVISIBLE);
                } else {
                    adView.getBodyView().setVisibility(View.VISIBLE);
                    ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
                }
                if (nativeAd.getIcon() == null) {
                    adView.getIconView().setVisibility(View.GONE);
                } else {
                    ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
                    adView.getIconView().setVisibility(View.VISIBLE);
                }
                if (isShowMedia) {
                    adView.getMediaView().setVisibility(View.VISIBLE);
                } else {
                    adView.getMediaView().setVisibility(View.GONE);
                }

                if (nativeAd.getAdvertiser() == null) {
                    adView.getAdvertiserView().setVisibility(View.INVISIBLE);
                } else {
                    ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                    adView.getAdvertiserView().setVisibility(View.VISIBLE);
                }

                adView.setNativeAd(nativeAd);
                VideoController vc = nativeAd.getMediaContent().getVideoController();
                vc.mute(true);
                if (vc.hasVideoContent()) {
                    vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                        @Override
                        public void onVideoEnd() {
                            super.onVideoEnd();
                        }
                    });
                }
                if (frameLayout != null) {
                    frameLayout.setVisibility(View.VISIBLE);
                }

                adView.setNativeAd(nativeAd);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "populateUnifiedNativeAdView Exception: " + e.getMessage());
            }
        }
    }

    public boolean isNetworkAvailable(Context c) {
        ConnectivityManager manager = (ConnectivityManager)
                c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
