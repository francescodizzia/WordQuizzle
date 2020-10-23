package com.dizzia.wordquizzle.commons;

public interface WQInterface {

    void login(String nickUtente, String password);
    void logout(String nickUtente);
    void aggiungi_amico(String nickUtente, String nickAmico);
    void lista_amici(String nickUtente);
    void sfida(String nickUtente, String nickAmico);
    void mostra_punteggio(String nickUtente);
    void mostra_classifica(String nickUtente);

}
