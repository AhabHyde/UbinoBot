package bot;

import bot.utils.*;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.telegram.telegrambots.api.methods.send.*;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;


public class UbinoBot extends TelegramLongPollingBot {
    //Costanti con il mio id e il nome del file delle richieste
    private final String admin_id = "18200812";
    private final String fileToken = "bot.token";
    private final String fileRichieste = "richieste.txt";
    private final String fileDatabase = "chat.json";
    
    @Override
    public void onUpdateReceived(Update update) {
        //Salva il mittente se non è presente
        Database.aggiornaDB(update, fileDatabase);
        
        /*Istruzione usata per prendere i gli ID dei file e documenti
        try {
            System.out.println(update.getMessage().getVoice().getFileId());
        } catch (Exception e) {System.out.println(e);}
        */        
        
        //Controllo per vedere se l'update è un messaggio testuale e che esso non sia vuoto
        if (update.hasMessage() && update.getMessage().hasText()) {
            //Testo e mittente
            String testoMessaggio = update.getMessage().getText();
            String chat_id = "" + update.getMessage().getChatId();
            
            //Comandi con slash
            switch(testoMessaggio.split(" ")[0].toLowerCase()) {
                case "/aiuta":
                    cAiuta(chat_id);
                    break;
                
                //Se dovesse venire menzionato
                case "/aiuta@ubinobot":
                    cAiuta(chat_id);
                    break;

                case "/start":
                    cAiuta(chat_id);
                    break;
            }
            
            //Comandi con parola chiave
            if(testoMessaggio.toLowerCase().contains("pls") || testoMessaggio.toLowerCase().contains("plz"))
                cPls(chat_id);
                        
            //Comandi con menzione
            //Split per prendere la prima parola, cioè "Ubino"
            if (testoMessaggio.split(" ")[0].toLowerCase().equals("ubino")) {
                //Split per prendere la seconda parola, il comando
                String comandoChiamato = testoMessaggio.split(" ")[1].toLowerCase();            
                switch(comandoChiamato) {
                    case "ripeti":
                        cEcho(chat_id, testoMessaggio);
                        break;

                    case "notifica":
                        cNotifica(update);
                        break;

                    case "audio":
                        cAudio(chat_id);
                        break;

                    case "richiesta:":
                        cRichiesta(update);
                        break;
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        //Return bot username
        //If bot username is @MyAmazingBot, it must return 'MyAmazingBot'
        return "UbinoBot";
    }

    @Override
    public String getBotToken() {
        try {
            //Return bot token from BotFather
            //Legge il file di testo con il nome passato. Mantiene gli a capo e tabulazioni
            BufferedReader reader;
            reader = new BufferedReader(new FileReader (fileToken));
            return reader.readLine();
        } catch (IOException e) {
            System.out.println("Errore apertura file token: " + e);
        }
        return "";
    }
    
    
    //----------COMANDI----------
    public void cEcho(String chat_id, String testoMessaggio){
        //Crea la stringa da mandare e la rende uguale al messaggio originale
        //ma toglie la prima parola (che è '/echo')
        //Suddivide la stringa in parole separate da spazi, e parte da 1 (non da 0)
        //per togliere il nome del comando. Poi aggiunge ogni parola alla stringa finale
        String finale = "";
        for (int k=2; k<testoMessaggio.split(" ").length; k++) {
            finale += testoMessaggio.split(" ")[k] + " ";
        }
        
        SendMessage messaggio;
        if (finale.length()>0 ) {
            //Vuol dire che c'era del testo da replicare
            messaggio = new SendMessage()
                .setChatId(chat_id)
                .setText(finale);
        }
        else {
            messaggio = new SendMessage()
                .setChatId(chat_id)
                .setText("Dopo /echo devi scrivere qualcosa.");
        }
        
        try {
            //Invia il messaggio all'utente
            sendMessage(messaggio);
        } catch (Exception e) {
            System.out.println("Errore: " + e);
        }
    }
    
    //---------------------------
    
    public void cNotifica(Update update){
        //Notifica tutti gli utenti conosciuti mandando il messaggio passato.
        //Funziona solo se chiamato da me
        String idmittente = "" + update.getMessage().getFrom().getId();
        String testoMessaggio = update.getMessage().getText();
        
        //Suddivide la stringa in parole separate da spazi, e parte da 1 (non da 0)
        //per togliere il nome del comando. Poi aggiunge ogni parola alla stringa finale
        String finale = "";
        for (int k=2; k<testoMessaggio.split(" ").length; k++) {
            finale += testoMessaggio.split(" ")[k] + " ";
        }
        
        //Controllo mittente che sia admin
        if (admin_id.equals(idmittente)) {
            //Controllo che la notifica abbia un testo
            if (finale.length()>0) {
                //Elenco destinatari dal database
                String[] destinatari = Database.getDestinatari(fileDatabase);
                
                //Per ogni destinatario, invio il messaggio
                SendMessage messaggio;
                for (String dest : destinatari) {
                    messaggio = new SendMessage()
                            .setChatId(dest)
                            .setText(finale);
                
                    try {
                        sendMessage(messaggio);
                    } catch (Exception e) {
                        System.out.println("Errore: " + e);
                    }
                }
            }
            else {
                //Se il messaggio non ha testo
                SendMessage messaggio = new SendMessage()
                        .setChatId(idmittente)
                        .setText("Scrivi una notifica da mandare.");
                
                try {
                    sendMessage(messaggio);
                } catch (Exception e) {
                    System.out.println("Errore: " + e);
                }
            }
        }        
    }
    
    //---------------------------
    
    public void cAudio(String chat_id){
        //Manda l'audio UUUU
        //String fileID = "AwADAgADIQADagEhSaaHencTsd2VAg";
        String fileID = "AwADAgADiQADm40hSZhY87viJvWFAg";
        
        //Crea il messaggio
        SendVoice messaggio = new SendVoice();
        messaggio.setChatId(chat_id);
        messaggio.setVoice(fileID);
        
        try {
            //Invia il messaggio all'utente
            sendVoice(messaggio);
        } catch (Exception e) {
            System.out.println("Errore: " + e);
        }
    }
    
    //---------------------------
    
    public void cPls(String chat_id){
        //Manda l'audio PLS
        String fileID = "AwADAgADigADm40hSSmVvmznywOZAg";
        
        //Crea il messaggio
        SendVoice messaggio = new SendVoice();
        messaggio.setChatId(chat_id);
        messaggio.setVoice(fileID);
        
        try {
            //Invia il messaggio all'utente
            sendVoice(messaggio);
        } catch (Exception e) {
            System.out.println("Errore: " + e);
        }
    }
    
    //---------------------------
    
    public void cRichiesta(Update update){
        //Salva il messaggio ricevuto togliendo la prima parola, cioè il comando
        String finale = "";
        String chat_id = "" + update.getMessage().getChatId();
        String testoMessaggio = update.getMessage().getText();
        //Stringa con nome e cognome dell'utente presi dal messaggio
        String utente = update.getMessage().getFrom().getFirstName() + " " +
                update.getMessage().getFrom().getLastName();
        
        //Suddivide la stringa in parole separate da spazi, e parte da 1 (non da 0)
        //per togliere il nome del comando. Poi aggiunge ogni parola alla stringa finale
        for (int k=2; k<testoMessaggio.split(" ").length; k++) {
            finale += testoMessaggio.split(" ")[k] + " ";
        }
        
        if (! finale.equals("")) {
            //Cioè il messaggio non è vuoto
            //Invia la conferma all'utente
            SendMessage messaggio = new SendMessage()
                .setChatId(chat_id)
                .setText("Grazie, la tua richiesta verrà considerata.");
            try {
                sendMessage(messaggio);
            } catch (Exception e) {
                System.out.println("Errore: " + e);
            }

            try {
                //Salva la data e ora attuale
                String timeStamp = new SimpleDateFormat("[dd-MM-yyyy, HH:mm ")
                        .format(Calendar.getInstance().getTime());
                
                //Crea la stringa da salvare che è data, utente e richiesta
                String daSalvare = timeStamp + utente + "]~ " + finale + "\r\n"; //\r\n usato per il crlf in windows
                
                //Salva la richiesta che ha ricevuto nel file delle richieste
                //new File usato per creare il file se non dovesse esistere, dato che APPEND non lo fa
                new File(fileRichieste).createNewFile();
                Files.write(Paths.get(fileRichieste), daSalvare.getBytes(), StandardOpenOption.APPEND);
                
                //Manda un messaggio di notifica a me
                messaggio = new SendMessage()
                    .setChatId(admin_id)
                    .setText("Hey, ho una nuova richiesta.");
                
                sendMessage(messaggio);
                
                //Allega il file con le richieste
                SendDocument documento = new SendDocument()
                    .setChatId(admin_id)
                    .setNewDocument(new File(fileRichieste));
                
                sendDocument(documento);
                
            } catch (Exception e) {
                System.out.println("Errore: " + e);
            }
        }
        
        else {
            //Se la richiesta è vuota
            SendMessage messaggio = new SendMessage()
                .setChatId(chat_id)
                .setText("Invia una richiesta valida. Dopo /richiesta devi scrivere qualcosa.");

            try {
                sendMessage(messaggio);
            } catch (Exception e) {
                System.out.println("Errore: " + e);
            }
        }
    }
    
    //---------------------------
    
    public void cAiuta(String chat_id){
        //Manda lo stesso messaggio descrizione del bot, quello d'aiuto
        String testo = "Sono l'assistente virtuale Ube, posso aiutarti in (quasi) tutto.\n" +
            "\n" +
            "https://github.com/AhabHyde/UbinoBot" +
            "\n" +
            "Ora non posso fare molto, se vuoi suggerirmi qualcosa da imparare scrivi:\n" +
            "Ubino richiesta: e il tuo suggerimento.\n" +
            "\n" +
            "Lista comandi (sono tutti case insensitive):\n" +
            "/aiuta - Visualizza i comandi disponibili\n" +
            "Ubino audio - Mando la mia firma vocale\n" +
            "pls - Appena rilevo un PLS in un messaggio, mando un PLSSS\n" +
            "Ubino richiesta: - Puoi inviare una richiesta con scritto cosa"
            + "vorresti che potessi fare. Ricordati i due punti dopo \"richiesta\"!\n" +
            "Ubino ripeti - Ripeto quello che mi dici";
                
        //Invia il messaggio all'utente
        SendMessage message = new SendMessage()
            .setChatId(chat_id)
            .setText(testo);
        
        try {
            sendMessage(message);
        } catch (Exception e) {
            System.out.println("Errore: " + e);
        }
    }
}