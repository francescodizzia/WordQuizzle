package com.dizzia.wordquizzle;

public interface WQInterface {

    public void login(String nickUtente, String password);
    public void logout(String nickUtente);
    public void aggiungi_amico(String nickUtente, String nickAmico);
    public void lista_amici(String nickUtente);
    public void sfida(String nickUtente, String nickAmico);
    public void mostra_punteggio(String nickUtente);
    public void mostra_classifica(String nickUtente);

}
