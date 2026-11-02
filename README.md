# TachiRepo Explorer (`Dranixx/tachiyomi-extensions-apk`)

تطبيق أندرويد مبني باستخدام **Kotlin** و **Jetpack Compose (Material 3)** لاستخراج وفحص مستودع إضافات تاتشيومي (`https://github.com/Dranixx/tachiyomi-extensions-apk.git`) من GitHub.

## المميزات الرئيسية
- **استخراج الفهرس المباشر (`index.min.json`)**: جلب وتحليل قائمة الإضافات، أرقام الإصدارات (`version` / `versionCode`)، اللغات، الأيقونات، والمصادر الفرعية (`sources`).
- **شجرة ملفات مستودع GitHub**: استعراض وفحص ملفات المستودع (`apk/`, `icon/`, `index.min.json`, `index.json`, `README.md`) مع عارض أكواد مدمج.
- **دعم مستودعات GitHub المخصصة**: التبديل الفوري بين `Dranixx/tachiyomi-extensions-apk` و`keiyoushi/extensions` أو إدخال أي مستودع GitHub آخر.
- **البناء التلقائي عبر GitHub Actions**: يحتوي المشروع على ملف سير عمل جاهز في `.github/workflows/android-build.yml` يقوم بتشغيل الاختبارات وبناء ملف `APK` تلقائياً عند رفع الكود إلى GitHub.

## البناء عبر GitHub (GitHub Actions)
1. من قائمة الإعدادات في **AI Studio**، اختر **Push to GitHub** (أو قم بتصدير المشروع كملف ZIP ورفعه إلى مستودع GitHub الخاص بك).
2. بمجرد رفع المشروع إلى GitHub، سيعمل سير العمل **Android CI & APK Build** تلقائياً داخل تبويب **Actions** في GitHub.
3. بعد اكتمال البناء، يمكنك تحميل ملف **`TachiRepo-Explorer-debug-apk`** الجاهز للتثبيت من قسم **Artifacts** في صفحة العملية داخل GitHub Actions.
