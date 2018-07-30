package cn.quanmin.appanalyse;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.DexClass;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Created by liquanmin on 2018/7/28.
 * <p>
 * 应用宝 + 小米爬虫
 */
public class AppMarketWebWorm {
    // 升序比较器
    Comparator<Map.Entry<ApkPackage, Integer>> valueComparator = new Comparator<Map.Entry<ApkPackage, Integer>>() {
        @Override
        public int compare(Map.Entry<ApkPackage, Integer> o1,
                           Map.Entry<ApkPackage, Integer> o2) {
            return o2.getValue() - o1.getValue();
        }
    };

    public void fetchPage() throws IOException {
        Set<Apk> apkSet = new HashSet<>();

//        // ############################# 应用宝 #############################
//        String appHome = "http://sj.qq.com/myapp/cate/appList.htm";
//        String homeUrl = "http://sj.qq.com/myapp/category.htm";
//        URL url = new URL(homeUrl);
//        URLConnection urlConn = url.openConnection();
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(
//                urlConn.getInputStream()));
//
//        List<String> cateList = new LinkedList<String>();
//        String line = null;
//        String href = "href=\"";
//        while ((line = br.readLine()) != null) {
//            if (line.contains("orgame")
//                    && line.contains("categoryId")) {
//                line = line.substring(line.indexOf(href) + href.length());
//                line = line.substring(0, line.indexOf("\""));
//                cateList.add(appHome + line);
//            }
//        }
//        br.close();
//
//        System.out.println("分类总数:" + cateList.size());
//
//        for (int count = 0; count < 3; count++) { // 应用宝有一定的防御措施
//            for (String cate : cateList) {
//                url = new URL(cate + "&pageSize=200&pageContext=63");
//                urlConn = url.openConnection();
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(
//                        urlConn.getInputStream()));
//
//                while ((line = reader.readLine()) != null) {
//                    JSONObject cateJosn = null;
//                    try {
//                        cateJosn = JSONObject.parseObject(line);
//                    } catch (Exception e) {
//                        continue;
//                    }
//
//                    if (cateJosn == null) {
//                        continue;
//                    }
//
//                    JSONArray apkJsons = cateJosn.getJSONArray("obj");
//                    if (apkJsons == null)
//                        continue;
//                    for (int index = 0; index < apkJsons.size(); index++) {
//                        JSONObject apkJson = apkJsons.getJSONObject(index);
//
//                        Apk apk = new Apk();
//                        apk.setAppName(apkJson.getString("appName"));
//                        apk.setApkUrl(apkJson.getString("apkUrl"));
//                        apk.setAppSize((long) (apkJson.getLong("fileSize") / 1024f / 1024f));
//                        apk.setPackageName(apkJson.getString("pkgName"));
//                        apk.setAppDownCount(apkJson.getInteger("appDownCount"));
//                        apkSet.add(apk);
//                    }
//                }
//
//                br.close();
//            }
//        }
//
//        System.out.println("统计 APP 总数: " + apkSet.size());
//        List<Apk> apkList = new LinkedList<>(apkSet);
//        apkList.sort(new Comparator<Apk>() {
//            @Override
//            public int compare(Apk o1, Apk o2) {
//                return o2.getAppDownCount() - o1.getAppDownCount();
//            }
//        });
//
//        for (Apk apk : apkList) {
//            System.out.println(apk.getAppName());
//        }

        // ############################# 应用宝 #############################

        // ############################# 豌豆荚 #############################
        String homeUrl = "http://www.wandoujia.com/category/app";
        URL url = new URL(homeUrl);
        URLConnection urlConn = url.openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(
                urlConn.getInputStream()));

        List<String> cateList = new LinkedList<String>();
        String line = "";
        String categoryCls = "class=\"cate-link\"";
        String href = "href=\"";
        while ((line = br.readLine()) != null) {
            if (line.contains(categoryCls)) {
                line = line.substring(line.indexOf(categoryCls) + categoryCls.length());
                line = line.substring(line.indexOf(href) + href.length());
                line = line.substring(0, line.indexOf("\""));
                cateList.add(line);
            }

            if (cateList.size() >= 14) { // 之后的就是游戏了
                break;
            }
        }
        br.close();


        // 特殊标记
        String pnAttr = "data-pn=\"";
        String appDesc = "class=\"app-desc\"";
        String titleAttr = "title=\"";
        String installCountCls = "class=\"install-count\">";
        String sizeAttr = "<span title=\"";
        String installCls = "class=\"i-source install-btn \"";
        String tagEnd = "\">";

        for (String category : cateList) { // 某一个分类下面
            int pageIndex = 1;
            while (true) {
                String appPage = category + "/" + pageIndex;

                URL apkURL = new URL(appPage);
                HttpURLConnection urlConnection = (HttpURLConnection) apkURL.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));

                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                String pageInfo = sb.toString();
                if (pageInfo.contains("亲，已经没有内容啦")) {
                    break;
                }

                while (pageInfo.contains(appDesc)) {
                    pageInfo = pageInfo.substring(pageInfo.indexOf(pnAttr) + pnAttr.length());
                    String packageName = pageInfo.substring(0, pageInfo.indexOf("\""));
                    pageInfo = pageInfo.substring(pageInfo.indexOf(appDesc) + appDesc.length());
                    pageInfo = pageInfo.substring(pageInfo.indexOf(titleAttr) + titleAttr.length());
                    String appName = pageInfo.substring(0, pageInfo.indexOf("\""));
                    pageInfo = pageInfo.substring(pageInfo.indexOf(installCountCls) + installCountCls.length());
                    String countStr = pageInfo.substring(0, pageInfo.indexOf("人安装"));

                    // 解析 count
                    String tempStr = countStr.substring(0, countStr.length() - 1);
                    int count = (int) (Float.parseFloat(tempStr) * (countStr.endsWith("万") ? 10000 : 100000000));

                    pageInfo = pageInfo.substring(pageInfo.indexOf(sizeAttr) + sizeAttr.length());
                    String sizeStr = pageInfo.substring(0, pageInfo.indexOf(tagEnd));
                    float size;
                    if (sizeStr.endsWith("MB")) {
                        sizeStr = sizeStr.substring(0, sizeStr.indexOf("MB"));
                        size = Float.parseFloat(sizeStr) * 1024;
                    } else {
                        sizeStr = sizeStr.substring(0, sizeStr.indexOf("KB"));
                        size = Float.parseFloat(sizeStr);
                    }

                    pageInfo = pageInfo.substring(pageInfo.indexOf(installCls) + installCls.length());
                    pageInfo = pageInfo.substring(pageInfo.indexOf(href) + href.length());
                    String apkUrl = pageInfo.substring(0, pageInfo.indexOf("\""));

                    Apk apk = new Apk();
                    apk.setApkUrl(apkUrl);
                    apk.setAppDownCount(count);
                    apk.setAppName(appName);
                    apk.setAppSize(size);
                    apk.setPackageName(packageName);

                    if (apk.getAppDownCount() < 200 * 10000) { // 200w 以下不做统计
                        continue;
                    }
                    apkSet.add(apk);
                }

                pageIndex++;
            }
        }

        Set<Apk> emptyApk = new HashSet<>();
        // 预处理 URL: 转成应用宝
        for (Apk apk : apkSet) {
            String yybPage = "http://sj.qq.com/myapp/detail.htm?apkName=" + apk.getPackageName();

            URL apkURL = new URL(yybPage);
            HttpURLConnection urlConnection = (HttpURLConnection) apkURL.openConnection();
            if (urlConnection.getResponseCode() != 200) {
                emptyApk.add(apk);
                continue;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));

            String urlAttr = "data-apkUrl=\"";
            while ((line = reader.readLine()) != null) {
                if (line.contains(urlAttr)) {
                    line = line.substring(line.indexOf(urlAttr) + urlAttr.length());
                    line = line.substring(0, line.indexOf("\""));

                    if (line == null || line.length() == 0) {
                        emptyApk.add(apk);
                        break;
                    }

                    apk.setApkUrl(line);
                }
            }
            br.close();
        }

        apkSet.removeAll(emptyApk);
        System.out.println("统计 APP 总数: " + apkSet.size());

        // ############################# 豌豆荚 #############################

        String apkDownloadDest = "/Users/liquanmin/Downloads/temp.apk";

        Map<ApkPackage, Integer> packageMap = new HashMap<>();

        int apkCount = 0;
        for (Apk apk : apkSet) {
            URL apkURL = new URL(apk.getApkUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) apkURL.openConnection();

            // 得到输入流
            InputStream inputStream = urlConnection.getInputStream();
            // 获取自己数组
            byte[] getData = readInputStream(inputStream);

            // 文件保存位置
            File file = new File(apkDownloadDest);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            fos.flush();
            if (fos != null) {
                fos.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }

            // 处理 apk
            Set<ApkPackage> apkPackageSet = extracPackageFromApk(file);
            for (ApkPackage apkPackage : apkPackageSet) {
                if (packageMap.containsKey(apkPackage)) {
                    int count = packageMap.get(apkPackage);
                    packageMap.put(apkPackage, ++count);
                } else {
                    packageMap.put(apkPackage, 1);
                }
            }

            apkCount++;
            if (apkCount > 10000) {
                break;
            }
        }

        // map转换成list进行排序
        List<Map.Entry<ApkPackage, Integer>> list = new ArrayList<Map.Entry<ApkPackage, Integer>>(packageMap.entrySet());
        // 排序
        Collections.sort(list, valueComparator);
        // 默认情况下，TreeMap对key进行升序排序
        System.out.println("------------ map 按照 value 升序排序--------------------");
        for (Map.Entry<ApkPackage, Integer> entry : list) {
            System.out.println(entry.getKey().getPackageName() + " : " + entry.getValue());
        }
    }

    private Set<ApkPackage> extracPackageFromApk(File file) throws IOException {
        ApkFile apkFile = new ApkFile(file);

        ApkMeta apkMeta = apkFile.getApkMeta();
        String packageName = apkMeta.getPackageName();

        Set<ApkPackage> apkPackages = new HashSet<>();
        try {
            DexClass[] classes = apkFile.getDexClasses();
            for (DexClass dexClass : classes) {
                String clsPackage = dexClass.getPackageName();
                if (clsPackage != null && !clsPackage.startsWith(packageName)
                        && !clsPackage.startsWith("android")) {
                    ApkPackage apkPackage = new ApkPackage();
                    apkPackage.setPackageName(dealPackageName(clsPackage));
                    apkPackages.add(apkPackage);
                }
            }
        } catch (Exception e) {

        }
        return apkPackages;
    }

    private String dealPackageName(String packageName) {
        String[] parts = packageName.split("\\.");
        if (parts.length > 3) {
            packageName = parts[0] + "." + parts[1] + "." + parts[2];
        }

        return packageName;
    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}