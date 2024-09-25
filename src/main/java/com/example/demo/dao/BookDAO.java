package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.vo.Book;

public interface BookDAO extends JpaRepository<Book, String> {

}
