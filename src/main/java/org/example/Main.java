package org.example;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            responseStream.write((
                    "HTTP/1.1 200 OK \r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            responseStream.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
        });

        server.run(9999);
    }
}