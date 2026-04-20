/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 *
 * @author ASUS
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println(
                "[" + LocalDateTime.now() + "] Incoming Request: "
                        + requestContext.getMethod() + " "
                        + requestContext.getUriInfo().getRequestUri()
        );
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        System.out.println(
                "[" + LocalDateTime.now() + "] Outgoing Response: Status "
                        + responseContext.getStatus()
        );
    }
}