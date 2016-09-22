/**
 * Prikaz i ponašanje svega na ekranu. Ako je u pitanju recycler ili nešto što se još negde upotrebljava,
 * aktivnost (*Activity klase) hostuje fragment (*Fragment klase). Trudio sam se da logika koja se tiče prikazivanja
 * bude u fragmentima, dok podatke koliko je moguće prebacim u aktivnosti. Potpakovanja:
 * dialogs - dijalozi, logično
 * recyclers - recikleri, liste i sl. npr. GroupActivity sadrži listu kurseva u nekoj grupi
 * singleitemactivities - (SingleItemActivities) sve što nisu recikleri, Login/Register, Add*Activity (dodavanje i
 *          editovanje stvari), Pager-i (horizontalno fullscreen skrolovanje stranica) i odgovarajući fragmenti
 * PoliteSwipeRefreshLayout - napravljen kako se ne bi triggerovao refresh stranice na swipe up ako recycler nije na vrhu
 *          Courtesy of Android chatroom on StackOverflow.
 * SingleFragmentActivity - templejt (superklasa) za aktivnosti koje hostuju samo jedan fragment, tj. sve sem
 *          LessonActivity i pejdžera
 * Snackbar.java - stvar prošlosti, dekompajliran source snackbara, jer ga Gugl još nije bio ubacio u Android repo,
 *          ručno prepravljan tako da se kompajlira, pošto je to bio jedini način da se promeni boja teksta i podigne
 *          FAB pri pojavljivanju Snackbara (pošto je sasvim logično da dark Snackbar uzima default boju teksta iako
 *          koristim light temu, zar ne? (tako da sam imao crno-na-crno tekst)). Trenutno ima istorijsku i
 *          sentimentalnu vrednost.
 */
package rs.luka.android.studygroup.ui;