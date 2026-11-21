package com.club.common;

/**
 * JSP 输出转义，避免用户输入里的 &lt; &gt; 破坏页面。
 */
public class HtmlUtil {

    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
