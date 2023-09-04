package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Request {
    protected static final String GET = "GET";
    protected static final String POST = "POST";
    protected final String method;
    protected final String path;
    protected final List<String> headers;
    protected List<NameValuePair> params;

    public Request(String method, String path, List<String> headers, List<NameValuePair> params) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.params = params;
    }

    public static Request create(BufferedInputStream in) throws IOException, URISyntaxException {
        final var allowedMethods = List.of(GET, POST);

        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

        if (requestLineEnd == -1) {
            return null;
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            return null;
        }

        final var path = requestLine[1];

        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        in.reset();
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

        return new Request(method, path, headers, params);
    }

    public List<NameValuePair> getQueryParams() {
        return params;
    }

    public NameValuePair getQueryParam(String name) {
        return getQueryParams().stream()
                .filter(param -> param.getName().equalsIgnoreCase(name))
                .findFirst().orElse(new NameValuePair() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String getValue() {
                        return null;
                    }
                });
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }
}