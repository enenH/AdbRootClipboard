package com.example.mylibrary;

import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;

import com.example.mylibrary.wrappers.ClipboardManager;
import com.example.mylibrary.wrappers.ServiceManager;

public class Main {
    public static void main(String[] args) {
        //为root用户设置uid为shell用户
        if (Os.getuid() == 0) {
            try {
                Os.seteuid(Process.SHELL_UID);
            } catch (ErrnoException ignored) {

            }
        }
        ClipboardManager manager = ServiceManager.getClipboardManager();
        if (manager == null) {
            System.out.println("Failed to get ClipboardManager");
            return;
        }
        if (args.length == 0) {
            System.out.println(manager.getText());
        } else {
            StringBuilder text = new StringBuilder();
            for (String arg : args) {
                text.append(arg).append(" ");
            }
            boolean res = manager.setText(text.substring(0, text.length() - 1));
            if (res) {
                System.out.println("Successed");
            } else {
                System.out.println("Failed");
            }
        }
    }
}
