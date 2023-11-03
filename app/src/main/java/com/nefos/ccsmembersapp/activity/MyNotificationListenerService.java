package com.nefos.ccsmembersapp.activity;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class MyNotificationListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        // Handle the notification
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle the removed notification
    }
}
