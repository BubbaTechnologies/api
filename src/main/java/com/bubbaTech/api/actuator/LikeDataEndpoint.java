package com.bubbaTech.api.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Endpoint(id="likeData")
public class LikeDataEndpoint {

    static int MAX_DAY_COUNT = 30;

    private ArrayDeque<Integer> likesPerDay = new ArrayDeque<>();
    private ArrayDeque<Integer> recommendsPerDay = new ArrayDeque<>();

    private ArrayDeque<Map<String, Integer>> pageClickPerDay = new ArrayDeque<>();

    private LocalDate lastCurrentDate = LocalDate.now();

    @Async
    public void addLikeAndRecommend() {
        updateSize();

        //Adds like and recommend
        addOne(likesPerDay);
        addOne(recommendsPerDay);
    }

    @Async
    public void addRecommend() {
        updateSize();

        //Adds recommend
        addOne(recommendsPerDay);
    }

    @Async
    public void addPageClick(String name) {
        updateSize();

        Map<String, Integer> todayCount = pageClickPerDay.removeLast();
        int currentCount = 0;

        if (todayCount.containsKey(name)) {
            currentCount = todayCount.get(name);
        }

        todayCount.put(name, currentCount + 1);
        pageClickPerDay.addLast(todayCount);
    }

    public Map<String, ?> getLikeMetrics() {
        Map<String, Double> ratioMap = new HashMap<>();

        int currentSize = likesPerDay.size();

        //Last 30-Day Ratio
        if (currentSize >= 30) {
            ratioMap.put("30Day", getRatio(30));
        }

        //7-Day Ratio
        if (currentSize >= 7) {
            ratioMap.put("7Day", getRatio(7));
        }

        //Last Day
        if (currentSize >= 1) {
            ratioMap.put("1Day", getRatio(1));
        }

        return ratioMap;
    }

    @ReadOperation
    public Map<String, Map<String, Integer>> getPageClickMetrics() {
        Map<String, Map<String, Integer>> ratioMap = new HashMap<>();

        int currentSize = pageClickPerDay.size();

        //Last 30-Day Ratio
        if (currentSize >= 30) {
            ratioMap.put("30Day", getPageClicks(30));
        }

        //7-Day Ratio
        if (currentSize >= 7) {
            ratioMap.put("7Day", getPageClicks(7));
        }

        //Last Day
        if (currentSize >= 1) {
            ratioMap.put("1Day", getPageClicks(1));
        }

        return ratioMap;
    }

    private Map<String, Integer> getPageClicks(int dayCount) {
        int currentSize = pageClickPerDay.size();
        Map<String, Integer> pageClicksByStore = new HashMap<>();

        if (currentSize >= (dayCount - 1)) {
            int currentCount = 0;
            Iterator<Map<String, Integer>> iterator = pageClickPerDay.iterator();
            while(iterator.hasNext() && currentCount < dayCount) {
                currentCount += 1;
                Map<String, Integer> day = iterator.next();
                for (String name : day.keySet()) {
                    if (!pageClicksByStore.containsKey(name)) {
                        pageClicksByStore.put(name, 0);
                    }

                    Integer storeCurrentCount = pageClicksByStore.get(name);
                    pageClicksByStore.put(name, storeCurrentCount + day.get(name));
                }
            }
        }

        return pageClicksByStore;
    }

    private double getRatio(int dayCount) {
        int currentSize = likesPerDay.size();

        if (currentSize >= (dayCount - 1)) {
            int totalLikes = 0;
            int totalRecommends = 0;

            int currentCount = 0;
            Iterator<Integer> iterator = likesPerDay.iterator();
            while(iterator.hasNext() && currentCount < dayCount) {
                currentCount += 1;
                totalLikes += iterator.next();
            }

            currentCount = 0;
            iterator = recommendsPerDay.iterator();
            while(iterator.hasNext() && currentCount < dayCount) {
                currentCount += 1;
                totalRecommends += iterator.next();
            }

            return ((double) totalLikes) / ((double) totalRecommends);
        }

        return -1.0;
    }

    private void updateSize() {
        LocalDate currentDate = LocalDate.now();
        int currentSize = likesPerDay.size();

        if (!lastCurrentDate.isEqual(currentDate)) {
            if (currentSize > MAX_DAY_COUNT) {
                likesPerDay.removeFirst();
                recommendsPerDay.removeFirst();
                pageClickPerDay.removeFirst();
            }

            likesPerDay.addLast(0);
            recommendsPerDay.addLast(0);
            pageClickPerDay.addLast(new HashMap<>());
        } else if (currentSize == 0) {
            likesPerDay.addLast(0);
            recommendsPerDay.addLast(0);
            pageClickPerDay.addLast(new HashMap<>());
        }

        lastCurrentDate = currentDate;
    }

    private void addOne(ArrayDeque<Integer> queue) {
        int todaysCount = queue.removeLast();
        todaysCount += 1;

        queue.addLast(todaysCount);
    }
}
