# Arabam Nerede? 🚗

Mobile Computing dersi projesi: aracınızı park ettiğiniz yeri kaydeden ve sonra
size oraya yol tarifi veren basit bir Android uygulaması.

## Özellikler

- **Park ettim** butonuyla anlık GPS konumunu kaydetme
- Park yerini ve kendi konumunuzu **haritada** görme (OpenStreetMap)
- Park yerine olan **uzaklığı** canlı görme
- Park yerine **not** (ör. "B2 katı, 14 numara") ve **fotoğraf** ekleme
- Google Maps ile **yürüyüş yol tarifi** alma
- Kayıt telefonda kalır; uygulama kapatılıp açılınca park yeri kaybolmaz

## Kullanılan Mobile Computing Konuları

| Konu | Uygulamada nerede | Android API |
|---|---|---|
| Konum servisleri (GPS / ağ) | Anlık konum, uzaklık hesabı | `LocationManager`, `Location.distanceBetween` |
| Çalışma zamanı izinleri | Konum izni isteme | `ActivityResultContracts.RequestMultiplePermissions` |
| Harita | Park yeri ve kullanıcı işaretleri | osmdroid (OpenStreetMap) |
| Kamera | Park yeri fotoğrafı | `ActivityResultContracts.TakePicturePreview` |
| Yerel veri saklama | Park bilgisi ve fotoğraf | `SharedPreferences`, uygulama dosyaları |
| Uygulamalar arası iletişim | Yol tarifi için Google Maps'i açma | `Intent.ACTION_VIEW` |

## Teknolojiler

- Kotlin, Jetpack Compose (Material 3)
- osmdroid 6.1 (API anahtarı gerektirmez)
- minSdk 26 (Android 8.0), targetSdk 35

## Çalıştırma

1. [Android Studio](https://developer.android.com/studio)'yu kurun.
2. **File > Open** ile bu klasörü açın, Gradle senkronizasyonunun bitmesini bekleyin.
3. Emülatör ya da gerçek telefon seçip **Run ▶** butonuna basın.

> **Emülatörde deneme:** *Extended Controls (⋯) > Location* menüsünden konum seçip
> "Set location" deyin, sonra "Park ettim"e basın. Konumu değiştirdiğinizde uzaklık güncellenir.

## Demo Senaryosu

1. Uygulamayı açın ve konum izni verin.
2. İsterseniz not yazıp fotoğraf çekin, ardından **Park ettim**'e basın.
3. Haritada kırmızı işaret park yerini, mavi nokta sizi gösterir.
4. Uzaklaştıkça uzaklık değişir; **Yol tarifi** Google Maps'i açar.
5. **Arabamı aldım** kaydı siler.

## Proje Yapısı

```
app/src/main/java/com/mobilecomputing/app/
├── MainActivity.kt      # Uygulama girişi
├── ParkScreen.kt        # Ana ekran: izin, park etme, kayıtlı park paneli
├── ParkMap.kt           # OpenStreetMap harita bileşeni
├── CurrentLocation.kt   # GPS konum dinleyicisi
└── ParkingStore.kt      # Park yeri ve fotoğrafın kaydedilmesi
```
