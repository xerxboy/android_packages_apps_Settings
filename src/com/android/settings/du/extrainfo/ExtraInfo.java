/*
 * Copyright (C) 2014 The Dirty Unicorns project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.du.extrainfo;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Display;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;

public class ExtraInfo {
    private static final String UNKNOWN = "unknown";
    private final Display mDisplay;
    private DisplayMetrics mDisplayMetrics;

    public String getBuildManufacturer() {         return gets(Build.MANUFACTURER); }
    public String getBuildVersionCodename() {      return gets(Build.VERSION.CODENAME); }
    public String getBuildCpuAbi() {               return gets(Build.CPU_ABI); }
    public String getBuildCpuAbi2() {              return gets(Build.CPU_ABI2); }
    public String getBuildBootloader() {           return gets(Build.BOOTLOADER); }
    public String getBuildDisplay() {              return gets(Build.DISPLAY); }

    public String getProp(String prop) {
        if (prop == null || prop.length() == 0) return UNKNOWN;
       String s = UNKNOWN;
        try { s = ExtraInfoLib.shellExec("getprop " + prop).get(0).trim(); }
        catch (IOException e) {}
        catch (SecurityException e) {}
        if (s == "[]") return UNKNOWN;
        return s;
    }

    public ExtraInfo(Context context) {
        mDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
        mDisplayMetrics = new DisplayMetrics();
        mDisplay.getMetrics(mDisplayMetrics);
    }

    private String gets(String s) {
        return (s == null || s.length() == 0) ? UNKNOWN : s;
        }

        @SuppressWarnings("deprecation")
        public String getSystemSize() {
        StatFs stat = new StatFs("/system");
        return String.valueOf((long)stat.getBlockSize() * (long)stat.getBlockCount());
    }

        @SuppressWarnings("deprecation")
        public String getSystemSize(String scale, int decimalPlaces) {
        StatFs stat = new StatFs("/system");
        return ExtraInfoLib.round(
                        ExtraInfoLib.scaleData(
                                (double)stat.getBlockSize() * (double)stat.getBlockCount(),
                                "B", scale), decimalPlaces) + " " + scale;
    }

        @SuppressWarnings("deprecation")
        public String getDataSize() {
        StatFs stat = new StatFs("/data");
        return String.valueOf((long)stat.getBlockSize() * (long)stat.getBlockCount());
    }

        @SuppressWarnings("deprecation")
        public String getDataSize(String scale, int decimalPlaces) {
        StatFs stat = new StatFs("/data");
        return ExtraInfoLib.round(
                ExtraInfoLib.scaleData(
                    (double)stat.getBlockSize() * (double)stat.getBlockCount(),
                    "B", scale), decimalPlaces) + " " + scale;
    }

    public String getPropName() {           return getProp("ro.product.name"); }
    public String getPropHardware() {       return getProp("ro.hardware"); }
    public String getPropCpuAbi2() {        return getProp("ro.product.cpu.abilist"); }

    private List<String> getProc(String proc) {
        List<String> list = new ArrayList<String>();
        if (proc == null || proc.length() == 0) return list;
        try { list = ExtraInfoLib.shellExec("cat /proc/" + proc); }
        catch (IOException e) {}
        catch (SecurityException e) {}
        return list;
    }
    public String getProcCpuField(String field) {
        if (field == null || field.length() == 0) return UNKNOWN;
        List<String> list = getProc("cpuinfo");
        for (String s : list) {
            String[] parts = s.split(":", 2);
            if (parts[0].trim().equals(field)) { return parts[1].trim(); }
        }
        return UNKNOWN;
    }
    public String getProcMemField(String field) {
        if (field == null || field.length() == 0) return UNKNOWN;
        List<String> list = getProc("meminfo");
        for (String s : list) {
            String[] parts = s.split(":", 2);
            if (parts[0].trim().equals(field)) { return parts[1].trim(); }
        }
        return UNKNOWN;
    }

    public List<String> getProcCpuInfo() {      return getProc("cpuinfo"); }

    public String getProcCpuModelName() {       return getProcCpuField("model name"); }
    public String getProcCpuBogoMips() {        return getProcCpuField("bogomips"); }
    public String getProcCpuFlags() {           return getProcCpuField("flags"); }
    public String getProcCpuTotalCores() {      return getProcCpuField("cpu cores"); }
    public String getProcCpuMhz() {             return getProcCpuField("cpu MHz"); }
    public String getProcCpuAddressSizes() {    return getProcCpuField("address sizes"); }

    public List<String> getProcMemInfo() {      return getProc("meminfo"); }

    public String getProcRamTotal() {           return getProcMemField("MemTotal"); }

    public String getProcRamTotal(String scale) {
        if (scale.length() == 0) return UNKNOWN;
        String[] parts = getProcRamTotal().split("\\s", 2);
        double value = 0.0;
        try { value = Double.valueOf(parts[0]); }
        catch (NumberFormatException e) {}
        return String.valueOf(ExtraInfoLib.scaleData(value, parts[0], scale));
    }

    public String getProcRamFree() {             return getProcMemField("MemFree"); }

    public String getProcRamFree(String scale) {
        if (scale.length() == 0) return UNKNOWN;
        String[] parts = getProcRamFree().split("\\s", 2);
        double value = 0.0;
        try { value = Double.valueOf(parts[0]); }
        catch (NumberFormatException e) {}
        return String.valueOf(ExtraInfoLib.scaleData(value, parts[0], scale));
    }

    public String getProcCached() {             return getProcMemField("Cached"); }

    public String getProcCached(String scale) {
        if (scale.length() == 0) return UNKNOWN;
        String[] parts = getProcCached().split("\\s", 2);
        double value = 0.0;
        try { value = Double.valueOf(parts[0]); }
        catch (NumberFormatException e) {}
        return String.valueOf(ExtraInfoLib.scaleData(value, parts[0], scale));
    }


    public String getDisplayWidthInches() {
        NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMaximumFractionDigits(2);
        return numFormat.format(mDisplay.getWidth() / mDisplayMetrics.xdpi);
    }
    public String getDisplayHeightInches() {
        NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMaximumFractionDigits(2);
        return numFormat.format(mDisplay.getHeight() / mDisplayMetrics.ydpi);
    }
    public String getDisplayDiagonalInches() {
        NumberFormat numFormat = NumberFormat.getInstance();
        numFormat.setMaximumFractionDigits(2);

        return numFormat.format(
                        Math.sqrt(
                        Math.pow(mDisplay.getWidth() / mDisplayMetrics.xdpi, 2) +
                        Math.pow(mDisplay.getHeight() / mDisplayMetrics.ydpi, 2)));
    }
    public String getDisplayWidth() { return String.valueOf(mDisplay.getWidth()); }
    public String getDisplayHeight() { return String.valueOf(mDisplay.getHeight()); }
    public String getDisplayDpiX() { return String.valueOf(mDisplayMetrics.xdpi); }
    public String getDisplayDpiY() { return String.valueOf(mDisplayMetrics.ydpi); }
    public String getDisplayRefreshRate() { return String.valueOf(mDisplay.getRefreshRate()); }
    public String getDisplayLogicalDensity() { return String.valueOf(mDisplayMetrics.density); }

    public String getDisplayDpi() {
        return String.valueOf(mDisplayMetrics.densityDpi);
    }
    public String getDisplayDensity() {
            if ((int) (160 * mDisplayMetrics.density) <= 159) return "LDPI";
            if ((int) (160 * mDisplayMetrics.density) <= 239) return "MDPI";
            if ((int) (160 * mDisplayMetrics.density) <= 319) return "HDPI";
            if ((int) (160 * mDisplayMetrics.density) <= 479) return "XHDPI";
            if ((int) (160 * mDisplayMetrics.density) <= 639) return "XXHDPI";
            if ((int) (160 * mDisplayMetrics.density) <= 799) return "XXXHDPI";
        return UNKNOWN;
    }
}

