## language: ar
#خاصية: استعراض امكانيات محرك الميكنة شافت
#  سيناريو: إختبار 001 - افتح المتصفح, قم بزيارة موقع جوجل, قم بالبحث عن كلمة, تأكد من النتيجة
#    بفرض انى قمت بفتح المتصفح المطلوب
#    عندما اقوم بزيارة هذا الموقع "https://www.google.com/"
#    و اقوم بكتابة "SHAFT_Engine" بداخل مربع الكتابة المحدد بإستخدام name بقيمة "q"
#    و اقوم بالضغط على زر Enter بداخل عنصر الويب المحدد بإستخدام name بقيمة "q"
#    اذاً أقوم بالتأكد من ان قيمة الصفة href الخاصة بعنصر الويب المحدد بإستخدام xpath بقيمة "(//h2[text()='نتيجة ويب تضم روابط الموقع']/following::a)[1]", من المفترض أن تكون "https://github.com/MohabMohie/SHAFT_ENGINE"