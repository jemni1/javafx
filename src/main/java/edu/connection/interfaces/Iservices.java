package edu.connection.interfaces;
import java.util.List;
public interface Iservices <T>{
    public abstract void addEntity(T t);
    public abstract void updateEntity(T t,int id);
    public abstract void deleteEntity(T t);

}
