package com.qiusuo.demo;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public class SignDemo {
    private static String accessId       = "uyupwpw758482302mjdksdkd";
    private static String accessPassword = "jjgjsll9934=5868993243-fg";

    // 通用字段 accessId, time, version, sign
    // 业务数据

    // Step 1:  创建accessId, accessPassword
    // Step 2:  构建一个待加签字符串。包含所有业务数据
    // Step 3:  使用Hash函数对Step 2中的数据进行哈希处理
    // Step 4：     把Step 3中的数据加上请求之上

    /**
     * 对Get Http请求签名
     * 
     * @param url
     * @param accessId
     * @param version
     * @return
     */
    public static URL signUrl(URL url, String accessId, String accessPassword, String version) {
        Preconditions.checkArgument(url != null);
        Preconditions.checkArgument(accessId != null);
        Preconditions.checkArgument(accessPassword != null);
        Preconditions.checkArgument(version != null);

        // 构建待加签字符串
        String path = blankToDefault(url.getPath(), "/");
        String queryParams = blankToDefault(url.getQuery(), "");
        String time = System.currentTimeMillis() + "";
        String newQueryParams = buildQueryParams(queryParams, accessId, time, version);
        String toSigned = path + "?" + newQueryParams;

        // 加签
        String passwordHashed = hash(accessPassword);
        String sign = hash(toSigned + passwordHashed);
        String newUrl = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "?" + newQueryParams
                + "&sign=" + sign;

        try {
            return new URL(newUrl);
        } catch (Exception e) {
            throw new RuntimeException("加签发生异常");
        }
    }

    private static String buildQueryParams(String queryParams, String accessId, String time, String version) {
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isBlank(queryParams)) {
            stringBuilder.append("accessId=" + accessId);
        } else {
            stringBuilder.append(queryParams);
            stringBuilder.append("&accessId=" + accessId);
        }
        stringBuilder.append("&time=" + time);
        stringBuilder.append("&version=" + version);
        return stringBuilder.toString();
    }

    /**
     * 用默认值替换空值
     */
    public static String blankToDefault(String originValue, String defaultValue) {
        if (StringUtils.isBlank(originValue)) {
            return defaultValue;
        } else {
            return originValue;
        }
    }

    /**
     * 对一个字符串进行SHA256处理
     * 
     * @param input 待哈希的字符串
     * @return HEX字符串
     */
    public static String hash(String input) {
        Preconditions.checkArgument(input != null);

        String output = null;
        try {
            byte[] inputBytes = input.getBytes("UTF-8");
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(inputBytes);
            byte[] outputBytes = messageDigest.digest();

            StringBuilder stringBuilder = new StringBuilder();
            for (byte item : outputBytes) {
                stringBuilder.append(Character.forDigit((item >> 4) & 0xF, 16));
                stringBuilder.append(Character.forDigit(item & 0xF, 16));
            }
            output = stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("输入数据有误，无法UTF8编码");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法找到SHA-256哈希算法");
        }

        return output;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(hash("143534"));

        String url = "http://10.139.53.231:10018/v1/examination/hospitals/citysflat?productCode=10005";
        URL newUrl = signUrl(new URL(url), accessId, accessPassword, "v1");
        System.out.println(newUrl.toString());
    }
}
