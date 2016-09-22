/**
 * Model podataka.
 * Course - kurs, tj. predmet (trudio sam se da biram neutralnije reči), pripada grupi
 * Edit - jedna izmena
 * Exam - kontrolni/ispit, pripada grupi, referencira kurs
 * Group - grupa, tj. škola
 * ID - id svakog člana, hijerarhijski. Menjao se par puta, pa mi ga je bilo lakše staviti u jednu klasu
 * Note - beleška, pripada kursu, sadrži lekciju
 * PastEvents - interfejs za klase koje beleže istoriju izmena todo naći bolje ime
 * Question - pitanje, pripada kursu, sadrži lekciju
 * User - static instanca drži trenutno ulogovanog korisnika, može se konstruisati tako da prikazuje druge korisnike
 * u MemberListActivity todo napraviti User manje konfuznim
 */
package rs.luka.android.studygroup.model;