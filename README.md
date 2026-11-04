# Mobile Computing

Mobile Computing dersi için geliştirilen basit bir Android uygulaması.
Uygulama, mobil cihazların temel özelliklerini tek ekranda gösterir:

| Bölüm | Kullanılan Android API | Konu |
|---|---|---|
| Konum | `LocationManager` (GPS / ağ) | Konum tabanlı servisler, çalışma zamanı izinleri |
| İvmeölçer | `SensorManager` | Mobil sensörler |
| Pil | `BatteryManager` + `BroadcastReceiver` | Enerji yönetimi |
| Ağ | `ConnectivityManager` | Kablosuz / mobil bağlantı |

## Teknolojiler

- Kotlin
- Jetpack Compose (Material 3)
- minSdk 26 (Android 8.0), targetSdk 35

## Çalıştırma

1. [Android Studio](https://developer.android.com/studio)'yu kurun.
2. **File > Open** ile bu klasörü açın, Gradle senkronizasyonunun bitmesini bekleyin.
3. Bir emülatör ya da gerçek telefon seçip **Run ▶** butonuna basın.

> Not: Sensör ve GPS değerleri gerçek telefonda daha anlamlıdır. Emülatörde konumu
> *Extended Controls > Location* menüsünden değiştirebilirsiniz.

## Proje Yapısı

```
app/src/main/
├── AndroidManifest.xml          # İzinler (konum, ağ durumu)
└── java/com/mobilecomputing/app/
    ├── MainActivity.kt          # Ana ekran
    └── DeviceCards.kt           # Konum, sensör, pil ve ağ kartları
```
