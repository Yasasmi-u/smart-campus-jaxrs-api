/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 *
 * @author ASUS
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET all sensors (with optional filter)
    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {

        if (type != null) {
            return DataStore.sensors.values()
                    .stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return DataStore.sensors.values();
    }

    // POST create sensor
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {

        // Validate input
        if (sensor == null || sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Sensor ID is required")
                    .build();
        }

        // Check if room exists
        Room room = DataStore.rooms.get(sensor.getRoomId());

        if (room == null) {
            throw new LinkedResourceNotFoundException("Invalid roomId - room does not exist");
        }

        // Save sensor
        DataStore.sensors.put(sensor.getId(), sensor);

        // Link sensor to room
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED)
        .entity(sensor)
        .header("Location", "/api/v1/sensors/" + sensor.getId())
        .build();
    }
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
