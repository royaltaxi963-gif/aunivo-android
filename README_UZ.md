# Aunivo Android APK loyihasi

Bu loyiha Aunivo HTML interfeysini Android ilovasi ichiga joylaydi va Android'ning SpeechRecognizer API'si orqali `uz-UZ` ovoz tanishni sinab ko'radi.

## APK olish — telefon orqali
1. GitHub'da yangi repository yarating: `aunivo-android`.
2. Ushbu ZIP ichidagi fayllarni repository ildiziga yuklang (fayllarni ichidagi papkadan olib, repository ildiziga joylang; `.github/workflows/build-apk.yml` ham saqlansin).
3. Repository'da **Actions** bo'limini oching. Workflow ishlashi kerak; bo'lmasa **Build Aunivo APK** → **Run workflow** bosing.
4. Build tugagach, run sahifasidagi **Artifacts** ostidan `aunivo-debug-apk` ni yuklab oling.
5. ZIP artifactni oching, `app-debug.apk` faylini bosing va Android so'rasa shu faylni o'rnatishga ruxsat bering.

## Eslatma
- Bu sinov/debug APK. Google Play uchun mo'ljallanmagan.
- Ovoz tanish Android qurilmadagi tanish xizmatiga bog'liq; `uz-UZ` barcha telefonlarda qo'llab-quvvatlanishi kafolatlanmaydi.
- HTML ichidagi AI backend (`https://aunivo-server.onrender.com/chat`) ishlayotgan bo'lishi va internet bo'lishi kerak.
- Hozircha ilova kontakt, qo'ng'iroq yoki Telegram xabarlarini mustaqil bajarmaydi; bularni keyingi bosqichda Android integratsiyasi bilan qo'shamiz.
