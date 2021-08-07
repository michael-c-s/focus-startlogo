package com.assessbyphone.zambia.Models;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.DataQueryBuilder;

import java.io.Serializable;
import java.util.List;

public class Students implements Serializable {
    private String firstName;
    private String sex;
    private String lastName;
    private String objectId;
    private String upn;
    private List<PhonicsResults> phonicsResults;
    private List<MathsResults> mathsResults;

    public static Students findById(String id) {
        return Backendless.Data.of(Students.class).findById(id);
    }

    public static List<Students> find(DataQueryBuilder queryBuilder) {
        return Backendless.Data.of(Students.class).find(queryBuilder);
    }

    public static void findAsync(DataQueryBuilder queryBuilder, AsyncCallback<List<Students>> callback) {
        Backendless.Data.of(Students.class).find(queryBuilder, callback);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getUpn() {
        return upn;
    }

    public void setUpn(String upn) {
        this.upn = upn;
    }

    public List<PhonicsResults> getResults() {
        return phonicsResults;
    }

    public void setResults(List<PhonicsResults> results) {
        this.phonicsResults = results;
    }

    public List<MathsResults> getMathsResults() {
        return mathsResults;
    }

    public void setMathsResults(List<MathsResults> mathsResults) {
        this.mathsResults = mathsResults;
    }

    public Students save() {
        return Backendless.Data.of(Students.class).save(this);
    }

    public void saveAsync(AsyncCallback<Students> callback) {
        Backendless.Data.of(Students.class).save(this, callback);
    }

    public Long remove() {
        return Backendless.Data.of(Students.class).remove(this);
    }
}