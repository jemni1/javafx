package edu.connexion3a41.interfaces;

import java.util.List;

public interface IService<T> {

    public abstract void addEntity(T t);
    public abstract void deleteEntity(T t);
    public abstract void updateEntity(T t,int id);
    public abstract List<T> getAllData();

}
