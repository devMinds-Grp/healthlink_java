package com.healthlink.Interfaces;

import java.util.List;

public interface InterfaceCRUD<T> {
    void add(T t);
//    void addMedecin(T t);
//    void addSoignant(T t);
//    void addPatient(T t);
    void update(T t);
    void delete(T t);
//    List<T> find();
//    List<User> findAllPatients();
//    List<User> findAllMedecins();
//    List<User> findAllSoignants();
    public List<T> find();

}