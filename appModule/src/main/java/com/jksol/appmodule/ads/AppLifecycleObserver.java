package com.jksol.appmodule.ads;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;


public class AppLifecycleObserver implements LifecycleObserver {

    AppOpenLifeCycleChange mAppOpenLifeCycleChange;

    public AppLifecycleObserver(AppOpenLifeCycleChange mAppOpenLifeCycleChange) {
        this.mAppOpenLifeCycleChange = mAppOpenLifeCycleChange;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        mAppOpenLifeCycleChange.onForeground();

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        mAppOpenLifeCycleChange.onBackground();
    }

}
