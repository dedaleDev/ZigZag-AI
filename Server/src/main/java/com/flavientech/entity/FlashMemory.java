package com.flavientech.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "flashmemory")
public class FlashMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Column(length = 500)
    private String request;

    @Column(length = 5000)
    private String answer;

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "FlashMemory{" +
                "id=" + id +
                ", user=" + user +
                ", request='" + request + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}