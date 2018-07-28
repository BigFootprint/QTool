package cn.quanmin.appanalyse;

/**
 * Created by liquanmin on 2018/7/28.
 */
public class ApkPackage {
    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApkPackage that = (ApkPackage) o;

        return packageName != null ? packageName.equals(that.packageName) : that.packageName == null;

    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }
}
