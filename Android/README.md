# KhanaBook Lite 🍲
### Built for the hustle. Simple, fast, and stays with you—online or off.

Running a food stall, cafe, or a busy restaurant is hard enough. **KhanaBook Lite** is here to make the "business side" of things feel effortless. It’s a Point of Sale (POS) app that doesn't need the internet to work and keeps your data exactly where it belongs: with you.

---

## 🌟 Why KhanaBook?

Most billing apps break the moment the Wi-Fi drops. We built this differently.

- ⚡ **Works Everywhere**: Whether you're in a basement cafe or a street-side stall, create bills instantly without worrying about a signal.
- 🥘 **Your Menu, Your Way**: Organize your dishes into simple categories. Got a "Large" and "Small" tea? We handle variants and pricing in a few taps.
- 🧾 **Digital & Paper Invoices**: Send professional-looking invoices directly to your customer's WhatsApp, or print them on the spot with a Bluetooth thermal printer.
- 📦 **Know Your Stock**: Stop guessing how much milk or sugar you have left. The app tracks your raw materials and pings you when you're running low.
- 🔄 **Smart Syncing**: The moment you're back online, the app quietly syncs everything (bills, menu changes, logs) to the cloud so you're always backed up.

---

## 🛠️ What's Under the Hood?

For the tech-savvy, here’s how we keep things fast and secure:
- **Kotlin & Jetpack Compose**: A modern, smooth UI that feels like a native app should.
- **Local-First Database**: We use Room with **SQLCipher** (AES-256 encryption), so your business data is locked away safely on your device.
- **Reliable Sync**: Powered by Android's **WorkManager**, our `MasterSyncWorker` ensures your offline work reaches the cloud without you lifting a finger.
- **Safe & Private**: We use BCrypt for passwords and keep your API keys tucked away in `local.properties`.

---

## 🚀 Getting Started

### 1. The Essentials
You'll need **Android Studio (Ladybug or newer)** and **JDK 17** to get the kitchen running.

### 2. Set Your Secret Ingredients
We don't share our secrets (like API tokens) on GitHub. You'll need to create a `local.properties` file in the root folder and add your Meta/WhatsApp credentials:

```properties
# Meta Cloud API for WhatsApp receipts
META_ACCESS_TOKEN=your_token_here
WHATSAPP_PHONE_NUMBER_ID=your_id_here
WHATSAPP_OTP_TEMPLATE_NAME=verification_otp
```

### 3. Cook & Serve
1. Open the project in Android Studio.
2. Let Gradle do its magic (Sync).
3. Plug in your Android device and hit **Run**. 
*Note: Using a real phone is better if you want to test Bluetooth printing!*

---

## 📁 A Quick Tour of the Project

- `ui/screens`: This is where the magic happens—from the 4-step billing flow to your menu setup.
- `domain/manager`: The "brains" of the app. It handles the math, generates PDFs, and talks to your printer.
- `data/local`: Your digital vault. This is where your bills and inventory live, encrypted and safe.
- `worker`: The `MasterSyncWorker` lives here, managing the bridge between your offline work and the online world.

---

## 🔒 Your Data, Protected
We take security seriously so you don't have to. Your database is encrypted, your network calls are strictly secure, and we've built the app to automatically handle session expirations.

---

## 📄 License
This is an internal/private project. All rights reserved. 

**Happy Billing!** ☕🥘
