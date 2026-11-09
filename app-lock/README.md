# Gizli Kasa 🔒 — Uygulama Kilitleme Örneği

Mobile Computing dersi örneği: uygulamayı **PIN** ve **parmak izi / yüz tanıma**
ile kilitleyen basit bir Android uygulaması. Kilidin koruduğu içerik olarak
örnek bir "gizli not" ekranı vardır.

## Özellikler

- İlk açılışta **4 haneli PIN oluşturma** (iki kez girilerek onaylanır)
- 1111, 1234 gibi **kolay tahmin edilen PIN'lerin reddedilmesi**
- **Parmak izi / yüz tanıma** ile kilit açma (kilit ekranı açılınca otomatik sorulur)
- Uygulama arka plana alınınca **otomatik kilitlenme** (hemen, 30 sn, 1 dk, 5 dk)
- **Şimdi kilitle** butonu
- 5 hatalı denemeden sonra **bekleme süresi** (30 sn, sonra her hatada 2 katı, en fazla 5 dk);
  uygulama kapatılıp açılsa da sıfırlanmaz
- **Ekran görüntüsü engelleme** ve "son uygulamalar" listesinde içeriğin gizlenmesi
- Mevcut PIN'i doğrulayarak **PIN değiştirme**

## Kullanılan Mobile Computing Konuları

| Konu | Uygulamada nerede | Android API |
|---|---|---|
| Biyometrik kimlik doğrulama | Parmak izi / yüz ile kilit açma | `androidx.biometric.BiometricPrompt`, `BiometricManager` |
| Uygulama yaşam döngüsü | Arka plana alınınca otomatik kilit | `onStart` / `onStop`, `isChangingConfigurations` |
| Güvenli veri saklama | PIN'in tuzlanmış özeti (hash) | `PBKDF2WithHmacSHA256`, `SecureRandom` |
| Yerel veri saklama | Ayarlar, deneme sayacı, gizli not | `SharedPreferences` |
| Ekran güvenliği | Ekran görüntüsü / kayıt engeli | `WindowManager.LayoutParams.FLAG_SECURE` |
| Kaba kuvvet koruması | Hatalı denemede bekleme | `LockPolicy` |

## Dosyalar

| Dosya | Görevi |
|---|---|
| `MainActivity.kt` | Yaşam döngüsünü izler, `FLAG_SECURE` uygular |
| `AppLock.kt` | Uygulama genelinde kilit durumu (kilitli / açık) |
| `PinSecurity.kt` | PIN hash'leme, PIN kuralları, bekleme ve otomatik kilit kuralları |
| `PinStore.kt` | PIN özeti, ayarlar ve not için `SharedPreferences` katmanı |
| `BiometricHelper.kt` | `BiometricPrompt` sarmalayıcısı |
| `PinPad.kt` | Nokta göstergeli sayısal tuş takımı |
| `Screens.kt` | PIN oluşturma, kilit, ana ekran ve ayarlar ekranları |

## Nasıl çalışır?

1. Uygulama her soğuk açılışta **kilitli** başlar (`AppLock.isLocked = true`).
2. PIN yoksa önce PIN oluşturma ekranı gösterilir.
3. Kilit ekranında PIN doğru girilir ya da biyometrik doğrulama başarılı olursa kilit açılır.
4. `onStop` anında zaman kaydedilir; `onStart` anında seçilen süre dolmuşsa uygulama
   yeniden kilitlenir. Ekran döndürmede (`isChangingConfigurations`) kilitlenmez.

## Teknolojiler

- Kotlin, Jetpack Compose (Material 3)
- androidx.biometric 1.1.0
- minSdk 26 (Android 8.0), targetSdk 35

## Çalıştırma

1. [Android Studio](https://developer.android.com/studio)'yu kurun.
2. **File > Open** ile bu `app-lock` klasörünü açın, Gradle senkronizasyonunun bitmesini bekleyin.
3. Emülatör ya da gerçek telefon seçip **Run ▶** butonuna basın.
4. Emülatörde parmak izi denemek için: emülatörün **Ayarlar > Güvenlik** bölümünden parmak izi
   ekleyin, sonra emülatör penceresinde **⋯ > Fingerprint > Touch Sensor** ile dokunun.

Birim testleri: Android Studio'da `PinSecurityTest` dosyasına sağ tık > **Run**.

## Notlar / geliştirme fikirleri

- Bu örnekte gizli not düz `SharedPreferences`'ta durur. Gerçek bir uygulamada veriler
  Android Keystore'daki bir anahtarla şifrelenmeli (ör. `BiometricPrompt.CryptoObject`).
- **Başka uygulamaları kilitlemek** (WhatsApp, Galeri vb.) için `UsageStatsManager` ya da
  `AccessibilityService` ile ön plandaki uygulamayı izleyip üstüne kilit ekranı açmak gerekir;
  bu ayrı izinler ve kullanıcı onayı ister.
