package com.awoke.springboot_jenkins.entity;

public class Student {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String major;

    public Student() {
    }

    public Student(Long id, String firstName, String lastName, String email, Integer age, String major) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.major = major;
    }

    // Getters and Setters
    public Long getId()          { return id; }
    public void setId(Long id)   { this.id = id; }

    public String getFirstName()       { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName()        { return lastName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }

    public String getEmail()           { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge()            { return age; }
    public void setAge(Integer age)    { this.age = age; }

    public String getMajor()           { return major; }
    public void setMajor(String major) { this.major = major; }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", major='" + major + '\'' +
                '}';
    }
}