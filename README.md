# 🌾 Raitha-Varta — Farmer's Voice

> An AI-powered, offline-first agricultural assistant built to empower Karnataka farmers with real-time guidance, multilingual accessibility, and smart farming insights.

---

## 📖 Overview

**Raitha-Varta** bridges the gap between traditional agriculture and intelligent digital assistance. Built for rural environments with unreliable connectivity, the app delivers crop-specific farming tips, AI-generated recommendations, localized agricultural insights, and an interactive chatbot named **Mitra AI** — all without requiring a constant internet connection.

Built with modern Android technologies: **Jetpack Compose**, **MVVM Architecture**, **Room Database**, and **Google Gemini AI**.

---

## ✨ Features

###  Mitra AI Assistant
- Powered by **Google Gemini 1.5 Flash**
- Instant guidance on pest control, fertilizer usage, irrigation, and soil health
- Bilingual support: **English** and **ಕನ್ನಡ (Kannada)**
- Context-aware responses based on your region, soil type, and location

###  Offline-First Experience
- Farming tips stored locally via **Room Database**
- Core features work fully without internet
- AI-generated tips cached for future offline access

###  Smart Farming Tips
Supported crops: Paddy 🌾 · Coconut 🥥 · Tomato 🍅 · Areca Nut 🌴

- Swipeable tip cards
- Success stories from farmers
- AI-generated insights per crop
- Category-based recommendations

###  Intelligent Region Profiling
- GPS-based location detection
- AI-assisted classification: **Malnad**, **Coastal**, and **Dry** regions
- Soil-aware farming recommendations

###  Voice Input
- Ask queries via voice using Android **SpeechRecognizer**
- Supports both Kannada and English

###  Dynamic Bilingual UI
- Instant language switching
- Entire UI updates reactively in real time

###  Personalized Experience
- Bookmark important tips
- Session management
- Personalized greeting experience

---

##  Architecture

Raitha-Varta follows the **MVVM (Model–View–ViewModel)** pattern with a reactive, offline-first data flow.

```
Jetpack Compose UI
        ↓
   ViewModel (StateFlow)
        ↓
  Repository (Single Source of Truth)
     ↙           ↘
Room DB       Gemini API
```

---

##  Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Repository Pattern |
| State Management | StateFlow, Kotlin Coroutines |
| Local Storage | Room Database, DAO |
| AI | Google Gemini 1.5 Flash |
| Networking | HttpURLConnection, Gson |
| Location | FusedLocationProviderClient |
| Voice | SpeechRecognizer API |
| Images | Coil |

---

##  Offline-First Workflow

```
tips.json → Room Database → UI
                ↑
          Gemini AI Sync
```

1. Initial tips loaded from local JSON assets
2. Data persisted in Room Database
3. App works offline using cached data
4. When internet is available → Gemini AI generates new insights and syncs locally

---

##  Project Structure

```
app/
├── data/
│   ├── local/          # Room DB, DAO
│   ├── remote/         # Gemini API client
│   └── repository/     # Single source of truth
│
├── ui/
│   ├── screens/        # Composable screens
│   ├── components/     # Reusable UI components
│   └── theme/          # Material 3 theming
│
├── viewmodel/          # State management
├── model/              # Data models
└── assets/
    └── tips.json       # Seed data
```

---

##  Getting Started

### Prerequisites
- Android Studio **Hedgehog** or **Ladybug**
- Android device or emulator running **Android 7.0+ (API 24)**
- A valid **Google Gemini API key**

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/Anangarekhaa/Raitha-vartha.git
cd Raitha-vartha
```

**2. Open in Android Studio**

Open the cloned folder in Android Studio Hedgehog / Ladybug.

**3. Configure your Gemini API key**

Add your API key to the appropriate configuration file (e.g., `local.properties` or a constants file):
```
GEMINI_API_KEY=your_api_key_here
```

**4. Build and run**

Run on a physical Android device or an emulator.

---

##  Minimum Requirements

| Requirement | Details |
|---|---|
| Android Version | Android 7.0+ (API 24) |
| RAM | 2 GB recommended |
| Internet | Required only for AI sync and chat |

---

##  Future Scope

- 🦠 AI-based crop disease detection from photos
- 🌦️ Weather forecasting integration
- 📈 Yield prediction system
- 🏛️ Government scheme recommendations
- 🌍 Multi-language expansion beyond Kannada
- 👨‍🌾 Farmer community platform

---

##  Core Objectives

- Deliver reliable farming knowledge in offline environments
- Empower farmers with accessible AI technology
- Improve inclusivity through bilingual support
- Bridge the gap between traditional agriculture and digital innovation


---

> Built with ❤️ for the farmers of Karnataka.
