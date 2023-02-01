package control;

import actors.*;
import messages.Message;
import messages.QuitMessage;
import observer.MonitorService;
import view.MainWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Control {
    private static ActorContext actorContext = getActorContext();
    private static MainWindow view;
    private static MonitorService monitorService = MonitorService.getInstance();
    public Control() {

    }

    public static ActorContext getActorContext() {
        return ActorContext.getInstance();
    }
    public static void setView(MainWindow v) {
        view = v;
    }
    public static void addActor(String name, Actor actor) {
        actorContext.spawnActor(name, actor);
        monitorService.monitorActor(name);
    }

    public static void runRingApp(int max) throws InterruptedException {

        RingActor[] actors = new RingActor[max];
        ActorContext context = ActorContext.getInstance();

        int i;
        for(i = 0; i < max; ++i) {
            if (i != max - 1) {
                actors[i] = new RingActor(false);
            } else {
                actors[i] = new RingActor(true);
            }

            context.spawnActor(String.valueOf(i), actors[i]);
            view.addEntry(String.valueOf(i), actors[i].getClass().getName());
        }

        for(i = 0; i < max; ++i) {
            if (i < max - 1) {
                actors[i].setNext(actors[i + 1]);
            } else {
                actors[i].setNext(actors[0]);
            }
        }

        ActorProxy first = new ActorProxy(context.lookup("0"));
        ActorProxy last = new ActorProxy(context.lookup("" + (max - 1)));
        ((RingActor)last.getActor()).setAp(last);

        int spins = 10;
        for (int j = 0; j < spins; j++) {
            first.send(new Message(first.getActor(), "Broken telephone."));
            last.receive();
        }

        Set<String> names = context.getNames();

        for (String name : names) {
            context.lookup(name).send(new QuitMessage((ActorImp) null, "QUIT"));
        }

    }

    public static void runPingPongApp() {

        ActorProxy ping = actorContext.spawnActor("PING", new PingPongActor(null));
        ActorProxy pong = actorContext.spawnActor("PONG", new PingPongActor(ping));
        ((PingPongActor)ping.getActor()).setMate(pong);

        view.addEntry("PING", "PingPongActor");
        view.addEntry("PONG", "PingPongActor");

        for(int i = 0; i < 99999; i++) {
            pong.send(new Message(ping, "Hello Ping"));
            ping.send(new Message(pong, "Hello Pong"));
        }

    }
    public static void sendMessages(String from, String name, int nMessages) {
        Actor a = actorContext.lookup(name);
        if (a!=null) {
            for(int i = 0; i < nMessages; i++) {
                a.send(new Message(actorContext.lookup(from), "Message" + i));
            }
        }
    }

    public static int getQueueOccupancy(String name) {
        Actor a = actorContext.lookup(name);
        if(a!=null) {
            return ((ActorImp)a).getMailbox().size();
        }
        else return 0;
    }

    public static String getProcessedMessages(String name) {
        Actor a = actorContext.lookup(name);
        if(a!=null) {
            HashMap<Actor, ArrayList<Message>> map = monitorService.getReceivedMessages(a);
            int totalMessages;
            if (map.containsValue(null))
                totalMessages = 0;
            else totalMessages = map.values().stream()
                    .mapToInt(List::size)
                    .sum();
            return  String.valueOf(totalMessages);
        }
        else return "0";
    }

    public static String getSentMessages(String name) {
        Actor a = actorContext.lookup(name);
        if(a!=null) {
            HashMap<Actor, ArrayList<Message>> map = monitorService.getSentMessages(a);

            int totalMessages;
            if (map.containsValue(null))
                totalMessages = 0;
            else totalMessages = map.values().stream()
                    .mapToInt(List::size)
                    .sum();

            return String.valueOf(totalMessages);
        }
        else return "0";
    }

    public static void quitActor(String actor) {
        Actor a = actorContext.lookup(actor);
        if(a!=null) {
            a.send(new QuitMessage(null, "Quit"));
            actorContext.removeActor(actor);
        }
    }
}
