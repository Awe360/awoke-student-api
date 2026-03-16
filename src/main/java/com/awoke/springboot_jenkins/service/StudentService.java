package com.awoke.springboot_jenkins.service;

import com.awoke.springboot_jenkins.entity.Student;
import com.awoke.springboot_jenkins.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student student) {
        if (!studentRepository.existsById(id)) {
            throw new NoSuchElementException("Student not found with id: " + id);
        }
        student.setId(id);
        return studentRepository.save(student);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + id));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public void deleteStudent(Long id) {
        boolean removed = studentRepository.deleteById(id);
        if (!removed) {
            throw new NoSuchElementException("Student not found with id: " + id);
        }
    }
}