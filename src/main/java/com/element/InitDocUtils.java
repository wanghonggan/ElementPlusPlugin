package com.element;

import com.element.document.DocumentConstant;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Properties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author WangHG
 * @date 23/4/24
 */

public class InitDocUtils {
    public static void main(String[] args) {
        String html = doGet(getUrl(DocumentConstant.ELEMENT_PLUS_COMPONENT_URL, DocumentConstant.INIT_COMPONENT));
        Document doc = Jsoup.parse(html);
        Elements elementsByClass = doc.getElementsByClass("sidebar-groups");

        List<String> componentList = new ArrayList<>();
        elementsByClass.forEach(elem -> {
            Elements allElements = elem.getElementsByTag("A");
            allElements.forEach(element -> componentList.add(element.attributes().get("href")));
        });

        //创建出Properties对象，并存入要写入的键值对
        Properties properties = new Properties();
        componentList.forEach(component -> {
            String url = getUrl(DocumentConstant.ELEMENT_PLUS_COMPONENT_URL, component);
            String componentHtml = doGet(url);
            Document componentDoc = Jsoup.parse(componentHtml);
            Element docElement = componentDoc.getElementsByClass("doc-content").get(0).getElementsByTag("div").get(0);
            Elements vpTable = docElement.getElementsByClass("vp-table");
            Elements vpTableParent = Objects.requireNonNull(docElement.children().first()).children();
            String aUrl = "<a href='" + url + "' target='_target'>" + url + "</a>\n";
            vpTable.forEach(element -> {
                String id = vpTableParent.get(element.elementSiblingIndex() - 1).attributes().get("id");
                String[] strings = id.split("-");
                String key = "el" + String.join("", Arrays.copyOfRange(strings, 0, strings.length - 1));
                String propertyVal = properties.getProperty(key);
                properties.put(key, propertyVal == null ? aUrl + element.html().trim() : propertyVal + "\n" + element.html().trim());
            });

        });

        //创建出输出流对象
        try (BufferedOutputStream bos1 = new BufferedOutputStream(Files.newOutputStream(Paths.get("src/main/resources/element-tips.properties")))) {
            //调用store()方法，通过输出流将键值对写入文件
            properties.store(bos1, "just do it!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getUrl(String url, String b) {
        return url + "/" + b + ".html";
    }

    /**
     * Http get请求
     *
     * @param httpUrl 连接
     * @return 响应数据
     */

    public static String doGet(String httpUrl) {

        StringBuilder result = new StringBuilder();
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(httpUrl).openConnection();
            //设置请求方式
            connection.setRequestMethod("GET");
            //设置连接超时时间
            connection.setReadTimeout(15000);
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //开始连接
            connection.connect();
            try (InputStream is = connection.getInputStream(); BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                //获取响应数据
                if (connection.getResponseCode() == 200) {
                    //获取返回的数据
                    String temp;
                    while (null != (temp = br.readLine())) {
                        result.append(temp);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //关闭远程连接
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result.toString();
    }
}
