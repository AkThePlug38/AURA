AURA — AI-Powered Emotional Wellness App

<p align="center">
  <img src="assets/icon.svg" width="140" height="140" alt="AURA App Logo" />
</p>


<p align="center">
  <strong>A warm, mindful journaling companion built with Kotlin &amp; Jetpack Compose.</strong>
</p>


<p align="center">
  <!-- Badges (Shields.io) -->
  <img alt="Android Compose" src="https://img.shields.io/badge/Android-Compose-3DDC84?logo=android&logoColor=white&style=for-the-badge" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge" />
  <img alt="Firebase" src="https://img.shields.io/badge/Firebase-Auth|Firestore-FFCA28?logo=firebase&logoColor=black&style=for-the-badge" />
  <img alt="HuggingFace" src="https://img.shields.io/badge/HuggingFace-Sentiment-FFD21E?logo=huggingface&logoColor=black&style=for-the-badge" />
  <img alt="License-MIT" src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" />
</p>



⸻

✨ One‑line

AURA combines private journaling, AI sentiment insights, mood analytics, and guided meditation into a cohesive, Compose‑first Android experience.

⸻

🚀 Highlights
	•	Smart Journaling with instant sentiment analysis (Hugging Face).
	•	Secure storage per user using Firebase Auth + Firestore.
	•	Mood analytics with clean MPAndroidChart visualizations.
	•	Meditation audio player powered by ExoPlayer.
	•	Modern UI with gradients, micro‑interactions, and accessibility in mind.

⸻

🖼 Visuals

Place these files in the repository (recommended paths):

assets/icon.svg                # app icon (foreground lotus SVG)
assets/background.xml          # gradient background (optional)
assets/screenshots/home.png
assets/screenshots/journal.png
assets/screenshots/analytics.png
assets/screenshots/meditate.png

<p align="center">
  <img src="assets/screenshots/home.png" width="30%" alt="home"/>
  <img src="assets/screenshots/journal.png" width="30%" alt="journal"/>
  <img src="assets/screenshots/analytics.png" width="30%" alt="analytics"/>
</p>



⸻

🏗 Architecture (visual)

Below is a compact, professional diagram and a small dataflow description.

Presentation (Compose UI)
  └─ ViewModels (StateFlow)  ←→  Repositories  ←→  Firestore
                                        ↓
                                  HuggingFace API

Dataflow: UI events -> ViewModel -> Repository -> (Firestore / HF API) -> Repository -> ViewModel -> UI.

⸻

🧩 Tech Stack
	•	Kotlin • Jetpack Compose • Material3
	•	Firebase Auth • Firestore
	•	Hugging Face (Roberta sentiment inference) via Retrofit
	•	ExoPlayer (audio) • MPAndroidChart (charts)
	•	Coroutines & StateFlow

⸻

⚙️ Quick Start (developer)
	1.	Clone the repo

git clone https://github.com/<your-org>/AURA.git
cd AURA

	2.	Add local.properties (not tracked)

sdk.dir=/path/to/android/sdk
HF_API_KEY=your_huggingface_key

	3.	Add Firebase google-services.json to app/.
	4.	Open in Android Studio and run.

⸻

🔐 Security notes
	•	Do not commit google-services.json, keystore files, or local.properties.
	•	Keep API keys in GitHub Secrets for CI.

⸻

📦 Build

# Debug
./gradlew assembleDebug

# Release (signed, requires keystore configured in local.properties)
./gradlew assembleRelease

Output: app/build/outputs/apk/ or app/build/outputs/bundle/.

⸻

📚 Project structure (high level)

AURA/
├─ app/
│  ├─ src/main/java/com/Rajath/aura/
│  │  ├─ ui/      # Compose screens
│  │  ├─ vm/      # ViewModels
│  │  ├─ data/    # Repositories & models
│  │  └─ network/ # Retrofit clients
│  └─ res/        # fonts, themes, drawables
├─ .github/workflows/
├─ README.md
└─ LICENSE


⸻

📄 License

MIT © Rajath Patil Kulkarni

⸻

If you want I can:
	•	generate an actual PNG/SVG architecture diagram and attach it to the canvas, or
	•	embed your lotus SVG into a polished banner image and place it in assets/.

Tell me which and I’ll create the image files and update the canvas README accordingly.
