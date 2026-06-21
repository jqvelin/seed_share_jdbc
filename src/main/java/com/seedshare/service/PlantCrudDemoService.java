package com.seedshare.service;

import com.seedshare.dao.PlantDao;
import com.seedshare.model.Plant;

import java.sql.SQLException;
import java.util.List;

public class PlantCrudDemoService {

    private static final PlantDao plantDao = new PlantDao();

    public static void run() {
        long suffix = System.currentTimeMillis() % 100000;
        Plant plant = new Plant("Кабачок " + suffix);
        try {
            int plantId = plantDao.insert(plant);
            System.out.println("Создано растение с id " + plantId);

            listAllPlants();

            plant.setName("Кабачок улучшенный " + suffix);
            plantDao.update(plant);
            System.out.println("Название растения обновлено");

            listAllPlants();

            plantDao.delete(plantId);
            System.out.println("Растение удалено");

            listAllPlants();
        } catch (SQLException e) {
            System.out.println("При работе с растениями возникла ошибка: " + e.getMessage());
        }
    }

    public static void listAllPlants() throws SQLException {
        List<Plant> plants = plantDao.findAll();
        for (Plant plant : plants) {
            System.out.println(plant.getId() + ": " + plant.getName());
        }
    }
}
