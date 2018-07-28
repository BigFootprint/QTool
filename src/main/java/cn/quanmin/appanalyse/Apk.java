package cn.quanmin.appanalyse;

/**
 * Created by liquanmin on 2018/7/28.
 */
public class Apk {
    private String appName;
    private String apkUrl;
    private long appSize; // 单位 M
    private String packageName;
    private int appDownCount;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public long getAppSize() {
        return appSize;
    }

    public void setAppSize(long appSize) {
        this.appSize = appSize;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getAppDownCount() {
        return appDownCount;
    }

    public void setAppDownCount(int appDownCount) {
        this.appDownCount = appDownCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Apk apk = (Apk) o;

        return packageName != null ? packageName.equals(apk.packageName) : apk.packageName == null;

    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }
}
