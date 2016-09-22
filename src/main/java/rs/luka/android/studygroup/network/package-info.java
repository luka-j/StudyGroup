/**
 * Komunikacija sa serverom. Nadam se da su imena klasa dovoljno deskriptivna
 * Network - najniži nivo komunikacije s mrežom. Nisam našao adekvatan mali framework koji će lepo da radi
 *          s DataManagerom, pa sam sam pisao kod za konektovanje, i u njemu uključio error handling (koristeći
 *          NetworkExceptionHandler) i autentifikaciju (token uzima iz User#getLoggedInUser) koja podrazumeva
 *          automatski token refresh kada dobije poruku 401/Expired sa servera i ponavljanje zahteva
 * NetworkRequests - helper metode za Network
 * UserManager - sve što se tiče trenutnog korisnika i menjanja njegovih podataka + token refreshing logic
 */
package rs.luka.android.studygroup.network;