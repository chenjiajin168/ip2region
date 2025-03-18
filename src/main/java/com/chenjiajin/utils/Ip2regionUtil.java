package com.chenjiajin.utils;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * IP解析
 *
 * @author chenjiajin
 */
@Component
public class Ip2regionUtil {


    private static Searcher searcher;

    @PostConstruct
    private static void initIp2RegionByCache() {
        try {
            InputStream inputStream = new ClassPathResource("/static/ip2region.xdb").getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            searcher = Searcher.newWithBuffer(bytes);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    /**
     * 获取 ip 所属地址
     *
     * @return 广东
     */
    public static String getIpRegionByCache(String ip) {
        boolean isIp = RequestUtil.checkIp(ip);
        if (isIp) {
            try {
                // searchIpInfo 的数据格式： 国家|区域|省份|城市|ISP
                String searchIpInfo = searcher.search(ip);
                String[] splitIpInfo = searchIpInfo.split("\\|");
                if (splitIpInfo.length > 0) {
                    if ("中国".equals(splitIpInfo[0])) {
                        // 国内属地返回省份
                        return splitIpInfo[2].replace("省", "");
                    } else if ("0".equals(splitIpInfo[0])) {
                        if ("内网IP".equals(splitIpInfo[4])) {
                            // 内网 IP
                            return splitIpInfo[4];
                        } else {
                            return "未知";
                        }
                    } else {
                        // 国外属地返回国家
                        return splitIpInfo[0];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  // throw new IllegalArgumentException("非法的IP地址");

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "内网IP";
        }
        if ("127.0.0.1".equals(ip)) {
            return "内网IP";
        }
        return "未知";
    }

    /**
     * 获取 ip 所属地址
     *
     * @return 广东省深圳市
     */
    public static String getIpRegionByCacheDetail(String ip) {
        boolean isIp = RequestUtil.checkIp(ip);
        if (isIp) {
            try {
                String searchIpInfo = searcher.search(ip);
                String[] splitIpInfo = searchIpInfo.split("\\|");
                // searchIpInfo 的数据格式： 国家|区域|省份|城市|ISP
                // 元数据  ["中国", "0", "广东省", "深圳市", "阿里云"]
                // 元数据  ["中国", "0", "广东省", "广州市", "电信"]
                // 元数据  ["加拿大", "0", "安大略", "0", "0"]

                //System.err.println(Arrays.toString(splitIpInfo));
                if (splitIpInfo.length > 0) {
                    if ("中国".equals(splitIpInfo[0])) {
                        // 国内属地返回省份
                        return splitIpInfo[2] + " " + splitIpInfo[3];
                    } else if ("0".equals(splitIpInfo[0])) {
                        if ("内网IP".equals(splitIpInfo[4])) {
                            // 内网 IP
                            return splitIpInfo[4];
                        } else {
                            return "未知";
                        }
                    } else {
                        // 国外属地返回国家
                        return splitIpInfo[0];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  // throw new IllegalArgumentException("非法的IP地址");

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "内网IP";
        }
        if ("127.0.0.1".equals(ip)) {
            return "内网IP";
        }
        return "未知";
    }


    /**
     * 获取 ip 所属城市
     *
     * @return 深圳市
     */
    public static String getIpCityByCacheDetail(String ip) {
        boolean isIp = RequestUtil.checkIp(ip);
        if (isIp) {
            try {
                // searchIpInfo 的数据格式： 国家|区域|省份|城市|ISP
                // 元数据  ["中国", "0", "广东省", "深圳市", "阿里云"]
                String searchIpInfo = searcher.search(ip);
                String[] splitIpInfo = searchIpInfo.split("\\|");
                if (splitIpInfo.length > 0 && "中国".equals(splitIpInfo[0])) {
                    return splitIpInfo[3];
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }


    /**
     * 获取 ip 所属地址
     *
     * @return 广东省深圳市
     */
    public static List<String> getIpRegionByCacheDetailList(List<String> ipList) {

        List<String> ipHomeLocationList = new ArrayList<>();

        for (String ip : ipList) {
            boolean isIp = RequestUtil.checkIp(ip);
            String ipHomeLocation = null;
            if (isIp) {
                try {
                    String searchIpInfo = searcher.search(ip);
                    String[] splitIpInfo = searchIpInfo.split("\\|");
                    if (splitIpInfo.length > 0) {
                        if ("中国".equals(splitIpInfo[0])) {
                            ipHomeLocation = splitIpInfo[2] + " " + splitIpInfo[3];
                        } else if ("0".equals(splitIpInfo[0])) {
                            if ("内网IP".equals(splitIpInfo[4])) {
                            } else {
                                ipHomeLocation = "未知";
                            }
                        } else {
                            ipHomeLocation = splitIpInfo[0];
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ipHomeLocation = "内网IP";
            }
            if ("127.0.0.1".equals(ip)) {
                ipHomeLocation = "内网IP";
            }
            if (ipHomeLocation == null) {
                ipHomeLocation = "内网IP";
            }
            ipHomeLocationList.add(ipHomeLocation);
        }
        return ipHomeLocationList;


    }

    public static String getIpRegionByCache() {
        return getIpRegionByCache(RequestUtil.getIPAddress());
    }

    /**
     * 在服务启动时，将 ip2region 加载到内存中
     * 1、完全基于文件的查询:  (较慢)
     *      searcher = Searcher.newWithFileOnly(dbPath);
     *      searcher.search(ip);
     * 2、缓存 VectorIndex 索引: (中等)
     *      vIndex = Searcher.loadVectorIndexFromFile(dbPath);
     *      searcher = Searcher.newWithVectorIndex(dbPath, vIndex);
     * 3、缓存整个 xdb 数据:  (较快)
     *      cBuff = Searcher.loadContentFromFile(dbPath);
     *      searcher = Searcher.newWithBuffer(cBuff);
     *      String region = searcher.search(ip);
     */
    public static void main(String[] args) {
        String ip = "8.134.74.170";
        //String ip = "142.171.44.180";
        String ipRegion = Ip2regionUtil.getIpRegionByCacheDetail(ip);
        System.err.println(ipRegion);

    }


}
