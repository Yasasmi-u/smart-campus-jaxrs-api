/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

/**
 *
 * @author ASUS
 */
public class Main {

    public static void main(String[] args) {
        URI uri = URI.create("http://localhost:8080/api/v1/");

        ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);

        System.out.println("Server running at http://localhost:8080/api/v1");
    }
}
