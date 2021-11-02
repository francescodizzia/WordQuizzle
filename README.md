# WordQuizzle
[19/20] WordQuizzle - Progetto di Reti &amp; Lab

Introduzione
============

Il progetto consiste nell’implementazione del servizio **WordQuizzle**,
un sistema che gestisce un gioco basato sulla traduzione di diverse
parole dall’italiano all’inglese. Il sistema è basato sulle interazioni
social con altri utenti e queste vengono gestite utilizzando diverse
tecnologie. La registrazione di un utente avviene mediante *RMI*, la
richiesta di sfida viene inoltrata dal server utilizzando *UDP*, mentre
il resto delle interazioni client-server avviene sopra *TCP*. Il sistema
si appoggia all’API REST di *MyMemory* per verificare la correttezza
delle traduzioni fornite dall’utente: ogni richiesta di traduzione
restituisce un oggetto JSON che viene parsato dal server attraverso la
libreria **GSON**. C’erano varie opzioni riguardo a quali librerie
utilizzare (json.simple, Jackson, etc...), ma la scelta è ricaduta su
GSON perché ha saputo combinare semplicità d’uso, una ricca
documentazione e delle buone performance.

Architettura del sistema
========================

Il server è stato progettato utilizzando NIO e adopera il meccanismo dei
Selector per implementare il multiplexing dei canali: si è preferito
utilizzare questo approccio in quanto offre diversi vantaggi rispetto
alla controparte multithreaded, la quale avrebbe tra i suoi principali
svantaggi l’overhead non trascurabile causato dai numerosi context
switch, che oltretutto minerebbero pesantemente la scalabilità del
sistema. Utilizzare il multiplexing ci permette di risparmiare molte
risorse e inoltre il meccanismo dei Selector ci permette di ottimizzare
le performance, evitando di iterare inutilmente a vuoto e al tempo
stesso ci garantisce di intervenire solo quando determinate operazioni
come lettura e scrittura sono effettivamente operazioni disponibili.

Protocollo di interazione client-server
---------------------------------------

Client e server comunicano attraverso un protocollo a domanda-risposta:
il client effettua una richiesta al server, il server decodifica la
richiesta, esegue la relativa operazione e manda al client l’esito
dell’operazione (che chiameremo **status code**), in alcune operazioni
al posto del codice il server invierà direttamente il dato richiesto,
come nel caso del comando *FRIENDLIST*.


  **Status code**              **Valore associato**
  --------------------------- ----------------------
  OK                                   200
  GENERIC\_ERROR                        -1
  USER\_NOT\_FOUND                      -2
  EMPTY\_PASSWORD                       -3
  USER\_ALREADY\_REGISTERED             -4
  USER\_ALREADY\_LOGGED                 -5
  WRONG\_PASSWORD                       -6
  SELF\_REQUEST                         -7
  REFUSED                           “REFUSED”
  BUSY\_FRIEND                        “BUSY”

Buona parte degli status code è abbastanza autoesplicativa, gli ultimi
un po’ meno: *SELF\_REQUEST* viene riscontrato quando un utente tenta di
aggiungere sé stesso alla lista amici, *REFUSED* viene inviato quando
una determinata richiesta di sfida viene rifiutata e *BUSY\_FRIEND*
quando tentiamo di sfidare un amico che è già in partita con qualcun
altro. Gli ultimi due codici sono stati implementati come stringhe per
questioni di comodità di implementazione.

Gestore del server
------------------

Il server handler gestisce le connessioni di nuovi client e la
lettura/scrittura sui channel dei client. Appena lanciato effettua il
binding TCP sulla porta di default, imposta il Selector e si prepara
all’arrivo di nuove connessioni. Quando un nuovo client si connette,
vengono allocate le risorse necessarie per gestirlo (buffer e
informazioni varie), dopodiché viene settato il channel come
non-blocking e viene registrato nel Selector, con l’operazione OP\_READ.
Quando il canale associato a un client diventa readable, vengono
reperite le risorse associate al client e il server prova a leggere il
canale e nel caso di successo effettua il parsing del comando letto ed
esegue l’operazione richiesta, dopodiché prepara il buffer con la
risposta da scrivere al client e setta l’interestOps a OP\_WRITE, in
modo tale che quando il canale risulterà “writeable” sarà possibile
scrivere il risultato salvato prima nel buffer.

Gestione delle risorse del client
---------------------------------

Quando un client instaura una connessione col server, vengono allocate
delle risorse e informazioni varie che vengono associate alle
SelectionKey, tra cui:

-   **Username**, che viene impostato durante la fase di login, in
    questo modo ogni volta che il server itera tra le varie chiavi sa
    chi è l’utente associato.

-   **Buffer**, in modo tale da non doverne allocare sempre dei nuovi,
    ottimizzando così le risorse.

-   **Porta UDP**, cosicché il server sappia per ogni client in quale
    porta udp è pronto a ricevere la richiesta di sfida.

-   **Tempo di sfida**, ovvero l’istante di tempo in cui la richiesta di
    sfida è stata mandata, necessario per verificare l’eventuale timeout
    (di cui riparleremo nella sezione apposita).

-   Altre informazioni riguardanti la sfida (numero di parole tradotte,
    traduzioni corrette, etc...).

Funzionamento della sfida
-------------------------

Una volta che l’utente B ha accettato la richiesta di sfida dell’utente
A, la sfida può avere inizio: viene creato un nuovo thread che si occupa
di gestire la sfida, che chiameremo Challenge Handler. Viene creato nel
nuovo thread un altro Selector apposito per la sfida, dove vengono
registrate le keys dei due giocatori, dopodiché si procede con la scelta
delle N parole casuali e della memorizzazione delle relative traduzioni
attraverso l’API REST di MyMemory. Il Challenge Handler comincia la
sfida mandando la prima parola ai due giocatori, dopodiché ogni volta
che un giocatore invia una traduzione, verifica che quest’ultima sia
nella lista delle traduzioni della relativa parola e assegna i punti in
base alla correttezza o meno della parola data. Una volta che uno dei
due giocatori ha terminato le parole, setta l’interestOps di quel
giocatore a zero, in modo tale da far attendere che anche l’altro
giocatore finisca la partita. Un giocatore finisce la partita quando ha
tradotto tutte le N parole, o ha finito il tempo (in quel caso le parole
non tradotte valgono 0 punti) oppure ancora si è disconnesso.\
 \
È importante notare che ogni volta che un utente invia una traduzione il
punteggio associato viene serializzato immediatamente, di conseguenza un
giocatore “poco sportivo” che intende uscire dal gioco prima di aver
concluso la partita (magari pensando di evitare di farsi scalare i
punti) si ritroverebbe ugualmente con il punteggio effettivo al suo
ritorno, mentre l’altro giocatore può concludere la sua partita
tranquillamente come se nulla fosse. Una volta appurato che la partita
si può concludere definitivamente, vengono assegnati eventuali punti
extra al vincitore e viene mandato ai giocatori il seguente messaggio:\
 \
\
 \
Dove \<*isWinner*\> è un intero che vale:\
 \

$   
    \begin{cases*}
        1 & se il giocatore ha vinto la partita \\
        0 & se la partita è finita in pareggio \\
     -1 & se il giocatore ha perso la partita \\
    \end{cases*}
    
$\
 \
Infine i client, ricevuto il messaggio, potranno stampare a schermo un
resoconto sull’esito della partita, mentre dal lato server le chiavi
associate vengono registrate nuovamente nel vecchio Selector, in modo
tale da ritornare alla situazione precedente alla sfida, con la
possibilità di eseguire i comandi standard.

Database e altre strutture dati
-------------------------------

La classe Database si basa su due ConcurrentHashMap: la prima contiene
delle coppie *\<String, HashSet\<String\>\>* e rappresenta il grafo
delle amicizie, mentre la seconda contiene delle coppie *\<String,
HashSet\<User\>\>* e rappresenta una tabella contenente le informazioni
dei giocatori.\
*User* è una classe che rappresenta una semplice coppia *\<password,
punteggio\>*. In entrambe le ConcurrentHashMap viene utilizzato
l’username come chiave, in modo tale da poter accedere alle relative
info di un determinato utente in tempo costante (assumendo che le
funzioni hash implementate dalla classe HashMap funzionino al meglio).
Quando un amico viene aggiunto nel grafo delle amicizie di un utente, a
sua volta l’utente che lo sta aggiungendo verrà aggiunto nel grafo del
suddetto amico.

Nel ServerHandler sono presenti altre due strutture dati degne di nota.
Una *ConcurrentHashMap\<String, InetSocketAddress\>*, che tiene traccia
degli utenti attualmente connessi al servizio e tiene in memoria anche
il loro indirizzo, che può essere utilizzato per far mandare al server
l’eventuale richiesta di sfida UDP (in combinazione con la porta udp
salvata durante la fase di login). Gli utenti vengono aggiunti in questa
tabella quando effettuano il login e vengono rimossi quando questi si
disconnettono: l’accesso a questa struttura dati può avvenire sia nel
ServerHandler che nel thread ChallengeHandler, per questo motivo è stata
usata la variante Concurrent.\
L’altra struttura è sempre una HashMap, questa volta non concorrente
visto che viene utilizzata esclusivamente nel ServerHandler: contiene
coppie della forma *\<String, SelectionKey\>* e serve a tenere in
memoria le key dei vari giocatori, che vengono impiegate nel caso in cui
siano degli amici di un utente che intende sfidarli (il server ha
bisogno di ottenere la key dell’amico per accedere alle risorse a lui
associate).

Dizionario e traduzioni
-----------------------

All’avvio del server viene creato un oggetto `WQDictionary` che si
occupa di leggere il file *words.txt* contenente tutte le possibili
parole e le inserisce in un Vector. Da quest’ultimo, all’inizio della
sfida, verranno prelevate N parole casuali e distinte, al tempo stesso
verrà creata una lista contenente le traduzioni associate alla relativa
parola. Le traduzioni, come già accennato, vengono richieste alla API
REST di MyMemory, nello specifico viene fatta una richiesta GET con una
query specifica, dopodiché il servizio esterno ritornerà un oggetto
JSON, che il server si occuperà di decodificare attraverso la libreria
GSON, che come prima cosa effettuerà la fetch dell’array `matches` e
guarderà il campo `translation` per ogni elemento, memorizzando il
risultato nella lista citata poc’anzi.

Gestione dei timeout
--------------------

Il timeout della partita e della richiesta di sfida sono stati gestiti
sia lato client che lato server, questo per migliorare e rendere più
flessibile l’esperienza degli utenti e al tempo stesso evitare che il
timer venga gestito esclusivamente dai client: sarebbe una scelta poco
saggia, infatti uno dei principi fondamentali su cui mi sono basato
durante la progettazione del sistema è quello di non fidarsi mai di
loro. Ho cercato di implementare il server pensando sempre alla
possibilità che possa esistere un client maligno, magari capace di
alterare i messaggi da mandare al server o di usare tool esterni per
abusare di possibili meccanismi.

### Timeout della richiesta di sfida

Il client A, una volta mandata la richiesta di sfida a B, si blocca in
lettura per X secondi, in attesa di una risposta dal server. Il server
tiene traccia dell’istante di tempo in cui la richiesta viene mandata e
calcolerà la differenza con il tempo di accettazione. Se l’accettazione
arriva in tempo la sfida può avere inizio e i timer verranno arrestati
all’arrivo della prima parola da tradurre. Se la risposta alla richiesta
non arriva entro il tempo prestabilito viene dichiarato come errore di
timeout e l’utente A può rimandare la richiesta o effettuare altre
operazioni. Se l’utente B rifiuta la richiesta viene mandato uno status
code “REFUSED” al client A dal server. Se invece B accetta la richiesta
ma lo fa dopo il tempo prestabilito, il server risponde a B con
“TIMEOUT”, mentre il client A si sarà sbloccato grazie alla sua lettura
bloccante da X secondi.

### Timeout della partita

Viene impostata la select del ChallengeHandler con un timer da 200
millisecondi, dopo il quale viene controllato ogni volta l’ammontare di
tempo trascorso: se è scaduto il tempo massimo della sfida, si procede
saltando alla fase finale della partita, ovvero la scelta del vincitore
e l’invio del resoconto finale attraverso il messaggio *FIN* di cui
abbiamo parlato in precedenza. Lato client, una volta concluso il timer,
la finestra si chiuderà in automatico e si attenderà la ricezione del
resoconto del server.

WQSettings
----------

L’interfaccia `WQSettings` definisce diversi parametri che possono
essere modificati per personalizzare degli aspetti di WordQuizzle, tra
cui:

-   Il numero di parole da tradurre in una sfida e il tempo messo a
    disposizione.

-   Il valore di timeout della richiesta di sfida.

-   I punti assegnati per ogni risposta corretta e sbagliata, oltre che
    i punti bonus assegnati al vincitore.

-   Porta TCP del server.

-   Hostname e indirizzo RMI del server.

Panoramica del client e dell’interfaccia grafica
================================================

Il client una volta avviato lancia subito `UDPListener`, un thread che
sceglie una porta UDP libera e si mette in attesa di eventuali richieste
di sfida. Viene lanciata l’interfaccia grafica, in particolare la
schermata iniziale di login.

Schermata di login
------------------

Dalla schermata è possibile registrarsi o effettuare il login. Viene
restituita una finestra contenente l’esito dell’operazione e premendo OK
è possibile effettuare il login.

<p align="center">
  <img src="https://raw.githubusercontent.com/francescodizzia/WordQuizzle/master/images/loginScreen.png" />
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/francescodizzia/WordQuizzle/master/images/register.png" />
</p>

Hub di gioco
------------

Una volta effettuato il login, viene visualizzata l’hub di gioco, che
rappresenta la schermata principale del client, dove è possibile
eseguire diverse operazioni: aggiornare il punteggio e la lista amici,
mostrare la classifica, effettuare il logout e aggiungere altri amici
attraverso la barra posta in alto a destra. Per mandare una richiesta di
sfida è sufficiente selezionare l’amico dalla lista amici e premere il
tasto “Gioca!”.

<p align="center">
  <img src="https://raw.githubusercontent.com/francescodizzia/WordQuizzle/master/images/hub.png" />
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/francescodizzia/WordQuizzle/master/images/request.png" />
</p>



L’amico riceverà la richiesta via UDP e visualizzerà una finestra da cui
sarà possibile accettare o rifiutare la sfida, comunicando all’amico la
relativa scelta. Qualora accettasse dopo il timeout il server gli
comunicherebbe errore, in caso contrario la sfida può avere inizio senza
problemi.

Schermata di gioco
------------------

In questa schermata è possibile inviare le traduzioni al server, che
provvederà a verificare la correttezza, ad assegnare i punti e a mandare
la parola successiva. Inviata l’ultima parola o finito il timer la
finestra viene chiusa e viene mostrato un resoconto finale della
partita. Se l’utente termina prima della scadenza del timer e nel
frattempo il suo avversario sta ancora continuando la partita allora
l’utente si mette in attesa mostrando una finestra apposita. Quando
l’avversario finisce a sua volta o termina la connessione l’utente viene
sbloccato e può mostrare il resoconto finale, per poi tornare all’hub di
gioco.

<p align="center">
  <img src="https://raw.githubusercontent.com/francescodizzia/WordQuizzle/master/images/challenge.png" />
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/francescodizzia/WordQuizzle/master/images/waitingDialog.png" />
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/francescodizzia/WordQuizzle/master/images/endReport.png" />
</p>


Informazioni aggiuntive
=======================

Il progetto è stato testato sia in locale, che da remoto attraverso una
Cloud VPS di OVH. Il gioco ha funzionato senza problemi ed è stato
interessante notare l’interoperabilità tra sistemi: il server remoto
girava su Ubuntu 20.04, mentre io e il mio avversario utilizzavamo
Windows 10.\
 \
**Nota bene:** per giocare da remoto, oltre a cambiare l’hostname della
macchina dai *WQSettings*, è stato necessario anche eseguire il port
forwarding delle porte UDP dei client, altrimenti il NAT avrebbe
ostacolato l’arrivo di pacchetti UDP. In particolare è necessario che
almeno uno dei due utenti faccia ciò, in modo tale che almeno lui possa
ricevere la richiesta di sfida dagli altri giocatori. È possibile
controllare la propria porta UDP una volta lanciato il client dal
terminale.

Istruzioni per la compilazione ed esecuzione
--------------------------------------------

Una volta estratto l’archivio, è necessario spostarsi da terminale
all’interno della cartella WordQuizzle. A quel punto per compilare
bisogna digitare:\

`javac src/com/dizzia/wordquizzle/server/*.java src/com/dizzia/wordquizzle/commons/*.java src/com/dizzia/wordquizzle/client/*.java -cp libs/*`

Mentre per eseguire server e client è necessario spostarsi nella
cartella *src* (`cd src`) e digitare rispettivamente:\
 
`java com.dizzia.wordquizzle.server.WQServer`\
`java com.dizzia.wordquizzle.client.WQClient`\
 
**Importante:** nel caso in cui si compili diversamente assicurarsi che
nella stessa directory da cui si eseguono questi ultimi due comandi
siano presenti tutti i file ausiliari (*security.policy* e *words.txt*
per il server e la cartella *resources* per il client).

Alternativa precompilata
------------------------

In alternativa è possibile avviare anche delle versioni già compilate in
formato .jar, sempre nella cartella WordQuizzle. Per avviarle è
sufficiente digitare:\

`java -jar WQServer.jar`\
`java -jar WQClient.jar`
