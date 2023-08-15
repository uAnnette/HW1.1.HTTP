package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    public static Map getQueryParam(String url) {
        Map res = new HashMap<>();
        List<NameValuePair> params;
        try {
            params = URLEncodedUtils.parse(new URI(url), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName() != null && param.getValue() != null)
                    res.put(param.getName(), param.getValue());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String getQueryParams(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        String result = null;
        int count = 0;
        for (int i = 0; i < name.length(); i++) {
            result = String.valueOf(stringBuilder.append(name.charAt(i)));
            if (name.charAt(i) == '?'){
                count++;
                i = name.length();
            }
        }
        if (count == -1) {
            return name;
        }
        return result;
    }
}
