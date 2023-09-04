package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    protected final List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html",
            "/forms.html", "/classic.html", "/events.html", "/events.js");
    protected ExecutorService service = Executors.newFixedThreadPool(64);
    protected final ConcurrentHashMap<String, Map<String, Handler>> handlers;

    public Server() {
        handlers = new ConcurrentHashMap<>();
    }

    public void run(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                final var socket = serverSocket.accept();
                service.execute(() -> connection(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            service.shutdown();
        }
    }

    public void connection(Socket socket) {
        try (
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = Request.create(in);

            if (request == null || !handlers.containsKey(request.getMethod())) {
                out.write((
                        "HTTP/1.1 400 Bad request\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                return;
            } else {
                System.out.println("Информация: ");
                System.out.println("METHOD: " + request.getMethod());
                System.out.println("PATH: " + request.getPath());
                System.out.println("HEADERS: " + request.getHeaders());
                System.out.println("Query Params: ");
                for (var param : request.getQueryParams()) {
                    System.out.println(param.getName() + " = " + param.getValue());
                }
                System.out.println("Test for name: ");
                System.out.println(request.getQueryParam(request.getQueryParams().toString()).getName());
            }

            Map<String, Handler> handlerMap = handlers.get(request.getMethod());
            String requestPath = request.getPath().split("\\?")[0];

            if (handlerMap.containsKey(requestPath)) {
                Handler handler = handlerMap.get(requestPath);
                handler.handle(request, out);
            } else {
                if (!validPaths.contains(requestPath)) {
                    out.write((
                            "HTTP/1.1 404 Not found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                } else {
                    final var filePath = Path.of(".", "public", requestPath);
                    final var mimeType = Files.probeContentType(filePath);

                    if (requestPath.startsWith("/classic.html")) {
                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        out.write(content);
                        return;
                    }

                    final var length = Files.size(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, out);
                    out.flush();
                }
            }
        } catch (IOException |
                 URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    protected void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);
    }
}