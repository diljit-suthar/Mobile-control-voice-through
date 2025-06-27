
# 🤖 Master App – Offline Android Voice Assistant Using Vosk

**Master App** is a fully offline, privacy-focused voice assistant for Android built using **Java + Vosk speech recognition**. It empowers users to control phone functions with voice – without internet, without cloud, and with full customization.

---

## 🔥 Features (In Progress)
- 🎤 100% Offline Voice Recognition (using [VOSK](https://github.com/alphacep/vosk-android))
- ⚙️ System Command Control:
  - "OTG on" → Opens OTG settings
  - "WiFi on", "Bluetooth off"
  - "Call my father"
  - "Scroll down", "Show numbers"
- 📩 SMS Trigger System:
  - Send SMS like `START OTG` to remotely activate commands
- 📱 Designed for use **without internet or PC**
- 📦 Built via **GitHub Actions** and **AIDE** (no Android Studio needed!)
- 🛡️ Private. Lightweight. Powerful.

---

## 🚧 Current Problem
> ❗ App compiles successfully, but only shows a **blank screen** after launch.

Looking for help to:
- Fix UI layout rendering
- Review `MainActivity.java`
- Optimize model loading
- Add service/wake word features

👉 View open issue: [#1 – Blank screen issue](https://github.com/diljit-suthar/Mobile-control-voice-through/issues/1)

---

## 🧠 Vision
A personal Android assistant that:
- Works offline, even without SIM or data
- Can be controlled by voice and secure SMS
- Helps control the full system (settings, calling, scrolling) without root

---

## 📂 Project Structure
app/ ├── src/ │   ├── main/ │   │   ├── java/.../MainActivity.java │   │   ├── res/layout/activity_main.xml │   │   └── assets/model-en-us/ └── build.gradle

---


---

## 🔗 Live Demo APK (Auto-built)
[![Build APK](https://github.com/diljit-suthar/Mobile-control-voice-through/actions/workflows/android.yml/badge.svg)](https://github.com/diljit-suthar/Mobile-control-voice-through/actions)

Download latest working APK from **Actions** tab.

---

## 🤝 Want to Contribute?
Please help with:
- Fixing the UI blank screen bug
- Improving speech command handling
- Adding background service / wake word trigger

Clone, fork, or open a PR. Let's make this better together! 🙏

---

## 👤 Author
**Diljit Suthar**  
📱 Voice control enthusiast, building Android projects using just a phone + AIDE.

---

## 📜 License
MIT – free to use, modify, and share.
