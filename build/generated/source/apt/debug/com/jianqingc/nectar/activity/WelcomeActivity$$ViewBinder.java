// Generated code from Butter Knife. Do not modify!
package com.jianqingc.nectar.activity;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class WelcomeActivity$$ViewBinder<T extends com.jianqingc.nectar.activity.WelcomeActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131165841, "field 'mIVEntry'");
    target.mIVEntry = finder.castView(view, 2131165841, "field 'mIVEntry'");
  }

  @Override public void unbind(T target) {
    target.mIVEntry = null;
  }
}
