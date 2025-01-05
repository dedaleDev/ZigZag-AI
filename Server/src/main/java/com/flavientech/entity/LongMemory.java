package com.flavientech.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "longmemory")
public class LongMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(length = 60)
    private String summary;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "LongMemory{" +
                "id=" + id +
                ", user=" + user +
                ", date=" + date +
                ", summary='" + summary + '\'' +
                '}';
    }
}