package com.storm.experiment1;

public interface PepperLog {
    void appendLog(String tag, String text);
    void appendLog(String text);
    void clearLog();
}
