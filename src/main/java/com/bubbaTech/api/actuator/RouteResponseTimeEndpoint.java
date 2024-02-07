/**
 * Author: Matthew Groholski
 * Date: 11/04/23
 * Description: Provides an endpoint to receive average response time of each endpoint.
 */

package com.bubbaTech.api.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Endpoint(id="routeResponseTime")
public class RouteResponseTimeEndpoint {

    /**
     * Key-value pair where the key is the route and the value is a queue of MAX_AMOUNT most recent response times.
     */
    private final ConcurrentHashMap<String, List<Long>> routeResponseTime = new ConcurrentHashMap<>();

    static int MAX_AMOUNT = 100;

    /**
     * @param route: String value of the route.
     * @param responseTime: Amount of time taken to process route.
     */
    @Async
    public void addResponseTime(String route, Long responseTime) {
        //Checks if route is within list
        List<Long> longList;
        if (!routeResponseTime.containsKey(route)) {
            longList = new ArrayList<>();
        } else {
            longList = routeResponseTime.get(route);
        }


        //Amount of values to include within the average.
        if (longList.size() + 1 > RouteResponseTimeEndpoint.MAX_AMOUNT) {
            longList.remove(0);
        }

        longList.add(responseTime);
        routeResponseTime.put(route, longList);
    }

    /**
     * @return: Key-value pairs where the key represents the route and value represents average time.
     */
    @ReadOperation
    public Map<String, String> getAverageResponseTimes() {
        Map<String, String> averageResponseTimes = new HashMap<>();

        for (Map.Entry<String, List<Long>> entry : routeResponseTime.entrySet()) {
            String route = entry.getKey();
            List<Long> responseTimes = entry.getValue();

            if (!responseTimes.isEmpty()) {
                double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                averageResponseTimes.put(route, (average / 1000.0) + " seconds");
            }
        }

        return averageResponseTimes;
    }
}
