# Notekeeper (Android app)
(previously StudyGroup and StudyBuddy)

## Ideja
Organizovati sve beleške, pitanja, itd. u centralizovani repozitorijum, po školama, i omogućiti što širi javni
pristup. Ovo ima dve velike prednosti: korišćenje i unapređivanje starih beležaka koje su ostavile prethodne
generacije i informisanje o programima druge škole. Iako koristim izraz "škola", trudio sam se da bude praktično
i ako je u pitanju faks i sve slične institucije (kako osnovnoškolac može da vidi šta se tačno radi u željenoj
srednjoj školi, tako i da srednjoškolci mogu da vide šta se radi na određenom fakultetu). Stoga i opštiji pojmovi
u engleskom prevodu - Group & Course.

## Kompatibilnost
Android 4.0-7.0 (testirano)

## Struktura projekta (app folder)
Svako pakovanje ima svoj package-info.java, i to je vrhunac dokumentacije koje ćete naći ovde
* UI layer
    * res/layouts - xml layouti, sve Što se iscrtava na ekranu
        * nije hijerarhijski, prva reč imena označava o kakvom layoutu se radi, activity_* i fragment_* su top-level, dok se ostali include-uju u te
    *java/....ui - Java kod koji binduje podatke za layoute i definiše sva ostala ponašanja
        * .dialogs - dijalozi
        * .recyclers - recikleri, tj. liste
        * .singleitemactivities - Add*Activity (za dodavanje/menjanje stvari), Pageri i single Note/Question Fragmenti
* Model layer
    * java/....model - spona između dela koji se prikazuje korisniku i onoga što se odvija u pozadini. Ništa iz UI layera ne pristupa direktno I/O metodama, jer id-ovi iz klase modela nikada ne idu na "gore"
*I/O layer
    *java/....io - lokalni ulaz i izlaz (baza, media fajlovi) i koordiniranje
        * /DataManager - "ulaz" u I/O layer, određuje da li će se podaci uzimati iz baze na uređaju ili sa servera, gomila metoda koje dele Executor i izvršavaju se u pozadini
        * /Database - sve što se tiče baze podataka
        * /Media*, Local* - fajlovi na uređaju, na internoj SD kartici, slike i audio zapisi
    *java/....network - komunikacija sa serverom i mrežom uopšte
        * /Network - pravi zahteve koji se šalju serveru, greške prosleđuje NetworkExceptionHandleru, radi token refreshing
        * /NetworkRequests - helper metode za Network
        * /* - metode koje se tiču konkretno neke stavke (npr. Courses su sve metode koje se tiču kurseva, i koji argumente koje prime pretvaraju u format koji Network klasa traži)

* Misc
   * manifests/AndroidManifest.xml - manifest projekta, deklaracije aktivnosti i sl.
   * .exceptions - custom exceptioni i ExceptionHandler za mrežne greške u pozadinskim threadovima
   * .google - standard sliding tabs implementation
   * .misc - sve ostalo

## Screenshots
Iz februara - <http://imgur.com/a/DfmP7>. Par stvari sam promenio od tad, ali i dalje je u principu to to

## TODO
* Loader invalid state bug - videti gde i zašto, možda race condition?
* sending and accepting invitations
* proper offline mode, tj. dodavanje stvari čak i oflajn, koje bi se uploadovale nakon što se uređaj konektuje na internet
* titles in Add*Activity - generic (Edit *) or dynamic (course/group/lesson name) ?
* UI consistency - sidebar in MemberList & ScheduleActivity, like in GroupActivity 
  (jer su courses, members i exams na istom hijerarhijskom nivou)
* store announcements
* ETag/Last-Modified caching for media
* Per-Course simple message board (chatroom-like) ?
* Veći ekrani?
* testing/bugfixing
* dokumentacija
* //todo komentari u kodu (ovde ih zapravo ima, za razliku od servera)
