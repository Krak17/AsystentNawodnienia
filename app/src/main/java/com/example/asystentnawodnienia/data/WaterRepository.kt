package com.example.asystentnawodnienia.data

/**
 * Repozytorium zarządza danymi i dostarcza je do reszty aplikacji.
 * Działa jako pośrednik między ViewModelami a źródłami danych (tutaj: Room DAO).
 * Dzięki temu ViewModel nie musi wiedzieć, skąd pochodzą dane.
 *
 * @param waterDao Obiekt dostępu do danych (DAO), dostarczany do repozytorium.
 */
class WaterRepository(private val waterDao: WaterDao) {

    /**
     * Dodaje nowy wpis o spożyciu wody do bazy danych.
     * Wywołuje odpowiednią funkcję w DAO.
     */
    suspend fun addWater(waterIntake: WaterIntake) {
        waterDao.insert(waterIntake)
    }

    /**
     * Aktualizuje istniejący wpis o spożyciu wody.
     * Wywołuje odpowiednią funkcję w DAO.
     */
    suspend fun updateWater(waterIntake: WaterIntake) {
        waterDao.update(waterIntake)
    }

    /**
     * Pobiera sumę spożytej wody dla konkretnego dnia.
     * Jeśli DAO zwróci null (brak wpisów), ta funkcja bezpiecznie zwróci 0.
     */
    suspend fun getTotalForDay(day: String): Int {
        return waterDao.getWaterIntakeForDay(day) ?: 0
    }

    /**
     * Pobiera całą historię spożycia wody.
     * Zwraca listę wszystkich wpisów, korzystając z funkcji w DAO.
     */
    suspend fun getHistory(): List<WaterIntake> {
        return waterDao.getAllWaterIntakeHistory()
    }
}
