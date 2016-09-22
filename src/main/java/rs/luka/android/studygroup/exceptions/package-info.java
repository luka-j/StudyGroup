/**
 * Svi custom exceptioni koji se koriste u aplikaciji, vezani za networking i I/O operacije. Ima dosta nekorištenih,
 * jer sam ovo pisao pre nego što sam skapirao da se Exceptioni ne propagiraju između Threadova, pa sam morao da
 * pređem na prosleđivanje ExceptionHandlera
 * Važnije klase:
 * MarkerException - superklasa svih exceptiona koji nemaju stack trace, tj. samo pojašnjavaju šta se desilo, ne i gde.
 *              Ne sećam se zašto sam ih tačno uvodio, ali pretpostavljam da ima veze s performansama.
 * NetworkExceptionHandler - definiše sve greške do kojih može doći pri pribavljanju podataka.
 *              Pošto se mrežni zahtevi obavljaju u background-u, neka instanca ovog interfejsa (tj. klase koja ga implementira)
 *              se prosleđuju DataManager-u i dalje do Network klase, koja u slučaju pogrešnog http response koda
 *              obaveštava pozivaoca u grešci. Unutar interfejsa se nalazi default implementacija s generičnim
 *              dijalozima o grešci i finishedSuccessfully() metodom, koju ostale klase koriste i po potrebi proširuju
 *              anonimnim klasama
 */
package rs.luka.android.studygroup.exceptions;