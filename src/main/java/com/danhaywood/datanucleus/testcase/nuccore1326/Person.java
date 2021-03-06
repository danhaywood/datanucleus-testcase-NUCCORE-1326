package com.danhaywood.datanucleus.testcase.nuccore1326;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PrimaryKey;

@javax.jdo.annotations.PersistenceCapable(
        identityType= IdentityType.APPLICATION,
        schema = "people",
        table = "Person")
public class Person {

    public Person(){}

    public Person(long id)
    {
        this(id, null);
    }
    public Person(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @PrimaryKey
    Long id;
    public Long getId()
    {
        return id;
    }

    String name;
    public String getName()
    {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


    private Person manager;

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(name="managerId", allowsNull="true")
    public Person getManager() {
        return manager;
    }

    public void setManager(final Person manager) {
        this.manager = manager;
    }
}
