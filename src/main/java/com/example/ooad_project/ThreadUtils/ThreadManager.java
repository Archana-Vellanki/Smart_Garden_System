package com.example.ooad_project.ThreadUtils;




public class ThreadManager {

    public static void run(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);  // Ensure threads don't prevent the application from exiting
        thread.start();
    }
}
