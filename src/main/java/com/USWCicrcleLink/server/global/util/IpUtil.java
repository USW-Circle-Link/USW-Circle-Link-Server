package com.USWCicrcleLink.server.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private IpUtil() {
    }

    /**
     * 클라이언트 IP 주소 가져오기 (프록시 환경 고려)
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 값이 여러 개일 경우 첫 번째 IP 사용
            return ip.split(",")[0].trim();
        }

        String[] headers = {
                "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr(); // 마지막으로 request에서 가져온 IP 반환
    }
}
