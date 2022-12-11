package com.tartantransporttracker.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.tartantransporttracker.notification.Observer;
import com.tartantransporttracker.notification.Subject;

import java.util.ArrayList;
import java.util.List;

public class Route implements Subject {
    @DocumentId
    private String id;
    private String name;

    private List<User> students;



    public Route() {
        students = new ArrayList<>();
    }

    public Route(String routeName) {
        name = routeName;
    }

    public String getName() {
        return name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getStudents() {
        return students;
    }

    public void setStudents(List<User> _users) {
        students = _users;
    }

    @Override
    public void register(User std) {
       students.add(std);
        Log.w("Number of Students",String.valueOf(students.size()));
    }

    @Override
    public void unregister(User std) {
            students.remove(std);
    }

    @Override
    public void notifyObservers(String notification) {
        for (User std : students) {
            std.update(notification);
        }
    }

    @Override
    public String toString() {
        return name ;
    }
}
