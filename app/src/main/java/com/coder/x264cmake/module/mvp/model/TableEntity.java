package com.coder.x264cmake.module.mvp.model;

import com.flyco.tablayout.listener.CustomTabEntity;

public class TableEntity implements CustomTabEntity {
    private final String tabTitle;
    private final int tabSelectedIcon;
    private final int tabUnselectedIcon;

    public TableEntity(String tabTitle,int tabSelectedIcon, int tabUnselectedIcon) {
        this.tabTitle = tabTitle;
        this.tabSelectedIcon = tabSelectedIcon;
        this.tabUnselectedIcon = tabUnselectedIcon;
    }

    @Override
    public String getTabTitle() {
        return tabTitle;
    }

    @Override
    public int getTabSelectedIcon() {
        return tabSelectedIcon;
    }

    @Override
    public int getTabUnselectedIcon() {
        return tabUnselectedIcon;
    }
}