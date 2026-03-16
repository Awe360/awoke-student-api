package com.awoke.springboot_jenkins.repository;

import com.awoke.springboot_jenkins.entity.Student;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class StudentRepository {

    private final List<Student> students = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostConstruct
    public void init() {
        save(new Student(null, "Abebe", "Kebede", "abebe.kebede@example.com", 21, "Computer Science"));
        save(new Student(null, "Sara", "Alemu", "sara.alemu@example.com", 22, "Electrical Engineering"));
        save(new Student(null, "Dawit", "Tadesse", "dawit.tadesse@example.com", 20, "Mechanical Engineering"));
        save(new Student(null, "Hanna", "Girma", "hanna.girma@example.com", 23, "Software Engineering"));
        save(new Student(null, "Yonas", "Mekonnen", "yonas.mekonnen@example.com", 19, "Civil Engineering"));
    }

    public Student save(Student student) {
        if (student.getId() == null) {
            // Create
            student.setId(idGenerator.incrementAndGet());
            students.add(student);
        } else {
            // Update → remove old + add new
            students.removeIf(s -> s.getId().equals(student.getId()));
            students.add(student);
        }
        return student;
    }

    public List<Student> findAll() {
        return new ArrayList<>(students);
    }

    public Optional<Student> findById(Long id) {
        return students.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public boolean deleteById(Long id) {
        return students.removeIf(s -> s.getId().equals(id));
    }

    public boolean existsById(Long id) {
        return students.stream().anyMatch(s -> s.getId().equals(id));
    }
}