# AURA – AI-Powered Mental Wellness App

AURA is a modern Android mental wellness application built using **Kotlin** and **Jetpack Compose**, designed to help users reflect on emotions, track mood trends, and practice mindfulness through a warm, intuitive interface.

---

## ✨ Features

### 🧠 AI Sentiment Analysis  
- Integrates **Hugging Face** NLP models  
- Analyzes journal entries for emotion and confidence score  
- Helps users understand mood patterns over time  

### 📓 Journaling  
- Beautiful and distraction-free journaling experience  
- Entries saved securely using **Firebase Firestore**  
- Real-time sync across devices  

### 📊 Mood Analytics  
- Charts powered by **MPAndroidChart**  
- Sentiment trends, mood distribution, and recent activity  

### 🎧 Meditation Player  
- Built with **ExoPlayer**  
- Smooth audio playback with a full-screen mode  
- Calming UI and easy access from the home screen  

### 🔐 Secure Authentication  
- Email/Password sign‑in using **Firebase Authentication**  
- First-time users provide their name for personalization  

### 🌈 Modern UI/UX  
- Jetpack Compose-first design  
- Gradient backgrounds, animations, micro-interactions  
- Personalized greeting and dynamic home screen  

---

## 🏗️ Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Firebase Auth & Firestore**
- **Retrofit + OkHttp**
- **Hugging Face API**
- **ExoPlayer**
- **MPAndroidChart**
- **Coroutines & Flow**

---

## 🚀 Installation

1. Clone the repository:  
   ```bash
   git clone https://github.com/RajathPatilKulkarni/AURA.git
   ```
2. Open in **Android Studio**.
3. Add your `local.properties` file (not included in repo):  
   ```
   sdk.dir=/path/to/android/sdk
   HF_API_KEY=your_huggingface_key
   ```
4. Add your own `google-services.json` under `app/`.
5. Build & run on your device or emulator.

---

## 📁 Project Structure

```
AURA/
 ├── app/
 │   ├── src/main/java/com/Rajath/aura/
 │   │   ├── ui/               # Compose UI screens
 │   │   ├── vm/               # ViewModels
 │   │   ├── data/             # Repository & models
 │   │   └── network/          # Retrofit API
 │   ├── src/main/res/         # Fonts, drawables, themes
 │   ├── build.gradle.kts
 │   └── proguard-rules.pro
 ├── .github/workflows/        # CI/CD (if any)
 ├── README.md
 ├── .gitignore
```

---

## 🛡️ Security Notes

This repository **does not** contain:
- `google-services.json`
- Keystore files
- Hugging Face API key  
These must be added locally for development.

---

## 📝 License

This project is released under the **MIT License**.

---

## 💬 About the Author

Built with care by **Rajath Patil Kulkarni**  
Focused on AI, mobile development, and creating meaningful digital experiences.

---

If you like this project, consider leaving a ⭐ star on GitHub!
