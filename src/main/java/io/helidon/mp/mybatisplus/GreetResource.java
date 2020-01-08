/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.mp.mybatisplus;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.helidon.mp.mybatisplus.common.config.GlobalConfig;
import io.helidon.mp.mybatisplus.entity.Employee;
import io.helidon.mp.mybatisplus.facade.EmployeeFacade;

/**
 * A simple JAX-RS resource to greet you. Examples:
 * <p>
 * Get default greeting message: curl -X GET http://localhost:8080/greet
 * <p>
 * Get greeting message for Joe: curl -X GET http://localhost:8080/greet/Joe
 * <p>
 * Change greeting curl -X PUT -H "Content-Type: application/json" -d
 * '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting
 * <p>
 * The message is returned as a JSON object.
 */
@Path("/greet")
@RequestScoped
public class GreetResource {

	private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

	/**
	 * The greeting message provider.
	 */
	private final GreetingProvider greetingProvider;

	@Inject
	EmployeeFacade employeeFacade;

	/**
	 * Using constructor injection to get a configuration property. By default this
	 * gets the value from META-INF/microprofile-config
	 *
	 * @param greetingConfig the configured greeting message
	 */
	@Inject
	public GreetResource(GreetingProvider greetingConfig, @Named("mybatisplus") DataSource ds) {
		this.greetingProvider = greetingConfig;
		GlobalConfig.ds = ds;
	}

	/**
	 * Return a wordly greeting message.
	 *
	 * @return {@link JsonObject}
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getDefaultMessage() {
		return createResponse("World");
	}

	/**
	 * Return a greeting message using the name that was provided.
	 *
	 * @param name the name to greet
	 * @return {@link JsonObject}
	 */
	@Path("/{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getMessage(@PathParam("name") String name) {
		return createResponse(name);
	}

	/**
	 * Set the greeting to use in future messages.
	 *
	 * @param jsonObject JSON containing the new greeting
	 * @return {@link Response}
	 */
	@Path("/greeting")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateGreeting(JsonObject jsonObject) {

		if (!jsonObject.containsKey("greeting")) {
			JsonObject entity = JSON.createObjectBuilder().add("error", "No greeting provided").build();
			return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
		}

		String newGreeting = jsonObject.getString("greeting");

		greetingProvider.setMessage(newGreeting);
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	private JsonObject createResponse(String who) {
		String msg = String.format("%s %s!", greetingProvider.getMessage(), who);

		return JSON.createObjectBuilder().add("message", msg).build();
	}

	@Path("/employees")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Employee> listEmployees() {

		List<Employee> employees = employeeFacade.selectEmployeeByExample();
		employees.forEach(v -> {
			System.out.println(v.getEmployeeId() + ":" + v.getLastName() + " " + v.getFirstName());
		});

		Employee employee = new Employee();
		employeeFacade.insertEmployee(employee);

		return employees;
	}
}
