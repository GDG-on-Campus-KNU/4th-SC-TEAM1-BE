<div align="center">  

# 2025 Google Developer Groups APAC Solution Challenge

![로고](./images/Todak_new_logo.png)

# AI-Powered Emotional Support Diary "Todak"
Todak(토닥) is an innovative, private diary service (Closed-Type SNS) where users can freely express their emotions while receiving AI-powered empathetic responses.

# [www.todak.site](https://www.todak.site)

<img src="images/Todak_img.png" width="300"/>

</div>
<br>

## Overview
In modern society, many people lack a safe and private space to express their emotions and receive emotional support. It's often difficult to openly share personal feelings with others, leading to emotional isolation and stress. Todak creates a warm, supportive environment where individuals can freely share their feelings and feel genuinely understood through AI-powered emotional support.

Our solution was developed for the APAC Solution Challenge to address this critical emotional well-being need.

The name "토닥" in Korean means to gently pat someone to offer comfort or emotional support - perfectly capturing the essence of our service.

## 🎥 Demo Video

[![Demo Video](https://img.youtube.com/vi/jZ-gx8eNw50/0.jpg)](https://www.youtube.com/watch?v=jZ-gx8eNw50)

## 🏗️ System Architecture
A web application architecture diagram showing a monolithic Spring Boot application deployed in multiple instances for rolling updates within a Kubernetes cluster, featuring client-side Vue.js and React, a shared MySQL database

![System Architecture Detailed](images/Todak_Web_Server_Architecture.png)

## 🖼️ Use Case Diagram
The following diagram illustrates the main interactions and relationships between users, AI friends, and the system

![Use Case Diagram](images/Todak_USECASE_Diagram.png)

## 🎯 Key Features

### ✍️ 1. Private Diary Writing  
Users can freely write and store their emotions in a closed, secure environment that encourages honest emotional expression without fear of judgment.  

<img width="800" alt="image" src="https://github.com/user-attachments/assets/4cb4d726-4619-45d2-8351-36716a105e74" />

---

### 🤖 2. AI Empathy Companion  
Our AI, "토닥이" reads diary entries and generates personalized, empathetic comments that feel like support from a warm, understanding friend. 

<img width="500" alt="image" src="https://github.com/user-attachments/assets/1687996b-c9e1-4adc-81e4-fa5cc102693a" />

---

### 🧬 3. MBTI-Based AI Responses  
AI provides empathetic and comforting comments based on the diary written by the user, based on one of the 16 MBTI personality types.  

<img width="600" alt="image" src="https://github.com/user-attachments/assets/fe8b1bbd-389f-4f2d-8e90-dce5f82e5d2b" />

---

### 🕵️‍♀️ 4. Anonymous Comment Function  
Based on the diary written by the user, users registered as friends and AI can comment. All comments by users and AI are provided anonymously, and anonymity can be deactivated through points.  

<img width="500" alt="image" src="https://github.com/user-attachments/assets/127f414e-f5dc-499d-90d2-78d738817d42" />

---

### 🌱 5. Emotion Growth Visualization  
A virtual tree on the main screen grows as users consistently write in their diary, symbolizing emotional growth and encouraging continuous engagement through visual feedback.  

<img width="800" alt="image" src="https://github.com/user-attachments/assets/88f0cc7d-002b-48d6-a792-53ab7a518155" />

---

### 👫 6. Friend Features  
Users can access friends' diaries and guestbooks (with permission) and write comments to support each other, creating a community of emotional support.  

<img width="800" alt="image" src="https://github.com/user-attachments/assets/269ade79-80c3-4264-9f3f-8eae55a18041" />

---

### 💎 7. Points System  
Users can earn points by checking in daily, writing in a journal, and leaving comments, which can be used to grow the tree and unlock visual progress milestones. Anonymous comments can also be unlocked.  

<img width="800" alt="image" src="https://github.com/user-attachments/assets/d48839da-a0ed-4dd7-a721-99a81cde47d0" />

---

### 📡 8. Real-Time Notifications  
The system alerts users when someone comments on their diary or guestbook, implemented using lightweight Server-Sent Events (SSE) and Redis Streams for efficient real-time communication.  

<img width="800" alt="image" src="https://github.com/user-attachments/assets/75706544-4125-46a2-ad36-4a5c96cfba0d" />

---

### 🔧 9. Admin Dashboard  
Built with Thymeleaf, the admin dashboard offers dynamic filtering and management by user, date, and status for effective platform oversight.  

<img width="800" alt="image" src="https://github.com/user-attachments/assets/f5867cdc-ba3e-47fd-a54d-94764f36ebef" />

---

### 📊 10. Monitoring and Logging  
The backend is monitored using Prometheus and Grafana, with error logs stored per user for analysis and stability improvement.  

<img width="800" alt="image" src="https://github.com/user-attachments/assets/9cedadc1-733a-45f5-a9a5-a93629a78fb7" />

---

## 📁 Repository

### [👉 FE Github Repository](https://github.com/GDG-on-Campus-KNU/4th-SC-TEAM1-FE)
### [👉 BE Github Repository](https://github.com/GDG-on-Campus-KNU/4th-SC-TEAM1-BE)

## 🛠️ Tech Spec
### 💻 Frontend
![Frontend Technology Stack](images/Todak_Frontend_Tech_Spec.png)

### ⚙️ Backend
![Backend Technology Stack](images/Todak_Backend_Tech_Spec.png)

### 📊 Monitoring
![Monitoring Technology Stack](images/Todak_Monitoring.png)

## 🗃️ ERD
Server Entity Relationship Diagram

![ERD](images/Todak_ERD.png)

## 👥 Team Todak
This project was developed by **Team Todak** to participate in the **APAC Solution Challenge**.
We aim to create a warm service that provides emotional support and emotional stability through technology.
