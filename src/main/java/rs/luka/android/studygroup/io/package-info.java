/**
 * Najniži nivo rada s podacima.
 * Database - sve što se tiče sqlite baze aplikacije
 * DataManager - prosleđivanje podataka bazi i mreži po potrebi, odlučuje odakle se uzimaju podaci. U svakom slučaju,
 * sve se prvo smešta u bazu, zatim učitava isključivo iz nje (jer mi je tako bilo lakše za implementirati)
 * Limits - limiti unosa, min/max vrednosti
 * Loaders - loaderi za recyclere (prikaz podataka, asinhrono učitavanje)
 * LocalAudio/LocalImages - čuvanje i brisanje slika i snimaka
 * MediaCleanup - čišćenje slika/snimaka kada više nisu potrebni
 * SQLiteCursorLoader - superklasa za loadere koji vuku podatke iz baze
 */
package rs.luka.android.studygroup.io;