# 🌳 Focus Tree

A modern Android productivity app that transforms focus sessions into a rewarding visual experience. Complete Pomodoro sessions to grow beautiful virtual trees, earn rewards, and build a forest that reflects your consistency.

Built using **Kotlin**, **Jetpack Compose**, **Material Design 3**, and the **MVVM** architecture.

---

## ✨ Features

### 🌱 Grow Virtual Trees

Watch your tree grow in real time as your focus session progresses. Complete the session to add it permanently to your collection.

### ⏱️ Custom Focus Sessions

Choose from multiple Pomodoro durations:

* 15 Minutes
* 25 Minutes
* 45 Minutes
* 60 Minutes

### 🪙 Reward System

Stay consistent and earn coins after every successful session.

* 10 Coins per minute completed
* Rewards only for completed sessions

### 📊 Session History

Keep track of your productivity with detailed statistics.

* Completed sessions
* Failed sessions
* Trees grown
* Coins earned
* Focus history

### 🎨 Modern User Interface

Designed with Material Design 3 and Jetpack Compose.

* Smooth animations
* Clean interface
* Responsive layouts
* Beautiful tree growth visualization

### 💬 Motivational Quotes

Stay motivated throughout your focus sessions with inspirational quotes.

---

# 📱 Screenshots

> Add screenshots or GIFs here.

| Home       | Focus Session | History    |
| ---------- | ------------- | ---------- |
| Screenshot | Screenshot    | Screenshot |

---

# 🚀 Installation

## Prerequisites

* Android Studio Hedgehog (2023.1.1) or newer
* JDK 11+
* Android SDK 24+

## Clone the Repository

```bash
git clone https://github.com/<your-username>/FocusTree.git
cd FocusTree
```

Open the project in Android Studio, sync Gradle, and run the application on an emulator or Android device.

---

# 🎮 How It Works

## 1. Start a Focus Session

* Select a timer duration.
* Tap **Start Focus**.
* The virtual tree begins growing.

## 2. Stay Focused

* Pause when necessary.
* Resume anytime.
* Giving up before completion causes the tree to wither.

## 3. Complete the Session

Successfully finishing the timer will:

* 🌳 Grow a new tree
* 🪙 Reward coins
* 📈 Update your statistics

## 4. Review Your Progress

View your achievements from the History screen.

* Total Trees
* Total Coins
* Session Records
* Completion Rate

---

# 🏗️ Architecture

The application follows the **MVVM (Model-View-ViewModel)** architecture for maintainability and scalability.

```
Presentation Layer
│
├── Jetpack Compose UI
├── Material Design 3
└── ViewModels
        │
        ▼
Business Logic
│
├── Timer Logic
├── Reward System
├── Tree Growth Engine
└── Session Management
        │
        ▼
Data Layer
│
└── App State & Session Storage
```

---

# 🛠️ Tech Stack

| Technology        | Purpose                      |
| ----------------- | ---------------------------- |
| Kotlin            | Primary Programming Language |
| Jetpack Compose   | Declarative Android UI       |
| Material Design 3 | UI Components                |
| MVVM              | Application Architecture     |
| ViewModel         | State Management             |
| Kotlin Coroutines | Asynchronous Operations      |

---

# 📂 Project Structure

```
app/
│
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
│
├── viewmodel/
│
├── model/
│
├── utils/
│
└── MainActivity.kt
```

---

# 🎨 Design Philosophy

The application is designed around a simple idea:

> **The more you focus, the more your virtual forest grows.**

This transforms productivity into a visual and rewarding experience, encouraging users to build consistency rather than simply complete timers.

---

# 🚀 Future Improvements

Planned features include:

* 🌲 Multiple tree species
* 🏞️ Expandable forests
* ☁️ Cloud backup
* 🏆 Achievements & badges
* 📈 Advanced productivity analytics
* 🌙 Dark mode improvements
* 🔔 Smart notifications
* 📅 Calendar integration
* 🎵 Ambient focus sounds

---

# 🤝 Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a feature branch.

```bash
git checkout -b feature/new-feature
```

3. Commit your changes.

```bash
git commit -m "Add new feature"
```

4. Push to your branch.

```bash
git push origin feature/new-feature
```

5. Open a Pull Request.

---

# 📄 License

This project is licensed under the **MIT License**.

See the `LICENSE` file for more information.

---

# ⭐ Support

If you found this project useful, consider giving it a **Star** on GitHub. It helps others discover the project and supports future development.

---

## Built With

* ❤️ Kotlin
* 🌳 Jetpack Compose
* 🎨 Material Design 3
* ⏱️ Pomodoro Technique
