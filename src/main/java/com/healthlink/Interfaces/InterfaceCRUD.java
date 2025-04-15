package com.healthlink.Interfaces;

import java.util.List;

public interface InterfaceCRUD <T>{
    public void add(T t);
    public void update(T t);
    public void delete(T t);
    public List<T> find();
}