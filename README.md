# University Campus AR Guide

Inertial sensor-based AR navigation system for university campus, featuring a custom .NET 8 backend with Clean Architecture for real-time localization and pathfinding.

## 📱 O projektu
Diplomski rad koji implementira AR navigaciju kroz kampus FTN-a. Sistem rešava ograničenja tradicionalnih 2D mapa tako što informacije o objektima (fakulteti, službe) projektuje direktno u 3D prostor okruženja. Rešenje kombinuje **2D mapu** za globalnu orijentaciju i **AR prikaz** za preciznu navigaciju na blizinu.

## 🏗 Tehnička arhitektura
- **Mobile Client:** Android aplikacija razvijena korišćenjem ARCore (Motion Tracking) i Sceneform (3D Rendering).
- **Backend:** .NET 8 Web API, implementiran po principima Clean Architecture, sa CQRS obrascem (MediatR) za efikasnu obradu podataka.
- **Data Source:** Dinamički sistem – lokacije se ne nalaze u aplikaciji, već se preuzimaju sa servera, omogućavajući ažuriranje informacija u realnom vremenu.

## 🛠 Tehnologije
- **Backend:** .NET 8, MediatR, Entity Framework Core, SQL Server
- **Mobile:** Android (Kotlin/Java), ARCore, Osmdroid
- **Komunikacija:** REST API

## ⚙️ Inženjerski izazovi i rešenja
Najveći izazov projekta bila je preciznost lokalizacije bazirana na senzorima (akcelerometar, žiroskop, magnetometar). Implementirana su sledeća rešenja:

* **Stabilizacija azimuta:** Umesto stalnog oslanjanja na kompas koji je podložan "driftu", pri inicijaciji se pamti stabilan azimut kao nulta tačka u AR prostoru.
* **Matematička preciznost:** Za izračunavanje udaljenosti korišćena je **Haversinusna formula** (uzima u obzir zakrivljenost Zemlje), uz radijus vidljivosti ograničen na 20 metara radi maksimalne tačnosti.
* **Filtriranje smetnji:** * **LERP interpolacija:** Implementirana za "peglanje" skokova markera uzrokovanih nepreciznošću GPS signala.
    * **Low-pass filter:** Korišćen za stabilizaciju magnetometra i eliminaciju drhtanja slike uzrokovanog spoljnim elektromagnetnim smetnjama.

## 🔐 Administrativni sistem
Sistem poseduje skriveni admin panel dostupan putem specijalne sekvence dodira, koji omogućava:
- Upravljanje lokacijama direktno sa terena.
- Kalibraciju sistema i ažuriranje podataka bez potrebe za izmenama u kodu.

## 🚀 Kako pokrenuti
[Uputstvo za pokretanje backend-a]
1. Kloniraj repozitorijum.
2. Konfiguriši `appsettings.json` sa bazom podataka.
3. Pokreni migracije: `dotnet ef database update`.
4. Pokreni API: `dotnet run`.

[Uputstvo za pokretanje mobilne aplikacije]
1. Otvori projekat u Android Studiju.
2. Podesi ARCore konfiguraciju i  Retrofit.
3. Instaliraj aplikaciju na Android uređaj sa podrškom za AR.

---
*Projekat realizovan kao diplomski rad na Fakultetu tehničkih nauka u Novom Sadu.*
