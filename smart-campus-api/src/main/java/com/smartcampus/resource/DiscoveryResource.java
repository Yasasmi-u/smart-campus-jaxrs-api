/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ASUS
 */
@Path("")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", "v1");
        response.put("message", "Smart Campus API running");
        return response;
    }
}