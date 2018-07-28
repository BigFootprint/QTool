package cn.quanmin.appanalyse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
            return o1.getValue() - o2.getValue();
        }
    };

    public void fetchPage() throws IOException {
        Set<Apk> apkSet = new HashSet<>();

        // 应用宝
        String appHome = "http://sj.qq.com/myapp/cate/appList.htm";
        String homeUrl = "http://sj.qq.com/myapp/category.htm";
        URL url = new URL(homeUrl);
        URLConnection urlConn = url.openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(
                urlConn.getInputStream()));

        List<String> cateList = new LinkedList<String>();
        String line = null;
        String href = "href=\"";
        while ((line = br.readLine()) != null) {
            if (line.contains("orgame")
                    && line.contains("categoryId")) {
                line = line.substring(line.indexOf(href) + href.length());
                line = line.substring(0, line.indexOf("\""));
                cateList.add(appHome + line);
            }
        }
        br.close();

        System.out.println("分类总数:" + cateList.size());

        for (int count = 0; count < 3; count++) { // 应用宝有一定的防御措施
            for (String cate : cateList) {
                url = new URL(cate + "&pageSize=200&pageContext=63");
                urlConn = url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        urlConn.getInputStream()));

                while ((line = reader.readLine()) != null) {
                    JSONObject cateJosn = null;
                    try {
                        cateJosn = JSONObject.parseObject(line);
                    } catch (Exception e) {
                        continue;
                    }

                    if (cateJosn == null) {
                        continue;
                    }

                    JSONArray apkJsons = cateJosn.getJSONArray("obj");
                    if (apkJsons == null)
                        continue;
                    for (int index = 0; index < apkJsons.size(); index++) {
                        JSONObject apkJson = apkJsons.getJSONObject(index);

                        Apk apk = new Apk();
                        apk.setAppName(apkJson.getString("appName"));
                        apk.setApkUrl(apkJson.getString("apkUrl"));
                        apk.setAppSize((long) (apkJson.getLong("fileSize") / 1024f / 1024f));
                        apk.setPackageName(apkJson.getString("pkgName"));
                        apk.setAppDownCount(apkJson.getInteger("appDownCount"));
                        apkSet.add(apk);
                    }
                }

                br.close();
            }
        }

        System.out.println("统计 APP 总数: " + apkSet.size());

//        // 小米
//        List<String> miCateList = new LinkedList<>();
//        int[] cateIds = {5, 27, 2, 7, 12, 10, 9, 4, 3, 6, 14, 8, 11};
//        miCateList.add("http://app.mi.com/categotyAllListApi?page=0&categoryId=%d&pageSize=30");

        String apkDownloadDest = "/Users/liquanmin/Downloads/temp.apk";

        Map<ApkPackage, Integer> packageMap = new HashMap<>();
        for (Apk apk : apkSet) {
            URL apkURL = new URL(apk.getApkUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) apkURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.75 Safari/537.36");
            urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7");
            urlConnection.setRequestProperty("Host", "imtt.dd.qq.com");
            urlConnection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            urlConnection.connect();


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
        }

        // map转换成list进行排序
        List<Map.Entry<ApkPackage, Integer>> list = new ArrayList<Map.Entry<ApkPackage, Integer>>(packageMap.entrySet());
        // 排序
        Collections.sort(list, valueComparator);
        // 默认情况下，TreeMap对key进行升序排序
        System.out.println("------------ map 按照 value 升序排序--------------------");
        for (Map.Entry<ApkPackage, Integer> entry : list) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    private Set<ApkPackage> extracPackageFromApk(File file) throws IOException {
        ApkFile apkFile = new ApkFile(file);

        ApkMeta apkMeta = apkFile.getApkMeta();
        String packageName = apkMeta.getPackageName();

        Set<ApkPackage> apkPackages = new HashSet<>();
        DexClass[] classes = apkFile.getDexClasses();
        for (DexClass dexClass : classes) {
            String clsPackage = dexClass.getPackageName();
            if (!clsPackage.startsWith(packageName)) {
                ApkPackage apkPackage = new ApkPackage();
                apkPackage.setPackageName(clsPackage);
                apkPackages.add(apkPackage);
            }
        }

        return apkPackages;
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