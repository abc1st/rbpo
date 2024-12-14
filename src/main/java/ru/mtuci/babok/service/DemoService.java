package ru.mtuci.babok.service;

import ru.mtuci.babok.model.Demo;

import java.util.List;

public interface DemoService {
    void save(Demo demo);
    List<Demo> findAll();
    Demo findById(long id);
}
